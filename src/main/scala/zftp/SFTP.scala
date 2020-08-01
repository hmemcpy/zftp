package zftp

import java.util.{Vector => JVector}

import com.jcraft.jsch.ChannelSftp
import zio.blocking.{Blocking, effectBlocking}
import zio.{ZIO, ZLayer}

import scala.jdk.CollectionConverters._

final class SFTP(session: SFTPSession) {

  def ls(path: String): ZIO[Blocking, Throwable, List[ChannelSftp#LsEntry]] =
    effectBlocking {
      session.channel.ls(path).asInstanceOf[JVector[ChannelSftp#LsEntry]]
        .asScala
        .toList
    }

}

object SFTP {
  def live(config: SFTPConfig) =
    ZLayer.fromManaged {
      for {
        sftpSession <- SFTPSession.create(config)
      } yield new SFTP(sftpSession)
    }
}
