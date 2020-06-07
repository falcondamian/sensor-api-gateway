package com.falcon.garden

import cats.effect.IO
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import com.typesafe.scalalogging.LazyLogging
import io.circe.generic.auto._
import io.finch._
import io.finch.catsEffect._
import io.finch.circe._

object Main extends App with LazyLogging {

  case class SensorData(id: String, airHumidity: Double, airTemperature: Double, soilHumidity: Double, timestamp: Long)

  def healthcheck: Endpoint[IO, String] = get(pathEmpty) {
    Ok("OK")
  }

  def collect: Endpoint[IO, String] = post("collect" :: jsonBody[SensorData]) { data: SensorData =>
    logger.info("Sensor data received " + data)
    Ok("OK")
  }

  def service: Service[Request, Response] =
    Bootstrap
      .serve[Text.Plain](healthcheck)
      .serve[Application.Json](collect)
      .toService

  val port = if (System.getProperty("http.port") != null) System.getProperty("http.port").toInt else 8081

  Await.ready(Http.server.serve(s":$port", service))
}
