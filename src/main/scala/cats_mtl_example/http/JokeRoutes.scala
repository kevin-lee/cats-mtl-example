package cats_mtl_example.http

import cats.*
import cats.effect.*
import cats.mtl.Handle
import cats.syntax.all.*
import cats_mtl_example.data.error.AppError
import cats_mtl_example.external.JokeClient
import cats_mtl_example.external.types.HttpError
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import io.circe.syntax.*

/** @author Kevin Lee
  * @since 2022-04-03
  */
object JokeRoutes {
  def apply[F[*]: Sync](
    using jokeClient: JokeClient[F],
    dsl: Http4sDsl[F],
  ): HttpRoutes[F] = {
    import dsl.*
    HttpRoutes.of[F] {
      case GET -> Root / "joke" =>
        Handle
          .allow[HttpError](
            for {
              joke <- jokeClient.getJoke
              resp <- Ok(joke)
            } yield resp
          )
          .rescue(err => InternalServerError(AppError.render(err)))
    }
  }

  class JokeRoutesErrorHandler[F[*]](using MonadError[F[*], HttpError])
      extends RoutesErrorHandler[F, HttpError]
      with Http4sDsl[F] {

    private val handler: HttpError => F[Response[F]] = {
      case HttpError.ResponseBodyDecodingFailure(message, _) =>
        InternalServerError(s"Something went wrong: $message".asJson)
      case err @ HttpError.FailedResponse(response) =>
        InternalServerError(s"Something went wrong: ${err.getMessage}".asJson)
    }

    val jokeRoutesErrorHandler: RoutesErrorHandler[F, HttpError] = RoutesErrorHandler(handler)

    override def handle(routes: HttpRoutes[F]): HttpRoutes[F] =
      jokeRoutesErrorHandler.handle(routes)

  }
}
