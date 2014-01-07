import sbt._
import Keys._
import sbtassembly.Plugin.AssemblyKeys._
import sbtassembly.Plugin.assemblySettings
import Configurations.CompilerPlugin

object S2JSBuild extends Build {
  override def rootProject = Some(plugin)

  lazy val plugin = Project(id = "s2js-plugin", base = file("plugin"))
    .settings(commonSettings:_*)
    .settings(assemblySettings:_*)
    .settings(
      //exportJars := true,
      offline := true,
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-compiler" % "2.10.3" % "compile;runtime;test",
        "com.googlecode.kiama" %% "kiama" % "1.5.1" % "compile;runtime;test",

        // test deps
        "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
        "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.2" % "test",
        "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2" % "test"
      ),
      test in assembly := {},
      artifact in (Compile, assembly) ~= {art =>
        art.copy(`classifier` = Some("assembly"))
      },
      assemblyOption in assembly ~= { _.copy(includeScala = false) }
    )
    .settings(
      addArtifact(artifact in (Compile, assembly), assembly) :_*
    )
    .dependsOn(externs)

  lazy val api = Project(id = "s2js-api", base = file("api"))
    .settings(commonSettings:_*)

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
    version := "0.0.5-SNAPSHOT",
    scalaVersion := "2.10.3",
    publishTo := Some("devstack" at "https://devstack.io/repo/gravitydev/public")
  )
}

