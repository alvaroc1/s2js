package com.gravitydev.s2js

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.plugins.PluginComponent
import scala.collection.mutable.ListBuffer

class S2JSComponent (val global:Global) extends PluginComponent {
	import global._
	import definitions.{ BooleanClass, IntClass, DoubleClass, StringClass, ObjectClass, UnitClass, AnyClass }
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
		"$amp$amp"		-> "&&",
		"$bar$bar"		-> "||"
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
			lazy val parsedUnit = getJsSourceFile(unit.body, name)
			
			val cleaned = clean( parsedUnit )
			
			val transformed = transform( cleaned )
			
			// print and save
			val code = JsPrinter print transformed
			
			println(code)
			
			println("======== BEFORE PROCESSING ======")
			println(JsAstPrinter print parsedUnit)
			println("======== AFTER CLEANING =========")
			println(JsAstPrinter print cleaned)
			println("======== AFTER TRANSFORMING =====")
			println(JsAstPrinter print transformed)
			
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
		
		def getConstructor (c:ClassDef, const:DefDef) = {
			//val params = const.vparamss.flatten map ((t) => JsParam(t.symbol.name.toString, getType(t.symbol)))
			/*
			val params = const.vparamss.flatten map ((t) => {
				JsParam(t.symbol.name.toString, getType(t.symbol))
			})
			*/
			
			val params = for (ValDef(mods, name, tpt, rhs) <- const.vparamss.flatten) yield {
				JsParam(name.toString, getType(tpt.symbol))
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
		
		def getMethod (method:DefDef) = {
			JsMethod(
				getType(method.symbol.owner),
				method.name.toString,
				for (ValDef(mods, name, tpt, rhs) <- method.vparamss.flatten) yield JsParam(name.toString, getType(tpt.symbol)),
				List(getJsTree(method.rhs)),
				getType(method.tpt.symbol)
			)
		}
		
		/** Get a symbol's type as a JsTree (JsSelect or JsBuiltInType) */
		def getType (typeTree:Symbol):JsTree = {
			import JsBuiltInType._
			
			typeTree match {
				case StringClass => JsBuiltInType(StringT)
				case AnyClass => JsBuiltInType(AnyT)
				case BooleanClass => JsBuiltInType(BooleanT)
				case IntClass|DoubleClass => JsBuiltInType(NumberT)
				
				// construct the JsSelect AST from the symbol
				// stop at the root
				case x if x.owner.name.toString == "<root>" => JsIdent(x.name.toString)
				case x => JsSelect( getType(x.owner), x.name.toString )
			}
		}
		
		def getProperty (c:ClassDef, prop:ValDef) = prop match {
			case ValDef(mods, name, tpt, rhs) => {
				val jsmods = JsModifiers(
					mods.isPrivate	
				)
				JsProperty(jsmods, name.toString, getType(tpt.symbol), getJsTree(rhs))
			}
		}
		
		def getJsSourceFile (p:Tree, name:String) = p match {
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
		
		def getJsTree (node:Tree):JsTree = node match {

			case c @ ClassDef(mods, name, tparams, Template(parents, self, body)) => {				
				val primary = body.collect({ case d:DefDef if d.symbol.isPrimaryConstructor => d }) head
				
				val params = primary.vparamss.flatten
				
				val properties = body.collect({ case x:ValDef if !x.symbol.isParamAccessor && !x.symbol.isParameter => x })
			
				val methods = body.collect({ case x:DefDef if !x.mods.isAccessor && !x.symbol.isConstructor => x })

				JsClass(
					getType(c.symbol.owner),
					c.symbol.name.toString,
					for (Select(qualifier, name) <- parents) yield JsSelect( getJsTree(qualifier), name.toString),
					getConstructor(c, primary),
					properties map (getProperty(c, _)),
					methods map getJsTree
				)
			}
			
			case m @ ModuleDef (mods, name, Template(parents, self, body)) => {
				JsModule(
					getType(m.symbol.owner), 
					name.toString, 
					body map getJsTree
				)
			}
			
			case m @ DefDef(mods, name, tparams, vparamss, tpt, rhs) => {
				JsMethod(
					getType(m.symbol.owner),
					name.toString,
					for (ValDef(mods, name, tpt, rhs) <- vparamss.flatten) yield JsParam(name.toString, getType(tpt.symbol)),
					List(getJsTree(rhs)),
					getType(tpt.symbol)
				)
			}

			// property re-assignment
			// TODO: move to transform
			case a @ Apply(fun @ Select(qualifier, name), args) if name.toString.endsWith("_$eq") => {
				
				val n = name.toString stripSuffix "_$eq"
			
				val select = JsSelect (
					getJsTree(qualifier), 
					// remove the suffix
					name.toString stripSuffix "_$eq"
				)
				
				JsAssign(select, getJsTree(args.head))
			}
			
			// package methods
			case Select(Select(Ident(_), name), _) if name.toString == "package" => {
				println("test")
				JsVoid()
			}
			
			/* do i need this one? probably not, we can collapse this in the transformations
			// new (instantiation)
			case Select( New( Select( q, name ) ), _ ) => {
				JsNew( JsSelect( getJsTree(q), name.toString ) )
			}
			*/
			//case New ( Select (q, name) ) => {
			case New ( select ) => {
				//JsNew( JsSelect( getJsTree(q), name.toString) )
				JsNew( getJsTree(select).asInstanceOf[JsSelect] )
			}
			
			// application (select)
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
			
			case v @ ValDef(mods,name,tpt,rhs) => {
				//inspect(v)
				JsVar(name.toString, getType(tpt.symbol), (if (rhs.isEmpty) JsEmpty() else getJsTree(rhs)))
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
			
			case Apply (select, args) => {
				
				JsApply(
					getJsTree(select), args map getJsTree
				)
			}
			
			case TypeApply(s, args) => {
				JsTypeApply(getJsTree(s), args map getJsTree)
			}
			
			case t @ TypeTree () => {
				getType(t.symbol)
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
		
		def clean (tree:JsTree):JsTree = {
			tree match {
				// collapse application of package methods
				case JsSelect(JsSelect(JsIdent(id), pkg, _), name, t) if pkg.toString == "package" => {
					JsSelect(JsIdent(id), name, t)
				}
				// remove extra select on instantiations
				case JsSelect(JsNew(tpe), name, t) => {
					JsNew( JsASTUtil.visitAST(tpe, clean).asInstanceOf[JsSelect] )
				}
				// collapse predefs selections
				case JsSelect(JsThis(), "Predef", _) => {
					JsPredef()
				}
				case x => JsASTUtil.visitAST(x, clean)
			}
		}
		
		def transform (tree:JsTree):JsTree = {
			tree match {
				// maps
				case JsApply( JsTypeApply( JsApply( JsSelect( JsSelect ( JsPredef(), "Map", _), "apply", _), _), List(JsBuiltInType(JsBuiltInType.StringT), _) ), args ) => {
					val argss = args
					JsMap(
						args.map((a) => {
							val m = a match {
								// whew!
								case JsApply( JsTypeApply( JsApply( JsSelect( JsApply( JsTypeApply( JsApply( JsSelect( JsPredef(), "any2ArrowAssoc", _), _), _), List(JsLiteral(key,_))), _, _), _), _), List(value)) => {
									Map("key"->key.stripPrefix("\"").stripSuffix("\""), "value"->value)
								}
							}
							JsMapElement(m.get("key").get.asInstanceOf[String], m.get("value").get.asInstanceOf[JsTree])
						})
					)
				}
				
				// println 
				case JsSelect(JsPredef(), "println", t) => {
					JsSelect(JsIdent("console"), "log", t)
				}
				
				// unary bang
				case JsApply( JsSelect(qualifier, "unary_$bang", t), _) => {
					JsUnaryOp(qualifier, "!")
				}
				
				// comparisons
				case JsApply( JsSelect(qualifier, name, _), args) if comparisonMap contains name.toString => {
					JsASTUtil.visitAST(
						JsComparison(
							qualifier,
							(comparisonMap get name.toString).get,
							args.head
						),
						transform
					)
				}
				
				// browser 
				case JsSelect( JsIdent("browser"), name, t) => {
					JsIdent(name)
				}
				
				// scala.Unit
				case JsSelect ( JsIdent("scala"), "Unit", t) => {
					JsVoid()
				}
				
				case x => JsASTUtil.visitAST(x, transform)
			}
		}
	
	}
}


