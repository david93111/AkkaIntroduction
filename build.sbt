name := "AkkaIntroduction"

version := "0.0.1"

scalaVersion := "2.12.8"

val akkaCoreVersion = "2.5.23"

lazy val root = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.introduction.akka.api"
  )

libraryDependencies ++= Seq(
  "org.scalatest"          %%  "scalatest"                  % "3.0.8"         % Test,
  "com.typesafe.akka"      %%  "akka-http-testkit"          % "10.1.8"        % Test,
  "com.typesafe.akka"      %%  "akka-testkit"               % akkaCoreVersion % Test,
  "com.typesafe.akka"      %%  "akka-stream-testkit"        % akkaCoreVersion % Test,
  "com.typesafe.akka"      %%  "akka-actor"                 % akkaCoreVersion,
  "com.typesafe.akka"      %%  "akka-cluster"               % akkaCoreVersion,
  "com.typesafe.akka"      %%  "akka-cluster-sharding"      % akkaCoreVersion,
  "com.typesafe.akka"      %%  "akka-stream"                % akkaCoreVersion,
  "com.typesafe.akka"      %%  "akka-slf4j"                 % akkaCoreVersion,
  "com.typesafe.akka"      %%  "akka-http"                  % "10.1.8",
  "com.github.romix.akka"  %%  "akka-kryo-serialization"    % "0.5.2",
  "com.typesafe"           %   "config"                     % "1.3.4",
  "de.heikoseeberger"      %%  "akka-http-circe"            % "1.27.0"
)

mainClass in Compile := Some("com.introduction.akka.boot.Startup")

fork in run := true

// Command aliases for SBT revolver to launch seeds and nodes in the cluster overwriting application.conf directly
addCommandAlias("seed1", "reStart --- -Dhttp-server.port=8189 -DAKKA_TCP_PORT=2551 -Dakka.cluster.roles.0=seed")
addCommandAlias("seed2", "reStart --- -Dhttp-server.port=8191 -DAKKA_TCP_PORT=2552 -Dakka.cluster.roles.0=seed")
addCommandAlias("node", "reStart ---  -Dhttp-server.port=0 -DAKKA_TCP_PORT=0 -Dakka.cluster.roles.0=node")

coverageMinimum := 80
coverageFailOnMinimum := true

coverageExcludedPackages := "<empty>;Reverse.*;.*Startup.*"