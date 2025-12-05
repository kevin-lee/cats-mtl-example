package cats_mtl_example.service

import cats.effect.kernel.Sync
import cats.mtl.Raise
import cats_mtl_example.common.FEA
import extras.render.Render
import extras.core.syntax.all.*

/** @author Kevin Lee
  * @since 2025-12-04
  */
trait Hello[F[*]] {
  type FE[E, A] = FEA[F, E, A]

  def world: F[String]

  def hello(name: String): FE[Hello.InvalidNameError, String]

}
object Hello {

  def apply[F[*]: Sync]: Hello[F] = HelloF[F]

  private final class HelloF[F[*]: {Sync as F}] extends Hello[F] {
    override def world: F[String] = F.pure("Hello, World!")

    override def hello(name: String): FE[InvalidNameError, String] = {
      if name.isEmpty then Raise.raise(InvalidNameError.EmptyString)
      else if name.isBlank then Raise.raise(InvalidNameError.BlankString(name))
      else F.pure(s"Hello, $name")
    }
  }

  enum InvalidNameError {
    case EmptyString
    case BlankString(value: String)
  }
  object InvalidNameError {
    given renderInvalidNameError: Render[InvalidNameError] = {
      case InvalidNameError.EmptyString =>
        "An empty String is given for the name. The name can't be an empty String."

      case InvalidNameError.BlankString(value) =>
        s"A blank String is given for the name. The name can't be a blank String. " +
          s"value='$value' / unicode=${value.encodeToUnicode}"
    }
  }

}
