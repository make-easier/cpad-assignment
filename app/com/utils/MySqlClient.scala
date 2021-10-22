package com.utils

import com.models.Person
import play.api.Configuration

import java.sql.{Connection, DriverManager}
import javax.inject.{Inject, Singleton}
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import com.utils.Java2Scala.asScala

@Singleton
class MySqlClient @Inject()(configuration: Configuration) {
  private val baseUrl = configuration.getOptional[String]("mysql.config.url").getOrElse("jdbc:mysql://localhost:3306/test?serverTimezone=UTC")
  private val userName = configuration.get[String]("mysql.config.username")
  private val password = configuration.get[String]("mysql.config.password")
  private val dbName = configuration.get[String]("mysql.config.database_name")
  private val connectionUrl = s"$baseUrl$dbName"

  println(s"DB Connection URL: $connectionUrl")
  val conn: Connection = DriverManager.getConnection(connectionUrl, userName, password)

//  val query = conn.prepareStatement("select * from person")
//  val rs = query.executeQuery()
/*
  while ( {
    rs.next
  }) {
    val id = rs.getLong("ID")
    val name = rs.getString("FIRST_NAME")
    val lastName = rs.getString("LAST_NAME")
    // do something with the extracted data...
    println (s"$id, $name, $lastName")
  }
*/
}
