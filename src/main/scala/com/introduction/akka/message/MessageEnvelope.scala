package com.introduction.akka.message

case class MessageEnvelope(ticketId: String, message: Message)
