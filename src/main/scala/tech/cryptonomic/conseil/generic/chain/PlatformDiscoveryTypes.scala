package tech.cryptonomic.conseil.generic.chain

import tech.cryptonomic.conseil.generic.chain.DataTypes.InvalidPredicateFiltering
import tech.cryptonomic.conseil.generic.chain.PlatformDiscoveryTypes.DataType.DataType
import tech.cryptonomic.conseil.generic.chain.PlatformDiscoveryTypes.KeyType.KeyType

/**
  * Classes used for Platform routes
  */
object PlatformDiscoveryTypes {

  /** Case class representing network */
  final case class Platform(name: String, displayName: String, description: Option[String] = None)

  /** Case class representing network */
  final case class Network(
      name: String,
      displayName: String,
      platform: String,
      network: String,
      description: Option[String] = None
  )

  /** Case class representing single entity of a given network */
  final case class Entity(
      name: String,
      displayName: String,
      count: Int,
      displayNamePlural: Option[String] = None,
      description: Option[String] = None,
      limitedQuery: Option[Boolean] = None
  )

  /** Case class representing single attribute of given entity from DB */
  final case class Attribute(
      name: String,
      displayName: String,
      dataType: DataType,
      cardinality: Option[Int],
      keyType: KeyType,
      entity: String,
      description: Option[String] = None,
      placeholder: Option[String] = None,
      dataFormat: Option[String] = None,
      valueMap: Option[Map[String, String]] = None,
      scale: Option[Int] = None,
      reference: Option[Map[String, String]] = None,
      displayPriority: Option[Int] = None,
      displayOrder: Option[Int] = None,
      sufficientForQuery: Option[Boolean] = None
  ) {

    /** Checks if attribute is valid for predicate */
    def doesPredicateContainValidAttribute: List[InvalidPredicateFiltering] =
      if (keyType == KeyType.UniqueKey || dataType == DataType.DateTime || sufficientForQuery.getOrElse(false)) {
        List.empty
      } else {
        List(InvalidPredicateFiltering(s"Query needs to contain a predicate on UniqueKey or DateTime attribute"))
      }
  }

  /** Enumeration of data types */
  object DataType extends Enumeration {
    type DataType = Value
    val Enum, Hex, Binary, Date, DateTime, String, Hash, AccountAddress, Int, LargeInt, Decimal, Boolean = Value
  }

  /** Enumeration of key types */
  object KeyType extends Enumeration {
    type KeyType = Value
    val NonKey, UniqueKey = Value
  }

  /** Attribute cache configuration */
  final case class AttributeCacheConfiguration(
      cached: Boolean = false,
      minMatchLength: Int = 0,
      maxResultSize: Int = Int.MaxValue
  )
}
