lazy val http4sVersion = "0.14.11"
libraryDependencies += "org.scalatest" % "scalatest_2.11" % "3.0.0" % "test"
libraryDependencies += "org.http4s" %% "http4s-blaze-server" % http4sVersion
libraryDependencies += "org.http4s" %% "http4s-dsl"          % http4sVersion
libraryDependencies += "org.http4s" %% "http4s-blaze-client" % http4sVersion
libraryDependencies += "com.google.guava" % "guava" % "19.0-rc1"
libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.7"
libraryDependencies += "io.argonaut" %% "argonaut" % "6.2-M2"
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.7"

name := "watermark"

scalaVersion := "2.11.8"
