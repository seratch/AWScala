import sbt._
import Keys._

object AwscalaProject extends Build {

  lazy val awsJavaSdkVersion = "1.10.77"

  lazy val root = Project("root", file(".")).
    aggregate(awscalaCore,
      awscalaS3,
      awscalaDynamoDBv2,
      awscalaSQS,
      awscalaEC2,
      awscalaEMR,
      awscalaIAM,
      awscalaRedshift,
      awscalaSimpleDB,
      awscalaSTS)

  lazy val commonSettings = Seq(
    organization := "com.github.seratch",
    version := "0.6.0-SNAPSHOT",
    scalaVersion := "2.11.8",
    crossScalaVersions := Seq("2.11.8", "2.10.6"),
    publishMavenStyle := true,
    resolvers += "spray repo" at "http://repo.spray.io",
    sbtPlugin := false,
    transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
    incOptions := incOptions.value.withNameHashing(true),
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { x => false }
  )

  lazy val awscalaCore = Project("awscalaCore", file("awscala/core"), settings = awscalaCoreSettings)

  lazy val awscalaCoreSettings = commonSettings ++ Seq(
    name := "awscala-core",
    libraryDependencies ++= Seq(
      "com.amazonaws"    %  "aws-java-sdk-iam"      % awsJavaSdkVersion,
      "joda-time"        %  "joda-time"       % "2.9.4",
      "org.joda"         %  "joda-convert"    % "1.8.1",
      "com.decodified"   %% "scala-ssh"       % "0.7.0"  % "provided",
      "org.bouncycastle" %  "bcprov-jdk16"    % "1.46"   % "provided",
      "ch.qos.logback"   %  "logback-classic" % "1.1.7"  % "test",
      "org.scalatest"    %% "scalatest"       % "2.2.6"  % "test"
    )
  )

  lazy val awscalaS3 = Project("awscalaS3", file("awscala/s3"), settings = awscalaS3Settings)
    .dependsOn(awscalaCore)

  lazy val awscalaS3Settings = commonSettings ++ Seq(
    name := "awscala-s3",
    libraryDependencies ++= Seq(
      "com.amazonaws"    %  "aws-java-sdk-s3"       % awsJavaSdkVersion,
      "ch.qos.logback"   %  "logback-classic" % "1.1.7"  % "test",
      "org.scalatest"    %% "scalatest"       % "2.2.6"  % "test"
    )
  )

  lazy val awscalaDynamoDBv2 = Project("awscalaDynamoDBv2", file("awscala/dynamodbv2"), settings = awscalaDynamoDBv2Settings)
    .dependsOn(awscalaCore)

  lazy val awscalaDynamoDBv2Settings = commonSettings ++ Seq(
    name := "awscala-dynamodbv2",
    libraryDependencies ++= Seq(
      "com.amazonaws"    %  "aws-java-sdk-dynamodb" % awsJavaSdkVersion,
      "ch.qos.logback"   %  "logback-classic" % "1.1.7"  % "test",
      "org.scalatest"    %% "scalatest"       % "2.2.6"  % "test"
    )
  )

  lazy val awscalaSQS = Project("awscalaSQS", file("awscala/sqs"), settings = awscalaSQSSettings)
    .dependsOn(awscalaCore)

  lazy val awscalaSQSSettings = commonSettings ++ Seq(
    name := "awscala-sqs",
    libraryDependencies ++= Seq(
      "com.amazonaws"    %  "aws-java-sdk-sqs"      % awsJavaSdkVersion,
      "ch.qos.logback"   %  "logback-classic" % "1.1.7"  % "test",
      "org.scalatest"    %% "scalatest"       % "2.2.6"  % "test"
    )
  )

  lazy val awscalaEC2 = Project("awscalaEC2", file("awscala/ec2"), settings = awscalaEC2Settings)
    .dependsOn(awscalaCore)

  lazy val awscalaEC2Settings = commonSettings ++ Seq(
    name := "awscala-ec2",
    libraryDependencies ++= Seq(
      "com.amazonaws"    %  "aws-java-sdk-ec2"      % awsJavaSdkVersion,
      "com.decodified"   %% "scala-ssh"       % "0.7.0"  % "provided",
      "ch.qos.logback"   %  "logback-classic" % "1.1.7"  % "test",
      "org.scalatest"    %% "scalatest"       % "2.2.6"  % "test"
    )
  )

  lazy val awscalaEMR = Project("awscalaEMR", file("awscala/emr"), settings = awscalaEMRSettings)
    .dependsOn(awscalaCore)

  lazy val awscalaEMRSettings = commonSettings ++ Seq(
    name := "awscala-emr",
    libraryDependencies ++= Seq(
      "com.amazonaws"    %  "aws-java-sdk-emr"      % awsJavaSdkVersion,
      "ch.qos.logback"   %  "logback-classic" % "1.1.7"  % "test",
      "org.scalatest"    %% "scalatest"       % "2.2.6"  % "test"
    )
  )

  lazy val awscalaIAM = Project("awscalaIAM", file("awscala/iam"), settings = awscalaIAMSettings)
    .dependsOn(awscalaCore)

  lazy val awscalaIAMSettings = commonSettings ++ Seq(
    name := "awscala-iam",
    libraryDependencies ++= Seq(
      "com.amazonaws"    %  "aws-java-sdk-iam"      % awsJavaSdkVersion,
      "ch.qos.logback"   %  "logback-classic" % "1.1.7"  % "test",
      "org.scalatest"    %% "scalatest"       % "2.2.6"  % "test"
    )
  )

  lazy val awscalaRedshift = Project("awscalaRedshift", file("awscala/redshift"), settings = awscalaRedshiftSettings)
    .dependsOn(awscalaCore)

  lazy val awscalaRedshiftSettings = commonSettings ++ Seq(
    name := "awscala-redshift",
    libraryDependencies ++= Seq(
      "com.amazonaws"    %  "aws-java-sdk-redshift"      % awsJavaSdkVersion,
      "ch.qos.logback"   %  "logback-classic" % "1.1.7"  % "test",
      "org.scalatest"    %% "scalatest"       % "2.2.6"  % "test"
    )
  )

  lazy val awscalaSimpleDB = Project("awscalaSimpleDB", file("awscala/simpledb"), settings = awscalaSimpleDBSettings)
    .dependsOn(awscalaCore)

  lazy val awscalaSimpleDBSettings = commonSettings ++ Seq(
    name := "awscala-simpledb",
    libraryDependencies ++= Seq(
      "com.amazonaws"    %  "aws-java-sdk-simpledb"      % awsJavaSdkVersion,
      "ch.qos.logback"   %  "logback-classic" % "1.1.7"  % "test",
      "org.scalatest"    %% "scalatest"       % "2.2.6"  % "test"
    )
  )

  lazy val awscalaSTS = Project("awscalaSTS", file("awscala/sts"), settings = awscalaSTSSettings)
    .dependsOn(awscalaCore)

  lazy val awscalaSTSSettings = commonSettings ++ Seq(
    name := "awscala-sts",
    libraryDependencies ++= Seq(
      "com.amazonaws"    %  "aws-java-sdk-sts"      % awsJavaSdkVersion,
      "ch.qos.logback"   %  "logback-classic" % "1.1.7"  % "test",
      "org.scalatest"    %% "scalatest"       % "2.2.6"  % "test"
    )
  )

}

