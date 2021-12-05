import xerial.sbt.Sonatype.autoImport._

val scala213 = "2.13.7"
val scala3 = "3.1.0"

lazy val commonSettings = Seq(
  organization := "com.github.seratch",
  name := "awscala",
  version := "0.9.2",
  scalaVersion := scala213,
  crossScalaVersions := Seq(scala213, scala3),
  sbtPlugin := false,
  transitiveClassifiers in Global := Seq(Artifact.SourceClassifier),
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  publishTo := _publishTo(version.value),
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
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

lazy val awsJavaSdkVersion = "1.12.125"

lazy val all = (project in file("."))
  .settings(commonSettings)
  .settings(
    moduleName := "awscala"
  )
  .aggregate(core, dynamodb, ec2, emr, iam, redshift, s3, simpledb, sqs, sts, stepfunctions)
  .dependsOn(core, dynamodb, ec2, emr, iam, redshift, s3, simpledb, sqs, sts, stepfunctions)

lazy val core = project
  .in(file("core"))
  .settings(commonSettings)
  .settings(
    moduleName := "awscala-core",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-core" % awsJavaSdkVersion,
      "joda-time" % "joda-time" % "2.10.13",
      "org.joda" % "joda-convert" % "2.2.1",
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.6.0",
      "org.bouncycastle" % "bcprov-jdk16" % "1.46" % "provided",
      "ch.qos.logback" % "logback-classic" % "1.2.7" % "test",
      "org.scalatest" %% "scalatest" % "3.2.10" % "test",
    ) ++ {scalaVersion.value.head match {
      case '2' => Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value)
      case _ => Seq()
    }}
  )

lazy val ec2 = awsProject("ec2")
  .settings(
    libraryDependencies ++= Seq(
      "com.decodified" %% "scala-ssh" % "0.11.1" % "provided"
    )
  )

lazy val iam = awsProject("iam")
lazy val dynamodb = awsProject("dynamodb").settings(dynamoTestSettings)
lazy val emr = awsProject("emr").dependsOn(ec2 % "test")
lazy val redshift = awsProject("redshift")
lazy val s3 = awsProject("s3")
lazy val simpledb = awsProject("simpledb")
lazy val sqs = awsProject("sqs")
lazy val sts = awsProject("sts")
lazy val stepfunctions = awsProject("stepfunctions").dependsOn(iam % "test")

def awsProject(service: String) = {
  Project
    .apply(service, file(service))
    .settings(commonSettings)
    .settings(
      moduleName := s"awscala-$service",
      libraryDependencies ++= Seq(
        "com.amazonaws" % s"aws-java-sdk-$service" % awsJavaSdkVersion,
        "ch.qos.logback" % "logback-classic" % "1.2.7" % "test",
        "org.scalatest" %% "scalatest" % "3.2.10" % "test"
      )
    )
    .dependsOn(core)
}

lazy val dynamoTestSettings = Seq(
  dynamoDBLocalDownloadDir := file(".dynamodb-local"),
  dynamoDBLocalPort := 8000,
  startDynamoDBLocal := startDynamoDBLocal.dependsOn(compile in Test).value,
  test in Test := (test in Test).dependsOn(startDynamoDBLocal).value,
  testOptions in Test += dynamoDBLocalTestCleanup.value,
  parallelExecution in Test := false
)

def _publishTo(v: String) = {
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
