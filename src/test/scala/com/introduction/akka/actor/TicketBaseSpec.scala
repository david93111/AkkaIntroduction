package com.introduction.akka.actor

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import com.introduction.akka.BaseSpec
import com.introduction.akka.actor.TicketActor.{Cancel, Resolve, RetrieveState, Start}

class TicketBaseSpec extends TestKit(ActorSystem("TicketActorSystem")) with ImplicitSender with BaseSpec{

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val ticketActor: ActorRef = system.actorOf(TicketActor.props("test_ticket"))
  val ticketToCancel: ActorRef = system.actorOf(TicketActor.props("cancelable_ticket"))

  "a TicketActor" must {

    "Receive an start message and return no answer" in {
      ticketActor ! Start
      expectNoMessage
    }

    "Receive a Cancel message and return no answer" in {
      ticketActor ! Cancel
      expectNoMessage
    }

    "Receive an Resolve message and return no answer" in {
      ticketActor ! Resolve
      expectNoMessage
    }

    "Return Created state in case of RetrieveState Message for a new ticket" in {
      ticketToCancel ! RetrieveState
      expectMsg("Created")
    }

    "Return Last state in case of RetrieveState Message for an operated ticket" in {
      ticketActor ! RetrieveState
      expectMsg("Resolved")
    }

    "Execute RetrieveState after Cancel operation and state should be Cancelled" in {
      ticketToCancel ! Cancel
      expectNoMessage
      ticketToCancel ! RetrieveState
      expectMsg("Cancelled")
    }

  }

}
