package com.gravitydev.s2js.runner

//import plugintemplate.PluginProperties
import scala.tools.nsc.CompilerCommand
import scala.tools.nsc.Settings

import java.io.File

object S2JSPluginRunner {
	def main(args: Array[String]) {
		val settings = new Settings
		
		settings.classpath.tryToSet(List("bin"))
		settings.d.tryToSet(List("bin"))
		
		
		val files = List(
			//"src/main/scala/com/gravitydev/s2js/sample/Dialog.scala"
			//"src/main/scala/com/gravitydev/s2js/sample/Test.scala"
			"src/main/scala/com/gravitydev/s2js/sample/Test2.scala"
			//"src/main/scala/com/gravitydev/s2js/sample/XmlTest.scala"
		)
		/*
		def recursiveListFiles(f: File): Array[File] = {
		  val these = f.listFiles
		  these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
		}
		
		val files = (for (file <- recursiveListFiles(new File("/home/alvaro/workspace/vanity-s2js/src")) if file.getName.endsWith(".scala")) yield file.getAbsolutePath).toList
		*/
	
		/*
		settings.classpath.tryToSet(List("/home/alvaro/scala-workspace/vanity-s2js/bin:bin"))
		settings.d.tryToSet(List("bin"))
		val files = List(
			"/home/alvaro/scala-workspace/vanity-s2js/src/vanity2/util.scala"
		)
		*/

		val command = new CompilerCommand(files, settings) {
				
			/** The command name that will be printed in in the usage message.
			 *	This is automatically set to the value of 'plugin.commandname' in the
			 *	file build.properties.
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
		
		// plugin options
		val options = List(
			"output:/home/alvaro/Desktop/s2js-output"
		)

		val runner = new PluginRunner(options, settings)
		val run = new runner.Run
		run.compile(command.files)
	}
}


//import plugintemplate.{TemplateAnnotationChecker, TemplatePlugin}
import scala.tools.nsc.{Global, Settings, SubComponent}
import scala.tools.nsc.reporters.{ConsoleReporter, Reporter}

/** This class is a compiler that will be used for running
 *	the plugin in standalone mode.
 */
class PluginRunner(options:List[String], settings: Settings, reporter: Reporter) extends Global(settings, reporter) {
	def this(options:List[String], settings: Settings) = this(options, settings, new ConsoleReporter(settings))

	/** 
	 * The phases to be run.
	 */
	override protected def computeInternalPhases() {
		import com.gravitydev.s2js.S2JSPlugin
		
		val plugin = new S2JSPlugin(this)
		plugin.processOptions(options, (err:String) => println(err))
		
		/* Add the internal compiler phases to the phases set
		 */
		phasesSet += syntaxAnalyzer					// The parser
		phasesSet += analyzer.namerFactory			//	 note: types are there because otherwise
		phasesSet += analyzer.packageObjects		//	 consistency check after refchecks would fail.
		phasesSet += analyzer.typerFactory

		for (phase <- plugin.components) {
			phasesSet += phase
		}
		
		phasesSet += superAccessors					// add super accessors
		phasesSet += pickler						// serialize symbol tables
		phasesSet += refchecks						// perform reference and override checking, translate nested objects
		// phasesSet += devirtualize				// Desugar virtual classes
		
		phasesSet += uncurry						// uncurry, translate function values to anonymous classes
		phasesSet += tailCalls						// replace tail calls by jumps
		if (!settings.nospecialization.value)
			phasesSet += specializeTypes
		phasesSet += explicitOuter					// replace C.this by explicit outer pointers, eliminate pattern matching
		phasesSet += erasure						// erase types, add interfaces for traits
		phasesSet += lazyVals
		phasesSet += lambdaLift						// move nested functions to top level
		// if (forJVM && settings.Xdetach.value)
		//	 phasesSet += detach					// convert detached closures
	 
		phasesSet += constructors					// move field definitions into constructors
		phasesSet += mixer							// do mixin composition
		phasesSet += cleanup						// some platform-specific cleanups
		phasesSet += genicode						// generate portable intermediate code
		phasesSet += inliner						// optimization: do inlining
		phasesSet += closureElimination				// optimization: get rid of uncalled closures
		phasesSet += deadCode						// optimization: get rid of dead cpde
		phasesSet += terminal						// The last phase in the compiler chain

	}

}


