package com.introduction.akka.actor

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{ImplicitSender, TestKit}
import com.introduction.akka.BaseSpec
import com.introduction.akka.actor.TicketActor.Start
import com.introduction.akka.actor.TicketManagerActor.{CreateTicket, GetTicket, SendTicketEvent}

class TicketManagerActorSpec extends TestKit(ActorSystem("TicketManagerActorSystem")) with ImplicitSender with BaseSpec{

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  val ticketManager: ActorRef = system.actorOf(TicketManagerActor.props, "TestTicketManager")

  "a TicketActor" must {

    "Receive a CreateTicket and create and actor with the ID included but send no answer" in {
      ticketManager ! CreateTicket("test_actor_ticket")
      expectNoMessage
    }

    "Receive a GetTicket message for a child actor, and forward the state operation" in {
      ticketManager ! GetTicket("test_actor_ticket")
      expectMsg("Created")
    }

    "Receive a GetTicket message for a non existing child actor, and return the error message" in {
      ticketManager ! GetTicket("non_existing_test_actor_ticket")
      expectMsg("Error, Ticket Actor with id: non_existing_test_actor_ticket not found")
    }

    "Receive a SendTicketEvent message for an existing actor, state must change in child ticket actor" in {
      ticketManager ! SendTicketEvent("test_actor_ticket", Start)
      expectNoMessage
      ticketManager ! GetTicket("test_actor_ticket")
      expectMsg("In Progress")
    }
  }

}
