package com.gravitydev.s2js

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.{Plugin, PluginComponent}


class S2JSPlugin (val global:Global) extends Plugin {
	val name = "s2js"
	val runsAfter = List("refchecks")
	val description = "Scala-to-Javascript compiler plugin"
	val components = List[PluginComponent](new S2JSComponent(global))
}

