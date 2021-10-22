package com.utils

import play.api.Configuration

object ConfigUtils {

  def toMap(config: Configuration): Map[String, String] = {
    config.entrySet.map { case (key, value) =>
      key -> value.unwrapped.toString
    }.toMap
  }

}
