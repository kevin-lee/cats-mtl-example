package cats_mtl_example.data

import cats_mtl_example.external.types.HttpError

import scala.util.control.NoStackTrace

/** @author Kevin Lee
  * @since 2022-04-02
  */
object error {
  abstract class AppError(val message: String) extends NoStackTrace {
    override def getMessage: String = message
  }

  object AppError {
    def render(appError: AppError): String = appError match {
      case err: HttpError => HttpError.render(err)
    }
  }

}
