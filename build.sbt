name := "scala-sql"

version := "0.1"

scalaVersion := "2.13.1"

// H2 Database
// https://mvnrepository.com/artifact/com.h2database/h2
libraryDependencies += "com.h2database" % "h2" % "1.4.197" % Test

// https://mvnrepository.com/artifact/org.scalatest/scalatest
//For Testing
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % Test

//enablePlugins(UniversalPlugin)
//enablePlugins(JavaAppPackaging)
