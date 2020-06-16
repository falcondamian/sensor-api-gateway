package com.falcon.garden

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.falcon.garden.db.{DataSource, SensorRepository}
import com.falcon.garden.web.HttpEndpoints
import com.typesafe.scalalogging.LazyLogging

object Main extends IOApp with LazyLogging {

  override def run(args: List[String]): IO[ExitCode] = {

    val resource = for {
      xa <- DataSource.transactor()
      repository = SensorRepository(xa)
      server <- HttpEndpoints.httpService[IO](repository)
    } yield server

    val server = resource.use { server =>
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
