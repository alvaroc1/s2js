import sbt._
import Keys._
import Configurations.CompilerPlugin

object S2JSBuild extends Build {
  lazy val plugin = Project(id = "s2js", base = file("plugin"))
    .settings(commonSettings:_*)
    .settings(
      exportJars := true,
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % "2.10.0" % "compile;runtime;test",
        "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test",
        "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2" % "test",
        "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2" % "test"
      )	  
    )
    .dependsOn(externs)

  lazy val externs = Project(id = "s2js-externs", base = file("externs"))
    .settings(commonSettings:_*)

  lazy val sample = Project(id = "s2js-sample", base = file ("sample"))
    .dependsOn(plugin % CompilerPlugin)
    .settings(commonSettings:_*)
    .settings(
      autoCompilerPlugins := true, // does this do anything?
      /* this doesn't seem to work
      libraryDependencies ++= Seq(
        compilerPlugin("com.gravitydev" %% "s2js-plugin" % "0.1-SNAPSHOT")
      ),
      */

      /* this is the only thing that works */
      scalacOptions ++= Seq(
        "-Xplugin:/home/alvaro/workspace-juno/s2js/plugin/target/scala-2.10/s2js_2.10-0.1-SNAPSHOT.jar",
        "-P:s2js:output:/home/alvaro/workspace-juno/s2js/out"
      )
    )

  val commonSettings = Seq(
    organization := "com.gravitydev",
    scalaVersion := "2.10.0"
  )
}

