package com.falcon.garden

import cats.effect.{ExitCode, IO, IOApp, Resource}
import cats.implicits._
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.{Http, Service}
import com.typesafe.scalalogging.LazyLogging
import io.catbird.util.effect.futureToAsync
import io.circe.generic.auto._
import io.finch._
import io.finch.catsEffect._
import io.finch.circe._

object Main extends IOApp with LazyLogging {

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

  val port: Int = if (System.getProperty("http.port") != null) System.getProperty("http.port").toInt else 8081

  override def run(args: List[String]): IO[ExitCode] = {

    val serverResource = Resource.make {
      IO(Http.server.serve(s":$port", service))
    } { server =>
      futureToAsync[IO, Unit](server.close())
    }

    val server = serverResource.use { server =>
      logger.info(s"Application started at ${server.boundAddress}")
      IO.shift *> IO.never
    }

    for {
      _ <- server.handleErrorWith { err =>
        logger.error("Application failed to start", err)
        IO.raiseError(err)
      }
    } yield ExitCode.Success
  }
}
