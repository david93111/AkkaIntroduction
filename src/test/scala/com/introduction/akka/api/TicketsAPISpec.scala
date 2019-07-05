package com.introduction.akka.api

import akka.cluster.Cluster
import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.testkit.TestDuration
import com.introduction.akka.BaseSpec
import com.introduction.akka.actor.TicketManagerActor.CreateTicket
import com.introduction.akka.message.MessageEnvelope

import scala.concurrent.duration._

class TicketsAPISpec extends BaseSpec with ScalatestRouteTest {

  val log = Logging(system.eventStream, "test_logger")

  implicit val timeout: RouteTestTimeout = RouteTestTimeout(5.seconds dilated)

  // boot test cluster for sharding to work locally
  val cluster: Cluster = Cluster(system)
  cluster.join(cluster.selfAddress)

  val api = new TicketsAPI(log)(system)

  api.ticketRegion ! MessageEnvelope("api_test_ticket", CreateTicket("api_test_ticket"))
  api.ticketRegion ! MessageEnvelope("cancelable_test_ticket", CreateTicket("cancelable_test_ticket"))

  "The Tickets API" should {

    "Return the current state for an already created ticket with GET" in {
      Get("/akka/ticket?ticketId=api_test_ticket") ~> api.route ~> check {
        response.status shouldEqual OK
        responseAs[String] shouldEqual "\"Created\""
      }
    }

    "Return a ticket creation for path /akka/ticket with POST method" in {
      Post("/akka/ticket") ~> api.route ~> check {
        response.status shouldEqual OK
        responseAs[String] should startWith("\"Ticket Created with ID:")
      }
    }

    "Return a ticket creation for path /akka/ticket/cancel with PUT method" in {
      Put("/akka/ticket/cancel?ticketId=cancelable_test_ticket") ~> api.route ~> check {
        response.status shouldEqual OK
        responseAs[String] should startWith("\"Cancel Event Sent to ticket:")
      }
    }

    "Return a ticket Started for path /akka/ticket/start with PUT method" in {
      Put("/akka/ticket/start?ticketId=cancelable_test_ticket") ~> api.route ~> check {
        response.status shouldEqual OK
        responseAs[String] should startWith("\"Start Event Sent to ticket:")
      }
    }

    "Return a ticket Resolved for path /akka/ticket/resolve with PUT method" in {
      Put("/akka/ticket/resolve?ticketId=cancelable_test_ticket") ~> api.route ~> check {
        response.status shouldEqual OK
        responseAs[String] should startWith("\"Resolve Event Sent to ticket:")
      }
    }

    "Return a state after changes for path /akka/ticket with GET method" in {
      Get("/akka/ticket?ticketId=cancelable_test_ticket") ~> api.route ~> check {
        response.status shouldEqual OK
        responseAs[String] shouldNot equal("\"Created\"")
      }
    }

    "Return the component version for path /akka/version with GET method" in {
      Get("/akka/version") ~> api.route ~> check {
        response.status shouldEqual OK
        responseAs[String] shouldEqual s""""v${BuildInfo.version}""""
      }
    }

    "Return the health check for path /akka/health_check with GET method" in {
      Get("/akka/health_check") ~> api.route ~> check {
        response.status shouldEqual OK
        responseAs[String] should startWith("\"Akka Intro - OK -")
      }
    }


  }


}
