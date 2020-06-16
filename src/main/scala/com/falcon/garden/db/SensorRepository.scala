package com.falcon.garden.db

import java.time.LocalDateTime

import cats.effect.IO
import com.falcon.garden.domain.SensorData
import doobie.util.transactor.Transactor
import cats._, cats.data._, cats.implicits._
import doobie._, doobie.implicits._

class SensorRepository(xa: Transactor[IO]) {

  implicit val ldtPut: Put[LocalDateTime] = Put[LocalDateTime]
  implicit val ldtGet: Get[LocalDateTime] = Get[LocalDateTime]

  def save(s: SensorData): IO[Unit] = {
    sql"""INSERT INTO sensor_data (sensor, airHumidity, airTemperature, soilHumidity, collectionDateTime)
          VALUES(${s.sensor}, ${s.airHumidity}, ${s.airTemperature}, ${s.soilHumidity}, ${s.collectionDateTime})
         """.update.run
      .transact(xa)
      .map(_ -> ())
  }

  def retrieveLatest(limit: Int): IO[List[SensorData]] =
    sql"""select * from sensor_data limit $limit"""
      .query[SensorData]
      .to[List]
      .transact(xa)
}

object SensorRepository {

  def apply(xa: Transactor[IO]): SensorRepository =
    new SensorRepository(xa)
}
