import java.io.File

import akka.actor.{Actor, ActorRef, PoisonPill, Props, ReceiveTimeout}
import io.circe.syntax._

import scala.collection.JavaConversions
import scala.concurrent.duration._
import scala.util.Try

class TraverseActor(start: File, printers: IndexedSeq[(String, ActorRef)]) extends Actor{

  val hash = start.hashCode()
  val mod = hash % printers.size
  val idx = if (mod < 0) -mod else mod
  val myPrinter = printers(idx)._2

  override def preStart = {
    self ! start
  }

  override def receive: Receive = {
    case f: File =>
      val path = f.getCanonicalPath
      val files = Option(f.list()).map(_.toIndexedSeq.map(x =>new File(path + "/" + x)))

      val directories = files.map(_.filter(_.isDirectory))

      directories.foreach(ds => processDirectories(ds))

      val entities = files.map{fs =>
        fs.map{ f =>
          val path = f.getCanonicalPath
          val owner = Try(java.nio.file.Files.getOwner(f.toPath).toString).toOption.getOrElse("")
          val permissions = Try(java.nio.file.Files.getPosixFilePermissions(f.toPath).toString).toOption.getOrElse("")
          val attributes = Try(java.nio.file.Files.readAttributes(f.toPath, "lastModifiedTime,creationTime,lastAccessTime"))
          val lastModifiedTime = attributes.flatMap(a => Try(a.get("lastModifiedTime").toString)).toOption.getOrElse("")
          val creationTime = attributes.flatMap(a => Try(a.get("creationTime").toString)).toOption.getOrElse("")
          val lastAccessTime = attributes.flatMap(a => Try(a.get("lastAccessTime").toString)).toOption.getOrElse("")

          if (f.isDirectory) FileEntity(path, owner, permissions, lastModifiedTime, creationTime, lastAccessTime)
          else DirectoryEntity(path, owner, permissions, lastModifiedTime, creationTime, lastAccessTime)
        }
      }

      directories match {
        case Some(seq) =>
          seq match {
            case x+:xs =>
            case IndexedSeq() => self ! PoisonPill
          }
        case None => self ! PoisonPill
      }

      entities.foreach(e => myPrinter ! Contents(f.getCanonicalPath, e).asJson.toString)
  }

  def processDirectories(directories: IndexedSeq[File]): Unit = {
    def inner(fs: IndexedSeq[File]): Unit = {
      fs match {
        case x +: xs =>
          context.actorOf(TraverseActor.props(x, printers))
          processDirectories(xs)
        case IndexedSeq() =>
      }

    }

    directories match {
      case x +: xs =>
        self ! x
        inner(xs)
      case IndexedSeq() =>
    }
  }

}

object TraverseActor {
  def props(start: File, printers: IndexedSeq[(String, ActorRef)]) = Props(new TraverseActor(start, printers))
}
