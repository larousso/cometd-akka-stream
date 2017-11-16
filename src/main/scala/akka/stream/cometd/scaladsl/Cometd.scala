package akka.stream.cometd.scaladsl

import java.util

import akka.NotUsed
import akka.stream.cometd.{CometdException, CometdSettings, CometdSourceStage}
import akka.stream.scaladsl.{Flow, Source}
import org.cometd.bayeux.Message
import org.cometd.bayeux.client.ClientSessionChannel

import scala.concurrent.{Future, Promise}


object Cometd {

  import scala.collection.JavaConverters._

  def handshake(settings: CometdSettings, params: Map[String, AnyRef] = Map.empty): Future[Boolean] = {
    val promise = Promise[Boolean]
    settings.client.handshake(params.asJava, new ClientSessionChannel.MessageListener() {
      def onMessage(handshakeChannel: ClientSessionChannel, message: Message): Unit = {
        if (message.isSuccessful) {
          promise.success(true)
        } else {
          promise.success(false)
        }
      }
    })
    promise.future
  }

  def source(settings: CometdSettings): Source[Message, Future[ClientSessionChannel]] = {
    Source.fromGraph(new CometdSourceStage(settings))
  }

  def flow(settings: CometdSettings): Flow[AnyRef, Message, NotUsed] = {
    val channel = settings.client.getChannel(settings.channel)
    Flow[AnyRef].mapAsync(1) { message =>
      val promise = Promise[Message]
      channel.publish(message, new ClientSessionChannel.MessageListener() {
        override def onMessage(channel: ClientSessionChannel , message: Message): Unit = {
          promise.success(message)
        }
      })
      promise.future
    }
  }

}
