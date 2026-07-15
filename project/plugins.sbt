addSbtPlugin("io.spray" % "sbt-revolver" % "0.10.0")

val sbtDevOopsVersion = "3.6.0"

addSbtPlugin("io.kevinlee" %% "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" %% "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" %% "sbt-devoops-starter"   % sbtDevOopsVersion)

addSbtPlugin("org.wartremover" % "sbt-wartremover" % "3.6.1")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.7")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.1")
