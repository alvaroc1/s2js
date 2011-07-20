package com.gravitydev.s2js

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.symtab.Symbols
import scala.tools.nsc.plugins.PluginComponent
import scala.collection.mutable.ListBuffer
import StringUtil._
import S2JSComponent._
import JsAstProcessor._

class S2JSComponent (val global:Global, val plugin:S2JSPlugin) extends PluginComponent {
	import global._
	import definitions.{ BooleanClass, IntClass, DoubleClass, StringClass, ObjectClass, UnitClass, AnyClass, FunctionClass }
	import treeInfo.{ isSuperConstrCall }
	
	//val runsAfter = List[String]("refchecks")
	val runsAfter = List[String]("typer")
	
	val phaseName = "s2js"
		
	val outputDir = plugin.output 
		
	def collect [T <: Tree] (tree: Tree)(pf: PartialFunction[Tree, T]): List[T] = {
		val lb = new ListBuffer[T]
		tree foreach (t => if (pf.isDefinedAt(t)) lb += pf(t))
		lb.toList
	}
		
	def newPhase (prev:Phase) = new StdPhase(prev) {
		
		val buffer = new StringBuffer
		
		override def name = phaseName
		
		override def apply (unit: CompilationUnit) {
			import java.io._
		
			// S2JSComponent.this.global.treeBrowser.browse(unit.body)
			
			// output paths
			val path = unit.body.symbol.fullName.replace('.', '/')
			val name = unit.source.file.name.stripSuffix(".scala").toLowerCase
			val dir = plugin.output + "/" + path
			
			// create the directories
			new File(dir).mkdirs
			
			// get the package
			var pkg = ""
			unit.body match {
				case PackageDef(pid,_) => pkg = pid.toString
			}
			
			// transform to Js AST
			lazy val parsedUnit = getJsSourceFile(unit.body.asInstanceOf[PackageDef], name)
			
			val processed = JsAstProcessor process parsedUnit
			
			// print and save
			val code = JsPrinter print processed
			println(code)
			
			//println("======== BEFORE PROCESSING ======")
			//println(JsAstPrinter print parsedUnit)
			//println("======== AFTER CLEANING =========")
			//println(JsAstPrinter print cleaned)
			println("======== AFTER TRANSFORMING =====")
			println(JsAstPrinter print processed)
			
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
				println("isGetter: " + s.isGetter)
				println("isSetter: " + s.isSetter)
				println("isMethod: " + s.isMethod)
				println("isMutable: " + s.isMutable)
				println("isParamAccessor: " + s.isParamAccessor)
				println("isParameter: " + s.isParameter)
				println("isSourceMethod: " + s.isSourceMethod)
				println("isSuperAccessor: " + s.isSuperAccessor)
				println("isSynthetic: " + s.isSynthetic)
				println("isLocal: " + s.isLocal)
				println("isClass: " + s.isClass)
				println("isPackage: " + s.isPackage)
				println("isPackageObject: " + s.isPackageObject)
				println("isPackageObjectClass: " + s.isPackageObjectClass)
				println("isModule: " + s.isModule)
				println("isError: " + s.isError)
				println("isType: " + s.isType)
				println("isModuleClass: " + s.isModuleClass)
				println("-----------------------------")
				println("")
			}
		}
		
		def getConstructor (c:ClassDef, const:DefDef) = {
			
			val body = for (child <- c.impl.body if child != EmptyTree && !child.isInstanceOf[DefDef] && !child.symbol.isParamAccessor) yield getJsTree(child)
			
			JsConstructor(
				getJsRef(const.symbol.owner.tpe),
				for (v @ ValDef(mods, name, tpt, rhs) <- const.vparamss.flatten) yield getParam(v),
				for (child <- const.rhs.children) yield getJsTree (child),
				
				// skip methods and param accessors (not needed in js)
				for (child <- c.impl.body if child != EmptyTree && !child.isInstanceOf[DefDef] && !child.symbol.isParamAccessor) yield getJsTree(child)
			)
		}
		
