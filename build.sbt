lazy val commonSettings = Seq(
  scalaVersion := "2.11.8"
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "springer",
    libraryDependencies += "org.scalatest" % "scalatest_2.11" % "3.0.0" % "test"
    libraryDependencies += "org.http4s" %% "http4s-blaze-server" % "0.13.2a"
    libraryDependencies += "org.http4s" %% "http4s-dsl"          % "0.13.2a"
    libraryDependencies += "com.google.guava" % "guava" % "19.0-rc1"
    libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.7"
    libraryDependencies += "io.argonaut" %% "argonaut" % "6.2-M2"

  )
