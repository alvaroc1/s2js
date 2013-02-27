import sbt._
import Keys._

object S2JSBuild extends Build {
  lazy val plugin = Project(id = "s2js-plugin", base = file("plugin")).settings(
    scalaVersion := "2.9.3-RC1",
    libraryDependencies := Seq(
	    "org.scala-lang" % "scala-compiler" % "2.9.3-RC1" % "compile;runtime;test",
      "org.scalatest" %% "scalatest" % "2.0.M5" % "test"
    )	  
  )

  lazy val externs = Project(id = "s2js-externs", base = file("externs")).settings(
    scalaVersion := "2.9.3-RC1"
  )
}

