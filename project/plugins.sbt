addSbtPlugin("org.scalariform"  % "sbt-scalariform"      % "1.6.0")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.1.10")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "1.0")
addSbtPlugin("com.jsuereth"     % "sbt-pgp"              % "1.0.0")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
