package cats_mtl_example.http

import cats.effect.IO
import cats.mtl.Raise
import cats.syntax.all.*
import cats_mtl_example.external.JokeClient
import cats_mtl_example.external.JokeClient.JokeResponse
import cats_mtl_example.external.types.{HttpError, HttpResponse}
import cats_mtl_example.http.types.ErrorMessage
import effectie.instances.ce3.fx.ioFx
import extras.hedgehog.ce3.CatsEffectRunner
import hedgehog.*
import hedgehog.runner.*
import loggerf.*
import loggerf.core.*
import loggerf.testing.CanLog4Testing
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.syntax.all.*
import refined4s.types.all.*

/** @author Kevin Lee
  * @since 2025-12-06
  */
object JokeRoutesSpec extends Properties, CatsEffectRunner {

  override def tests: List[Test] = List(
    property("GET /joke should return 200 and the joke", testGetJokeReturnsJoke),
    property("GET /joke should return 500 when decoding fails", testGetJokeDecodingFailure),
    property("GET /joke should return 500 when response fails", testGetJokeResponseFailure),
  )

  given canLog4Testing: CanLog4Testing = CanLog4Testing()

  type F[A] = IO[A]
  val F: IO.type = IO

  given ioDsl: Http4sDsl[F] = org.http4s.dsl.io

  def testGetJokeReturnsJoke: Property = for {
    id           <- Gen.string(Gen.alphaNum, Range.linear(10, 16)).map(NonBlankString.unsafeFrom).log("id")
    joke         <- Gen.string(Gen.alphaNum, Range.linear(10, 20)).map(NonBlankString.unsafeFrom).log("joke")
    jokeResponse <- Gen.constant(JokeResponse(JokeResponse.Id(id), JokeResponse.Joke(joke))).log("jokeResponse")
  } yield runIO {

    val client  = new JokeClient[F] {
      override def getJoke: FE[HttpError, JokeResponse] = jokeResponse.pure[F]
    }
    val routes  = JokeRoutes[F](client)
    val request = Request[F](Method.GET, uri"/joke")

    for {
      response     <- routes.orNotFound(request)
      statusResult <- F.delay(response.status ==== Status.Ok)
      body         <- response.as[JokeResponse]
    } yield {
      Result.all(
        List(
          statusResult.log("Status should be 200 OK"),
          body ==== jokeResponse
        )
      )
    }
  }

  def testGetJokeDecodingFailure: Property = for {
    errorMessage <- Gen.string(Gen.alpha, Range.linear(1, 100)).log("errorMessage")
  } yield runIO {
    val error   = HttpError.ResponseBodyDecodingFailure(errorMessage, None)
    val client  = new JokeClient[F] {
      override def getJoke: FE[HttpError, JokeResponse] = Raise.raise(error)
    }
    val routes  = JokeRoutes[F](client)
    val request = Request[F](Method.GET, uri"/joke")

    val expectedBody = ErrorMessage(s"Fetched joke but it couldn't be decoded: $errorMessage")

    for {
      response     <- routes.orNotFound(request)
      statusResult <- F.delay(response.status ==== Status.InternalServerError)
      body         <- response.as[ErrorMessage]
    } yield {
      Result.all(
        List(
          statusResult.log("Status should be 500 Internal Server Error"),
          body ==== expectedBody,
        )
      )
    }
  }

  def testGetJokeResponseFailure: Property = for {
    responseBody <- Gen.string(Gen.alpha, Range.linear(1, 100)).log("errorMessage")
  } yield runIO {
    val httpResponse = HttpResponse(
      HttpResponse.Status(Status.BadRequest),
      Some(HttpResponse.Body(NonEmptyString.unsafeFrom(responseBody).value))
    )

    val error   = HttpError.FailedResponse(httpResponse)
    val client  = new JokeClient[F] {
      override def getJoke: FE[HttpError, JokeResponse] = Raise.raise(error)
    }
    val routes  = JokeRoutes[F](client)
    val request = Request[F](Method.GET, uri"/joke")

    val expectedBody = ErrorMessage(s"Something went wrong: ${error.message}")

    for {
      response     <- routes.orNotFound(request)
      statusResult <- F.delay(response.status ==== Status.InternalServerError)
      body         <- response.as[ErrorMessage]
    } yield {
      Result.all(
        List(
          statusResult.log("Status should be 500 Internal Server Error"),
          body ==== expectedBody,
        )
      )
    }
  }

}
