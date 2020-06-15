package com.falcon.garden.web

import cats.effect.{ContextShift, Effect, IO, Resource}
import com.falcon.garden.Main.SensorData
import com.falcon.garden.db.SensorRepository
import com.twitter.finagle.{Http, ListeningServer}
import com.typesafe.scalalogging.LazyLogging
import io.catbird.util.effect.futureToAsync
import io.circe.generic.auto._
import io.finch._
import io.finch.catsEffect._
import io.finch.circe._

object HttpEndpoints extends LazyLogging {

  val port: Int = if (System.getProperty("http.port") != null) System.getProperty("http.port").toInt else 8081

  def httpService[F[_]: Effect: ContextShift](sensorRepository: SensorRepository): Resource[IO, ListeningServer] = {

    def healthcheck: Endpoint[IO, String] = get(pathEmpty) {
      Ok("OK")
    }

    def collect: Endpoint[IO, String] = post("collect" :: jsonBody[SensorData]) { data: SensorData =>
      logger.info("Sensor data received " + data)
      sensorRepository.save(data)
      Ok("OK")
    }

    def retrieveLatest: Endpoint[IO, String] = get("retrieveLatest") {
      logger.info("Retrieving sensor data")
      sensorRepository.retrieveLatest(100)
      Ok("OK")
    }

    val service = Bootstrap
      .serve[Text.Plain](healthcheck)
      .serve[Application.Json](collect :: retrieveLatest)
      .toService

    Resource.make {
      IO(Http.server.serve(s":$port", service))
    } { server =>
      futureToAsync[IO, Unit](server.close())
    }
  }
}
