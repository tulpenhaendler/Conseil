package tech.cryptonomic.conseil.util

/**
  * A type class that enables to convert an object to another
  * in a single direction (i.e. it's not necessarily an invertible operation).
  * The conversion result is wrapped into a generic "effect" of type `F`
  */
trait Conversion[F[_], FROM, TO] {

  /** Takes a `FROM` object and retuns the `TO` object, with an effect `F`.*/
  def convert(from: FROM): F[TO]

}

/** type class companion */
object Conversion {

  /** This alias allows to return converted objects with no actual wrapping effect
    * This is a type constructor that corresponds to his type parameter, while
    * conforming to the required "shape" `F[_]` for the conversion to be defined.
    */
  type Id[T] = T

  /** Implicitly summons a `Conversion` instance for the given types,
    * if available in scope.
    */
  def apply[F[_], FROM, TO](implicit conv: Conversion[F, FROM, TO]) = conv

}

/** Adds extension methods based on `Conversion.convert` call for any type
  * for which the implicit `Conversion` is available
  */
object ConversionSyntax {

  //extension pattern
  implicit class ConversionOps[FROM](from: FROM) {

    /** converts the object to a `TO` instance, wrapped in a `F` effect. */
    def convertToA[F[_], TO](implicit conv: Conversion[F, FROM, TO]): F[TO] =
    conv.convert(from)

    /** converts the object to a `TO` instance, with no effect wrapping the result */
    def convertTo[TO](implicit conv: Conversion[Conversion.Id, FROM, TO]): TO =
      conv.convert(from)
  }

}