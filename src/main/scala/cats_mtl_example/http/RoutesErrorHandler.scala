package cats_mtl_example.http

import cats.ApplicativeError
import cats.data.{Kleisli, OptionT}
import cats.syntax.all.*
import org.http4s.{HttpRoutes, Response}

trait RoutesErrorHandler[F[*], E] {
  def handle(routes: HttpRoutes[F]): HttpRoutes[F]
}
object RoutesErrorHandler {
  def apply[F[*], E](handler: E => F[Response[F]])(
    using ApplicativeError[F[*], E]
  ): RoutesErrorHandler[F, E] = RoutesErrorHandlerF(handler)

  private final class RoutesErrorHandlerF[F[*], E](handler: E => F[Response[F]])(
    using ApplicativeError[F[*], E]
  ) extends RoutesErrorHandler[F, E] {
    override def handle(routes: HttpRoutes[F]): HttpRoutes[F] =
      Kleisli(req => OptionT(routes.run(req).value.handleErrorWith(e => handler(e).map(Option.apply))))
  }

}
