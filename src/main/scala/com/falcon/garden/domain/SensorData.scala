package com.falcon.garden.domain

case class SensorData(sensor:             String,
                      airHumidity:        Double,
                      airTemperature:     Double,
                      soilHumidity:       Double,
                      collectionDateTime: Long)
