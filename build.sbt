name := "zftp"
version := "0.1"
scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % "1.0.0-RC21-2",
  "com.jcraft" % "jsch" % "0.1.55",
)