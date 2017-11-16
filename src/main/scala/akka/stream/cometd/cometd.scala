package akka.stream.cometd

import org.cometd.bayeux.Message
import org.cometd.bayeux.client.ClientSession


object CometdSettings {
  def create(client: ClientSession, channel: String) = CometdSettings(client, channel)
}

case class CometdSettings(client: ClientSession, channel: String, bufferSize: Int = 100, doHandshake: Boolean = true) {
  def withBufferSize(bufferSize: Int) =  this.copy(bufferSize = bufferSize)
  def withDoHandshake(doHandshake: Boolean) =  this.copy(doHandshake = doHandshake)
}

object CometdException {
  def apply(error: String): CometdException = new CometdException(error, None)
  def apply(error: String, message: Message): CometdException = new CometdException(error, Some(message))
}

case class CometdException(error: String, message: Option[Message]) extends RuntimeException(s"$error ${message.map(m => s" : $m").getOrElse("")}")

