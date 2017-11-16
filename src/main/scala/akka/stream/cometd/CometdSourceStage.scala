package akka.stream.cometd

import java.util
import java.util.concurrent.Semaphore

import akka.stream.cometd.scaladsl.Cometd
import akka.stream.stage.{GraphStageLogic, GraphStageWithMaterializedValue, OutHandler}
import akka.stream.{Attributes, Outlet, SourceShape}
import org.cometd.bayeux.Message
import org.cometd.bayeux.client.ClientSessionChannel

import scala.collection.mutable
import scala.concurrent.{Future, Promise}
import scala.util.Success

class CometdSourceStage(settings: CometdSettings) extends GraphStageWithMaterializedValue[SourceShape[Message], Future[ClientSessionChannel]] {

  import org.cometd.bayeux.client.ClientSessionChannel

  val out: Outlet[Message] = Outlet("CometdSource")
  override val shape: SourceShape[Message] = SourceShape(out)

  private val client = settings.client

  override def createLogicAndMaterializedValue(inheritedAttributes: Attributes): (GraphStageLogic, Future[ClientSessionChannel]) = {

    val materializedValue = Promise[ClientSessionChannel]

    val logic = new GraphStageLogic(shape) {

      private var currentChannel: Option[ClientSessionChannel] = None
      private val bufferSize = settings.bufferSize
      private val queue = mutable.Queue[Message]()
      private val backpressure = new Semaphore(bufferSize)

      setHandler(out, new OutHandler {
        override def onPull(): Unit =
          if (queue.nonEmpty) {
            pushMessage(queue.dequeue())
          }
      })

      private val messageListener = new ClientSessionChannel.MessageListener() {
        override def onMessage(clientSessionChannel: ClientSessionChannel, message: Message) = {
            backpressure.acquire()
            handleMessage.invoke(message)
        }
      }

      override def preStart(): Unit = {

        if (settings.doHandshake) {
          Cometd.handshake(settings).onComplete {
            case Success(true) => subscribe()
            case _ => failure.invoke(CometdException("Handshake fail"))
          }
        } else {
          subscribe()
        }
      }

      override def postStop(): Unit = {
        queue.clear()
        currentChannel.foreach(_.unsubscribe(messageListener))
      }

      private def subscribe(): Unit = {
        val channel = client.getChannel(settings.channel)
        currentChannel = Some(channel)
        channel.subscribe(messageListener, new ClientSessionChannel.MessageListener() {
          override def onMessage(clientSessionChannel: ClientSessionChannel, message: Message) = {
            if (message.isSuccessful) {
              materializedValue.success(clientSessionChannel)
            } else {
              val exception = CometdException(s"Error during subscription to channel ${settings.channel}", message)
              materializedValue.failure(exception)
              failure.invoke(exception)
            }
          }
        })
      }

      private val handleError = getAsyncCallback[Throwable](e => {
        fail(out, e)
      })

      private val handleMessage = getAsyncCallback[Message](msg => {
        require(queue.size <= bufferSize)
        if (isAvailable(out)) {
          pushMessage(msg)
        } else {
          queue.enqueue(msg)
        }
      })

      private val failure = getAsyncCallback[Throwable](e => failStage(e))

      private def pushMessage(msg: Message): Unit = {
        push(out, msg)
        backpressure.release()
      }

    }

    (logic, materializedValue.future)
  }
}