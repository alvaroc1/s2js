import sbt._
import Keys._

object S2JSBuild extends Build {
  lazy val plugin = Project(id = "s2js-plugin", base = file("plugin")).settings(
    organization  := "com.gravitydev",
    scalaVersion  := "2.10.0",
    libraryDependencies := Seq(
	    "org.scala-lang" % "scala-compiler" % "2.10.0" % "compile;runtime;test",
      "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
      "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2",
      "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2"
    )	  
  )

  lazy val externs = Project(id = "s2js-externs", base = file("externs")).settings(
    organization  := "com.gravitydev",
    scalaVersion  := "2.10.0"
  )
}

