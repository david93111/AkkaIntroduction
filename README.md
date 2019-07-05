# AKKA Introduction

This project is simple and designed for the Scala course, it features core AKKA concepts 
as Actors, Clustering and Sharding with Serialization. In summary all the general tools to
develop and deploy a "Production Ready" Stateful system with High Concurrency capabilities using AKKA

for a quick understanding of the actor model, I recommend the following article [https://www.brianstorti.com/the-actor-model/]

## Presentation of the course

Link to the presentation: [https://slides.com/davidandresvasquezmarin/akka-introduction]

## Postman Collection for API
Link to import collection: [https://www.getpostman.com/collections/5db048ad3b5b2c54a0db]

## AKKA Modules included

* AKKA Actors
* AKKA Clustering
* AKKA Sharding
* AKKA HTTP (built on top of AKKA Streams)

Akka Typed was not used, since is still under heavy API changes for 2.6, and while 
adds type safety, implies more complexity to understand Behaviors and AbstractBehaviors.
Also for simplicity, serialization is done using Kryo.

The AKKA version used is 2.5.23 and AKKA HTTP 10.1.8

## How to boot the cluster
SBT is a must for running this project, is the only pre requisite for using it.

Inside the SBT build file, there are 3 commands alias, 2 for launching seeds, seed1 and seed2
and another third command for launching generic nodes, using dynamic host ports.

In the application.conf is specified the cluster minimum membership size and role distribution,
by default are expected: 
* 2 seed nodes
* 1 node

This can be changed inside the application.conf to met the desired requirements,
in the Startup main class, is present the registerOnMemberUp condition to boot the HTTP Server and thus also
the API that contains the ShardRegion Actor.

To boot the cluster the next steps in the console are needed:
* go to the root folder of the project on as many console session as nodes you want to boot (remember the minimum required
* then execute
  ```bash
  sbt
  [command_alias_desired]
  ```

#### example:

*First Terminal:*
   ```bash
   sbt
   seed1
   ```
*Second Terminal:*
   ```bash
   sbt
   seed2
   ```
*Third Terminal:*
   ```bash
   sbt
   node
   ```
*Fourth Terminal:*
   ```bash
   sbt
   node
   ```

This should boot up 4 nodes, two seeds, and to basic nodes 
(The only difference is that nodes are not contact points, without seeds, nodes can not establish a connection)