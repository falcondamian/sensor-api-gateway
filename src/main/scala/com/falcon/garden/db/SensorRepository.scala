package com.falcon.garden.db

import cats.effect.IO
import com.falcon.garden.Main.SensorData
import doobie.util.transactor.Transactor
import doobie.implicits._

class SensorRepository(xa: Transactor[IO]) {

  def save(s: SensorData): IO[Unit] = {
    sql"""INSERT INTO sensor_data (sensor, airHumidity, airTemperature, soilHumidity, collectDateTime)
          VALUES(${s.sensor}, ${s.airHumidity}, ${s.airTemperature}, ${s.soilHumidity}, ${s.collectDateTime})
         """.update.run
      .transact(xa)
      .map(_ -> ())
  }

  def retrieveLatest(limit: Int): IO[List[SensorData]] = ???
}

object SensorRepository {

  def apply(xa: Transactor[IO]): SensorRepository =
    new SensorRepository(xa)
}
