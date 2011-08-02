package com.gravitydev.s2js

import scala.collection.mutable.ListBuffer
import StringUtil._

object JsPrinter {
	
	/**
	 * Print a JS AST
	 */
	def print (tree:JsTree):String = {
		tree match {
			case f @ JsSourceFile(path,name,classes) => {
				// provides
				val p = findProvides(tree).toSet
				val provides = p.map( (id:String) => "goog.provide('"+id+"');\n" ).mkString("") + "\n"
				
				// requires
				val reqs = findRequires(tree).toSet.filter( !p.contains(_) ).map( (id:String) => "goog.require('"+id+"');\n" ).mkString("") + "\n"
				
				// is it a script?
				// only one object as top level with a main method
				val content = classes match {
					// script, print only the body of the main method
					case JsModule(_, _, _, _, JsMethod(_, "main", args, body, _) :: Nil, _, _) :: Nil => {
						// values from window
						val values = args.map((a:JsParam)=>"window[\""+a.name+"\"]").mkString(", ")
						
						// grab the first provides and export it
						"goog.exportSymbol('" + p.head + "', function ("+printParamList(args)+") {\n" +
						indent(
							print(body)
						) +
						"});\n"
					}
					// library, print all the classes and modules
					case classes => {
						classes.map(print).mkString("\n") 
					}
				}
				
				provides + reqs + content
			}
			
			case c @ JsClass(owner, name, superClass, constructor, properties, methods) => {
				
				val const = printConstructor(c, constructor, properties)
				
				val props = properties.map(printProp(_)+"\n").mkString("")
				
				val methds = methods.map(print).mkString("")
				
				const + props + methds
			}
			
			case o @ JsModule (owner, name, body, props, methods, classes, modules) => {
				/* not sure why this is here, modules should not be functions
				val b = print(owner)+"."+name+" = function () { \n" +
						indent(
							(body map print).mkString("\n")
						) +"\n" +
					"}; \n"
				*/
				val fullName = print(owner)+"."+name
				
				/* HACK: we should actually check weather this module is a companion, 
				 * 	if it is, we should not instantiate it since it will override its class
				 *  for right now, this ugly hack will have to do
				 */
				val b = "if (!"+fullName+") " + fullName + " = {};\n"
				
				val p = props.map(printModuleProp(_)+"\n").mkString("")
				
				val methds = methods.map(print).mkString("")
				
				// export main method if there is one
				val mainMethod = methods.find({
					case JsMethod(_, "main", args, body, _) => true
					case _ => false
				})
				val exported = mainMethod.map((x) => {
					val fullName = print(owner)+"."+name+".main"
					"goog.exportSymbol(\""+fullName+"\", "+fullName+"); \n"
				}).getOrElse("")
				
				val c = classes.map(print).mkString("")
				val m = modules.map(print).mkString("")
				
				b + p + methds + c + m + exported
			}
			
			case m @ JsMethod (owner, name, params, body, ret) => {
				// jsdoc
				val l = new ListBuffer[String]()
				params foreach ((p) => l += getParamDoc(p))
				if (ret != JsType.VoidT) l += "@return {"+print(ret)+"}"
				val jsdoc = doc(l.toList)
				
				val start = owner.name + "." + (if (owner.isInstanceOf[JsClassRef]) "prototype." else "") + name + " = function (" + printParamList(params) + ") {\n"
				
				val middle = indent(
					print(body)
				)
				val end = "};\n"
					
				jsdoc + start + indent(getDefaultParamsInit(params)) + middle + end + "\n"
			}
			
			case JsFunction (params, body) => {
				"function (" + printParamList(params) + ") {\n" +
					indent(
						print(body)	
					) + 
				"}"
			}
			
			case JsLiteral (value, tpe) => {
				value.replace("\n", "\\n")
			}
			
			case JsNew ( s ) => "new " + print(s)
			
			// applies on super
			case JsApply (JsSelect(JsSuper(qualifier), name, tpe, returnType), params) => {
				print(qualifier) + ".superClass_." + name + ".call(this" + (if (params.length > 0) ", " else "") + params.map(print).mkString(", ") + ")"
			}
			
			// not sure if this one is necessary
			case JsApply (fun, params) => {
				print(fun) + "(" + params.map(print).mkString(", ") + ")"
			}
			
			// other
			case JsSelect (qualifier, name, t, tpe) => {
				val s = qualifier match {
					case s @ JsSelect(q, n, _, _) => {
						print(s)+"."+name
					}
					case JsIdent(n,_) => n+"."+name
					case JsThis() => "this."+name
					
					//case JsModuleRef (name) => name
					case JsModuleRef (n) => n+"."+name
					
					case x => {
						//println(x)
						
						print(qualifier) + "." + name
					}
				}
				
				s
			}
			
			case JsIdent (name,_)  => {
				name
			}
			
			case JsAssign (lhs, rhs) => {
				print(lhs) + " = " + print(rhs) + ";\n"
			}
			
			case JsComparison (lhs, operator, rhs) => {
				print(lhs) + " " + operator + " " + print(rhs)
			}
			
			case JsUnaryOp (select, op) => {
				op + print(select)
			}
			
			case JsInfixOp (a, b, op) => {
				print(a) + " " + op + " " + print(b)
			}
			
			case JsIf (cond, thenp, elsep) => {			
				val condition = "if (" + print(cond) + ") {\n"
				val body = indent(printWithSemiColon(thenp))
				val elseline = "} else {\n"
				val e = indent(printWithSemiColon(elsep))
				val last = "}\n"
				
				condition + body + (if (elsep == JsType.VoidT) "" else elseline + e) + last
			}
			
			case JsTernary (cond, thenp, elsep) => {
				"("+print(cond)+" ? "+print(thenp)+":"+print(elsep)+")"
			}
			
			case JsThrow (expr) => {
				"throw " + print(expr) + ";\n"
			}
			
			case JsThis () => "this"
			
			case JsType.VoidT => "" //"{void}"
			
			case JsVar (id,tpe,rhs) => {				
				"var "+id+" = " + print(rhs)+";\n"
			}
			case JsBlock (stats, expr) => {
				(
					stats.map((x) => x match {
						// methods found in JsBlock are local functions
						case m @ JsMethod(_,_,_,_,_) => printLocalFunction(m)
						
						case _ => printWithSemiColon(x)
					}) 
					
					::: List(printWithSemiColon(expr))
				).mkString("")
				//children.map(printWithSemiColon).mkString("")
			}
			
			case JsOther (clazz,children) => {
				"## "+clazz+" ##"
			}
			
			case JsEmpty () => "EMPTY"
			
			case JsParam (name, tpe, default) => name
			
			case JsType.StringT => "string"
			case JsType.BooleanT => "boolean"
			case JsType.NumberT => "number"
			case JsType.AnyT => "Object"
			case JsType.ArrayT => "Array"
			case JsType.UnknownT  => "UNKNOWN"
			
			case JsType (name, typeParams) => {
				name
			}
			
			
			case JsTypeApply (fun, args) => {
				"TYPEAPPLY: " + print(fun) + "(" + args.map(print).mkString(", ") + ")"
			}
			
			case p @ JsPredef () => {
				println(p)
				""
			}
			
			case JsMap (elements) => elements match {
				case Nil => "{}"
				case _ => {
					"{\n" +
					indent(
						elements.map((e) => "\"" + e.key + "\": " + print(e.value)).mkString(",\n")
					) +
					"}"
				}
			}
			
			case JsArray (elements) => elements match {
				case Nil => "[]"
				case _ => {
					"[\n" +
					indent(
						elements.map(print).mkString(",\n")
					) +
					"]"
				}
			}

			case JsReturn (expr) => {
				"return " + print(expr) + ";\n"
			}
			
			case JsCast (subject, tpe) => {
				"/** @type {" + print(tpe) + "} */ (" + print(subject) + ")"
			}
			
			case JsArrayAccess (subject, index) => {
				print(subject) + "[" + print(index) + "]"
			}
			
			case JsClassRef (name) => name.stripPrefix("browser.")
			case JsPackageRef (name) => name
			case JsModuleRef (name) => name
		}
	}
	
