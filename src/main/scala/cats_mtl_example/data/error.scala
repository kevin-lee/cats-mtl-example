package cats_mtl_example.data

import cats_mtl_example.config.AppConfig
import cats_mtl_example.external.types.HttpError
import cats_mtl_example.service.Hello
import extras.render.syntax.*

//import scala.util.control.NoStackTrace

/** @author Kevin Lee
  * @since 2022-04-02
  */
object error {
//  abstract class AppError(val message: String) extends NoStackTrace {
//    override def getMessage: String = message
//  }

  // TODO: Since errors from Routes were handled inside Routes, this need to be redesigned.
  type AppError = AppConfig.InvalidConfigError | HttpError | Hello.InvalidNameError
  object AppError {

    def render(appError: AppError): String = appError match {
      case AppConfig.InvalidConfigError(message) => s"Invalid config: $message"
      case err: HttpError => HttpError.render(err)
      case err: Hello.InvalidNameError => err.render
    }

    def toException(appError: AppError): RuntimeException = new RuntimeException(render(appError))

  }

}
