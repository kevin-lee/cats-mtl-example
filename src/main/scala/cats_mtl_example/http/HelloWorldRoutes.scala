package cats_mtl_example.http

import cats.effect.Sync
import io.circe.Json
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.*

/** @author Kevin Lee
  * @since 2022-04-02
  */
object HelloWorldRoutes {

  def apply[F[_]: Sync](using dsl: Http4sDsl[F]): HttpRoutes[F] = {
    import dsl.*
    HttpRoutes.of[F] {
      case GET -> Root =>
        Ok(Json.obj("message" -> Json.fromString("Hello, World")))

      case GET -> Root / name =>
        Ok(Json.obj("message" -> Json.fromString(s"Hello, $name")))

      case GET -> Root / "add" / IntVar(a) / IntVar(b) =>
        Ok(Json.obj("result" -> Json.fromLong(a.toLong + b.toLong)))
    }
  }
}
