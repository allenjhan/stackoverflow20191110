import MonitorActor.ShutDown
import ShutDownActor.ShutDownYet
import akka.actor.{Actor, Props, Stash}

class ShutDownActor() extends Actor with Stash {

  override def receive: Receive = {
    case ShutDownYet => sender ! false
    case ShutDown => context.become(canShutDown())
  }

  def canShutDown(): Receive = {
    case ShutDownYet => sender ! true
  }

}

object ShutDownActor {
  def props = Props(new ShutDownActor())

  case object ShutDownYet
}