package com.utils

import java.util.concurrent.Executor
import javax.inject.{Inject, Singleton}
import akka.actor.ActorSystem
import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import play.api.Configuration
import play.api.libs.concurrent.ActorSystemProvider

import java.sql.ResultSet
import scala.concurrent.{ExecutionContext, Future, Promise}

@Singleton
class ConcurrencyUtils @Inject()(configuration: Configuration, actorSystemProvider: ActorSystemProvider) {
  implicit val ec: ExecutionContext = actorSystemProvider.get.dispatchers.lookup("async-pool")
  implicit val ex: Executor = ec.asInstanceOf[Executor]
  val blockingPool: ExecutionContext = actorSystemProvider.get.dispatchers.lookup("blocking-pool")
  val actorSystem: ActorSystem = actorSystemProvider.get
}

object ConcurrencyUtils {

  implicit def toScalaFuture[T](listenableFuture: ListenableFuture[T])(implicit ec: ExecutionContext): Future[T] = {
    val promise = Promise[T]()
    Futures.addCallback(listenableFuture, new FutureCallback[T] {
      def onFailure(error: Throwable) = promise.failure(error)
      def onSuccess(result: T) = promise.success(result)
    }, ec.asInstanceOf[Executor])
    promise.future
  }

//  implicit def toScalaResultSet(rs: ResultSet): Seq
}