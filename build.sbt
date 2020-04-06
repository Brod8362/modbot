name := "modbot"

version := "0.1"

scalaVersion := "2.13.1"

resolvers += "jcenter-bintray" at "https://jcenter.bintray.com"

libraryDependencies ++=
  Seq("net.dv8tion" % "JDA" % "4.1.1_130",
    "org.scalikejdbc" %% "scalikejdbc" % "3.4.0",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.xerial" % "sqlite-jdbc" % "3.7.2")

assemblyMergeStrategy in assembly := {
    case "module-info.class" => MergeStrategy.first
    case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
}