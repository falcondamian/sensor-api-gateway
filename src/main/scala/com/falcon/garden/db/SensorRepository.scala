package com.falcon.garden.db

import cats.effect.IO
import com.falcon.garden.domain.SensorData
import doobie.implicits._
import doobie.implicits.javatime._
import doobie.util.transactor.Transactor

class SensorRepository(xa: Transactor[IO]) {

  def save(s: SensorData): IO[Unit] =
    sql"""INSERT INTO sensor_data (sensor, airHumidity, airTemperature, soilHumidity, collectionDateTime)
          VALUES(${s.sensor}, ${s.airHumidity}, ${s.airTemperature}, ${s.soilHumidity}, ${s.collectionDateTime})
         """.update.run
      .transact(xa)
      .map(_ -> ())

  def retrieveLatest(limit: Int): IO[List[SensorData]] =
    sql"""select * from sensor_data order by collectionDateTime desc limit $limit"""
      .query[SensorData]
      .to[List]
      .map(_.reverse)
      .transact(xa)
}

object SensorRepository {

  def apply(xa: Transactor[IO]): SensorRepository =
    new SensorRepository(xa)
}
