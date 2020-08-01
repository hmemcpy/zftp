package zftp

import com.jcraft.jsch.{ChannelSftp, JSch, Session}
import zio.ZManaged
import zio.blocking.{Blocking, effectBlocking}

final case class SFTPConfig(hostname: String, username: String, password: String, privateKey: Option[String])

final case class SFTPSession(session: Session, channel: ChannelSftp)

object SFTPSession {
  def create(config: SFTPConfig) =
    for {
      session <- establishSession(config)
      channel <- createSFTPChannel(session)
    } yield SFTPSession(session, channel)

  def establishSession(config: SFTPConfig): ZManaged[Blocking, Throwable, Session] =
    effectBlocking {
      val jsch = new JSch
      config.privateKey.foreach { privateKey =>
        jsch.addIdentity("pk", privateKey.getBytes, null, null)
      }
      val session = jsch.getSession(config.username, config.hostname)
      session.setConfig("StrictHostKeyChecking", "no")
      session.setPassword(config.password)
      session.connect()
      session
    }.toManaged(session => effectBlocking(session.disconnect()).orDie)

  def createSFTPChannel(session: Session): ZManaged[Blocking, Throwable, ChannelSftp] =
    effectBlocking {
      val channel = session.openChannel("sftp")
      channel.connect()
      channel.asInstanceOf[ChannelSftp]
    }.toManaged(channel => effectBlocking(channel.disconnect()).orDie)

}
