package cats_mtl_example.external

import cats.effect.*
import cats.mtl.Handle
import cats.syntax.all.*
import cats_mtl_example.external.JokeClient.JokeResponse
import cats_mtl_example.external.types.{HttpError, HttpResponse}
import extras.hedgehog.ce3.CatsEffectRunner
import hedgehog.*
import hedgehog.runner.*
import io.circe.literal.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.given
import org.http4s.client.Client
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.*
import refined4s.types.all.*

/** @author Kevin Lee
  * @since 2025-12-06
  */
object JokeClientSpec extends Properties, CatsEffectRunner {

  override def tests: List[Test] = List(
    property("test getJoke returns a joke", testGetJoke),
    property("test getJoke returns an error when response body decoding fails", testGetJokeDecodingFailure),
    property("test getJoke returns an error when response failed (non-success status)", testGetJokeResponseFailure),
  )

  type F[A] = IO[A]
  val F: IO.type = IO

  def testGetJoke: Property = for {
    id           <- Gen.string(Gen.alphaNum, Range.linear(10, 16)).map(NonBlankString.unsafeFrom).log("id")
    joke         <- Gen.string(Gen.alphaNum, Range.linear(10, 20)).map(NonBlankString.unsafeFrom).log("joke")
    jokeResponse <- Gen.constant(JokeResponse(JokeResponse.Id(id), JokeResponse.Joke(joke))).log("jokeResponse")
  } yield runIO {

    val json =
      json"""{
               "id": ${id.value},
               "joke": ${joke.value},
               "status": 200
             }"""

    val dsl = new Http4sDsl[F] {}
    import dsl.*

    val client = Client.fromHttpApp[F](
      HttpRoutes
        .of[F] {
          case GET -> Root => Ok(json)
        }
        .orNotFound
    )

    val jokeClient = JokeClient[F](client)

    Handle
      .allow[HttpError](
        jokeClient.getJoke.map(actual => actual ==== jokeResponse)
      )
      .rescue { err =>
        F.pure(Result.failure.log(s"Expected success but got error: ${HttpError.render(err)}"))
      }

  }

  def testGetJokeDecodingFailure: Property = for {
    invalidJson <- Gen.string(Gen.alpha, Range.linear(1, 100)).log("invalidJson")
  } yield runIO {
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    val client = Client.fromHttpApp[F](
      HttpRoutes
        .of[F] {
          case GET -> Root => Ok(invalidJson)
        }
        .orNotFound
    )

    val jokeClient = JokeClient[F](client)

    Handle
      .allow[HttpError](
        jokeClient
          .getJoke
          .map(joke => Result.failure.log(s"Expected ResponseBodyDecodingFailure but got success: $joke"))
      )
      .rescue {
        case HttpError.ResponseBodyDecodingFailure(_, _) =>
          F.pure(Result.success)
        case err =>
          F.pure(Result.failure.log(s"Expected ResponseBodyDecodingFailure but got ${HttpError.render(err)}"))

      }
  }

  def testGetJokeResponseFailure: Property = for {
    status <- Gen.element1(Status.BadRequest, Status.NotFound, Status.InternalServerError).log("status")
    body   <- Gen.string(Gen.alpha, Range.linear(1, 100)).log("body")
  } yield runIO {
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    val client = Client.fromHttpApp[F](
      HttpRoutes
        .of[F] {
          case GET -> Root => Response[F](status).withEntity(body.asJson).pure[F]
        }
        .orNotFound
    )

    val jokeClient = JokeClient[F](client)

    Handle
      .allow[HttpError](
        jokeClient
          .getJoke
          .map(joke => Result.failure.log(s"Expected FailedResponse but got success: $joke"))
      )
      .rescue {
        case HttpError.FailedResponse(httpResponse) =>
          F.pure(
            Result.all(
              List(
                (httpResponse.status.value ==== status).log(s"Status should be ${status.code}"),
                (httpResponse.body.map(_.value) ==== Some(body.asJson.noSpaces))
                  .log(s"Body should be ${body.asJson.noSpaces}"),
              )
            )
          )
        case err =>
          F.pure(Result.failure.log(s"Expected FailedResponse but got ${HttpError.render(err)}"))
      }

  }

}
