package com.gravitydev.s2js

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.symtab.Symbols
import scala.tools.nsc.plugins.PluginComponent
import scala.collection.mutable.ListBuffer
import StringUtil._

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
	
	// TODO: comparison operators should brobably be here
	val infixOperatorMap = Map(
		"$plus" -> "+",
		"$minus" -> "-"
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
			lazy val parsedUnit = getJsSourceFile(unit.body.asInstanceOf[PackageDef], name)
			
			val cleaned = clean( parsedUnit )
			
			val transformed = transform( cleaned ) 
			
			// print and save
			val code = JsPrinter print transformed
			//println(code)
			
			//println("======== BEFORE PROCESSING ======")
			//println(JsAstPrinter print parsedUnit)
			//println("======== AFTER CLEANING =========")
			//println(JsAstPrinter print cleaned)
			//println("======== AFTER TRANSFORMING =====")
			//println(JsAstPrinter print transformed)
			
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
				
				// replace Map with Object
				case x if x.fullName == "scala.collection.immutable.Map" => {
					JsIdent("Object")
				}
				
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
				val tpe = getType(tpt.symbol)
				
				JsMethod(
					getType(m.symbol.owner),
					name.toString,
					for (v @ ValDef(mods, name, tpt, rhs) <- vparamss.flatten) yield getParam(v),
					// add return if not void
					tpt.toString match { // don't know how to match Unit without toString
						case "Unit" => getJsTree(rhs)
						case x => addReturn(getJsTree(rhs))
					},
					tpe
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
				JsNew( getJsTree(select) )
			}
			

			// toString on XML literal
			case Apply(Select(b @ Block(_, Block(_, elemConst @ Apply(Select(New(tpt),_), args))), methodName), Nil) if methodName.toString == "toString" && tpt.toString == "scala.xml.Elem" => {
				val x = getXml(b)
				
				val pretty = xml.Utility.toXML(x, minimizeTags=true)
				
				JsLiteral("'" + pretty + "'", "string")
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
				JsBlock( getJsTreeList[JsTree](stats), getJsTree(expr) )
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
			case f @ Function (vparams, body) => {
				// is there a better way to get a function's return type?
				val tpe = getType(body.tpe.typeSymbol)
				JsFunction (
					for (v @ ValDef(mods, name, tpt, rhs) <- vparams) yield getParam(v),
					tpe match {
						case JsVoid() => getJsTree(body) 
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
		
		def getXml (tree:Tree, attributes:Map[String,String]=Map()):xml.Node = tree match {
			case Apply(Select(New(tpt),_), args) => tpt match {
				case tpt if tpt.toString == "scala.xml.Elem" => 
					val tag = args(1).toString.stripPrefix("\"").stripSuffix("\"")
					
					var prev:xml.MetaData = xml.Null
					attributes.foreach((t) => {
						val a = new xml.UnprefixedAttribute(t._1, t._2, prev)
						prev = a
					})
					
					val children = if (args.length > 4) {
						val Typed(Block(stats, expr), tpt) = args(4)
						
						// get children from buffer
						for (a @ Apply(fun, List(node)) <- stats) yield getXml(node)
					} else {
						Nil
					}
					
					new xml.Elem(null, tag, prev, xml.TopScope, children : _*)
					
				case tpt if tpt.toString == "scala.xml.Text" => {
					
					new xml.Text(
						stripQuotes(args(0).toString)
							.replace("""\012""", """\n""") // new line
							.replace("""\011""", """\t""") // tab
					)
				}
			}
			case Block(_, inner @ Block(stats, a @ Apply(_,_))) => {
				// get attributes
				val attributes = (for (Assign(_, Apply(fun, List(name, Apply(_, List(value)), _))) <- stats) 
					yield (stripQuotes(name.toString), stripQuotes(value.toString))).toMap
				
				getXml(a, attributes)
			}
		}
		
		def getSuperClass (c:ClassDef):Option[String] = {
			val superClass = c.impl.parents.head
			if (superClass.toString == "java.lang.Object") None else Some(superClass.tpe.toString)
		}
		
		def clean (tree:JsTree):JsTree = {
			def visit[T <: JsTree] (t:JsTree):T = JsAstUtil.visitAst(t, clean).asInstanceOf[T]
			
			tree match {
				// remove packages
				case JsSourceFile (path, name, packages) => visit {
					def flatten (tree:JsTree) : List[JsTree] = tree match {
						case JsPackage(name, children) => {
							children flatMap flatten
						}
						case x => {
							List(x)
						}
					}
					
					JsSourceFile(
						path,
						name,
						packages flatMap flatten
					)
				}
				
				// collapse application of package methods
				// TODO: come up with better way to identify package objects that doesn't rely on strings, probably with symbol.isPackageObject
				case JsSelect(JsSelect( q, "package", JsSelectType.Module), name, t) => visit {
					JsSelect(q, name, t)
				}
				// collapse definition of package methods
				case JsSelect(JsSelect(q, name, t), "package", JsSelectType.Module) => visit [JsTree] {
					JsSelect(q, name, t)
				}
				case JsModule(JsSelect(q, name, JsSelectType.Module), "package", props, methods, classes, modules) => visit {
					JsModule(q, name, props, methods, classes, modules)
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
				
				case JsModule (owner, name, properties, methods, classes, modules) => visit [JsTree] {
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
			def visit[T <: JsTree] (t:JsTree):T = JsAstUtil.visitAst(t, transform).asInstanceOf[T]
			
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
				
				// lists
				case JsApply ( 
						JsTypeApply( 
							JsApply( 
								JsSelect( 
									JsSelect( 
										JsSelect(
											JsSelect(JsIdent("scala"), "collection", JsSelectType.Module), 
											"immutable", 
											JsSelectType.Module
										), 
										"List", 
										JsSelectType.Module
									), 
									"apply", 
									JsSelectType.Method
								), 
								_
							), 
							_
						), 
						params
					) => {
					JsArray(params)
				}
				
				// remove remaining type applications
				case JsApply ( JsTypeApply( JsApply(fun, _), _), args ) => visit [JsTree] { // not sure why i need to specify JsTree here, cast exception otherwise
					JsApply( fun, args )
				}
				
				// println 
				case JsSelect(JsPredef(), "println", t) => visit {
					JsSelect(JsIdent("console"), "log", t)
				}
				
				// mkString on List
				// TODO: anything for right now, will narrow down to list later (not sure how yet)
				case JsApply(JsSelect(q, "mkString", t), List(glue)) => visit {
					JsApply(JsSelect(q, "join", t), List(glue))
				}
				
				
				// unary bang
				case JsApply( JsSelect(qualifier, "unary_$bang", t), _) => visit {
					JsUnaryOp(qualifier, "!")
				}
				
				// infix ops
				case JsApply( JsSelect(q, name, t), args) if infixOperatorMap contains name => visit [JsTree] {
					JsInfixOp(q, args(0), infixOperatorMap.get(name).get)
				}
				
				// plain exception
				case JsThrow( JsApply( JsNew( JsSelect( JsSelect( JsIdent("scala"), "package",_ ), "Exception", _) ), params) ) => visit {
					JsThrow( JsApply( JsNew(JsIdent("Error")), params) )
				}
				
				// comparisons
				case JsApply( JsSelect(qualifier, name, _), args) if comparisonMap contains name.toString => visit [JsTree] {
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
		
		def addReturn (tree:JsTree):JsTree = tree match {
			case JsIf(cond, thenExpr, elseExpr) => JsIf(cond, addReturn(thenExpr), addReturn(elseExpr))
			case JsBlock(stats, expr) => JsBlock(stats, addReturn(expr))
			case JsVoid() => JsVoid()
			case x => JsReturn(tree)
		}
		
	}
	
}


