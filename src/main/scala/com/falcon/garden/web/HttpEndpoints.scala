package com.falcon.garden.web

import java.time.LocalDateTime.now

import cats.effect.{ContextShift, Effect, IO, Resource}
import com.falcon.garden.db.SensorRepository
import com.falcon.garden.domain.SensorData
import com.twitter.finagle.{Http, ListeningServer}
import com.typesafe.scalalogging.LazyLogging
import io.catbird.util.effect.futureToAsync
import io.circe.generic.auto._
import io.finch._
import io.finch.catsEffect._
import io.finch.circe._

object HttpEndpoints extends LazyLogging {

  case class SensorDataRequest(sensor: String, airHumidity: Double, airTemperature: Double, soilHumidity: Double)

  val port: Int = if (System.getProperty("http.port") != null) System.getProperty("http.port").toInt else 8081

  def httpService[F[_]: Effect: ContextShift](sensorRepository: SensorRepository): Resource[IO, ListeningServer] = {

    def healthcheck: Endpoint[IO, String] = get(pathEmpty) {
      Ok("OK")
    }

    def collect: Endpoint[IO, Unit] = post("collect" :: jsonBody[SensorDataRequest]) { data: SensorDataRequest =>
      logger.info("Sensor data received " + data)
      val sd =
        SensorData(data.sensor, data.airHumidity, data.airTemperature, data.soilHumidity, collectionDateTime = now())
      sensorRepository.save(sd).map(Ok)
    }

    def retrieveLatest: Endpoint[IO, List[SensorData]] = get("retrieveLatest") {
      logger.info("Retrieving sensor data")
      sensorRepository.retrieveLatest(50).map(Ok)
    }

    val service = Bootstrap
      .serve[Application.Json](healthcheck :+: collect :+: retrieveLatest)
      .toService

    Resource.make {
      IO(Http.server.serve(s":$port", service))
    } { server =>
      futureToAsync[IO, Unit](server.close())
    }
  }
}
