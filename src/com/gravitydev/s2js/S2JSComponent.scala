package com.gravitydev.s2js

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.PluginComponent
import scala.collection.mutable.ListBuffer

class S2JSComponent (val global:Global) extends PluginComponent {
	import global._
	import definitions.{ BooleanClass, IntClass, DoubleClass, StringClass, ObjectClass, UnitClass }
	import treeInfo.{ isSuperConstrCall }
	
	val runsAfter = List[String]("refchecks")
	
	val phaseName = "s2js"
		
	val outputDir = "/home/alvaro/Desktop/s2js-output"
		
	def collect [T <: Tree] (tree: Tree)(pf: PartialFunction[Tree, T]): List[T] = {
		val lb = new ListBuffer[T]
		tree foreach (t => if (pf.isDefinedAt(t)) lb += pf(t))
		lb.toList
	}
	
	// TODO figure out how to do parenthesized comparisons
	val comparisonMap = Map(
		"$eq$eq"		-> "==",
		"$bang$eq" 		-> "!=",
		"$greater"		-> ">",
		"$greater$eq" 	-> ">=",
		"$less"			-> "<",
		"$less$eq"		-> "<=",
		
		// should probably have a separate map for these
		"$amp$amp"		-> "&&"
	)
		
	def newPhase (prev:Phase) = new StdPhase(prev) {
		
		val buffer = new StringBuffer
		
		override def name = phaseName
		
		override def apply (unit: CompilationUnit) {
			import java.io._
		
			// S2JSComponent.this.global.treeBrowser.browse(unit.body)
			
			// output paths
			val path = unit.body.symbol.fullName.replace('.', '/')
			val name = unit.source.file.name.stripSuffix(".scala").toLowerCase
			val dir = outputDir + "/" + path
			
			// create the directories
			new File(dir).mkdirs
			
			// transform to Js AST
			lazy val parsedUnit = JsSourceFile(path, name, getClasses(unit.body))
			
			// print and save
			val code = JsPrinter print parsedUnit
			
			println(code)
			
			var stream = new FileWriter(dir + "/" + name + ".js")
			var writer = new BufferedWriter(stream)
			writer write code
			writer.close()
		}
		
		// for debugging
		def inspect (t:Tree) {
			val s = t.symbol
			
			if (s != null) {
				println("-----------------------------")
				println("SYMBOL: " + s)
				println("name: " + s.name)
				println("hasDefault: " + s.hasDefault)
				println("isGetterOrSetter: " + s.isGetterOrSetter)
				println("isMethod: " + s.isMethod)
				println("isMutable: " + s.isMutable)
				println("isParamAccessor: " + s.isParamAccessor)
				println("isParameter: " + s.isParameter)
				println("isSourceMethod: " + s.isSourceMethod)
				println("isSuperAccessor: " + s.isSuperAccessor)
				println("isSynthetic: " + s.isSynthetic)
				println("isLocal: " + s.isLocal)
				println("-----------------------------")
				println("")
			}
		}
		
		def getClasses (tree:Tree) = for (c @ ClassDef(_, _, _, _) <- tree.children) yield getClass(c)
		
		def getClass (c:Tree) = c match {
			case c @ ClassDef(mods, name, tparams, Template(parents, self, body)) => {
				val primary = body.collect({ case d:DefDef if d.symbol.isPrimaryConstructor => d }) head
				
				val params = primary.vparamss.flatten
				
				val properties = body.collect({ case x:ValDef if !x.symbol.isParamAccessor && !x.symbol.isParameter => x })
			
				val methods = body.collect({ case x:DefDef if !x.mods.isAccessor && !x.symbol.isConstructor => x })
				
				JsClass(
					c.symbol.tpe.toString,
					getSuperClass(c),
					getConstructor(c, primary),
					properties map (getProperty(c, _)),
					methods map (getMethod(c, _))
				)
			}
		}
		
		def getConstructor (c:ClassDef, const:DefDef) = {
			val params = const.vparamss.flatten map ((t) => JsParam(t.symbol.name.toString, getType(t.symbol)))
			
			JsConstructor(
				c.symbol.tpe.toString,
				params,
				for (child <- const.rhs.children) yield getJsTree (child),
				for (child <- c.impl.body if !child.isInstanceOf[DefDef]) yield getJsTree(child)
			)
		}
		
		def getMethod (c:ClassDef, method:DefDef) = {
			val tpe = method.tpt.symbol
			
			//S2JSComponent.this.global.treeBrowser.browse(method)
			JsMethod(
				c.impl.tpe.toString+".prototype."+method.name.toString,
				for (arg <- parseMethodArgs(method)) yield JsParam(arg.symbol.name.toString, getType(arg.symbol)),
				//for (child <- method.rhs.children) yield getJsTree (child)
				List(getJsTree(method.rhs)),
				getType(method.tpt.symbol)
			)
		}
		
		def getProperty (c:ClassDef, prop:ValDef) = prop match {
			case ValDef(mods, name, tpt, rhs) => {
				val jsmods = JsModifiers(
					mods.isPrivate	
				)
				
				JsProperty(jsmods, name.toString, "{"+getType(tpt.symbol)+"}", getJsTree(rhs))
			}
		}
		
		def getJsTree (node:Tree):JsTree = node match {

			// property re-assignment
			case a @ Apply(fun @ Select(qualifier, name), args) if name.toString.endsWith("_$eq") => {
				
				val select = JsSelect(
					// if it's local method, strip class name
					if (fun.symbol.isSourceMethod) qualifier.toString.split('.').tail.mkString(".") else qualifier.symbol.fullName, 
					name.toString stripSuffix "_$eq", 
					fun.symbol.isParamAccessor
				)
				
				JsAssign(select, getJsTree(args.head))
			}
			
			// comparison
			case a @ Apply(fun @ Select(qualifier, name), args) if comparisonMap contains name.toString => {
				JsComparison(
					getJsTree(qualifier),
					comparisonMap.getOrElse(name.toString, "huh?"),
					getJsTree(args.head)
				)
			}
			
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
			
			case s @ Select(qualifier,name) => {
				// for some reason symbol is null sometimes
				val q = if (qualifier.symbol == null) qualifier.toString + "(NULL SYMBOL)" else qualifier.symbol.fullName
				
				// if it's qualified by a local variable
				val q2 = if (qualifier.symbol != null && qualifier.symbol.isLocal) {
					qualifier.toString.split('.').last
				} else {
					// if it's a local method, strip the class name
					// not sure how else to do this :(
					if (qualifier.toString.split('.') contains "this") {
						qualifier.toString.split('.').tail.mkString(".")
					} else {
						q
					}
				}
				
				JsSelect(q2, name.toString, s.symbol.isParamAccessor)
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
		
		/*
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
		*/
	
		/*
		def printValDef (valdef:ValDef, i:Int) {
			throw new Exception("not used")
			p("/** @type " + getType(valdef.symbol) + " */", i)
			p("var " + valdef.name + " = " + valdef.rhs + ";", i)
		}
		*/
		
		def getType (symbol:Symbol) = symbol.tpe.typeSymbol match {
			case BooleanClass 			=> "boolean"
			case IntClass|DoubleClass	=> "number"
			case StringClass 			=> "string"
			case UnitClass				=> "void"
			
			// closure built-in types, hack for right now
			case x if x.tpe.toString.startsWith("browser.") => x.tpe.toString.substring(8)
			
			case x						=> x.tpe.toString
		}
		
		def parseMethodArgs (method:DefDef) = for (arg <- method.children; if arg.isInstanceOf[ValDef]) yield arg
	
	}
}


