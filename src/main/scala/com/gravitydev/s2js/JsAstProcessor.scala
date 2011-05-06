package com.gravitydev.s2js

import S2JSComponent._

object JsAstProcessor {
	def process (tree:JsTree):JsTree = 
 		transformTernaries (
		transform (
			clean (
				removeTypeApplications(
					transformMapsAndLists(
						prepare(tree)
					)
				)
			)
		)
		)
		
	// this must be performed before anything else
	def prepare (tree:JsTree):JsTree = {
		def visit[T <: JsTree] (t:JsTree):T = JsAstUtil.visitAst(t, prepare).asInstanceOf[T]
		
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
			case JsSelect(JsSelect( q, "package", JsSelectType.Module, _), name, t, tpe) => visit {
				JsSelect(q, name, t, tpe)
			}
			// collapse definition of package methods
			case JsSelect(JsSelect(q, name, t, tpe), "package", JsSelectType.Module, _) => visit [JsTree] {
				JsSelect(q, name, t, tpe)
			}
			// ref stuff
			//case JsModule(JsSelect(q, name, JsSelectType.Module, _), "package", props, methods, classes, modules) => visit {
			case JsModule(mr @ JsModuleRef(name), "package", body, props, methods, classes, modules) => visit {
				JsModule(mr, name, body, props, methods, classes, modules)
			}
			
			// remove extra select on instantiations
			case JsSelect(JsNew(tpe), name, t, _) => visit {
				JsNew(tpe)
			}
			
			// ===  collapse predefs selections ===
			case JsSelect(JsThis(), "Predef", _, _) => {
				JsPredef()
			}
			case JsSelect(JsIdent("scala",_), "Predef", _, _) => {
				JsPredef()
			}
			
			case x => visit[JsTree](x)
		}
			
	}
	
	// must be done before removing type applications
	def transformMapsAndLists (tree:JsTree):JsTree = {
		def visit[T <: JsTree] (t:JsTree):T = JsAstUtil.visitAst(t, transformMapsAndLists).asInstanceOf[T]
		
		tree match {
			
			/*
			 * Turn:
			 *   Object("test" -> 1, "blah" -> "something")
			 * Into:
			 *   {"test":1, "blah":"something"}
			 */
			case JsApply(
					JsSelect (
						JsSelect(
							JsIdent("browser", _),
							"Object",
							JsSelectType.Module,
							JsType(_,_)
						),
						"apply",
						JsSelectType.Method,
						returnType
					),
					params
				) => {
					JsMap(
						params.collect({
							case JsApply( JsTypeApply( JsApply( JsSelect( JsApply( JsTypeApply( JsApply( JsSelect( JsPredef(), "any2ArrowAssoc", _, _), _), _), List(JsLiteral(key,_))), _, _, _), _), _), List(value)) => {
								JsMapElement(key.stripPrefix("\"").stripSuffix("\""), value)
							}
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
										JsSelect(JsIdent("scala",_), "collection", JsSelectType.Module, _), 
										"immutable", 
										JsSelectType.Module,
										_
									), 
									"List", 
									JsSelectType.Module,
									_
								), 
								"apply", 
								JsSelectType.Method,
								_
							), 
							_
						), 
						_
					), 
					params
				) => {
				JsArray(params)
			}
							
			case x => visit[JsTree]{x}
		}
	}
	
	// this must be performed before removing default params invocation
	def removeTypeApplications (tree:JsTree):JsTree = {
		def visit[T <: JsTree] (t:JsTree):T = JsAstUtil.visitAst(t, removeTypeApplications).asInstanceOf[T]
		
		tree match {
			// strange setup here: the inner apply's selector goes with the outer apply's paras
			case JsApply(JsTypeApply (JsApply(s, _), _), params) => visit {
				JsApply(s, params)
			}
			// remove applications in params, which are not wrapped by an apply
			case JsTypeApply (fun, _) => visit {
				fun
			}
			case x => visit[JsTree](x)
		}
	}
	
	def transformTernaries (tree:JsTree):JsTree = {
		def visit[T <: JsTree] (t:JsTree):T = JsAstUtil.visitAst(t, transformTernaries).asInstanceOf[T]
		
		def jsIfToTernary (jsif:JsIf) = JsTernary(jsif.cond, jsif.thenp, jsif.elsep)
		
		tree match {
						
			// ternary
			// TODO: there are a lot more, should probably do this with any function application
			case JsVar (id, tpe, i @ JsIf(_,_,_)) => visit {
				JsVar(id, tpe, jsIfToTernary(i))
			}
			case JsAssign (lhs, i @ JsIf(_,_,_)) => visit {
				JsAssign (lhs, jsIfToTernary(i))
			}
			case a @ JsApply (fun, params) => visit {
				JsApply(fun, params.map(_ match {
					case i:JsIf => jsIfToTernary(i)
					case x => x
				}))
			}
			
			case x => visit[JsTree](x)
		}
	}
	
	def clean (tree:JsTree):JsTree = {
		def visit[T <: JsTree] (t:JsTree):T = JsAstUtil.visitAst(t, clean).asInstanceOf[T]
		
		tree match {				
	
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
			case JsModule (owner, name, body, properties, methods, classes, modules) => visit [JsTree] {
				JsModule (
					owner, 
					name, 
					body,
					properties, 
					methods.filter(!_.name.contains("$default$")), 
					classes, 
					modules
				)
			}
			case JsBlock (stats, expr) => visit [JsTree] {
				JsBlock(
					stats.filterNot((m) => m.isInstanceOf[JsMethod] && m.asInstanceOf[JsMethod].name.contains("$default$")),
					expr
				)
			}
			
			/*
			case a @ JsApply(JsSelect(a2 @ JsApply(_,_),_,_,JsType.NumberT),_) if true => {
				println(a2)
				a
			}
			*/
			
			case JsType("Unit", List()) => JsType.VoidT
			
			// turn method String.length() into property
			case JsApply(JsSelect(l @ JsLiteral(value, JsType.StringT), "length", JsSelectType.Method, _), _) => visit {
				JsSelect(l, "length", JsSelectType.Prop, JsType.NumberT)
			}
			case JsApply(JsSelect(JsApply(s @ JsSelect(_,_,_, JsType.StringT),_),"length",JsSelectType.Method, JsType.NumberT), _) => visit [JsApply] {
				JsSelect(s, "length", JsSelectType.Prop, JsType.NumberT)
			}
			case JsApply(JsSelect(i @ JsIdent(_, JsType.StringT), "length", JsSelectType.Method, _), _) => visit {
				JsSelect(i, "length", JsSelectType.Prop, JsType.NumberT)
			}
			
			// turn List.length() into property
			case JsApply(JsSelect(qualifier @ JsApply(JsSelect(_,_,_,JsType.ArrayT),_), "length", JsSelectType.Method,_), Nil) => visit [JsSelect] {
				JsSelect(qualifier, "length", JsSelectType.Prop, JsType.NumberT)
			}
			
			// remove default invocations
			case JsApply (s, params) => visit[JsApply] {
				JsApply (
					s,
					params.map(visit[JsTree]).filter((p) => {
						p match {
							case JsApply(JsSelect(_, name, _, _), params) if name contains "$default$" => false
							case JsIdent(name, _) if name.contains("$default$") => false
							case x => true
						}
					})
				)
			}
			
			case x => visit[JsTree](x)
		}
	}
	
	def transform (tree:JsTree):JsTree = {
		def visit[T <: JsTree] (t:JsTree):T = JsAstUtil.visitAst(t, transform).asInstanceOf[T]
		
		tree match {

			case JsSelect(JsSelect(JsSelect(JsIdent("scala",_), "collection", JsSelectType.Module, _), "immutable", JsSelectType.Module, _), "List", JsSelectType.Class, _) => {
				JsType.ArrayT
			}
			
			// println 
			case JsSelect(JsPredef(), "println", t, _) => visit {
				JsSelect(JsIdent("console"), "log", t)
			}
			
			// mkString on List
			// TODO: anything for right now, will narrow down to list later (not sure how yet)
			case JsApply(JsSelect(q, "mkString", t, _), List(glue)) => visit {
				JsApply(JsSelect(q, "join", t), List(glue))
			}
			
			
			// unary bang
			case JsApply( JsSelect(qualifier, "unary_$bang", t, _), _) => visit {
				JsUnaryOp(qualifier, "!")
			}
			
			// infix ops
			case JsApply( JsSelect(q, name, t, _), args) if infixOperatorMap contains name => visit [JsTree] {
				JsInfixOp(q, args(0), infixOperatorMap.get(name).get)
			}
			
			case JsThrow ( JsApply( JsNew( JsClassRef("java.lang.Exception") ), params ) ) => visit {
				JsThrow( JsApply( JsNew( JsClassRef("Error") ), params ) )
			}
			
			// comparisons
			case JsApply( JsSelect(qualifier, name, _, _), args) if comparisonMap contains name.toString => visit [JsTree] {
				JsComparison(
					qualifier,
					(comparisonMap get name.toString).get,
					args.head
				)
			}
			
			// TODO: move to JsPrinter
			// remove browser package prefix
			case JsSelect( JsIdent("browser",_), name, t, _) => visit {
				JsIdent(name)
			}
			case JsType (name:String, t @ _) if name.startsWith("browser.") => visit {
				JsType (name.stripPrefix("browser."), t)
			}
			
			// i don't think I need this anymore
			// scala.Unit
			case JsSelect ( JsIdent("scala",_), "Unit", t, _) => visit {
				JsType.VoidT
			}
			
			/* NOT SURE WHY THIS IS HERE
			 * it is breaking parseInt("2").toString
			// toString on XML literal
			case JsApply ( JsSelect(_, "toString", JsSelectType.Method, _ ), Nil) => {
				JsType.VoidT
			}
			*/
			
			// method applications with no parameter list
			// turn them into method applications with empty parameter list
			case JsSelect( s @ JsSelect(_,_,JsSelectType.Method,_), name, t, ret) => visit [JsSelect] {
				JsSelect( JsApply(s, Nil), name, t, ret )
			}
			
			// application of foreach on an identifier of type List
			case a @ JsApply (JsSelect(i @ JsIdent(_, JsType.ArrayT), "foreach", JsSelectType.Method, _), params) => visit {
				JsApply(
					JsSelect(JsSelect(JsIdent("goog"), "array", JsSelectType.Module), "forEach", JsSelectType.Method),
					i :: params
				)
			}
			
			// array access on variables
			case JsApply (JsSelect(id @ JsIdent(a, JsType.ArrayT), "apply", JsSelectType.Method,_), params) => visit {
				JsArrayAccess(id, params.head)
			}
			// array access on method returns
			case JsApply( JsSelect( a @ JsApply( JsSelect(_,_,_,JsType.ArrayT),_), "apply", JsSelectType.Method, _), params) => visit {
				JsArrayAccess(a, params.head)
			}
			
			// map access on variables 
			case JsApply (JsSelect(id @ JsIdent(a, JsType.ObjectT), "get", JsSelectType.Method,_), params) => visit {
				JsArrayAccess(id, params.head)
			}
			// map access on method returns
			case JsApply( JsSelect( a @ JsApply( JsSelect(_,_,_,JsType.ObjectT),_), "get", JsSelectType.Method, _), params) => visit {
				JsArrayAccess(a, params.head)
			}
			
			// unwrap methods on objects wrapped in "refArrayOps"
			case JsApply (JsSelect(JsApply(JsSelect(JsPredef(), "refArrayOps",_,_), List(subject)), name,_, tpe), params) if true => {
				JsApply(JsSelect(subject, name, JsSelectType.Method, tpe), params)
			}
			
			// collapse applications on local functions
			case a @ JsApply ( JsSelect(i @ JsIdent(_,JsType.FunctionT), "apply",_,_), params) => visit[JsApply] {
				JsApply(i, params)
			}

			// toInt on augmented strings, turn to parseInt
			case a @ JsApply(JsSelect(JsApply(JsSelect(JsPredef(), "augmentString", JsSelectType.Method, _), List(subject)), "toInt", JsSelectType.Method, _), params) => visit {
				JsApply(JsIdent("parseInt"), List(subject, JsLiteral("10", JsType.NumberT)))
			}
			
			// JsThis on modules need to be fully qualified
			case m @ JsModule (owner, name, body, props, methods, classes, modules) => visit {
				JsModule(owner, name, body, props.map(fullyQualifyJsThis(_,m).asInstanceOf[JsProperty]), methods.map(fullyQualifyJsThis(_, m).asInstanceOf[JsMethod]), classes, modules)
			}
			
			case x => visit[JsTree]{x}
		}
	}
	
	def fullyQualifyJsThis (tree:JsTree, module:JsModule):JsTree = {
		def visit[T <: JsTree] (t:JsTree):T = JsAstUtil.visitAst(t, (t:JsTree) => fullyQualifyJsThis(t, module)).asInstanceOf[T]
		
		tree match {
			case JsThis() => JsSelect(module.owner, module.name, JsSelectType.Module)
			
			// stop at inner classes and modules
			case c @ JsClass(_,_,_,_,_,_) => c
			case o @ JsModule(_,_,_,_,_,_,_) => o
			
			case x => visit[JsTree]{x}
		}
	}
}
