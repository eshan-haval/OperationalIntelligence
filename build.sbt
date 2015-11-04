
name := "OperationalIntelligence"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies += "org.apache.spark" % "spark-streaming_2.10" % "1.5.1" % "provided"
libraryDependencies += "org.apache.spark" % "spark-streaming-kafka_2.10" % "1.5.1"
libraryDependencies += "org.apache.spark" %% "spark-core" % "1.5.1" % "provided"


mergeStrategy in assembly := {

  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class")
=> MergeStrategy.first

  case x => (mergeStrategy in assembly).value(x)

}
    
