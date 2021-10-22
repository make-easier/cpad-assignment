package com.utils

import javax.inject.Singleton

import scala.util.matching.Regex

@Singleton
class RegexUtils {
  def matches(url: String, pattern: Regex): Boolean = {
    pattern.findFirstMatchIn(url).isDefined
  }
}