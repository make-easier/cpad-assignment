//package com.utils
//
//import javax.inject.{Inject, Singleton}
//import com.datastax.driver.core.Cluster
//import com.datastax.driver.core.policies.ConstantReconnectionPolicy
//import com.intellivision.auth.Auth
//import com.intellivision.auth.model.TokenManager
//import play.api.inject.ApplicationLifecycle
//import play.api.{Configuration, Logger}
//
//import scala.collection.JavaConverters._
//import scala.concurrent.Future
//
//@Singleton
//class CassandraClient @Inject()(configuration: Configuration, lifecycle: ApplicationLifecycle, concurrencyUtils: ConcurrencyUtils) {
//
//  private val nodes = configuration.getStringList("CASSANDRA_ENDPOINT").map(_.asScala.toList).getOrElse(List("127.0.0.1"))
//
//  import com.datastax.driver.core.ProtocolVersion._
//
//  private val nativeProtocolVersion = configuration.getString("CASSANDRA_NATIVE_PROTOCOL_VERSION").map(_.trim.toUpperCase).getOrElse("V4") match {
//    case "V1" => V1
//    case "V2" => V2
//    case "V3" => V3
//    case "V4" => V4
//    case "V5" => V5
//    case _ =>
//      Logger.warn(s"native protocol version for cassandra driver is mis-configured, using V4")
//      V4
//  }
//
//  Logger.info(s"using native protocol $nativeProtocolVersion to connect to cassandra")
//
//  private val reconnectionPolicy = new ConstantReconnectionPolicy(1000L)
//
//  private val cluster = Cluster.builder
//    .addContactPoints(nodes: _*)
//    .withProtocolVersion(nativeProtocolVersion)
//    .withReconnectionPolicy(reconnectionPolicy)
//    .build
//
//  val session = cluster.connect()
//
//  val blockedUrls: Map[String, List[String]] = configuration.getConfig("auth.blocked") match {
//    case Some(c) =>
//      c.keys.map {
//        k =>
//          Logger.info(s"Blocking enabled for Partner [$k]")
//          k -> c.getStringList(k).get.asScala.toList
//      }.toMap
//    case None =>
//      Map.empty
//  }
//
//  Auth.initialize(session, concurrencyUtils.ec, concurrencyUtils.ex, blockedUrls)
//  TokenManager.init(session, concurrencyUtils.ec, concurrencyUtils.ex)
//
//  lifecycle.addStopHook { () =>
//    Future.successful {
//      Logger.info(s"Closing cassandra connection")
//      session.close()
//      cluster.close()
//      Logger.info(s"Closed cassandra connection")
//    }
//  }
//
//}