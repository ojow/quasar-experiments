name := "quasar-experiments"

scalaVersion := "2.11.7"

fork in run := true

lazy val quasarVersion = "0.7.3"

javaOptions in run ++= Seq(
  s"-javaagent:${ivyPaths.value.ivyHome.get}/cache/co.paralleluniverse/quasar-core/jars/" +
    s"quasar-core-$quasarVersion-jdk8.jar",
  "-Xmx1G")

libraryDependencies ++= Seq(
  "co.paralleluniverse" % "quasar-core" % "0.7.3" classifier "jdk8",
  "io.netty" % "netty-codec-http" % "4.0.33.Final"
)

