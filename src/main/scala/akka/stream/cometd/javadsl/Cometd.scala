package akka.stream.cometd.javadsl

import java.util.concurrent.CompletionStage

import akka.NotUsed
import akka.stream.cometd.CometdSettings
import org.cometd.bayeux.Message
import org.cometd.bayeux.client.ClientSessionChannel

import scala.compat.java8.FutureConverters

object Cometd {

  import scala.collection.JavaConverters._

  def handshake(settings: CometdSettings, params: java.util.Map[String, AnyRef]): CompletionStage[Boolean] = {
    val map: Map[String, AnyRef] = params.asScala.toMap
    FutureConverters.toJava(akka.stream.cometd.scaladsl.Cometd.handshake(settings, map))
  }
  
  def source(settings: CometdSettings): akka.stream.javadsl.Source[Message, CompletionStage[ClientSessionChannel]] = {
    akka.stream.cometd.scaladsl.Cometd.source(settings)
      .mapMaterializedValue { f =>
        FutureConverters.toJava(f)
      }
      .asJava
  }

  def flow(settings: CometdSettings): akka.stream.javadsl.Flow[AnyRef, Message, NotUsed] = {
    akka.stream.cometd.scaladsl.Cometd.flow(settings).asJava
  }
}
