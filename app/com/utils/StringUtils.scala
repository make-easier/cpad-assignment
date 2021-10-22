package com.utils

import scala.util.Random

object StringUtils {

  def getAlphaNumericString(length: Int): String = {
    val stream = Random.alphanumeric
    stream.take(length).mkString
  }

}
