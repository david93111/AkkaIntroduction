package com.introduction.akka.actor

import akka.actor.{Actor, Props}
import com.introduction.akka.actor.TicketActor.{Cancel, Resolve, RetrieveState, Start}
import com.introduction.akka.message.Message

class TicketActor(id: String) extends Actor{

  var state = "Created"

  override def receive: Receive = {
    case Cancel =>
      updateState("Cancelled")
    case Start =>
      updateState("In Progress")
    case Resolve =>
      updateState("Resolved")
    case RetrieveState =>
      sender() ! state
  }

  def updateState(newState: String): Unit = {
    state = newState
  }
}

object TicketActor{

  def props(id: String): Props = Props(new TicketActor(id))

  trait TicketMessage extends Message

  case object Cancel extends TicketMessage
  case object Start extends TicketMessage
  case object Resolve extends TicketMessage
  case object RetrieveState extends TicketMessage

}
