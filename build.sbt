name := """cometd-akka-stream"""

version := "0.0.8"

organization := "com.adelegue"

scalaVersion := "2.12.2"

resolvers += "Local Maven Repository" at "file://D:/DevJava/Params/Maven/DepotLocal"


//crossScalaVersions := Seq("2.11.8", scalaVersion.value)

val akkaVersion = "2.5.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"     % akkaVersion,
  "com.typesafe.akka" %% "akka-stream"    % akkaVersion,
  "org.cometd.java" % "cometd-java-client" % "3.1.3",
  "org.cometd.java" % "cometd-java-server" % "3.1.3" % Test,
  "org.eclipse.jetty" % "jetty-server" % "9.4.6.v20170531" % Test,
  "org.eclipse.jetty" % "jetty-servlet" % "9.4.6.v20170531" % Test,
  "com.typesafe.akka" %% "akka-testkit"   % akkaVersion       % Test,
  "org.scalatest"     %% "scalatest"      % "3.0.1"           % Test,
  "org.slf4j" % "slf4j-api" % "1.7.25" % Test
//  "org.apache.logging.log4j" % "log4j-api" % "2.8.2" % Test,
//  "org.apache.logging.log4j" % "log4j-core" % "2.8.2" % Test
)

parallelExecution in Test := false

scalacOptions in Test ++= Seq("-Yrangepos")

publishTo := {
  val localPublishRepo = "./repository"
  if (isSnapshot.value) {
    Some(Resolver.file("snapshots", new File(localPublishRepo + "/snapshots")))
  } else {
    Some(Resolver.file("releases", new File(localPublishRepo + "/releases")))
  }
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <developers>
      <developer>
        <id>alexandre.delegue</id>
        <name>Alexandre Del�gue</name>
        <url>https://github.com/larousso</url>
      </developer>
    </developers>
  )