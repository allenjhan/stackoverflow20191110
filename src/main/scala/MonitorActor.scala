import akka.actor.{Actor, ActorRef, Props, ReceiveTimeout, Stash}
import io.circe.syntax._

import MonitorActor.ShutDown
import PrintActor.HeartBeat

import scala.concurrent.duration._

class MonitorActor(shutdownActor: ActorRef, timeoutInS: Int) extends Actor with Stash {

  context.setReceiveTimeout(timeoutInS seconds)

  override def receive: Receive = {
    case ReceiveTimeout =>
      shutdownActor ! ShutDown
    case HeartBeat =>
  }

}

object MonitorActor {
  def props(shutdownActor: ActorRef, timeoutInS: Int) = Props(new MonitorActor(shutdownActor, timeoutInS))

  case object ShutDown
}
