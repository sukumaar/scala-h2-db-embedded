name := "scala-h2-db-embedded"

version := "0.1"

scalaVersion := "2.13.1"

// H2 Database
libraryDependencies += "com.h2database" % "h2" % "1.4.197" % Test

//For Testing
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % Test