	def printType (node:JsTree) = node match {
		case JsParam(_,tpe,default) => {
			"{" + printTypeForAnnotation(tpe) + default.map((a)=>"=").mkString("") + "}" // annotate default param
		}
	}
	
	def printTypeForAnnotation (node:JsTree) = {
		node match {
			case JsType.FunctionT => "Function"
		
			// the previous one should catch everything, but just in case
			case JsSelect(JsIdent("scala",_), "Function1", JsSelectType.Class,_) => {
				"Function"
			}
			case _ => print(node)
		}
	}
	
	/**
	 * Get the name of a param, prepend opt_ if it has default value
	 */
	def getParamName (p:JsParam) = p.default match {
		case Some(x) => "opt_" + p.name
		case None => p.name
	}
	
	def printParamList (params:List[JsParam]) = {		
		params.map(getParamName).mkString(", ")
	}
	
	def getParamDoc (node:JsParam) = {		
		"@param " + printType(node) + " " + getParamName(node)
	}
	
	def getDefaultParamsInit (params:List[JsParam]) = {
		params.filter(_.default.isDefined).map((p) => "var "+p.name+" = opt_" + p.name + " || " + print(p.default.get) +";\n").mkString("")
	}
	
	def printLocalFunction (m:JsMethod) = {
		// jsdoc
		val l = new ListBuffer[String]()
		m.params foreach ((p) => l += getParamDoc(p))
		if (m.ret != JsType.VoidT) l += "@return {"+print(m.ret)+"}"
		val jsdoc = doc(l.toList)
		
		val start = "var " + m.name + " = function (" + printParamList(m.params) + ") {\n"
		val middle = indent(
			print(m.body)
		)
		val end = "};\n"
			
		jsdoc + start + indent(getDefaultParamsInit(m.params)) + middle + end
	}
	
