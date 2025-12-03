package cats_mtl_example.http

import cats.effect.IO
import extras.cats.syntax.all.*
import extras.hedgehog.ce3.CatsEffectRunner
import hedgehog.*
import hedgehog.runner.*
import org.http4s.dsl.Http4sDsl
import org.http4s.{Method, Request, Response, Status, Uri}

/** @author Kevin Lee
  * @since 2022-04-02
  */
object HelloWorldSpec extends Properties, CatsEffectRunner {

  override def tests: List[Test] = List(
    example(helloWorldService("""/ should return "Hello, World""""), testSlashShouldReturnHelloWorld),
    example(helloWorldService("""/ should return 200""""), testSlashShouldReturn200),
    property(helloWorldService("""/{name} should return "Hello, {name}""""), testSlashNameShouldReturnHelloName),
    property(helloWorldService("/{name} should return 200"), testSlashNameShouldReturn200),
    property(
      helloWorldService("/{add}/int1/int2 should return the result of int1 + int2"),
      testSlashAddInt1Int2ShouldReturnInt1PlusInt2
    ),
  )

  private def helloWorldService(testDesc: String): String = s"HelloWorldService $testDesc"

  given ioDsl: Http4sDsl[IO] = org.http4s.dsl.io

  def retHelloWorld(path: String): IO[Response[IO]] = for {

    uri <- IO.delay(Uri.fromString(raw"/$path"))
             .eitherT
             .foldF(
               IO.raiseError(_),
               IO(_)
             )
    getHW = Request[IO](Method.GET, uri)
    response <- HelloWorldRoutes.apply[IO].orNotFound(getHW)
  } yield response

  def testSlashShouldReturnHelloWorld: Result = runIO {

    val expected = raw"""{"message":"Hello, World"}"""

    retHelloWorld("").flatMap(_.as[String]).map { actual =>
      actual ==== expected
    }
  }

  def testSlashShouldReturn200: Result = runIO {
    val expected = Status.Ok
    retHelloWorld("").map(_.status).map { actual =>
      actual ==== expected
    }

  }

  def testSlashNameShouldReturnHelloName: Property =
    for {
      name <- Gen.string(Gen.alpha, Range.linear(1, 10)).log("name")
    } yield runIO {
      val expected = raw"""{"message":"Hello, $name"}"""
      retHelloWorld(name).flatMap(_.as[String]).map { actual =>
        actual ==== expected
      }
    }

  def testSlashNameShouldReturn200: Property =
    for {
      name <- Gen.string(Gen.alpha, Range.linear(1, 10)).log("name")
    } yield runIO {
      val expected = Status.Ok
      retHelloWorld(name).map(_.status).map { actual =>
        actual ==== expected
      }
    }

  def testSlashAddInt1Int2ShouldReturnInt1PlusInt2: Property =
    for {
      n1 <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("n1")
      n2 <- Gen.int(Range.linear(Int.MinValue, Int.MaxValue)).log("n2")
    } yield runIO {
      val expected = raw"""{"result":${(n1.toLong + n2.toLong).toString}}"""
      retHelloWorld(s"add/${n1.toString}/${n2.toString}").flatMap(_.as[String]).map { actual =>
        actual ==== expected
      }

    }

}
