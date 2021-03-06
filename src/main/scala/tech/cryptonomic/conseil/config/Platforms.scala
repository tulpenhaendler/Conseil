package tech.cryptonomic.conseil.config

import com.typesafe.scalalogging.LazyLogging

/** defines configuration types for conseil available platforms */
object Platforms extends LazyLogging {

  /** a trait defining existing platforms */
  sealed trait BlockchainPlatform extends Product with Serializable {

    /** a name usually used in configurations to identify the platforrm */
    def name: String

    /** the type of underlying configuration, specific for the platform */
    type ConfigurationType <: PlatformConfiguration
  }

  case object Tezos extends BlockchainPlatform {
    val name = "tezos"
    type ConfigurationType = TezosConfiguration
  }

  case class UnknownPlatform(name: String) extends BlockchainPlatform {
    type ConfigurationType = UnknownPlatformConfiguration
  }

  object BlockchainPlatform {

    /** maps a generic string to a typed BlockchainPlatform */
    def fromString(s: String) = s match {
      case "tezos" => Tezos
      case unknown => UnknownPlatform(name = unknown)
    }
  }

  /**
    * Collects all platforms defined in configuration in a map.
    * Should associates each platform to a list of its internally defined configuration type, matching the
    * inner `platform.ConfigurationType`
    */
  case class PlatformsConfiguration(platforms: Map[BlockchainPlatform, List[PlatformConfiguration]])

  /** configurations to describe a tezos node */
  final case class TezosNodeConfiguration(
      hostname: String,
      port: Int,
      protocol: String,
      pathPrefix: String = ""
  )

  /** generic trait for any platform configuration, where each instance corresponds to a network available on that chain */
  sealed trait PlatformConfiguration extends Product with Serializable {
    def network: String
  }

  /** collects all config related to a tezos network */
  final case class TezosConfiguration(network: String, nodeConfig: TezosNodeConfiguration) extends PlatformConfiguration

  /** unexpected or yet to define platform */
  final case class UnknownPlatformConfiguration(network: String = "", depth: Depth = Newest)
      extends PlatformConfiguration

}
