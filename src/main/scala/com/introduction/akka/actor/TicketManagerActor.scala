package com.introduction.akka.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import com.introduction.akka.actor.TicketActor.{RetrieveState, TicketMessage}
import com.introduction.akka.actor.TicketManagerActor.{CreateTicket, GetTicket, SendTicketEvent}
import com.introduction.akka.conf.AppConfig.{shards => numberOfShards}
import com.introduction.akka.message.{Message, MessageEnvelope}

class TicketManagerActor extends Actor with ActorLogging{

  override def receive: Receive = {
    case CreateTicket(id) =>
      log.info(s"Creating Child actor with id: $id")
      val actor = context.actorOf(TicketActor.props(id), id)
      context.watch(actor)
    case GetTicket(id) =>
      log.info(s"Looking for actor with id: $id")
      context.child(id).fold(
        sender() ! s"Error, Ticket Actor with id: $id not found"
      ){ actorRef =>
        actorRef forward RetrieveState
      }
    case SendTicketEvent(id, message) =>
      log.info(s"Looking for actor with id: $id")
      context.child(id).foreach( ref => ref ! message)
  }

}

object TicketManagerActor {

  def props: Props = Props(new TicketManagerActor)

  trait TicketManagerMessage extends Message

  case class CreateTicket(id: String) extends TicketManagerMessage
  case class GetTicket(id: String) extends TicketManagerMessage
  case class SendTicketEvent(id: String, message: TicketMessage) extends TicketManagerMessage

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case MessageEnvelope(id, message) => (id.toString, message)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case MessageEnvelope(id, _)       => (id.hashCode % numberOfShards).toString
    case ShardRegion.StartEntity(id) => (id.hashCode % numberOfShards).toString
  }

}
