ThisBuild / organization := props.Org
ThisBuild / scalaVersion := props.ScalaVersion
ThisBuild / version := props.ProjectVersion

lazy val http4sExampleApp = (project in file("."))
  .settings(
    name := props.ProjectName,
    scalacOptions ++= List("-explain"),
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    wartremoverErrors ++= Warts.allBut(Wart.ImplicitParameter, Wart.ImplicitConversion, Wart.Any, Wart.Nothing),
    libraryDependencies ++= libs.all,
  )

lazy val props = new {
  val Org = "io.kevinlee"

  val ProjectVersion = "0.1.0-SNAPSHOT"

  val ScalaVersion = "3.7.4"

  val ProjectName = "cats-mtl-example-app"

  val Refined4sVersion = "1.14.1"

  val CatsVersion = "2.13.0"

  val CatsEffectVersion = "3.6.3"

  val CatsMtlVersion = "1.6.0"

  val CirceVersion = "0.14.15"

  val EffectieVersion = "2.3.0"

  val Http4sVersion      = "0.23.33"
  val Http4sBlazeVersion = "0.23.17"

  val PureconfigVersion = "0.17.10"

  val LogbackVersion = "1.5.27"

  val LoggerFVersion = "2.9.0"

  val extrasVersion = "0.50.1"

  val KittensVersion = "3.5.0"

  val hedgehogVersion = "0.13.0"
}

lazy val libs = new {

  lazy val refined4s = List(
    "io.kevinlee" %% "refined4s-core"          % props.Refined4sVersion,
    "io.kevinlee" %% "refined4s-cats"          % props.Refined4sVersion,
    "io.kevinlee" %% "refined4s-circe"         % props.Refined4sVersion,
    "io.kevinlee" %% "refined4s-pureconfig"    % props.Refined4sVersion,
    "io.kevinlee" %% "refined4s-doobie-ce3"    % props.Refined4sVersion,
    "io.kevinlee" %% "refined4s-extras-render" % props.Refined4sVersion,
    "io.kevinlee" %% "refined4s-tapir"         % props.Refined4sVersion,
    "io.kevinlee" %% "refined4s-chimney"       % props.Refined4sVersion,
  )

  lazy val cats       = "org.typelevel" %% "cats-core"   % props.CatsVersion
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % props.CatsEffectVersion
  lazy val catsMtl    = "org.typelevel" %% "cats-mtl"    % props.CatsMtlVersion

  lazy val circe = List(
    "io.circe" %% "circe-core"    % props.CirceVersion,
    "io.circe" %% "circe-generic" % props.CirceVersion,
    "io.circe" %% "circe-parser"  % props.CirceVersion,
    "io.circe" %% "circe-literal" % props.CirceVersion,
  )

  lazy val http4s = List(
    "org.http4s" %% "http4s-blaze-server" % props.Http4sBlazeVersion,
    "org.http4s" %% "http4s-blaze-client" % props.Http4sBlazeVersion,
    "org.http4s" %% "http4s-circe"        % props.Http4sVersion,
    "org.http4s" %% "http4s-dsl"          % props.Http4sVersion,
  )

  lazy val pureconfigCirce      = "com.github.pureconfig" %% "pureconfig-circe"       % props.PureconfigVersion
//  lazy val pureconfigGeneric    = "com.github.pureconfig" %% "pureconfig-generic"     % props.PureconfigVersion
  lazy val pureconfigCatsEffect = "com.github.pureconfig" %% "pureconfig-cats-effect" % props.PureconfigVersion
  lazy val pureconfigHttp4s     = "com.github.pureconfig" %% "pureconfig-http4s"      % props.PureconfigVersion

  lazy val effectie = List(
    "io.kevinlee" %% "effectie-cats-effect3" % props.EffectieVersion,
  )

  lazy val logback = "ch.qos.logback" % "logback-classic" % props.LogbackVersion

  lazy val loggerF = List(
    "io.kevinlee" %% "logger-f-cats"     % props.LoggerFVersion,
    "io.kevinlee" %% "logger-f-slf4j"    % props.LoggerFVersion,
    "io.kevinlee" %% "logger-f-test-kit" % props.LoggerFVersion % Test,
  )

  lazy val extras = List(
    "io.kevinlee" %% "extras-cats"         % props.extrasVersion,
    "io.kevinlee" %% "extras-hedgehog-ce3" % props.extrasVersion,
  )

  lazy val kittens = "org.typelevel" %% "kittens" % props.KittensVersion

  lazy val testLibs = List(
    "io.kevinlee" %% "extras-hedgehog-ce3" % props.extrasVersion,
    "qa.hedgehog" %% "hedgehog-core"       % props.hedgehogVersion,
    "qa.hedgehog" %% "hedgehog-runner"     % props.hedgehogVersion,
    "qa.hedgehog" %% "hedgehog-sbt"        % props.hedgehogVersion,
  ).map(_ % Test)

  lazy val all =
    List(
      cats,
      catsEffect,
      catsMtl,
      pureconfigCirce,
//      pureconfigGeneric,
      pureconfigCatsEffect,
      pureconfigHttp4s,
      kittens,
      logback
    ) ++ effectie ++ loggerF ++
      circe ++ extras ++ refined4s ++ http4s ++ testLibs
}
