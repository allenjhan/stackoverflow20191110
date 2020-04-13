import io.circe.Encoder
import io.circe.generic.semiauto._

sealed trait Entity {
  def path: String
  def owner: String
  def permissions: String
  def lastModifiedTime: String
  def creationTime: String
  def lastAccessTime: String
  def hashCode: Int
}

object Entity {
  implicit val entityEncoder: Encoder[Entity] = deriveEncoder
}

case class FileEntity(path: String, owner: String, permissions: String, lastModifiedTime: String, creationTime: String, lastAccessTime: String) extends Entity

object fileentityEncoder {
  implicit val fileentityEncoder: Encoder[FileEntity] = deriveEncoder
}

case class DirectoryEntity(path: String, owner: String, permissions: String, lastModifiedTime: String, creationTime: String, lastAccessTime: String) extends Entity

object DirectoryEntity {
  implicit val directoryentityEncoder: Encoder[DirectoryEntity] = deriveEncoder
}

case class Contents(path: String, files: IndexedSeq[Entity])

object Contents {
  implicit val contentsEncoder: Encoder[Contents] = deriveEncoder
}
