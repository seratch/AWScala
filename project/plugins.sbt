addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform"      % "1.3.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.1.8")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "0.5.0")
addSbtPlugin("com.jsuereth"     % "sbt-pgp"              % "1.0.0")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

