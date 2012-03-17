import sbt._

import sbt._
import Keys._

object S2JSBuild extends Build {
  lazy val s2jsProject = Project(id = "s2js", base = file(".")).settings(
    libraryDependencies := Seq(
	  "org.scala-lang" % "scala-compiler" % "2.9.1" % "compile;runtime;test",
	  "org.specs2" %% "specs2" % "1.7" % "test"
    )	  
  )

  /*  
  lazy val foo = Project(id = "hello-foo",
                           base = file("foo"))

  lazy val bar = Project(id = "hello-bar",
                           base = file("bar"))
                           */
}
