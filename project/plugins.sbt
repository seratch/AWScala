addSbtPlugin("org.scalariform"  % "sbt-scalariform"      % "1.8.2")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.4.0")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "2.5")
addSbtPlugin("com.jsuereth"     % "sbt-pgp"              % "1.1.2")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
