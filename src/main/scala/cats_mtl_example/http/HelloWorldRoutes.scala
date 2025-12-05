package cats_mtl_example.http

import cats.effect.Sync
import cats.syntax.all.*
import cats.mtl.Handle
import cats_mtl_example.service.Hello
import extras.render.syntax.*
import io.circe.Json
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.*

/** @author Kevin Lee
  * @since 2022-04-02
  */
object HelloWorldRoutes {

  def apply[F[_]: Sync](hello: Hello[F])(using dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl.*
    HttpRoutes.of[F] {
      case GET -> Root =>
        for {
          helloWorld <- hello.world
          response   <- Ok(Json.obj("message" -> Json.fromString(helloWorld)))
        } yield response

      case GET -> Root / name =>
        Handle
          .allow(
            for {
              helloMessage <- hello.hello(name)
              response     <- Ok(Json.obj("message" -> Json.fromString(helloMessage)))
            } yield response
          )
          .rescue(err => BadRequest(Json.obj("error" -> Json.fromString(err.render))))

      case GET -> Root / "add" / IntVar(a) / IntVar(b) =>
        Ok(Json.obj("result" -> Json.fromLong(a.toLong + b.toLong)))
    }
  }
}
