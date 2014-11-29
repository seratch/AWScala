addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform"      % "1.3.0")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.1.7")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "0.2.1")
addSbtPlugin("com.jsuereth"     % "sbt-pgp"              % "1.0.0")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

