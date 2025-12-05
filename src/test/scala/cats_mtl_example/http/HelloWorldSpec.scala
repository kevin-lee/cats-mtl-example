package cats_mtl_example.http

import cats.effect.IO
import cats_mtl_example.service.Hello
import extras.cats.syntax.all.*
import extras.core.syntax.all.*
import extras.hedgehog.ce3.CatsEffectRunner
import hedgehog.*
import hedgehog.runner.*
import org.http4s.dsl.Http4sDsl
import org.http4s.{Method, Request, Response, Status, Uri}

/** @author Kevin Lee
  * @since 2022-04-02
  */
object HelloWorldSpec extends Properties, CatsEffectRunner {

  type F[A] = IO[A]
  val F: IO.type = IO

  override def tests: List[Test] = List(
    example(helloWorldService("""/ should return "Hello, World""""), testSlashShouldReturnHelloWorld),
    example(helloWorldService("""/ should return 200""""), testSlashShouldReturn200),
    property(helloWorldService("""/{name} should return "Hello, {name}""""), testSlashNameShouldReturnHelloName),
    property(helloWorldService("/{name} should return 200"), testSlashNameShouldReturn200),
    property(
      helloWorldService("""/{name} should return {"error": message... } when the given name is a blank String"""),
      testSlashNameShouldReturnErrorForBlankStringName
    ),
    property(
      helloWorldService("/{name} should return 400 when the given name is a blank String"),
      testSlashNameShouldReturn400ForBlankStringName
    ),
    property(
      helloWorldService("/{add}/int1/int2 should return the result of int1 + int2"),
      testSlashAddInt1Int2ShouldReturnInt1PlusInt2
    ),
  )

  private def helloWorldService(testDesc: String): String = s"HelloWorldService $testDesc"

  given ioDsl: Http4sDsl[F] = org.http4s.dsl.io

  def retHelloWorld(path: String): IO[Response[F]] = {
    val hello            = Hello[F]
    val helloWorldRoutes = HelloWorldRoutes[F](hello)
    val httpEncodedPath  = Uri.encode(s"/$path")
    for {

      uri <- F.delay(Uri.fromString(httpEncodedPath))
               .eitherT
               .foldF(
                 F.raiseError(_),
                 F.delay(_)
               )
      getHW = Request[F](Method.GET, uri)
      response <- helloWorldRoutes.orNotFound(getHW)
    } yield response
  }

  def testSlashShouldReturnHelloWorld: Result = runIO {

    val expected = raw"""{"message":"Hello, World!"}"""

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
      for {
        response <- retHelloWorld(name)
        actual = response.status
      } yield {
        actual ==== expected
      }
    }

  def testSlashNameShouldReturnErrorForBlankStringName: Property =
    for {
      name <- Gen
                .string(
                  Gen.element1('\u0009', '\u000a', '\u000b', '\u000c', '\u000d', '\u001c', '\u001d', '\u001e', '\u001f',
                    '\u0020', '\u1680', '\u2000', '\u2001', '\u2002', '\u2003', '\u2004', '\u2005', '\u2006', '\u2008',
                    '\u2009', '\u200a', '\u2028', '\u2029', '\u205f', '\u3000'),
                  Range.linear(1, 10)
                )
                .log("name")
      _    <- Gen.constant(name.encodeToUnicode).log("nameInUnicode")
    } yield runIO {
      import io.circe.literal.*
      val value =
        "A blank String is given for the name. The name can't be a blank String. " +
          s"value='$name' / unicode=${name.encodeToUnicode}"

      val expected = json"""{ "error": $value }"""

      for {
        response <- retHelloWorld(name)
        actual   <- response.as[String]
      } yield {
        actual ==== expected.noSpaces
      }
    }

  def testSlashNameShouldReturn400ForBlankStringName: Property =
    for {
      name <- Gen
                .string(
                  Gen.element1('\u0009', '\u000a', '\u000b', '\u000c', '\u000d', '\u001c', '\u001d', '\u001e', '\u001f',
                    '\u0020', '\u1680', '\u2000', '\u2001', '\u2002', '\u2003', '\u2004', '\u2005', '\u2006', '\u2008',
                    '\u2009', '\u200a', '\u2028', '\u2029', '\u205f', '\u3000'),
                  Range.linear(1, 10)
                )
                .log("name")
      _    <- Gen.constant(name.encodeToUnicode).log("nameInUnicode")
    } yield runIO {
      val expected = Status.BadRequest
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
