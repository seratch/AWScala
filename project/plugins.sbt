addSbtPlugin("org.scalariform"  % "sbt-scalariform"      % "1.8.2")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.3.3")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "2.0")
addSbtPlugin("com.jsuereth"     % "sbt-pgp"              % "1.1.0")
addSbtPlugin("com.localytics"   % "sbt-dynamodb"         % "2.0.2")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
