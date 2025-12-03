package cats_mtl_example

import cats.effect.{IO, IOApp}
import cats.syntax.all.*
import cats_mtl_example.config.AppConfig
import cats_mtl_example.config.AppConfig.InvalidConfigError
import extras.cats.syntax.all.*
import org.http4s.dsl.{Http4sDsl, Http4sDslBinCompat}

object MainApp extends IOApp.Simple {

//  type F[A] = EitherT[IO, AppError, A]
  type F[A] = IO[A]

//  given val dsl: Http4sDsl[F] = org.http4s.dsl.io
  given dsl: Http4sDsl[F] = new Http4sDslBinCompat[F] {}

  override def run: IO[Unit] =
    (for {
      appConfig <- AppConfig
                     .load[F]
                     .eitherT
                     .flatMapF(conf => conf.asRight.pure[IO])
                     .leftFlatMap(err => IO.pure(InvalidConfigError(err.prettyPrint(0)).asLeft[AppConfig]).eitherT)
      _         <- MainServer
                     .stream[F](appConfig)
                     .compile
                     .drain
                     .rightT
    } yield ())
      .foldF(err => IO.raiseError(err), _ => IO.unit)

}
