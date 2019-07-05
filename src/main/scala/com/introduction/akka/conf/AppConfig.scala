package com.introduction.akka.conf

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration.Duration

object AppConfig {

  val conf: Config = ConfigFactory.load("application")
  val serverHost: String = conf.getString("http-server.host")
  val serverPort: Int = conf.getInt("http-server.port")
  implicit val defaultAskTimeout: Duration = Duration(conf.getString("defaults.ask-timeout"))
  val shards: Int = conf.getInt("defaults.shards")

}
