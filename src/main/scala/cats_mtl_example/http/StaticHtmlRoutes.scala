package cats_mtl_example.http

import cats.effect.Sync
import cats.syntax.all.*
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Response, StaticFile}

/** @author Kevin Lee
  * @since 2018-06-16
  */
object StaticHtmlRoutes {
  def apply[F[_]: {Sync, Http4sDsl as dsl}](notFound: (String => String) => F[Response[F]]): HttpRoutes[F] = {
    import dsl.*
    HttpRoutes.of[F] {

      case request @ GET -> Root / filename if filename.endsWith(".html") =>
        StaticFile
          .fromResource[F](name = s"/static/$filename", req = request.some)
          .getOrElseF(notFound(prefix => s"HTML file not found at $prefix/$filename"))
    }
  }
}
