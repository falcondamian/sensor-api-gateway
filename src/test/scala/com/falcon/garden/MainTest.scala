package com.falcon.garden

import com.falcon.garden.Main.SensorData
import io.circe.generic.auto._
import io.finch._
import io.finch.circe._
import org.scalatest.FunSuite

class MainTest extends FunSuite {

  val sensorData = SensorData("id", 0.0, 0.0, 0.0, 1L)

  test("healthcheck") {
    assert(Main.healthcheck(Input.get("/")).awaitValueUnsafe().contains("OK"))
  }

  test("collect") {
    assert(
      Main.collect(Input.post("/collect").withBody[Application.Json](sensorData)).awaitValueUnsafe().contains("OK"))
  }
}