		def getParam (v:ValDef) = {
			JsParam(v.name.toString, getJsType(v.tpt.tpe), v.rhs match {
				case EmptyTree => None
				case p => Some(getJsTree(p))
			})
		}
		
		/** 
		 * Get a symbol's type as a JsTree (JsSelect or JsBuiltInType) 
		 */
		@deprecated("Try to use getJsType or getJsRef", "A while ago") 
		def getType (typeTree:Symbol):JsTree = {
			typeTree match {
				case StringClass => JsType.StringT
				case AnyClass => JsType.AnyT
				case BooleanClass => JsType.BooleanT
				case IntClass|DoubleClass => JsType.NumberT
				
				// replace Map with Object
				case x if x.fullName == "scala.collection.immutable.Map" => {
					JsIdent("Object")
				}
				
				// construct the JsSelect AST from the symbol
				// stop at the root
				case x if x.owner.name.toString == "<root>" => {
					JsIdent(x.name.toString, JsType.PackageT)
				}
				
				case x => JsSelect( 
					getType(x.owner), 
					x.name.toString, 
					x match {
						case s:ModuleClassSymbol => JsSelectType.Module
						case s:ClassSymbol => JsSelectType.Class
						case s => JsSelectType.Other
					},
					JsType.UnknownT
				)
			}
		}
		
		def getJsType (tpe:Type):JsType = {			
			tpe.typeSymbol match {
				case IntClass|DoubleClass => JsType.NumberT
				case StringClass => JsType.StringT
				case AnyClass => JsType.AnyT
				case BooleanClass => JsType.BooleanT
				case x if FunctionClass.contains(x) => JsType.FunctionT
				case x if x.isModuleClass => JsType(x.asInstanceOf[ModuleClassSymbol].fullName)
				
				case _ => tpe.typeConstructor.toString match {
					case func if func.startsWith("Function") => JsType.FunctionT
					case "List"|"Array" => JsType.ArrayT
					case "browser.Object" => JsType.ObjectT
					
					// lame, maybe we don't need this
					case _ => JsType(tpe.toString)
				}
			}
		}
		
		def getJsRef (tpe:Type):JsRef = {			
			tpe.typeSymbol match {
				case x if x.isModuleClass => JsModuleRef(x.asInstanceOf[ModuleClassSymbol].fullName)
				case x if x.isClass => JsClassRef(x.asInstanceOf[ClassSymbol].fullName)
				case x if x.isPackage => JsPackageRef(x.fullName)
			}
		}
		
		def getJsRef (sym:Symbol):JsRef = {
			
			sym match {
				case x if x.isModuleClass => JsModuleRef(x.asInstanceOf[ModuleClassSymbol].fullName)
				case x if x.isClass => JsClassRef(x.asInstanceOf[ClassSymbol].fullName)
				case x if x.isPackage => JsPackageRef(x.nameString)
				case x if x.isMethod => JsMethodRef(x.fullName)
				case x => JsUnknownRef(x.fullName)
			}
		}
		
		def getProperty (prop:ValDef) = {			
			JsProperty(
				getType(prop.symbol.owner),
				prop.name.toString,
				getJsType(prop.tpt.tpe),
				prop match {
					// if it's a param accessor
					case v if v.symbol.isParamAccessor => JsIdent(v.name.toString)
					// if rhs is a param accessor
					case ValDef(_,_,_,rhs @ Select(_,_)) if rhs.symbol.isParamAccessor => JsIdent(rhs.name.toString)
					// other wise
					case v => getJsTree(v.rhs)
				},
				JsModifiers(
					prop.mods.isPrivate	
				)
			)
		}
		
		def getJsSourceFile (p:PackageDef, name:String) = {
			val path = p.symbol.fullName.replace('.', '/')
			
			JsSourceFile(
				path,
				name,
				List(getJsTree(p).asInstanceOf[JsPackage])
			)
		}
		
