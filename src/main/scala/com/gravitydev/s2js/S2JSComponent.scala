package com.gravitydev.s2js

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.symtab.Symbols
import scala.tools.nsc.plugins.PluginComponent
import scala.collection.mutable.ListBuffer

class S2JSComponent (val global:Global, val plugin:S2JSPlugin) extends PluginComponent {
	import global._
	import definitions.{ BooleanClass, IntClass, DoubleClass, StringClass, ObjectClass, UnitClass, AnyClass }
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
			val dir = plugin.output + "/" + path
			
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
			
			//println("======== BEFORE PROCESSING ======")
			//println(JsAstPrinter print parsedUnit)
			//println("======== AFTER CLEANING =========")
			//println(JsAstPrinter print cleaned)
			//println("======== AFTER TRANSFORMING =====")
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
				println("isClass: " + s.isClass)
				println("isPackage: " + s.isPackage)
				println("isPackageObject: " + s.isPackageObject)
				println("isPackageObjectClass: " + s.isPackageObjectClass)
				println("isModule: " + s.isModule)
				println("isError: " + s.isError)
				println("isType: " + s.isType)
				println("-----------------------------")
				println("")
			}
		}
		
		def getConstructor (c:ClassDef, const:DefDef) = {
			JsConstructor(
				getType(const.symbol.owner),
				for (v @ ValDef(mods, name, tpt, rhs) <- const.vparamss.flatten) yield getParam(v),
				for (child <- const.rhs.children) yield getJsTree (child),
				
				// skip methods and param accessors (not needed in js)
				for (child <- c.impl.body if child != EmptyTree && !child.isInstanceOf[DefDef] && !child.symbol.isParamAccessor) yield getJsTree(child)
			)
		}
		
		def getParam (v:ValDef) = {
			JsParam(v.name.toString, getType(v.tpt.symbol), v.rhs match {
				case EmptyTree => None
				case p => Some(getJsTree(p))
			})
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
				case x => JsSelect( 
					getType(x.owner), 
					x.name.toString, 
					x match {
						case s:ModuleClassSymbol => JsSelectType.Module
						case s:ClassSymbol => JsSelectType.Class
						case s => JsSelectType.Other
					}
				)
			}
		}
		
		def getProperty (prop:ValDef) = {			
			JsProperty(
				getType(prop.symbol.owner),
				prop.name.toString,
				getType(prop.tpt.symbol),
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
			/*		
			prop match {
				// if it's a param accessor
				case v @ ValDef(mods, name, tpt, rhs) if v.symbol.isParamAccessor => {	
					JsProperty(getType(prop.symbol.owner), name.toString, getType(tpt.symbol), JsIdent(v.name.toString), jsmods)
				}
				// if rhs is a param accessor
				case v @ ValDef(mods, name, tpt, rhs @ Select(_,_)) if rhs.symbol.isParamAccessor => {
					val jsmods = JsModifiers(
						mods.isPrivate	
					)
					JsProperty(getType(prop.symbol.owner), name.toString, getType(tpt.symbol), JsIdent(rhs.name.toString), jsmods)
				}
				
				case v @ ValDef(mods, name, tpt, rhs) => {
					val jsmods = JsModifiers(
						mods.isPrivate	
					)
					JsProperty(getType(prop.symbol.owner), name.toString, getType(tpt.symbol), getJsTree(rhs), jsmods)
				}
			}
			*/
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
		
		def getJsTreeList[T <: JsTree] (l:List[Tree]):List[T] = l map (getJsTree(_).asInstanceOf[T])
		
		def getJsTree (node:Tree) : JsTree = node match {

			case c @ ClassDef(mods, name, tparams, Template(parents, self, body)) => {				
				val primary = body.collect({ case d:DefDef if d.symbol.isPrimaryConstructor => d }) head
				
				val params = primary.vparamss.flatten
				
				//val properties = body.collect({ case x:ValDef if !x.symbol.isParamAccessor && !x.symbol.isParameter => x })
							
				// get properties
				// all valdefs that have a corresponding accessor
				val accessorNames = body.collect({ case x:DefDef if x.mods.isAccessor => x.name.toString })
				val properties = body.collect({
					// don't know why but valdef's name has a space at the end
					// trim it
					case x:ValDef if accessorNames.contains(x.name.toString.trim) => x
				})
			
				val methods = body.collect({ case x:DefDef if !x.mods.isAccessor && !x.symbol.isConstructor => x })

				JsClass(
					getType(c.symbol.owner),
					c.symbol.name.toString,
					for (s @ Select(qualifier, name) <- parents) yield JsSelect( getJsTree(qualifier), name.toString, JsSelectType.Class ),
					getConstructor(c, primary),
					properties map getProperty,
					methods map (getJsTree(_).asInstanceOf[JsMethod])
				)
			}
			
			case m @ ModuleDef (mods, name, Template(parents, self, body)) => {				
				val properties = body.collect({ case x:ValDef if !x.symbol.isParamAccessor && !x.symbol.isParameter => x })
				val methods = body.collect({ case x:DefDef if !x.mods.isAccessor && !x.symbol.isConstructor => x })
				val classes = body.collect({ case x:ClassDef => x })
				val modules = body.collect({ case x:ModuleDef => x })
				
				JsModule(
					getType(m.symbol.owner),
					m.symbol.name.toString,
					properties map getProperty, 
					methods map (getJsTree(_).asInstanceOf[JsMethod]),
					classes map (getJsTree(_).asInstanceOf[JsClass]),
					modules map (getJsTree(_).asInstanceOf[JsModule])
				)
			}
			
			case m @ DefDef(mods, name, tparams, vparamss, tpt, rhs) => {
				JsMethod(
					getType(m.symbol.owner),
					name.toString,
					for (v @ ValDef(mods, name, tpt, rhs) <- vparamss.flatten) yield getParam(v),
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
			
			/* don't think we need this anymore
			// package methods
			case Select(Select(Ident(_), name), _) if name.toString == "package" => {
				println("test")
				JsVoid()
			}
			*/
			
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
			

			// toString on XML literal
			case Apply(Select(Block(_, Block(_, Apply(Select(New(tpt),_), args))), methodName), Nil) if methodName.toString == "toString" && tpt.toString == "scala.xml.Elem" => {
				val tag = args(1).toString.stripPrefix("\"").stripSuffix("\"")
				
				val x = getXml(tag)
				
				JsLiteral("\"" + x.toString + "\"", "string")
				//JsApply( JsSelect( getJsTree(qualifier), name.toString, JsSelectType.Method ), getJsTreeList(args))
			}
			
			// application (select)
			case Apply(Select(qualifier, name), args) => {
				val q = qualifier
				
				JsApply( JsSelect( getJsTree(qualifier), name.toString, JsSelectType.Method ), getJsTreeList(args))
			}
			
			// select (parent != Apply)
			case s @ Select(qualifier,name) => {
				s match {
					// if it's a local reference to a module, make absolute
					case s @ Select(t @ This(q), name) if s.symbol.isModule => {
						JsSelect(
							getType(t.symbol),
							name.toString,
							JsSelectType.Module 
						)
					}
					
					// if it's a method, and it wasn't caught on the previous case
					// it is a no-parameter-list application
					// wrap with apply
					case s if !s.symbol.isGetterOrSetter && s.symbol.isMethod => {
						JsApply( JsSelect( getJsTree(qualifier), name.toString, JsSelectType.Method), Nil)
					}
					case s => {
						JsSelect(
							getJsTree(qualifier), 
							name.toString,
							s match {
								case s if (s.symbol.isGetterOrSetter) => JsSelectType.Prop
								case s if (s.symbol.isParamAccessor) => JsSelectType.ParamAccessor
								case s if (s.symbol.isPackage) => JsSelectType.Package
								case s if (s.symbol.isClass || s.symbol.isType) => JsSelectType.Class
								case s if (s.symbol.isModule) => JsSelectType.Module 
								
								case s => {
									inspect (s.symbol)
									
									JsSelectType.Other
								}
							}
						)
					}
				}
				
			}
			
			case v @ ValDef(mods,name,tpt,rhs) => {
				//inspect(v)
				var t = getJsTree(rhs)
				
				JsVar(name.toString, getType(tpt.symbol), (if (rhs.isEmpty) JsEmpty() else getJsTree(rhs)))
			}
			case b @ Block(stats,expr) => {
				//println(b)
				JsBlock( getJsTreeList[JsTree](stats) ::: List(getJsTree(expr)) )
			}
			
			// unit 
			case Literal(Constant(())) => JsVoid()
			
			case This(qual) => {
				JsThis()
			}
			
			case l @ Literal(Constant(value)) => {
				getLiteral(l)
			}
			
			case i @ Ident(name) => {
				i.symbol match {
					// for objects, get the full path
					case s:ModuleSymbol => {
						getType(s)
					}
					// local variables (not objects or classes)
					case x => {
						JsIdent (name.toString)
					}
				}
			}
			
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
			
			case Throw (expr:Tree) => {
				JsThrow (getJsTree(expr))
			}
			
			case t @ TypeTree () => {
				getType(t.symbol)
			}
			
			// this code should share some code with DefDef
			case Function (vparams, body) => {
				JsFunction (
					for (v @ ValDef(mods, name, tpt, rhs) <- vparams) yield getParam(v),
					getJsTree(body) 
				) 
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
		
		def getXml (label:String) = new xml.Elem(null, label, xml.Null, xml.TopScope) 
		
		def getSuperClass (c:ClassDef):Option[String] = {
			val superClass = c.impl.parents.head
			if (superClass.toString == "java.lang.Object") None else Some(superClass.tpe.toString)
		}
		
		def clean (tree:JsTree):JsTree = {
			def visit[T <: JsTree] (t:JsTree):T = JsASTUtil.visitAST(t, clean).asInstanceOf[T]
			
			tree match {
				// collapse application of package methods
				// TODO: come up with better way to identify package objects that doesn't rely on strings, probably with symbol.isPackageObject
				case JsSelect(JsSelect( q, pkg, JsSelectType.Module), name, t) if pkg == "package" => visit {
					JsSelect(q, name, t)
				}
				// remove extra select on instantiations
				case JsSelect(JsNew(tpe), name, t) => visit {
					JsNew(tpe)
				}
				
				// remove default param methods
				case JsClass (owner, name, parents, constructor, properties, methods) => visit {
					JsClass (
						owner, 
						name, 
						parents, 
						constructor, 
						properties,
						methods.filter(!_.name.contains("$default$"))
					)
				}
				
				case JsModule (owner, name, properties, methods, classes, modules) => visit {
					JsModule (
						owner, 
						name, 
						properties, 
						methods.filter(!_.name.contains("$default$")), 
						classes, 
						modules
					)
				}
				
				// remove default invocations
				case JsApply (s, params) => visit {
					JsApply (
						s,
						params.filter((p) => {
							p match {
								case JsApply(JsSelect(_, name, _), params) if name contains "$default$" => false
								case x => true
							}
						})
					)
				}
				
				// collapse predefs selections
				case JsSelect(JsThis(), "Predef", _) => {
					JsPredef()
				}
				case JsSelect(JsIdent("scala"), "Predef", _) => {
					JsPredef()
				}
				case x => visit[JsTree](x)
			}
		}
		
		def transform (tree:JsTree):JsTree = {
			def visit[T <: JsTree] (t:JsTree):T = JsASTUtil.visitAST(t, transform).asInstanceOf[T]
			
			tree match {
				
				// maps
				case JsApply( JsTypeApply( JsApply( JsSelect( JsSelect ( JsPredef(), "Map", _), "apply", _), _), List(JsBuiltInType(JsBuiltInType.StringT), _) ), args ) => visit {
					val argss = args
					JsMap(
						args.map((a) => {
							val m = a match {
								// whew!
								case JsApply( JsTypeApply( JsApply( JsSelect( JsApply( JsTypeApply( JsApply( JsSelect( JsPredef(), "any2ArrowAssoc", _), _), _), List(JsLiteral(key,_))), _, _), _), _), List(value)) => {
									Map("key"->key.stripPrefix("\"").stripSuffix("\""), "value"->value)
								}
							}
							JsMapElement(
								m.get("key").get.asInstanceOf[String], 
								m.get("value").get.asInstanceOf[JsTree]
							)
						})
					)
				}
				
				// println 
				case JsSelect(JsPredef(), "println", t) => visit {
					JsSelect(JsIdent("console"), "log", t)
				}
				
				// unary bang
				case JsApply( JsSelect(qualifier, "unary_$bang", t), _) => visit {
					JsUnaryOp(qualifier, "!")
				}
				
				// plain exception
				case JsThrow( JsApply( JsNew( JsSelect( JsSelect( JsIdent("scala"), "package",_ ), "Exception", _) ), params) ) => visit {
					JsThrow( JsApply( JsNew(JsIdent("Error")), params) )
				}
				
				// comparisons
				case JsApply( JsSelect(qualifier, name, _), args) if comparisonMap contains name.toString => visit {
					JsComparison(
						qualifier,
						(comparisonMap get name.toString).get,
						args.head
					)
				}
				
				// browser 
				case JsSelect( JsIdent("browser"), name, t) => visit {
					JsIdent(name)
				}
				
				// scala.Unit
				case JsSelect ( JsIdent("scala"), "Unit", t) => visit {
					JsVoid()
				}
				
				// toString on XML literal
				case JsApply ( JsSelect(_, "toString", JsSelectType.Method ), Nil ) => {
					JsVoid()
				}
				
				case x => visit[JsTree]{x}
			}
		}
	
	}
}


