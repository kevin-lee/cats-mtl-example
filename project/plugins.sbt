addSbtPlugin("io.spray" % "sbt-revolver" % "0.10.0")

val sbtDevOopsVersion = "3.3.1"

addSbtPlugin("io.kevinlee" %% "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" %% "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" %% "sbt-devoops-starter"   % sbtDevOopsVersion)

addSbtPlugin("org.wartremover" % "sbt-wartremover" % "3.5.0")
addSbtPlugin("ch.epfl.scala"   % "sbt-scalafix"    % "0.14.5")
addSbtPlugin("org.scalameta"   % "sbt-scalafmt"    % "2.5.6")
