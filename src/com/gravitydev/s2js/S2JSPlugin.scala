package com.gravitydev.s2js

import scala.tools.nsc
import nsc.Global
import nsc.Phase
import nsc.plugins.Plugin
import nsc.plugins.PluginComponent


class S2JSPlugin (val global:Global) extends Plugin {
	import global._
	
	val name = "s2js"
	val description = "Scala-to-Javascript compiler plugin"
	val components = List[PluginComponent](Component)
	
	private object Component extends PluginComponent {

		val global: S2JSPlugin.this.global.type = S2JSPlugin.this.global
		
		def newPhase (_prev:Phase) = new S2JSPhase(_prev)
		
		val runsAfter = List("refchecks")
		
		val phaseName = S2JSPlugin.this.name
		
		class S2JSPhase (prev:Phase) extends StdPhase(prev) {
			var buffer = new StringBuffer
			
			def apply (unit:CompilationUnit) {
				import java.io._
				
				S2JSPlugin.this.global.treeBrowser.browse(unit.body)
		
				parseTree(unit.body, 0)
				
				var stream = new FileWriter("out.js")
				var writer = new BufferedWriter(stream)
				writer.write(buffer.toString)
				writer.close()
			}
			
			def parseTree (tree:Tree, i:Int) {
				for (u <- tree.children) {
					u match {
						case c:ClassDef => {
							p("goog.provide('" + c.symbol.tpe + "');")
							p("")
							p("/**")
							p(" * @constructor")
							p(" */")
							p(c.symbol.tpe + " = function () {")
							p("};")
							p("")
							parseTree(c, i)
							//TCPlugin.this.global.treeBrowser.browse(c)
						}
						case d:DefDef => printMethod(d, i+1)
						case a:Tree => {
							//println(a.getClass)
							parseTree(a, i)
						}
					}
				}
			}
			
			def printMethod (d:DefDef, i:Int) {
				if (d.name.toString != "<init>") {
					val args = parseMethodArgs(d)
					
					p("/**")
					
					for (arg <- args) p(" * @param " + getType(arg) + " " + arg.symbol.name) 
					
					p(" */")
					
					p(d.symbol.owner.tpe +".prototype."+ d.name + " = function (" + args.map(_.symbol.name).mkString(", ") + ") {")
					
					for (child <- d.children) child match {
						case c:ValDef => () //println("Arg def: "+c)
						case a:Tree => printTree(a, i+1)
					}
					
					p("};")
					p("")
					//TCPlugin.this.global.treeBrowser.browse(d)
				}
			}
			
			def printTree (tree:Tree, i:Int) {
				tree match {
					case c:Apply => printApply(c, i)
					case c:ValDef => printValDef(c, i)
					case c:If => printIf(c, i)
					case b:Tree => for (a <- b.children) printTree(a, i)
					//case a:Tree => ()//println("Unknown: " + a)
				}
			}
			
			def printApply (apply:Apply, i:Int) {
				p(apply.toString, i)
			}

			
			def printValDef (valdef:ValDef, i:Int) {
				p("/** @type " + getType(valdef) + " */", i)
				p("var " + valdef.name + " = " + valdef.rhs + ";", i)
			}
			
			def printIf (ifD:If, i:Int) {
				p("if (" + ifD.cond + ") {", i)
				printTree(ifD.thenp, i+1)
				p("} else {", i)
				printTree(ifD.elsep, i+1)
				p("}", i)
				
				p("}", i) 
			}
			
			def getType (tree:Tree) = {
				tree.symbol.tpe.toString match {
					case "Boolean" => "{boolean}"
					case x:String => "{"+x+"}"
				}
			}
			
			def parseMethodArgs (method:DefDef) = for (arg <- method.children; if arg.isInstanceOf[ValDef]) yield arg
			
		
			def p (code:{def toString:String}, indent:Int) {
				for (i <- 1 to indent) add("\t")
				add(code + "\n")
			}
			
			
			def p (code:{def toString:String}) {
				p(code, 0)
			}
			
			def add (code:{def toString:String}) {
				buffer.append(code)
				//print(code)
			}
		}
	}
}
