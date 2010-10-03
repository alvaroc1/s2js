package com.gravitydev.s2js

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.PluginComponent


class S2JSComponent (val global:Global) extends PluginComponent {
	import global._
	
	val runsAfter = List[String]("refchecks")
	
	val phaseName = "s2js"
		
	val outputDir = "/home/alvaro/Desktop/s2js-output"
		
	def newPhase (prev:Phase) = new StdPhase(prev) {
		
		val buffer = new StringBuffer
		
		override def name = phaseName
		
		override def apply (unit:CompilationUnit) {
			import java.io._
		
			//S2JSComponent.this.global.treeBrowser.browse(unit.body)
			
			val path = getPackage(unit.body).replace('.', '/')
			val name = unit.source.file.toString.split("/").reverse.head.toList.reverse.drop(6).reverse.mkString("").toLowerCase
			val dir = outputDir + "/" + path
			
			// create the directories
			new File(dir).mkdirs
	
			// parseTree(unit.body, 0)
			
			println(JsPrinter.print(parseUnit(unit)))
			
			/*
			var stream = new FileWriter(dir + "/" + name + ".js")
			var writer = new BufferedWriter(stream)
			writer.write(buffer.toString)
			writer.close()
			*/
		}
		
		def getPackage (tree:Tree) = tree match {
			case PackageDef(pid,_) => pid.name
		}

		def parseUnit (unit:CompilationUnit) = {
			val path = getPackage(unit.body).replace('.', '/').toString
			val name = unit.source.file.toString.split("/").reverse.head.toList.reverse.drop(6).reverse.mkString("").toLowerCase
			
			JsSourceFile(path, name, getClasses(unit.body))
		}
		
		def getClasses (tree:Tree) = for (c @ ClassDef(_, _, _, _) <- tree.children) yield getClass(c)
		
		def getClass (c:Tree) = c match {
			case c @ ClassDef(mods, name, tparams, Template(parents, self, body)) => {

				val params = for (m @ ValDef(_, _, _, _) <- body; if m.toString.startsWith("<paramaccessor>")) yield m 
				
				val properties = for (m @ ValDef(_, _, _, _) <- body; if !m.toString.startsWith("<paramaccessor>")) yield getProperty(c, m)
				
				val constructors = for (const @ DefDef(mods,name,tparams,vparamss,tpt,rhs) <- body; if name.toString == "<init>") yield const
			
				val methods = for (m @ DefDef(mods, _, _, _, _, _) <- body; if !mods.isAccessor && m.name.toString != "<init>") yield getMethod(c, m)
				
				JsClass(
					c.symbol.tpe.toString,
					getSuperClass(c),
					getConstructor(c, constructors.head),
					properties,
					methods
				)
			}
		}
		
		def getConstructor (c:ClassDef, const:DefDef) = JsConstructor(
			c.symbol.tpe.toString,
			for (arg <- parseMethodArgs(const)) yield JsParam(arg.symbol.name.toString, getType(arg.symbol)),
			for (child <- const.rhs.children) yield getJsTree (child),
			for (child <- c.impl.body if !child.isInstanceOf[DefDef]) yield getJsTree(child)
		)
		
		def getMethod (c:ClassDef, method:DefDef) = {			
			//S2JSComponent.this.global.treeBrowser.browse(method)
			JsMethod(
				c.impl.tpe.toString+".prototype."+method.name.toString,
				for (arg <- parseMethodArgs(method)) yield JsParam(arg.symbol.name.toString, getType(arg.symbol)),
				//for (child <- method.rhs.children) yield getJsTree (child)
				List(getJsTree(method.rhs))
			)
		}
		
		def getProperty (c:ClassDef, prop:ValDef) = prop match {
			case ValDef(mods, name, tpt, rhs) => {
				val jsmods = JsModifiers(
					mods.isPrivate	
				)
				
				JsProperty(jsmods, name.toString, "{"+getType(tpt.toString)+"}", getJsTree(rhs))
			}
		}
		
		def getJsTree (node:Tree):JsTree = node match {
			
			case Apply(fun, args) => {
				JsApply(getJsTree(fun), args.map(getJsTree))
			}
			case ValDef(mods,name,tpt,rhs) => {
				JsVar(name.toString, tpt.toString, (if (rhs.isEmpty) JsEmpty() else getJsTree(rhs)))
			}
			case b @ Block(stats,expr) => {
				//println(b)
				JsBlock( stats.map(getJsTree(_)) ::: List(getJsTree(expr)) )
			}
			
			// unit 
			case Literal(Constant(())) => JsVoid()
			
			case Select(qualifier,name) => {
				// check if calling member of self
				val q = if (qualifier.toString.split('.').reverse.head == "this") qualifier.toString.split('.').tail.mkString(".")
					else qualifier.toString
					
				JsSelect(q, name.toString)
			}
			
			case This(qual) => {
				JsThis()
			}
			
			case l @ Literal(Constant(value)) => {
				getLiteral(l)
			}
			
			case Ident(name) => JsIdent (name.toString)
			
			case c @ ClassDef(_,_,_,_) => getClass(c)
			
			case If(cond, thenp, elsep) => JsIf(getJsTree(cond), getJsTree(thenp), getJsTree(elsep))
			
			case t:Tree => {
				JsOther(t.getClass.toString, for (child <- t.children) yield getJsTree(child))
			}
		}
		
		def getLiteral (node:Tree):JsLiteral = node match {
			case Literal(Constant(value)) => value match {
				case v:String => JsLiteral("\""+v.toString.replace("\"", "\\\"")+"\"", "")
				case _ => JsLiteral( if (value != null) value.toString else "null", "" )
			}
			case a @ _ => {
				JsLiteral("", "")
			}
		}
		
		def getSuperClass (c:ClassDef):Option[String] = {
			val superClass = c.impl.parents.head
			if (superClass.toString == "java.lang.Object") None else Some(superClass.tpe.toString)
		}
		
		def printApply (apply:Apply, i:Int) {
			// if it's a call on the super class' constructor
			val q = (for (s @ Select(qualifier, name) <- apply) yield qualifier).head
			
			//p("Q: "+q.toString+", class: "+apply.symbol.enclClass.fullName)
			
			if (q.toString.endsWith("super")) {
				val params = for (param <- apply.args) yield param;
				p(apply.symbol.enclClass.fullName + ".call(this" + (if (params.length>0) ", " else "") + params.map(_.toString).mkString(", ") + ");", i)
			
			} else {
				
				p(apply.toString, i)
			}
		}
	
		
		def printValDef (valdef:ValDef, i:Int) {
			throw new Exception("not used")
			p("/** @type " + getType(valdef.symbol) + " */", i)
			p("var " + valdef.name + " = " + valdef.rhs + ";", i)
		}
		
		def printIf (ifD:If, i:Int) {
			p("if (" + ifD.cond + ") {", i)
			//printTree(ifD.thenp, i+1)
			p("} else {", i)
			//printTree(ifD.elsep, i+1)
			p("}", i)
			
			p("}", i) 
		}
		
		@deprecated("Use the one that takes a string")
		def getType (symbol:Symbol) = {
			symbol.tpe.toString match {
				case "Boolean" => "{boolean}"
				case x:String => "{string}"
			}
		}
		
		def getType (tpe:String) = tpe match {
			case "Boolean" => "boolean"
			case "String" => "string"
			case "java.lang.String" => "string"
			case "browser.Element" => "Element"
			case "Double" => "number"
			case x:String => x
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


