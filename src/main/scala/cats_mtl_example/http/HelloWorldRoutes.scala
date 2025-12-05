package cats_mtl_example.http

import cats.effect.Sync
import cats.mtl.Handle
import cats.syntax.all.*
import cats_mtl_example.http.types.{ErrorMessage, ResultResponse}
import cats_mtl_example.service.Hello
import extras.render.syntax.*
import org.http4s.HttpRoutes
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.dsl.Http4sDsl

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
          response   <- Ok(ResultResponse(helloWorld))
        } yield response

      case GET -> Root / name =>
        Handle
          .allow(
            for {
              helloMessage <- hello.hello(name)
              response     <- Ok(ResultResponse(helloMessage))
            } yield response
          )
          .rescue(err => BadRequest(ErrorMessage(err.render)))

      case GET -> Root / "add" / IntVar(a) / IntVar(b) =>
        Ok(ResultResponse(a.toLong + b.toLong))
    }
  }
}
