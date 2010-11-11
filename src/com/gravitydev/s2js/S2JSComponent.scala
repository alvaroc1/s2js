package com.gravitydev.s2js

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.PluginComponent
import scala.collection.mutable.ListBuffer

class S2JSComponent (val global:Global) extends PluginComponent {
	import global._
	import definitions.{ BooleanClass, IntClass, DoubleClass, StringClass, ObjectClass, UnitClass }
	import treeInfo.{ isSuperConstrCall }
	
	//val runsAfter = List[String]("refchecks")
	val runsAfter = List[String]("typer")
	
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
			
			// get the package
			var pkg = ""
			unit.body match {
				case PackageDef(pid,_) => pkg = pid.toString
			}
			
			// transform to Js AST
			lazy val parsedUnit = JsSourceFile(path, name, getClasses(unit.body, pkg))
			
			// process ast
			val processed = processAST( parsedUnit )
			
			// print and save
			val code = JsPrinter print processed
			
			println(code)
			
			println(JsAstPrinter print parsedUnit)
			
			var stream = new FileWriter(dir + "/" + name + ".js")
			var writer = new BufferedWriter(stream)
			writer write code
			writer.close()
		}
		
		// for debugging
		def inspect (t:Tree):Unit = inspect(t.symbol)
		
		def inspect (s:Symbol) {
			if (s!=null) {
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
		
		def getClasses (tree:Tree, pkg:String) = for (c @ ClassDef(_, _, _, _) <- tree.children) yield getClass(c, pkg:String)
		
		def getClass (c:Tree, pkg:String) = c match {
			case c @ ClassDef(mods, name, tparams, Template(parents, self, body)) => {				
				val primary = body.collect({ case d:DefDef if d.symbol.isPrimaryConstructor => d }) head
				
				val params = primary.vparamss.flatten
				
				val properties = body.collect({ case x:ValDef if !x.symbol.isParamAccessor && !x.symbol.isParameter => x })
			
				val methods = body.collect({ case x:DefDef if !x.mods.isAccessor && !x.symbol.isConstructor => x })

				JsClass(
					c.symbol.tpe.toString,
					pkg,
					for (Select(qualifier, name) <- parents) yield JsSelect( getJsTree(qualifier), name.toString),
					getConstructor(c, primary),
					properties map (getProperty(c, _)),
					methods map (getMethod(c, _))
				)
			}
		}
		
		def getConstructor (c:ClassDef, const:DefDef) = {
			//val params = const.vparamss.flatten map ((t) => JsParam(t.symbol.name.toString, getType(t.symbol)))
			/*
			val params = const.vparamss.flatten map ((t) => {
				JsParam(t.symbol.name.toString, getType(t.symbol))
			})
			*/
			
			val params = for (ValDef(mods, name, tpt, rhs) <- const.vparamss.flatten) yield {
				JsParam(name.toString, getType(tpt))
			}
			
			//S2JSComponent.this.global.treeBrowser.browse(const.rhs);
			
			JsConstructor(
				c.symbol.tpe.toString,
				params,
				for (child <- const.rhs.children) yield getJsTree (child),
				
				// skip methods and param accessors (not needed in js)
				for (child <- c.impl.body if !child.isInstanceOf[DefDef] && !child.symbol.isParamAccessor) yield getJsTree(child)
			)
		}
		
		def getMethod (c:ClassDef, method:DefDef) = {
			val tpe = method.tpt.symbol
			
			//S2JSComponent.this.global.treeBrowser.browse(method)
			JsMethod(
				c.impl.tpe.toString+".prototype."+method.name.toString,
				for (ValDef(mods, name, tpt, rhs) <- method.vparamss.flatten) yield JsParam(name.toString, getType(tpt)),
				List(getJsTree(method.rhs)),
				getType(method.tpt.symbol)
			)
		}
		
		def getType (typeTree:Tree) = {
			// get the original Select (for some reason it is now a TypeTree)
			// i hate casting
			typeTree.asInstanceOf[TypeTree].original match {
				case Select(Ident(qname), name) if qname.toString == "scala" => JsBuiltInType(
					name.toString match {
						case "Boolean" => JsBuiltInType.BooleanT
						case "Double" => JsBuiltInType.NumberT
					}
				)
				case Select(Select(This(qual), name), t) if name.toString == "Predef" => JsBuiltInType(
					// should use types instead of strings
					t.toString match {
						case "String" => JsBuiltInType.StringT
					}
				)
				case x => getJsTree( x )
			}
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
				
				val n = name.toString stripSuffix "_$eq"
			
				val select = JsSelect (
					getJsTree(qualifier), 
					// remove the suffix
					name.toString stripSuffix "_$eq"
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
			
			// package methods
			case Select(Select(Ident(_), name), _) if name.toString == "package" => {
				println("test")
				JsVoid()
			}
			
			// new (instantiation)
			case Select( New( Select( q, name ) ), _ ) => {
				JsNew( JsSelect( getJsTree(q), name.toString ) )
			}
			
			// application
			case Apply(Select(qualifier, name), args) => {
				JsApply( JsSelect( getJsTree(qualifier), name.toString, JsSelectType.Method ), args map getJsTree)
			}
			
			// select (parent != Apply)
			case s @ Select(qualifier,name) => s match {
				// if this select is a method, and it wasn't caught on the previous case
				// it is no-parameter-list application
				// wrap on apply
				case s if !s.symbol.isGetterOrSetter && s.symbol.isMethod => {
					JsApply( JsSelect( getJsTree(qualifier), name.toString, JsSelectType.Method), Nil)
				}
				case s => {
					//inspect(s)
					JsSelect(
						getJsTree(qualifier), 
						name.toString,
						s match {
							case s if (s.symbol.isGetterOrSetter) => JsSelectType.Prop
							case s if (s.symbol.isParamAccessor) => JsSelectType.ParamAccessor
							case _ => JsSelectType.Other
						}
					)
				}
			}
			
			/*
			case Apply(fun, args) => {
				JsApply(getJsTree(fun), args.map(getJsTree))
			}
			*/
			
			case v @ ValDef(mods,name,tpt,rhs) => {
				//inspect(v)
				JsVar(name.toString, tpt.toString, (if (rhs.isEmpty) JsEmpty() else getJsTree(rhs)))
			}
			case b @ Block(stats,expr) => {
				//println(b)
				JsBlock( stats.map(getJsTree(_)) ::: List(getJsTree(expr)) )
			}
			
			// unit 
			case Literal(Constant(())) => JsVoid()
			
			case This(qual) => {
				JsThis()
			}
			
			case l @ Literal(Constant(value)) => {
				getLiteral(l)
			}
			
			case Ident(name) => JsIdent (name.toString)
			
			//case c @ ClassDef(_,_,_,_) => getClass(c)
			
			case If(cond, thenp, elsep) => JsIf(getJsTree(cond), getJsTree(thenp), getJsTree(elsep))
			
			case Super (qual, mix) => {
				JsSuper()
			}
			
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
		
		def processAST (tree:JsTree):JsTree = {
			tree match {
				case JsSelect(JsSelect(JsIdent(_), pkg, _), name, t) if pkg.toString == "package" => {
					
					JsVoid()
				}
				case x => visitAST(x, processAST)
			}
		}
		
		// method that can apply a function to all children of a Node
		def visitAST (tree:JsTree, fn:(JsTree)=>JsTree):JsTree = tree match {
			case JsSourceFile(path,name,classes) => JsSourceFile(
				path, 
				name, 
				classes map (fn(_).asInstanceOf[JsClass]) 
			)
			case JsClass(name,pkg,parents,constructor,properties,methods) => JsClass(
				name,
				pkg,
				parents map (fn(_).asInstanceOf[JsSelect]),
				fn(constructor).asInstanceOf[JsConstructor],
				properties map (fn(_).asInstanceOf[JsProperty]),
				methods map (fn(_).asInstanceOf[JsMethod])
			)
			case JsConstructor(name,params,constructorBody,classBody) => JsConstructor(
				name,
				params map (fn(_).asInstanceOf[JsParam]),
				constructorBody map fn,
				classBody map fn
			)
			case JsApply(fun,params) => JsApply(
				fn(fun),
				params map fn
			)
			case JsSelect(qualifier,name,t) => JsSelect(
				fn(qualifier),
				name,
				t
			)
			case JsMethod(name,params,children,ret) => JsMethod(
				name,
				params map (fn(_).asInstanceOf[JsParam]),
				children map fn,
				ret
			)
			case JsBlock(children) => JsBlock(
				children map fn	
			)
			case x:JsSuper => x
			case x:JsVoid => x
			case x:JsIdent => x
			case x:JsLiteral => x
			case x => {
				println(x)
				
				x
			}
		}
	
	}
}