	def printConstructor (c:JsClass, const:JsConstructor, properties:List[JsTree]) = const match {
		case JsConstructor(owner, params, constructorBody, classBody) => {
				
			val superClass = c.parents match {
				case Nil => None
				case x => Some(print(x.head))
			}
			
			val parent = superClass.map( (s) => "@extends {"+s+"}" )
			
			val jsdoc = doc(
				("@constructor") :: params.map(getParamDoc) ++ parent 
			)
			
			val sig = print(owner) + " = function (" + printParamList(params) + ") {\n"
			
			val body = constructorBody.map(_ match {
				// if calling the super constructor
				case JsApply( JsSelect( JsSuper(qualifier), "<init>", _, _ ), params) => {
					// if there is a superclass
					superClass.map(_+".call(this" + (if (params.length>0) ", " else "") + params.map(print).mkString(", ") + ");\n").mkString("")
				}
				case x => print(x)
				
			}).mkString("\n")
			
			// get assignments that are not literal or empty
			// class variables must be prefixed with "this."
			/* DON'T use classBody any more, use properties 
			val content = classBody.collect({
				case JsVar(id, tpe, rhs @ JsSelect(_,_,_)) => {
					"this."+id+" = " + print(rhs)+";"
				}
			}).mkString("\n") + "\n"
			*/
			val props = properties.collect({
				case p @ JsProperty(_,_,_,rhs,_) if !rhs.isInstanceOf[JsLiteral] && !rhs.isInstanceOf[JsEmpty] => p
			}).map((p) => p match {
				case JsProperty(owner, name, tpe, rhs, mods) => "this." + name + " = " + print(rhs)
			}).mkString("\n") + "\n"
			
			val content = classBody.filterNot(_.isInstanceOf[JsVar]).map(print).mkString("\n")+"\n"
			
			val close = "};\n" 
			val ext = superClass.map( (s) => "goog.inherits("+print(owner)+", "+s.toString+");\n" ).getOrElse("")
			
			jsdoc + sig + indent(getDefaultParamsInit(params)) + "\n" + indent(body) + indent(props) + indent(content) + close + ext + "\n"
		}
	}
	
	def getLastStatement (tree:JsTree) : JsTree = tree match {
		/*
		case JsMethod(_,_, _, children, _) => {
			children.last match {
				case a:JsBlock => getLastStatement(a)
				case a => a
			}
		}
		*/
		case JsBlock (stats, expr) => {
			/*
			children.last match {
				case a:JsBlock => getLastStatement(a)
				case a => a
			}
			*/
			JsType.VoidT
		}
	}
	
	/**
	 * Non-literal class props need to be initialized in the construtor
	 */
	def printProp (prop:JsProperty) = prop match {
		case JsProperty (owner @ JsSelect(_,_, t,_), name, tpt, rhs, mods) => {		
			val docs = List(
				//private
				if (mods.isPrivate) Some("@private") else None,
				
				// type
				Some("@type {"+print(tpt)+"}")
				
				// collapse 
			).flatMap(_.toList.flatMap(List(_)))
			
			val jsdoc = doc(docs)
		
			// right hand side
			val r = rhs match {
				case JsLiteral(value, tpe) => value
				case _ => "null"
			}
			
			val prop = print(owner)+"." + (if (t == JsSelectType.Class) "prototype." else "") + name + " = " + r + ";\n"
			
			jsdoc + prop + "\n"
		}
	}
	
