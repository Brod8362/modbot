name := "modbot"

version := "0.1"

scalaVersion := "2.13.1"

resolvers += "jcenter-bintray" at "https://jcenter.bintray.com"

libraryDependencies ++=
  Seq("net.dv8tion" % "JDA" % "4.1.1_109")