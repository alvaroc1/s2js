package com.gravitydev.s2js

import scala.tools.nsc
import nsc.Global


class S2JS (val global:Global) {
	import global._
	
	var buffer = new StringBuffer
	
	def parse (unit:CompilationUnit) {
		import java.io._
		
		//S2JSPlugin.this.global.treeBrowser.browse(unit.body)

		parseTree(unit.body, 0)
		
		var stream = new FileWriter("out.js")
		var writer = new BufferedWriter(stream)
		writer.write(buffer.toString)
		writer.close()
	}
	
	def parseTree (tree:Tree, i:Int) {
		for (u <- tree.children) u match {
			case c:ClassDef => printClass(c, i)
			case d:DefDef => printMethod(d, i+1)
			case a:Tree => {
				//println(a.getClass)
				parseTree(a, i)
			}
		}
	}
	
	def printClass (c:ClassDef, i:Int) {
		
		// constructor set properties
		val valDefs = for (Template(_,_,body) <- c.children; v @ ValDef(mods, name, tpt, rhs) <- body) yield v
		
		p("goog.provide('" + c.symbol.tpe + "');")
		p("")
		
		printConstructor(getConstructor(c), valDefs, i)
		
		p("goog.inherits(" + c.symbol.tpe + ", " + c.symbol.superClass.tpe + ");")
		p("")
		
		for (method <- getMethods(c) if method.name.toString != "<init>") printMethod(method, i)
	}
	
	def getConstructor (c:ClassDef) = (for (method <- getMethods(c) if method.name.toString == "<init>") yield method).head
	
	def getMembers (c:ClassDef) = for (Template(_,_,body) <- c.children; d @ DefDef(_,_,_,_,_,_) <- body) yield d
	
	/**
	 * Get all methods that are not properties
	 */
	def getMethods (c:ClassDef) = getMembers(c) filter ( !getProperties(c).contains(_) ) filter ( !_.name .toString.endsWith("_$eq") )
	
	/**
	 * Fish out properties of a class by looking for a matching _$eq method
	 */
	def getProperties (c:ClassDef) = for (m1 <- getMembers(c); m2 <- getMembers(c); if m1.name.toString == m2.name .toString + "_$eq") yield m2
	
	def printMethod (d:DefDef, i:Int) {
		val args = parseMethodArgs(d)
		
		p("/**")
	
		if (d.symbol.isConstructor) p(" * @constructor")
		
		for (arg <- args) p(" * @param " + getType(arg.symbol) + " " + arg.symbol.name) 
		
		p(" */")
		
		val name = if (d.symbol.isConstructor) d.symbol.owner.tpe else d.symbol.owner.tpe + ".prototype." + d.name
		
		p(name + " = function (" + args.map(_.symbol.name).mkString(", ") + ") {")
		
		for (child <- d.children) child match {
			case c:ValDef => () //println("Arg def: "+c)
			case a:Tree => printTree(a, i+1)
		}
		
		p("};")
		p("")
	}
	
	def printConstructor (d:DefDef, valDefs:List[Tree], i:Int) {
		val args = parseMethodArgs(d)
		
		p("/**")
		p(" * @constructor")
		
		for (arg <- args) p(" * @param " + getType(arg.symbol) + " " + arg.symbol.name) 
		
		p(" */")
		
		p(d.symbol.owner.tpe + " = function (" + args.map(_.symbol.name).mkString(", ") + ") {")
		
		var constructorParams = for (v @ ValDef(_,_,_,_) <- d.children) yield v
		
		/*
		for (param <- constructorParams) p("PARAM: " + param)
		for (child <- d.children) p("CHILD: " + child)
		for (vald @ ValDef(mods,_,_,_) <- valDefs if !vald.toString.startsWith("<paramaccessor>")) p("VALDEF: " + vald)
		*/
		
		// print any properties that are not constructor parameters
		for (vd @ ValDef(mods,_,_,_) <- valDefs if !vd.toString.startsWith("<paramaccessor>")) printTree(vd, i+1)
		
		for (child <- d.children) child match {
			case c:ValDef => () //println("Arg def: "+c)
			case a:Tree => printTree(a, i+1)
		}
		
		p("};")
		p("")
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
		val q = (for (s @ Select(qualifier, name) <- apply) yield qualifier).head
		if (q.toString.endsWith("super")) {
			val params = for (param <- apply.args) yield param;
			p(apply.symbol.enclClass.fullName + ".call(this" + (if (params.length>0) ", " else "") + params.map(_.toString).mkString(", ") + ");", i)
		} else {
			p(apply.toString, i)
		}
	}

	
	def printValDef (valdef:ValDef, i:Int) {
		p("/** @type " + getType(valdef.symbol) + " */", i)
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
	
	def getType (symbol:Symbol) = {
		symbol.tpe.toString match {
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
