package com.falcon.garden.domain

import java.time.LocalDateTime

case class SensorData(sensor:             String,
                      airHumidity:        Double,
                      airTemperature:     Double,
                      soilHumidity:       Double,
                      collectionDateTime: LocalDateTime)
