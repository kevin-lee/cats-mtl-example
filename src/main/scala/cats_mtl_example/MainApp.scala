package cats_mtl_example

import cats.effect.{IO, IOApp}
import cats.syntax.all.*
import cats_mtl_example.config.AppConfig
import cats_mtl_example.config.AppConfig.InvalidConfigError
import cats_mtl_example.data.error.AppError
import effectie.instances.ce3.fx.ioFx
import extras.cats.syntax.all.*
import loggerf.logger.*
import org.http4s.dsl.{Http4sDsl, Http4sDslBinCompat}

object MainApp extends IOApp.Simple {

  given canLog: CanLog = Slf4JLogger.slf4JCanLog[this.type]

//  type F[A] = EitherT[IO, AppError, A]
  type F[A] = IO[A]
  val F: IO.type = IO

  given dsl: Http4sDsl[F] = new Http4sDslBinCompat[F] {}

  override def run: IO[Unit] =
    for {
      appConfigOrError <- AppConfig
                            .load[F]
                            .innerLeftMap(err => InvalidConfigError(err.prettyPrint(0)))

      _ <- appConfigOrError match {
             case Right(appConfig) =>
               MainServer
                 .stream[F](appConfig)
                 .compile
                 .drain
             case Left(err) =>
               F.raiseError(AppError.toException(err))
           }
    } yield ()

}
