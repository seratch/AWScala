addSbtPlugin("org.scalariform"  % "sbt-scalariform"      % "1.6.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.3.1")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "2.0")
addSbtPlugin("com.jsuereth"     % "sbt-pgp"              % "1.1.0-M1")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
