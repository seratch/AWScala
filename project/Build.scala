import sbt._
import Keys._

object AwscalaProject extends Build {

  lazy val root = Project("root", file("."), settings = mainSettings)

  lazy val mainSettings: Seq[Project.Setting[_]] = Defaults.defaultSettings ++ Seq(
    organization := "com.github.seratch",
    name := "awscala",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.10.3",
    crossScalaVersions := Seq("2.10.0"),
    publishTo <<= version { (v: String) => 
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishMavenStyle := true,
    resolvers += "spray repo" at "http://repo.spray.io",
    libraryDependencies ++= Seq(
      "com.amazonaws"  %  "aws-java-sdk"    % "[1.6,)",
      "joda-time"      %  "joda-time"       % "2.3",
      "org.joda"       %  "joda-convert"    % "1.2",
      "com.decodified" %% "scala-ssh" % "0.6.4" % "provided",
      "org.bouncycastle" % "bcprov-jdk16" % "1.46" % "provided",
      "ch.qos.logback" %  "logback-classic" % "1.0.13"  % "test",
      "org.scalatest"  %% "scalatest"       % "1.9.1"   % "test"
    ),
    sbtPlugin := false,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { x => false },
    pomExtra := <url>http://seratch.github.com/awscala</url>
      <licenses>
        <license>
          <name>Apache License, Version 2.0</name>
          <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:seratch/awscala.git</url>
        <connection>scm:git:git@github.com:seratch/awscala.git</connection>
      </scm>
      <developers>
        <developer>
          <id>seratch</id>
          <name>Kazuhuiro Sera</name>
          <url>http://seratch.net/</url>
        </developer>
      </developers>
  )

}

