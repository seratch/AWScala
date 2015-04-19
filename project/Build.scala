import sbt._
import Keys._

object AwscalaProject extends Build {

  lazy val awsJavaSdkVersion = "1.9.31"

  lazy val root = Project("root", file("."), settings = mainSettings)

  lazy val mainSettings = Seq(
    organization := "com.github.seratch",
    name := "awscala",
    version := "0.5.1",
    scalaVersion := "2.11.6",
    crossScalaVersions := Seq("2.11.6", "2.10.5"),
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
      else Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishMavenStyle := true,
    resolvers += "spray repo" at "http://repo.spray.io",
    libraryDependencies ++= Seq(
      "com.amazonaws"    %  "aws-java-sdk-iam"      % awsJavaSdkVersion,
      "com.amazonaws"    %  "aws-java-sdk-sts"      % awsJavaSdkVersion,
      "com.amazonaws"    %  "aws-java-sdk-ec2"      % awsJavaSdkVersion,
      "com.amazonaws"    %  "aws-java-sdk-s3"       % awsJavaSdkVersion,
      "com.amazonaws"    %  "aws-java-sdk-sqs"      % awsJavaSdkVersion,
      "com.amazonaws"    %  "aws-java-sdk-emr"      % awsJavaSdkVersion,
      "com.amazonaws"    %  "aws-java-sdk-redshift" % awsJavaSdkVersion,
      "com.amazonaws"    %  "aws-java-sdk-dynamodb" % awsJavaSdkVersion,
      "com.amazonaws"    %  "aws-java-sdk-simpledb" % awsJavaSdkVersion,
      "joda-time"        %  "joda-time"       % "2.7",
      "org.joda"         %  "joda-convert"    % "1.7",
      "com.decodified"   %% "scala-ssh"       % "0.7.0"  % "provided",
      "org.bouncycastle" %  "bcprov-jdk16"    % "1.46"   % "provided",
      "ch.qos.logback"   %  "logback-classic" % "1.1.3"  % "test",
      "org.scalatest"    %% "scalatest"       % "2.2.4"  % "test"
    ),
    sbtPlugin := false,
    transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
    incOptions := incOptions.value.withNameHashing(true),
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
          <name>Kazuhiro Sera</name>
          <url>http://seratch.net/</url>
        </developer>
        <developer>
          <id>mslinn</id>
          <name>Mike Slinn</name>
          <url>https://github.com/mslinn</url>
        </developer>
        <developer>
          <id>Rheeseyb</id>
          <name>RheeseyB</name>
          <url>https://github.com/Rheeseyb</url>
        </developer>
        <developer>
          <id>gribeiro</id>
          <name>Georges Kovacs Ribeiro</name>
          <url>https://github.com/gribeiro</url>
        </developer>
      </developers>
  )

}

