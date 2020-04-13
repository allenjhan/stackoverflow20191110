import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import java.io.{BufferedWriter, File, FileWriter}

import ShutDownActor.ShutDownYet

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.Try

object Main {

  val defaultNumPrinters = 50

  val defaultMonitorTickInterval = 500

  val defaultTimeoutInS = 60

  def main(args: Array[String]): Unit = {
    val timeoutInS = Try(args(1).toInt).toOption.getOrElse(defaultTimeoutInS)

    val system = ActorSystem("SearchHierarchy")

    val shutdown = system.actorOf(ShutDownActor.props)

    val monitor = system.actorOf(MonitorActor.props(shutdown, timeoutInS))

    val refs = (0 until Try(args(2).toInt).toOption.getOrElse(defaultNumPrinters)).map{x =>
      val name = "logfile" + x
      (name, system.actorOf(PrintActor.props(name, Try(args(3).toInt).toOption.getOrElse(defaultMonitorTickInterval), monitor)))
    }

    val root = system.actorOf(TraverseActor.props(new File(args(0)), refs))

    implicit val askTimeout = Timeout(timeoutInS seconds)

    var isTimedOut = false

    while(!isTimedOut){
      Thread.sleep(30000)
      val fut = (shutdown ? ShutDownYet).mapTo[Boolean]
      isTimedOut = Await.result(fut, timeoutInS seconds)
    }

    refs.foreach{ x =>
      val fw = new BufferedWriter(new FileWriter(new File(x._1), true))
      fw.write("{}\n]")
      fw.close()
    }

    system.terminate
  }

}
