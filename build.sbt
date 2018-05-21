import xerial.sbt.Sonatype.autoImport._

lazy val commonSettings = Seq(
  organization := "com.github.seratch",
  name := "awscala",
  version := "0.6.4-SNAPSHOT",
  scalaVersion := "2.12.6",
  crossScalaVersions := Seq("2.12.6", "2.11.12", "2.10.7"),
  resolvers += "spray repo" at "http://repo.spray.io",
  sbtPlugin := false,
  transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  publishTo := _publishTo(version.value),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { x =>
    false
  },
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

lazy val awsJavaSdkVersion = "1.11.285"

lazy val all = (project in file("."))
  .settings(commonSettings)
  .settings(
    moduleName := "awscala"
  )
  .aggregate(core, dynamodb, ec2, emr, iam, redshift, s3, simpledb, sqs, sts)
  .dependsOn(core, dynamodb, ec2, emr, iam, redshift, s3, simpledb, sqs, sts)

lazy val core = project
  .in(file("core"))
  .settings(commonSettings)
  .settings(
    moduleName := "awscala-core",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-core" % awsJavaSdkVersion,
      "joda-time" % "joda-time" % "2.9.9",
      "org.joda" % "joda-convert" % "2.0",
      "org.bouncycastle" % "bcprov-jdk16" % "1.46" % "provided",
      "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    )
  )

lazy val iam = project
  .in(file("iam"))
  .settings(commonSettings)
  .settings(
    moduleName := "awscala-iam",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-iam" % awsJavaSdkVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    )
  )
  .dependsOn(core)

lazy val dynamodb = project
  .in(file("dynamodb"))
  .settings(commonSettings)
  .settings(
    moduleName := "awscala-dynamodb",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-dynamodb" % awsJavaSdkVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    )
  )
  .dependsOn(core)

lazy val ec2 = project
  .in(file("ec2"))
  .settings(commonSettings)
  .settings(
    moduleName := "awscala-ec2",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-ec2" % awsJavaSdkVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test",
      "com.github.seratch.com.veact" %% "scala-ssh" % "0.8.0-1" % "provided"
    )
  )
  .dependsOn(core)

lazy val emr = project
  .in(file("emr"))
  .settings(commonSettings)
  .settings(
    moduleName := "awscala-emr",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-emr" % awsJavaSdkVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    )
  )
  .dependsOn(core)

lazy val redshift = project
  .in(file("redshift"))
  .settings(commonSettings)
  .settings(
    moduleName := "awscala-redshift",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-redshift" % awsJavaSdkVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    )
  )
  .dependsOn(core)

lazy val s3 = project
  .in(file("s3"))
  .settings(commonSettings)
  .settings(
    moduleName := "awscala-s3",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-s3" % awsJavaSdkVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    )
  )
  .dependsOn(core)

lazy val simpledb = project
  .in(file("simpledb"))
  .settings(commonSettings)
  .settings(
    moduleName := "awscala-simpledb",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-simpledb" % awsJavaSdkVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    )
  )
  .dependsOn(core)

lazy val sqs = project
  .in(file("sqs"))
  .settings(commonSettings)
  .settings(
    moduleName := "awscala-sqs",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-sqs" % awsJavaSdkVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    )
  )
  .dependsOn(core)

lazy val sts = project
  .in(file("sts"))
  .settings(commonSettings)
  .settings(
    moduleName := "awscala-sts",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-sts" % awsJavaSdkVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3" % "test",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test"
    )
  )
  .dependsOn(core)


def _publishTo(v: String) = {
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
