package cats_mtl_example.config

import cats.effect.*
import cats_mtl_example.data.error.AppError
import refined4s.*
import refined4s.types.all.*
import refined4s.modules.pureconfig.derivation.types.all.given
import org.http4s.Uri
import pureconfig.module.http4s.*
import pureconfig.{ConfigReader, ConfigSource}
import refined4s.modules.pureconfig.derivation.PureconfigNewtypeConfigReader

/** @author Kevin Lee
  * @since 2022-04-02
  */
final case class AppConfig(
  server: AppConfig.ServerConfig
) derives ConfigReader
object AppConfig {

  def load[F[*]: Sync]: F[ConfigReader.Result[AppConfig]] =
    Sync[F].delay(ConfigSource.default.load[AppConfig])

  final case class ServerConfig(host: ServerConfig.HostAddress, port: ServerConfig.Port) derives ConfigReader
  object ServerConfig {
    type HostAddress = HostAddress.Type
    object HostAddress extends Newtype[Uri], PureconfigNewtypeConfigReader[Uri]

    type Port = Port.Type
    object Port extends Newtype[PortNumber], PureconfigNewtypeConfigReader[PortNumber]
  }

  final case class InvalidConfigError(override val message: String) extends AppError(message)

}
