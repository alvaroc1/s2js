import sbt._
import Keys._
import Configurations.CompilerPlugin

object S2JSBuild extends Build {
  override def rootProject = Some(plugin)

  lazy val plugin = Project(id = "s2js-plugin", base = file("plugin"))
    .settings(commonSettings:_*)
    .settings(
      exportJars := true,
      offline := true,
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % "2.10.3" % "compile;runtime;test",
        "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
        "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2" % "test",
        "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2" % "test",
        "com.googlecode.kiama" %% "kiama" % "1.5.1"
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
        "-Xplugin:/Users/alvarocarrasco/workspace/s2js/plugin/target/scala-2.10/s2js_2.10-0.1-SNAPSHOT.jar",
        "-P:s2js:output:/Users/alvarocarrasco/workspace/s2js/out"
      )
    )

  val commonSettings = Seq(
    organization := "com.gravitydev",
    scalaVersion := "2.10.3"
  )
}

