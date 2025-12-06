package cats_mtl_example.http

import cats.*
import cats.effect.*
import cats.mtl.Handle
import cats.syntax.all.*
import cats_mtl_example.external.JokeClient
import cats_mtl_example.external.types.HttpError
import cats_mtl_example.http.types.ErrorMessage
import loggerf.core.Log
import loggerf.syntax.all.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl

/** @author Kevin Lee
  * @since 2022-04-03
  */
object JokeRoutes {
  def apply[F[*]: {Sync, Log, Http4sDsl as dsl}](
    jokeClient: JokeClient[F],
  ): HttpRoutes[F] = {
    import dsl.*
    HttpRoutes.of[F] {
      case GET -> Root / "joke" =>
        Handle
          .allow[HttpError](
            (for {
              joke <- jokeClient.getJoke
              resp <- Ok(joke)
            } yield resp)
//              .onError {
//                case err => // This err is cats.mtl.Handle$Submarine ☹️
//                  s"Failed to get a joke: ${err.toString}".logS_(error)
//              }
          )
          .rescue {
            case HttpError.ResponseBodyDecodingFailure(message, _) =>
              val errorMessage = s"Fetched joke but it couldn't be decoded: $message"
              errorMessage.logS_(error) *>
                InternalServerError(ErrorMessage(errorMessage))

            case err @ HttpError.FailedResponse(response) =>
              val errorMessage = s"Something went wrong: ${err.message}"
              errorMessage.logS_(error) *>
                InternalServerError(ErrorMessage(errorMessage))
          }
    }
  }
}
