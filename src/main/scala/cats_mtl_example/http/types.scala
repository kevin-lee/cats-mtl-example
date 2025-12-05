package cats_mtl_example.http

import io.circe.*

/** @author Kevin Lee
  * @since 2025-12-04
  */
object types {
  final case class ResultResponse[A](result: A)
  object ResultResponse {
    given resultResponseCodec[A: {Encoder, Decoder}]: Codec[ResultResponse[A]] =
      Codec.AsObject.derived[ResultResponse[A]]
  }

  final case class ErrorMessage(error: String) derives Codec.AsObject

}
