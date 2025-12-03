package cats_mtl_example.external

import cats.effect.*
import cats.mtl.Raise
import cats.syntax.all.*
import cats_mtl_example.common.*
import cats_mtl_example.external.JokeClient.JokeResponse
import cats_mtl_example.external.types.*
import cats_mtl_example.external.types.HttpResponse.TakeNBytes
import io.circe.{Codec, Decoder, Encoder}
import org.http4s.Method.GET
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.Accept
import org.http4s.syntax.all.*
import org.http4s.{MediaType, Uri}
import refined4s.*
import refined4s.modules.circe.derivation.CirceNewtypeCodec
import refined4s.modules.circe.derivation.types.all.given
import refined4s.types.all.*

/** @author Kevin Lee
  * @since 2022-04-03
  */
trait JokeClient[F[*]] {
  type FE[E, A] = FEA[F, E, A]

  def getJoke: FE[HttpError, JokeResponse]

}
object JokeClient {

  def apply[F[*]: {Concurrent, Client}]: JokeClient[F] = JokeClientF[F]

  private final class JokeClientF[F[*]: {Concurrent, Client as client}] extends JokeClient[F], Http4sClientDsl[F] {
    val url: Uri = uri"https://icanhazdadjoke.com/"

    override def getJoke: FE[HttpError, JokeResponse] =
      for {
        req      <- GET(url, Accept(MediaType.application.json)).pure
        result   <- client
                      .run(req)
                      .use(
                        HttpResponse.responseHandler[F, JokeResponse](
                          takeNBytesWhenFail = TakeNBytes.nBytes(NonNegLong(4096L))
                        )
                      )
        response <- result match {
                      case Right(r) => r.pure
                      case Left(err) => Raise.raise(err)
                    }
      } yield response

  }

  final case class JokeResponse(id: JokeResponse.Id, joke: JokeResponse.Joke) derives Codec.AsObject
  object JokeResponse {

    type Id = Id.Type
    object Id extends Newtype[PosLong], CirceNewtypeCodec[PosLong]

    type Joke = Joke.Type
    object Joke extends Newtype[NonEmptyString], CirceNewtypeCodec[NonEmptyString]

  }
}