	/**
	 * Module properties need to be initialized in place
	 */
	def printModuleProp (prop:JsProperty) = prop match {
		case JsProperty (owner @ JsSelect(_,_, t,_), name, tpt, rhs, mods) => {		
			val docs = List(
				//private
				if (mods.isPrivate) Some("@private") else None,
				
				// type
				Some("@type {"+print(tpt)+"}")
				
				// collapse 
			).flatMap(_.toList.flatMap(List(_)))
			
			val jsdoc = doc(docs)
		
			// right hand side
			val r = print(rhs)
			
			val prop = print(owner)+"." + (if (t == JsSelectType.Class) "prototype." else "") + name + " = " + r + ";\n"
			
			jsdoc + prop + "\n"
		}
	}
	
	def doc (annotations:List[String]) = {		
		"/**\n" + 
		annotations.map((a) => " * "+a+"\n").mkString("") +
		" */\n"
	}
	
	def printWithSemiColon (tree : JsTree): String = {
		tree match {
			case a:JsApply => print(a) + ";\n"
			case a:JsSelect => print(a) + ";\n"
			case a:JsIdent => print(a) + ";\n"
			case x => print(x)
		}
	}
	
	def findProvides (tree : JsTree) : List[String] = {
		val l = new ListBuffer[String]
		
		def find (tree:JsTree) : JsTree = {
			tree match {
				case JsClass(owner, name, _,_,_,_) => {
					l.append(owner.name+"."+name)
					
					JsAstUtil visitAst ( tree, find )
				}
				
				case JsModule(owner, name, _, _,_,_,_) => {
					l.append(owner.name+"."+name)
					
					JsAstUtil visitAst ( tree, find )
				}
				
				// check everything else
				case _ => {
					JsAstUtil visitAst ( tree, find )
					()
				}
			}
			tree
		}
		find(tree)
		l.toList
	}
	
	def findRequires (tree : JsTree) : List[String] = {			
		val l = new ListBuffer[String]
		
		def find (tree:JsTree) : JsTree = {		
			def visit[T <: JsTree] (t:JsTree):T = JsAstUtil.visitAst(t, find).asInstanceOf[T]
				
			tree match {				
				// ignore members
				case JsSelect(JsThis(), _, _, _) => ()
				
				// ignore applications on members
				case JsSelect(JsSelect(JsThis(),_,_, _),_,_, _) => ()
				
				// ignore super calls
				case JsApply(JsSelect(JsSuper(qualifier),_,_, _),_) => ()
				
				// ignore function types
				case JsSelect(JsIdent("scala",_),_,JsSelectType.Class,_) => ()
				
				// ignore right side of JsProperty and JsVar if it's a select, but add the tpe
				case JsVar(id, tpe @ JsSelect(_,_,_,_), JsSelect(_,_,_, _)) => {
					l.append(print(tpe))
				}
				case JsProperty(owner, name, tpt @ JsSelect(_,_,_,_), JsSelect(_,_,_,_), mods) => {
					l.append(print(tpt))
				}
				// TODO: these should be collapsed into one case with guards
				// select class
				case s @ JsSelect(_,_,JsSelectType.Class,_) => {
					l.append(print(s))
				}
				// select module
				case JsSelect(s @ JsSelect(_,_,JsSelectType.Module,_), _, JsSelectType.Method, _ ) => {
					l.append(print(s))
				}
				// select package
				case JsSelect( s @ JsSelect(_,_,JsSelectType.Package,_), _, JsSelectType.Method, _ ) => {
					l.append(print(s))
				}
				// select prop
				case JsSelect (s @ JsSelect(_,_,JsSelectType.Module,_),_,JsSelectType.Prop,_) => {
					l.append(print(s))
				}
				
				// TODO: had to move this one down to not conflict with the package one. Gotta figure out what the deal is
				// ignore applications on local variables
				case JsSelect(JsIdent(_,_),_,_,_) => ()
				
				case JsClassRef(name) => l.append(name)
				
				case _ => ()
			}
			visit(tree)
		}
		
		find(tree)
		
		// remove anything that ends in underscore or starts with browser
		l.filterNot(_.endsWith("_")).filterNot(_.startsWith("browser.")).toList
	}
	
}
