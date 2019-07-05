package com.introduction.akka.boot

import akka.actor.ActorSystem
import akka.cluster.Cluster
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.introduction.akka.api.TicketsAPI
import com.introduction.akka.cluster.ClusterListener
import com.introduction.akka.conf.AppConfig.{serverHost, serverPort}

import scala.util.{Failure, Success}

object Startup {

  def main(args: Array[String]): Unit = {

    implicit val acSystem: ActorSystem = ActorSystem("tickets-ac-system")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    val log: LoggingAdapter = Logging(acSystem.eventStream, "akka_log")

    implicit val cluster: Cluster = Cluster(acSystem)

    // Initialize Cluster Listener
    acSystem.actorOf(ClusterListener.props(cluster), "ClusterListener")
    // example path: user/ClusterListener

    val api: TicketsAPI = new TicketsAPI(log)

    // Wait until minimum members are met to start the AKKA HTTP Server
    cluster.registerOnMemberUp {

      val bindingFuture = Http().bindAndHandle(api.route, serverHost , serverPort)
      bindingFuture.onComplete{
        case Success(serverBinding) =>
          log.info(s"Binding Successfully achieved, server started on ${serverBinding.localAddress}")
          acSystem.registerOnTermination(serverBinding.unbind())
        case Failure(exception) =>
          log.error(s"HTTP server binding Failed, starting actor system shutdown, cause: $exception")
          acSystem.terminate()
      }(acSystem.dispatcher)

    }

  }

}