		/* p match {
			case PackageDef(pid, stats) => {
				
				val path = p.symbol.fullName.replace('.', '/')
				
				JsSourceFile(path, name, 
					stats.map(_ match {
						// discard imports
						case t:Import => null
						
						case x => getJsTree(x)
						
					}).filter(_ != null)
				)
			}
		}
		*/
		
		def getJsTreeList[T <: JsTree] (l:List[Tree]):List[T] = l map (getJsTree(_).asInstanceOf[T])
		
		def getMethods (body:List[AnyRef]) = body.collect({ case x:DefDef if !x.symbol.isGetter && !x.symbol.isSetter && !x.symbol.isConstructor => x })
		
		def getJsTree (node:Tree) : JsTree = node match {

			case c @ ClassDef(mods, name, tparams, Template(parents, self, body)) => {				
				val primary = body.collect({ case d:DefDef if d.symbol.isPrimaryConstructor => d }) head
				
				val params = primary.vparamss.flatten
				
				//val properties = body.collect({ case x:ValDef if !x.symbol.isParamAccessor && !x.symbol.isParameter => x })
							
				// get properties
				// all valdefs that have a corresponding accessor
				val accessorNames = body.collect({ case x:DefDef if x.symbol.isGetter => x.name.toString })
				val properties = body.collect({
					// don't know why but valdef's name has a space at the end
					// trim it
					case x:ValDef if accessorNames.contains(x.name.toString.trim) => x
				})
			
				val methods = getMethods(body)

				JsClass(
					getJsRef(c.symbol.owner.tpe),
					c.symbol.name.toString,
					for (s @ Select(qualifier, name) <- parents) yield JsSelect( getJsTree(qualifier), name.toString, JsSelectType.Class, JsType.UnknownT ),
					getConstructor(c, primary),
					properties map getProperty,
					methods map (getJsTree(_).asInstanceOf[JsMethod])
				)
			}
			
			case m @ ModuleDef (mods, name, Template(parents, self, body)) => {
				val bodyContents = body.groupBy(_ match {
					case x:ValDef if !x.symbol.isParamAccessor && !x.symbol.isParameter => "properties"
					case x:DefDef if /*!x.mods.isAccessor*/ !x.mods.isSynthetic && !x.symbol.isConstructor => "methods"
					case x:ClassDef => "classes"
					case x:ModuleDef => "modules"
					case _ => "expressions"
				})
				
				val methods = getMethods(body)
				
				body.foreach(inspect)
				
				val properties = bodyContents.get("properties").getOrElse(Nil).map(_.asInstanceOf[ValDef])
				val classes = bodyContents.get("classes").getOrElse(Nil).map(_.asInstanceOf[ClassDef])
				val modules = bodyContents.get("modules").getOrElse(Nil).map(_.asInstanceOf[ModuleDef])
				
				// get the expressions in the body of the <init> method, remove the constructor
				val expressions = bodyContents.get("expressions").get.filter({
					case x:DefDef if x.mods.isSynthetic => false
					case x:DefDef if x.name.toString == "<init>" => false
					case x if x.symbol.isGetter => false
					case _ => true
				})
				
				JsModule(
					getJsRef(m.symbol.owner.tpe),
					m.symbol.name.toString,
					expressions map getJsTree,
					properties map getProperty, 
					methods map (getJsTree(_).asInstanceOf[JsMethod]),
					classes map (getJsTree(_).asInstanceOf[JsClass]),
					modules map (getJsTree(_).asInstanceOf[JsModule])
				)
			}
			
			case m @ DefDef(mods, name, tparams, vparamss, tpt, rhs) => {			
				JsMethod(
					getJsRef(m.symbol.owner),
					name.toString,
					for (v @ ValDef(mods, name, tpt, rhs) <- vparamss.flatten) yield getParam(v),
					// add return if not void
					tpt.toString match { // don't know how to match Unit without toString
						case "Unit" => getJsTree(rhs)
						case x => addReturn(getJsTree(rhs))
					},
					getJsType(tpt.tpe)
				)
			}

			// property re-assignment
			// TODO: move to transform
			case a @ Apply(fun @ Select(qualifier, name), args) if name.toString.endsWith("_$eq") => {
				
				val n = name.toString stripSuffix "_$eq"
			
				val select = JsSelect (
					getJsTree(qualifier), 
					// remove the suffix
					name.toString stripSuffix "_$eq",
					JsSelectType.Other,
					JsType.UnknownT
				)
				
				JsAssign(select, getJsTree(args.head))
			}
			
			case Assign (lhs, rhs) => {
				JsAssign(getJsTree(lhs), getJsTree(rhs))
			}

			case New ( select ) => {
				JsNew( getJsRef(select.tpe) )
			}
			
			/*
			// application on super
			case Apply(Select(qualifier @ Super(qual, mix), name), args) => {
				JsApply( JsSelect( JsSuper(getType(qualifier.symbol)), name.toString, JsSelectType.Method ), getJsTreeList(args))
			}
			*/
			
			// application (select)
			case Apply(fun @ Select(qualifier, name), args) => {
				JsApply( JsSelect( getJsTree(qualifier), name.toString, JsSelectType.Method, getJsType(fun.tpe.resultType) ), getJsTreeList(args))
			}
			
			// select (parent != Apply)
			case s @ Select(qualifier,name) => {
				s match {
					// if it's a local reference to a module, make absolute
					case s @ Select(t @ This(q), name) if s.symbol.isModule => {
						JsSelect(
							getType(t.symbol),
							name.toString,
							JsSelectType.Module,
							JsType.UnknownT
						)
					}
					
					// if it's a method, and it wasn't caught on the previous case
					// it is a no-parameter-list application
					// wrap with apply
					case s if !s.symbol.isGetter && s.symbol.isMethod => {
						val t = getJsType(s.tpe)
						JsApply( JsSelect( getJsTree(qualifier), name.toString, JsSelectType.Method, getJsType(s.tpe)), Nil)
					}
					case s => {
						JsSelect(
							getJsTree(qualifier), 
							name.toString,
							s match {
								case s if s.symbol.isGetter => JsSelectType.Prop
								case s if s.symbol.isParamAccessor => JsSelectType.ParamAccessor
								case s if s.symbol.isPackage => JsSelectType.Package
								case s if (s.symbol.isClass || s.symbol.isType) => JsSelectType.Class
								case s if s.symbol.isModule => JsSelectType.Module
								case s if s.symbol.isMethod => JsSelectType.Method
								
								case s => {
									inspect (s.symbol)
									
									JsSelectType.Other
								}
							},
							JsType.UnknownT
						)
					}
				}
				
			}
			
			case v @ ValDef(mods,name,tpt,rhs) => {
				//inspect(v)
				var t = getJsTree(rhs)
				
				JsVar(name.toString, getJsType(tpt.tpe), (if (rhs.isEmpty) JsEmpty() else getJsTree(rhs)))
			}
			case b @ Block(stats,expr) => {
				//println(b)
				JsBlock( 
					getJsTreeList[JsTree](
						// remove imports
						stats.filterNot(_.isInstanceOf[Import])
					), 
					getJsTree(expr) 
				)
			}
			
			// Unit 
			case Literal(Constant(())) => JsType.VoidT
			
			case This(qual) => {
				JsThis()
			}
			
			case l @ Literal(Constant(value)) => {
				getLiteral(l)
			}
			
			case i @ Ident(name) => {
				i.symbol match {
					// for objects, get the full path
					case s:ModuleSymbol => getType(s)

					// local variables (not objects or classes)
					case s:TermSymbol => JsIdent(name.toString, getJsType(s.tpe))
					// local variables (not objects or classes)
					/*
					case x => {
						val tpe = x.tpe.typeConstructor
						val s = x.alias
				
						JsIdent (name.toString, tpe.toString)
					}
					*/
					case x => JsEmpty()
				}
			}
			
			//case c @ ClassDef(_,_,_,_) => getClass(c)
			
			case If(cond, thenp, elsep) => {
				JsIf(getJsTree(cond), getJsTree(thenp), getJsTree(elsep))
			}
			
			case Super (qual, mix) => {
				JsSuper(getType(node.symbol).asInstanceOf[JsSelect])
			}
			
			case Apply (select, args) => {
				
				JsApply(
					getJsTree(select), args map getJsTree
				)
			}
			
			// handle asInstanceOf casts
			case TypeApply(Select(qualifier, name), args) if name.toString == "asInstanceOf" => {
				JsCast(getJsTree(qualifier), getType(args(0).symbol))
			}
			
			case TypeApply(s, args) => {
				JsTypeApply(getJsTree(s), args map getJsTree)
			}
			
			case Throw (expr:Tree) => {
				JsThrow (getJsTree(expr))
			}
			
			case t @ TypeTree () => {
				getType(t.symbol)
			}
			
			// this code should share some code with DefDef
			case f @ Function (vparams, body) => {
				// is there a better way to get a function's return type?
				val tpe = getType(body.tpe.typeSymbol)
				JsFunction (
					for (v @ ValDef(mods, name, tpt, rhs) <- vparams) yield getParam(v),
					tpe match {
						case JsType.VoidT => getJsTree(body) 
						case _ => addReturn(getJsTree(body))
					}
				) 
			}
			
			case Return (expr) => {
				JsReturn (getJsTree(expr))
			}
			
			case PackageDef (pid, stats) => {
				JsPackage(
					pid.toString, 
					getJsTreeList(stats.filterNot(_.isInstanceOf[Import])) // remove imports
				)
			}
			
			case EmptyTree => {
				JsEmpty()
			}
			
			case t @ Typed (expr, tpt) => {
				JsBlock(Nil, getJsTree(expr))
			}
			
			case t:Tree => {
				println(t)
				JsOther(t.getClass.toString, for (child <- t.children) yield getJsTree(child))
			}
		}
		
