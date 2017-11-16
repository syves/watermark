resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
lazy val http4sVersion = "0.16.5"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.4"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test"
libraryDependencies += "org.http4s" %% "http4s-blaze-server" % http4sVersion
libraryDependencies += "org.http4s" %% "http4s-dsl"          % http4sVersion
libraryDependencies += "org.http4s" %% "http4s-blaze-client" % http4sVersion
//remove quava?
libraryDependencies += "com.google.guava" % "guava" % "19.0-rc1"
//libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.7"
//update akka?
//libraryDependencies += "com.typesafe.akka" % "akka-actor_2.11" % "2.4.12"
//libraryDependencies += "com.typesafe.akka" %% "akka-slf4j"     % "2.4.12"
//libraryDependencies += "com.typesafe.akka" %% "akka-remote"    % "2.4.12"
//libraryDependencies += "com.typesafe.akka" %% "akka-agent"     % "2.4.12"
//libraryDependencies += "com.typesafe.akka" %% "akka-testkit"   % "2.4.12" % "test"
libraryDependencies += "io.argonaut" %% "argonaut" % "6.2"
//Note that the 6.2 development stream supports scala 2.10.* and 2.11.* with scalaz 7.2.*.

//libraryDependencies += "org.http4s" %% "http4s-argonaut" % http4sVersion
//libraryDependencies += "com.github.alexarchambault" %% "argonaut-shapeless_6.2" % "1.2.0-M3"

//resolvers ++= Seq(Resolver.sonatypeRepo("releases"), Resolver.sonatypeRepo("snapshots"))

name := "wordcount"

scalaVersion := "2.12.0"
