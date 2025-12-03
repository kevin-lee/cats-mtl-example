package cats_mtl_example.external

import cats.syntax.all.*
import cats.effect.Concurrent
import cats_mtl_example.data.error.AppError
import fs2.text
import org.http4s
import org.http4s.{EntityDecoder, Response}
import org.http4s.Status.Successful
import refined4s.*
import refined4s.types.all.*

/** @author Kevin Lee
  * @since 2022-04-03
  */
object types {

  final case class HttpResponse(status: HttpResponse.Status, body: Option[HttpResponse.Body])
  object HttpResponse {

    enum TakeNBytes {
      case AllBytes
      case NBytes(n: NonNegLong)
    }
    object TakeNBytes {

      def nBytes(n: NonNegLong): TakeNBytes = TakeNBytes.NBytes(n)

      def allBytes: TakeNBytes = TakeNBytes.AllBytes

    }

    def httpResponseBodyToString[F[*]: Concurrent](
      response: Response[F],
      takeNBytes: TakeNBytes,
    ): F[String] =
      (takeNBytes match {
        case TakeNBytes.NBytes(n) =>
          response.body.take(n.value)
        case TakeNBytes.AllBytes =>
          response.body
      })
        .through(text.utf8.decode)
        .through(text.lines)
        .compile[F, F, String]
        .string

    def responseHandler[F[*]: Concurrent, A](
      takeNBytesWhenFail: TakeNBytes
    )(using entityDecoderA: EntityDecoder[F, A]): Response[F] => F[Either[HttpError, A]] = {
      case Successful(successResponse) =>
        entityDecoderA
          .decode(successResponse, strict = false)
          .leftMap(failure => HttpError.responseBodyDecodingFailure(failure.message, failure.cause))
          .value

      case failedResponse =>
        val fOfMaybeBody: F[Option[String]] =
          (
            if failedResponse.status.isEntityAllowed then
              httpResponseBodyToString(failedResponse, takeNBytesWhenFail).some
            else none[F[String]]
          ).sequence

        fOfMaybeBody.map { maybeBody =>
          val httpResponse = HttpResponse(
            HttpResponse.Status(
              failedResponse.status,
            ),
            maybeBody.map(HttpResponse.Body(_)),
          )
          HttpError.failedResponse(httpResponse).asLeft[A]
        }
    }

    type Status = Status.Type
    object Status extends Newtype[http4s.Status]

    type Body = Body.Type
    object Body extends Newtype[String]

  }

  sealed abstract class HttpError(override val message: String) extends AppError(message)

  object HttpError {
    final case class ResponseBodyDecodingFailure(override val message: String, cause: Option[Throwable])
        extends HttpError(message)
    final case class FailedResponse(httpResponse: HttpResponse)
        extends HttpError(
          s"HttpError(status=${httpResponse.status.value.renderString}, body=${httpResponse.body.fold("")(_.value)})"
        )

    def responseBodyDecodingFailure(message: String, cause: Option[Throwable]): HttpError =
      ResponseBodyDecodingFailure(message, cause)

    def failedResponse(httpResponse: HttpResponse): HttpError =
      FailedResponse(httpResponse)

    def render(httpError: HttpError): String = httpError match {
      case ResponseBodyDecodingFailure(message, cause) =>
        s"Decoding ResponseBody has failed: message=$message, cause=${cause.fold("")(_.toString)}"

      case err @ FailedResponse(_) =>
        s"Response failure: ${err.getMessage}"
    }
  }
}
