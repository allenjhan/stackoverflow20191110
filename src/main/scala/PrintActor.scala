import java.io.{BufferedWriter, File, FileWriter, PrintWriter}

import akka.actor.{Actor, ActorRef, Props, Stash}
import PrintActor.{Count, HeartBeat}

class PrintActor(name: String, interval: Int, monitorActor: ActorRef) extends Actor with Stash {

  val file = new File(name)

  override def preStart = {
    val fw = new BufferedWriter(new FileWriter(file, true))
    fw.write("[\n")
    fw.close()

    self ! Count(0)
  }

  override def receive: Receive = {
    case Count(c) =>
      context.become(withCount(c))
      unstashAll()

    case _ =>
      stash()
  }

  def withCount(c: Int): Receive = {
    case s: String =>
      val fw = new BufferedWriter(new FileWriter(file, true))
      fw.write(s)
      fw.write(",\n")
      fw.close()

      if (c == interval) {
        monitorActor ! HeartBeat
        context.become(withCount(0))
      } else {
        context.become(withCount(c+1))
      }
  }

}

object PrintActor {
  def props(name: String, interval: Int, monitorActor: ActorRef) = Props(new PrintActor(name, interval, monitorActor))

  case class Count(count: Int)

  case object HeartBeat
}