		def getLiteral (node:Tree):JsLiteral = {
			node match {
				case Literal(Constant(value)) => value match {
					case v:String => JsLiteral("\""+v.toString.replace("\"", "\\\"")+"\"", JsType.StringT)
					case _ => JsLiteral( if (value != null) value.toString else "null", JsType.NullT )
				}
				case a @ _ => {
					JsLiteral("", JsType.UnknownT)
				}
			}
		}
		
		def getSuperClass (c:ClassDef):Option[String] = {
			val superClass = c.impl.parents.head
			if (superClass.toString == "java.lang.Object") None else Some(superClass.tpe.toString)
		}
		
		def addReturn (tree:JsTree):JsTree = tree match {
			case JsIf(cond, thenExpr, elseExpr) => JsIf(cond, addReturn(thenExpr), addReturn(elseExpr))
			case JsBlock(stats, expr) => JsBlock(stats, addReturn(expr))
			case JsType.VoidT => JsType.VoidT
			case x => JsReturn(tree)
		}
		
	}
	
}

object S2JSComponent {
	
	// TODO figure out how to do parenthesized comparisons
	val comparisonMap = Map(
		"$eq$eq"		-> "==",
		"$bang$eq" 		-> "!=",
		"$greater"		-> ">",
		"$greater$eq" 	-> ">=",
		"$less"			-> "<",
		"$less$eq"		-> "<=",
		"$amp$amp"		-> "&&",
		"$bar$bar"		-> "||"
	)
	
	// TODO: comparison operators should brobably be here
	val infixOperatorMap = Map(
		"$plus" -> "+",
		"$minus" -> "-"
	)
}
