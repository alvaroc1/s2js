package com.gravitydev.s2js

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.{Plugin, PluginComponent}


class S2JSPlugin (val global:Global) extends Plugin {
	val name = "s2js"
	val runsAfter = List("refchecks")
	val description = "Scala-to-Javascript compiler plugin"
	
	// options
	var output = ""
	var packages = Nil : List[String]
	
	val components = List[PluginComponent](new Component(global, this))
	
	override def processOptions (options:List[String], error:String=>Unit) {
		options find (_ startsWith "output:") foreach {out => output = out stripPrefix "output:"}
		options find (_ startsWith "packages:") foreach {pkgs => packages = pkgs.stripPrefix("packages:").split("\\|").toList}

    println("S2JS Options:")
    println("  output: " + output)
    println("  packages: " + packages)
		
		// validate
		if (output == "") error("You must provide an [output] option. Like this: -P:s2js:output:/path/to/output")
	}
}
