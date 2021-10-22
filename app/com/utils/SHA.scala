package com.utils

//import org.apache.shiro.crypto.hash.Sha1Hash

object SHA {
//  def getSHA1(planText: String, saltValue: String): String = {
//    new Sha1Hash(planText + saltValue).toString
//  }

  def matches(plainText: String, saltValue: String, sha: String): Boolean = {
//    getSHA1(plainText, saltValue).equals(sha)
    true
  }
}
