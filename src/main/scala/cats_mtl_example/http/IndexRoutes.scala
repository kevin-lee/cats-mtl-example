package cats_mtl_example.http

import cats.effect.Sync
import cats.syntax.all.*
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response, StaticFile}

/** @author Kevin Lee
  * @since 2025-12-06
  */
object IndexRoutes {
  def apply[F[_]: {Sync, Http4sDsl as dsl}](notFound: => F[Response[F]]): HttpRoutes[F] = {
    import dsl.*
    HttpRoutes.of[F] {

      case request @ GET -> Root =>
        StaticFile
          .fromResource[F](name = "/static/index.html", req = request.some)
          .getOrElseF(notFound)
    }
  }
}
