package com.introduction.akka.api

import java.util.UUID.randomUUID

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.DateTime
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import com.introduction.akka.actor.TicketActor.{Cancel, Resolve, Start}
import com.introduction.akka.actor.TicketManagerActor
import com.introduction.akka.actor.TicketManagerActor.{CreateTicket, GetTicket, SendTicketEvent}
import com.introduction.akka.conf.AppConfig.defaultAskTimeout
import com.introduction.akka.message.MessageEnvelope
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class TicketsAPI(log: LoggingAdapter)(implicit actorSystem: ActorSystem) {

  val clusterSharding: ClusterSharding = ClusterSharding(actorSystem)

  implicit val askTimeout: Timeout = Timeout(FiniteDuration(defaultAskTimeout.length, defaultAskTimeout.unit))

  // Start a Shard Region inside each node of the cluster
  // Sharding can be booted up only on certain role nodes using the sharding configuration
  val ticketRegion : ActorRef = clusterSharding.start(
    typeName = "TicketManager",
    entityProps = TicketManagerActor.props,
    settings = ClusterShardingSettings(actorSystem),
    extractEntityId = TicketManagerActor.extractEntityId,
    extractShardId = TicketManagerActor.extractShardId
  )

  val route: Route = {
    pathPrefix("akka") {
      pathPrefix("ticket") {
        pathEndOrSingleSlash{
          post {
            val id = randomUUID().toString
            ticketRegion ! MessageEnvelope(id, CreateTicket(id))
            complete(OK -> s"Ticket Created with ID: $id")
          } ~ get {
            parameter("ticketId".as[String]) { id => {
              val status: Future[String] = (ticketRegion ? MessageEnvelope(id, GetTicket(id))).mapTo[String]
                onSuccess(status) { ticketStatus => {
                    complete(OK, ticketStatus)
                  }
                }
              }
            }
          }
        } ~ put {
          parameter("ticketId".as[String]){ id =>{
              path("cancel"){
                ticketRegion ! MessageEnvelope(id, SendTicketEvent(id, Cancel))
                complete(OK -> s"Cancel Event Sent to ticket: $id")
              } ~ path("resolve"){
                ticketRegion ! MessageEnvelope(id, SendTicketEvent(id, Resolve))
                complete(OK -> s"Resolve Event Sent to ticket: $id")
              } ~ path("start") {
                ticketRegion ! MessageEnvelope(id, SendTicketEvent(id, Start))
                complete(OK -> s"Start Event Sent to ticket: $id")
              }
            }
          }
        }
      } ~ version() ~ healthCheck()
    }
  }


  /** BuildInfo object generated with sbt plugin to access current sbt version without tricks */
  def version(): Route = {
    (path("version") & get){
      complete(OK -> s"v${BuildInfo.version}")
    }
  }

  def healthCheck(): Route = {
    (path("health_check") & get){
      complete(OK -> s"Akka Intro - OK - ${DateTime.now.toString()}")
    }
  }

}
