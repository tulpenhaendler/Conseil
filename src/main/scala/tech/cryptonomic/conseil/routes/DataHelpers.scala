package tech.cryptonomic.conseil.routes

import java.sql.Timestamp

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.ByteString
import cats.Functor
import endpoints.akkahttp
import endpoints.algebra.Documentation
import tech.cryptonomic.conseil.generic.chain.DataTypes._
import tech.cryptonomic.conseil.routes.openapi.{DataEndpoints, QueryStringListsServer, Validation}
import tech.cryptonomic.conseil.tezos.Tables

/** Trait with helpers needed for data routes */
trait DataHelpers extends QueryStringListsServer with Validation with akkahttp.server.Endpoints
  with akkahttp.server.JsonSchemaEntities with DataEndpoints {

  import io.circe._
  import io.circe.syntax._
  import tech.cryptonomic.conseil.util.Conversion.Syntax._
  import tech.cryptonomic.conseil.routes.openapi.CsvConversions._

  /** Function validating request for the query endpoint */
  override def validated[A](response: A => Route, invalidDocs: Documentation): Either[List[QueryValidationError], A] => Route = {
    case Left(errors) =>
      complete(StatusCodes.BadRequest -> s"Errors: \n${errors.mkString("\n")}")
    case Right(QueryResponseWithOutput(queryResponse, OutputType.csv)) =>
      complete(HttpEntity.Strict(ContentTypes.`text/csv(UTF-8)`, ByteString(queryResponse.convertTo[String])))
    case Right(success) =>
      response(success)
  }

  /** Function extracting option out of Right */
  protected def eitherOptionOps[A, B](x: Either[A, Option[B]]): Option[Either[A, B]] = x match {
    case Left(value) => Some(Left(value))
    case Right(Some(value)) => Some(Right(value))
    case Right(None) => None
  }

  override implicit def qsFunctor: Functor[QueryString] = new Functor[QueryString] {
    override def map[From, To](f: QueryString[From])(map: From => To): QueryString[To] = new QueryString[To](
      f.directive.map(map)
    )
  }

  override implicit def queryResponseSchemaWithOutputType: JsonSchema[QueryResponseWithOutput] =
    new JsonSchema[QueryResponseWithOutput] {
      override def encoder: Encoder[QueryResponseWithOutput] = (a: QueryResponseWithOutput) => a match {
        case queryResponse =>
          queryResponse.queryResponse.asJson(queryResponseSchema.encoder)
      }

      override def decoder: Decoder[QueryResponseWithOutput] = ???
    }

  /** Implementation of JSON encoder for Any */
  def anyEncoder: Encoder[Any] = (a: Any) => a match {
    case x: java.lang.String => Json.fromString(x)
    case x: java.lang.Integer => Json.fromInt(x)
    case x: java.sql.Timestamp => Json.fromLong(x.getTime)
    case x: java.lang.Boolean => Json.fromBoolean(x)
    case x: scala.collection.immutable.Vector[Any] => x.map(_.asJson(anyEncoder)).asJson //Due to type erasure, a recursive call is made here.
    case x: Tables.BlocksRow => x.asJson(blocksRowSchema.encoder)
    case x: Tables.AccountsRow => x.asJson(accountsRowSchema.encoder)
    case x: Tables.OperationGroupsRow => x.asJson(operationGroupsRowSchema.encoder)
    case x: Tables.OperationsRow => x.asJson(operationsRowSchema.encoder)
    case x: java.math.BigDecimal => Json.fromBigDecimal(x)
    case x => Json.fromString(x.toString)
  }

  /** JSON schema implementation for Any */
  override implicit def anySchema: JsonSchema[Any] = new JsonSchema[Any] {
    override def encoder: Encoder[Any] = anyEncoder

    override def decoder: Decoder[Any] =
      (c: HCursor) => {
        Right(c.value)
      }
  }

  /** Query response JSON schema implementation */
  override implicit def queryResponseSchema: JsonSchema[List[QueryResponse]] =
    new JsonSchema[List[QueryResponse]] {
      override def encoder: Encoder[List[QueryResponse]] = (a: List[QueryResponse]) =>
        a.map { myMap =>
          Json.obj(myMap.map(field => (field._1, field._2 match {
            case Some(y) => y.asJson(anyEncoder)
            case None => Json.Null
          })).toList: _*)
        }.asJson

      override def decoder: Decoder[List[QueryResponse]] = ???
    }

  /** Blocks by hash JSON schema implementation */
  override implicit def blocksByHashSchema: JsonSchema[AnyMap] = new JsonSchema[AnyMap] {
    override def encoder: Encoder[AnyMap] = (a: AnyMap) => Json.obj(a.map {
      case (k, v) => (k, v.asJson(anyEncoder))
    }.toList: _*)

    override def decoder: Decoder[AnyMap] = ???
  }

  /** Timestamp JSON schema implementation */
  override implicit def timestampSchema: JsonSchema[Timestamp] = new JsonSchema[Timestamp] {
    override def encoder: Encoder[Timestamp] = (a: Timestamp) => a.getTime.asJson

    override def decoder: Decoder[Timestamp] = ???
  }
}