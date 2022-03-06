addSbtPlugin("org.scalariform"  % "sbt-scalariform"      % "1.8.3")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"          % "0.6.1")
addSbtPlugin("org.xerial.sbt"   % "sbt-sonatype"         % "3.9.12")
addSbtPlugin("com.github.sbt"     % "sbt-pgp"              % "2.1.2")
addSbtPlugin("com.localytics"   % "sbt-dynamodb"         % "2.0.3")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")
