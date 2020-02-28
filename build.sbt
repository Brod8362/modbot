name := "modbot"

version := "0.1"

scalaVersion := "2.13.1"

resolvers += "jcenter-bintray" at "https://jcenter.bintray.com"

libraryDependencies ++=
  Seq("net.dv8tion" % "JDA" % "4.1.1_109",
    "org.scalikejdbc" %% "scalikejdbc" % "3.4.0",
    "com.h2database" % "h2" % "1.4.200",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.xerial" % "sqlite-jdbc" % "3.7.2")