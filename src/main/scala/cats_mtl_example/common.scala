package cats_mtl_example

import cats.mtl.Raise

/** @author Kevin Lee
  * @since 2025-12-02
  */
object common {

  type FEA[F[*], E, A] = Raise[F, E] ?=> F[A]

}
