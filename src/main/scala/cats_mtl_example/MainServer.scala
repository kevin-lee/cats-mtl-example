package cats_mtl_example

import cats.effect.*
import cats.syntax.all.*
import cats_mtl_example.config.AppConfig
import cats_mtl_example.external.JokeClient
import cats_mtl_example.http.{HelloWorldRoutes, JokeRoutes, StaticHtmlRoutes}
import cats_mtl_example.service.Hello
import fs2.Stream
import org.http4s.HttpRoutes
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.client.Client
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

/** @author Kevin Lee
  * @since 2022-04-02
  */
object MainServer {

  def stream[F[*]: {Async, Http4sDsl}](appConfig: AppConfig): Stream[F, ExitCode] =
    for {

      client <- BlazeClientBuilder[F].stream
      given Client[F]     = client
      given JokeClient[F] = JokeClient[F]

      hello = Hello[F]

      jokeRoutes = JokeRoutes[F]
      httpApp    = (appRoutes(hello) <+> jokeRoutes).orNotFound
      exitCode <- BlazeServerBuilder[F]
                    .bindHttp(
                      appConfig.server.port.value.value,
                      appConfig.server.host.value.renderString
                    )
                    .withHttpApp(httpApp)
                    .serve
    } yield exitCode

  def helloWorldService[F[*]: {Sync, Http4sDsl}](hello: Hello[F]): HttpRoutes[F] =
    HelloWorldRoutes[F](hello)

  def staticHtmlService[F[*]: Sync](using dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl.*
    StaticHtmlRoutes[F](dsl.NotFound())
  }

//  def jokeRoutes[F[*]: {Async, Http4sDsl}](using client: JokeClient[F], H: Handle[F, HttpError]): HttpRoutes[F] =
//    JokeRoutes[F]

  def appRoutes[F[*]: {Sync, Http4sDsl}](hello: Hello[F]): HttpRoutes[F] =
    Router(
      "/hello" -> helloWorldService(hello),
      "/html"  -> staticHtmlService,
    )

}
