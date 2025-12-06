package cats_mtl_example

import cats.effect.*
import cats.syntax.all.*
import cats_mtl_example.config.AppConfig
import cats_mtl_example.external.JokeClient
import cats_mtl_example.http.{HelloWorldRoutes, IndexRoutes, JokeRoutes, StaticHtmlRoutes}
import cats_mtl_example.service.Hello
import extras.render.syntax.*
import fs2.Stream
import loggerf.core.Log
import org.http4s.HttpApp
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

/** @author Kevin Lee
  * @since 2022-04-02
  */
object MainServer {

  def stream[F[*]: {Async, Log, Http4sDsl}](appConfig: AppConfig): Stream[F, ExitCode] =
    for {

      client <- BlazeClientBuilder[F].stream
      jokeClient = JokeClient[F](client)
      hello      = Hello[F]
      httpApp    = buildHttpApp(jokeClient, hello)
      exitCode <- BlazeServerBuilder[F]
                    .bindHttp(
                      port = appConfig.server.port.toValue,
                      host = appConfig.server.host.render
                    )
                    .withHttpApp(httpApp)
                    .serve
    } yield exitCode

  def buildHttpApp[F[*]: {Sync, Log, Http4sDsl as dsl}](
    jokeClient: JokeClient[F],
    hello: Hello[F]
  ): HttpApp[F] = {
    import dsl.*
    val appRoutes  = Router(
      "/"      -> IndexRoutes[F](dsl.NotFound()),
      "/hello" -> HelloWorldRoutes[F](hello),
      "/html"  -> StaticHtmlRoutes[F](f => dsl.NotFound(f("/html"))),
    )
    val jokeRoutes = JokeRoutes[F](jokeClient)
    (appRoutes <+> jokeRoutes).orNotFound
  }

}
