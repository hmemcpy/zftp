package zftp

import java.io.{FileInputStream, InputStream}
import java.util.{Vector => JVector}

import com.jcraft.jsch.{ChannelSftp, SftpException}
import zio.blocking.{Blocking, effectBlocking}
import zio.{Has, ZIO, ZLayer}

import scala.jdk.CollectionConverters._

object sftp {
  type Sftp = Has[SFTP] with Blocking

  final class SFTP(session: SFTPSession) {

    def ls(path: String): ZIO[Blocking, SftpException, List[String]] =
      effectBlocking {
        session.channel.ls(path).asInstanceOf[JVector[ChannelSftp#LsEntry]]
          .asScala
          .map(_.getFilename)
          .toList
      }.refineToOrDie[SftpException]

    def mkdir(path: String): ZIO[Blocking, SftpException, Unit] =
      effectBlocking {
        session.channel.mkdir(path)
      }.refineToOrDie[SftpException]

    def upload(path: String, is: InputStream): ZIO[Blocking, SftpException, Unit] =
      effectBlocking {
        session.channel.put(is, path, ChannelSftp.OVERWRITE)
      }.refineToOrDie[SftpException]
  }

  object SFTP {
    def live(config: SFTPConfig) =
      ZLayer.fromManaged {
        for {
          sftpSession <- SFTPSession.create(config)
        } yield new SFTP(sftpSession)
      }
  }

  def ls(path: String) =
    ZIO.service[SFTP].flatMap(_.ls(path))

  def mkdir(path: String) =
    ZIO.service[SFTP].flatMap(_.mkdir(path))

  def upload(location: String, is: InputStream) =
    ZIO.service[SFTP].flatMap(_.upload(location, is))
}