package com.gravitydev.s2js.runner

//import plugintemplate.PluginProperties
import scala.tools.nsc.CompilerCommand
import scala.tools.nsc.Settings

import java.io.File

/** An object for running the plugin as standalone application.
 * 
 *  @todo: print, parse and apply plugin options !!!
 *  ideally re-use the TemplatePlugin (-> runsAfter, optionsHelp,
 *  processOptions, components, annotationChecker) instead of
 *  duplicating it here and in PluginRunner.
 */
object S2JSPluginRunner {
  def main(args: Array[String]) {
    val settings = new Settings
    
    settings.classpath.tryToSet(List("bin"))
    settings.d.tryToSet(List("bin"))
    
    val files = List(
    	"src/com/gravitydev/s2js/sample/Dialog.scala"
    	//"/home/alvaro/scala-workspace/test/src/gravity/Dialog.scala"
    	/*"/home/alvaro/scala-workspace/test/src/gravity/Test.scala",
    	"/home/alvaro/scala-workspace/test/src/gravity/UserList.scala"*/
    )

    val command = new CompilerCommand(files, settings) {
      /** The command name that will be printed in in the usage message.
       *  This is automatically set to the value of 'plugin.commandname' in the
       *  file build.properties.
       */
      override val cmdName = "runs2js"
    }

    if (!command.ok)
      return()

    /** The version number of this plugin is read from the properties file
     */
    if (settings.version.value) {
      println(command.cmdName +" version "+ "1.0")
      return()
    }
    if (settings.help.value) {
      println(command.usageMsg)
      return()
    }

    val runner = new PluginRunner(settings)
    val run = new runner.Run
    run.compile(command.files)
  }
}


//import plugintemplate.{TemplateAnnotationChecker, TemplatePlugin}
import scala.tools.nsc.{Global, Settings, SubComponent}
import scala.tools.nsc.reporters.{ConsoleReporter, Reporter}

/** This class is a compiler that will be used for running
 *  the plugin in standalone mode.
 */
class PluginRunner(settings: Settings, reporter: Reporter)
extends Global(settings, reporter) {
  def this(settings: Settings) = this(settings, new ConsoleReporter(settings))

  /*
  val annotChecker = new TemplateAnnotationChecker {
    val global: PluginRunner.this.type = PluginRunner.this
  }
  addAnnotationChecker(annotChecker.checker)
  */

  /** The phases to be run.
   *
   *  @todo: Adapt to specific plugin implementation
   */
  override protected def computeInternalPhases() {
	import com.gravitydev.s2js.S2JSPlugin
	
	println("computing...")
	  
  /* Add the internal compiler phases to the phases set
   */
    phasesSet += syntaxAnalyzer             // The parser
    phasesSet += analyzer.namerFactory      //   note: types are there because otherwise
    phasesSet += analyzer.packageObjects    //   consistency check after refchecks would fail.
    phasesSet += analyzer.typerFactory
    phasesSet += superAccessors             // add super accessors
    phasesSet += pickler                    // serialize symbol tables
    phasesSet += refchecks                  // perform reference and override checking, translate nested objects
    // phasesSet += devirtualize               // Desugar virtual classes

    for (phase <- new S2JSPlugin(this).components) {
      phasesSet += phase
    }
    
    
    phasesSet += uncurry                    // uncurry, translate function values to anonymous classes
    phasesSet += tailCalls                  // replace tail calls by jumps
    if (!settings.nospecialization.value)
      phasesSet += specializeTypes
    phasesSet += explicitOuter              // replace C.this by explicit outer pointers, eliminate pattern matching
    phasesSet += erasure                    // erase types, add interfaces for traits
    phasesSet += lazyVals
    phasesSet += lambdaLift                 // move nested functions to top level
    // if (forJVM && settings.Xdetach.value)
    //   phasesSet += detach                // convert detached closures
   
    phasesSet += constructors               // move field definitions into constructors
    phasesSet += mixer                      // do mixin composition
    phasesSet += cleanup                    // some platform-specific cleanups
    phasesSet += genicode                   // generate portable intermediate code
    phasesSet += inliner                    // optimization: do inlining
    phasesSet += closureElimination         // optimization: get rid of uncalled closures
    phasesSet += deadCode                   // optimization: get rid of dead cpde
    phasesSet += terminal                   // The last phase in the compiler chain

  }

}