import xerial.sbt.Sonatype.autoImport._

lazy val awsJavaSdkVersion = "1.11.184"

lazy val root = (project in file(".")).settings(
  organization := "com.github.seratch",
  name := "awscala",
  version := "0.6.1",
  scalaVersion := "2.12.3",
  crossScalaVersions := Seq("2.12.3", "2.11.8", "2.10.6"),
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
    "joda-time"        %  "joda-time"             % "2.9.9",
    "org.joda"         %  "joda-convert"          % "1.8.3",
    "com.github.seratch.com.veact" %% "scala-ssh" % "0.8.0-1" % "provided",
    "org.bouncycastle" %  "bcprov-jdk16"          % "1.46"             % "provided",
    "ch.qos.logback"   %  "logback-classic"       % "1.1.7"            % "test",
    "org.scalatest"    %% "scalatest"             % "3.0.1"            % "test"
  ),
  sbtPlugin := false,
  transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
  incOptions := incOptions.value.withNameHashing(true),
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  publishTo := _publishTo(version.value),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { x => false },
  pomExtra := <url>https://github.com/seratch/awscala</url>
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
    </developers>,
  organization := "com.github.seratch",
  sonatypeProfileName := "com.github.seratch"
)

def _publishTo(v: String) = {
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
