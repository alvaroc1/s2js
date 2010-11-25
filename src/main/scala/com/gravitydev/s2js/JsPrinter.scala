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
					case JsModule(_, _, _, JsMethod(_, "main", _, children, _) :: Nil, _, _) :: Nil => {
						"(function () {\n" +
						indent(children.map(printWithSemiColon).mkString("")) +
						"})();\n"
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
			
			case JsModule (owner, name, props, methods, classes, modules) => {
				val p = props.map(printProp(_)+"\n").mkString("")
				
				val methds = methods.map(print).mkString("")
				
				val c = classes.map(print).mkString("")
				val m = modules.map(print).mkString("")
				
				p + methds + c + m
			}
			
			case m @ JsMethod (owner, name, params, children, ret) => {
				// jsdoc
				val l = new ListBuffer[String]()
				params foreach ((p) => l += getParamDoc(p))
				if (ret != JsVoid()) l += "@return {"+print(ret)+"}"
				val jsdoc = doc(l.toList)
				
				val start = print(owner) + ".prototype." + name + " = function (" + printParamList(params) + ") {\n"
				val middle = indent(
					// weird looking code, will have to refactor later
					if (ret == JsVoid()) children.map(printWithSemiColon).mkString("") else printWithReturn(m)
				)
				val end = "};\n"
					
				jsdoc + start + indent(getDefaultParamsInit(params)) + middle + end + "\n"
			}
			
			case JsLiteral (value, tpe) => {
				value
			}
			
			case JsNew ( s ) => "new " + print(s)
			
			// not sure if this one is necessary
			case JsApply (fun, params) => {
				print(fun) + "(" + params.map(print).mkString(", ") + ")"
			}
			
			/* SELECTS */
			// predef
			/*
			case JsSelect(JsSelect(JsThis(),"Predef",_),name,_) => {
				name match {
					case "String" => "string"
					case x => x
				}
			}
			*/
			
			// other
			case JsSelect (qualifier, name, t) => {
				val s = qualifier match {
					case s @ JsSelect(q, n, _) => {
						print(s)+"."+name
					}
					case JsIdent(n) => n+"."+name
					case JsThis() => "this."+name
					case x => {
						//println(x)
						
						name
					}
				}
				
				s
			}
			
			case JsIdent (name)  => {
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
			
			case JsIf (cond, thenp, elsep) => {			
				val condition = "if (" + print(cond) + ") {\n"
				val body = indent(printWithSemiColon(thenp))
				val elseline = "} else {\n"
				val e = indent(printWithSemiColon(elsep))
				val last = "}\n"
				
				condition + body + (if (elsep.isInstanceOf[JsVoid]) "" else elseline + e) + last
			}
			
			case JsThrow (expr) => {
				"throw " + print(expr) + ";\n"
			}
			
			case JsThis () => "this"
			
			case JsVoid () => "" //"{void}"
			
			case JsVar (id,tpe,rhs) => {				
				"var "+id+" = " + print(rhs)+";\n"
			}
			case JsBlock (children) => {
				children.map(printWithSemiColon).mkString("")
			}
			
			case JsOther (clazz,children) => {
				"## "+clazz+" ##"
			}
			
			case JsEmpty () => "EMPTY"
			
			case JsParam (name, tpe, default) => name
			
			case JsBuiltInType (t) => t match {
				case JsBuiltInType.StringT => "string"
				case JsBuiltInType.BooleanT => "boolean"
				case JsBuiltInType.NumberT => "number"
				case JsBuiltInType.AnyT => "Object"
				case JsBuiltInType.UnknownT  => "UNKNOWN"
			}
			
			case JsTypeApply (fun, args) => {
				"TYPEAPPLY: " + print(fun) + "(" + args.map(print).mkString(", ") + ")"
			}
			
			case p @ JsPredef () => {
				println(p)
				""
			}
			
			case JsMap (elements) => {
				"{\n" +
				indent(
					elements.map((e) => "\"" + e.key + "\": " + print(e.value)).mkString(",\n")
				) +
				"}"
			}
		}
	}
	
	def printType (node:JsTree) = node match {
		case JsParam(_,tpe,default) => {
			"{" + print(tpe) + default.map((a)=>"=").mkString("") + "}" // annotate default param
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
				case JsApply( JsSelect( JsSuper(), "<init>", _ ), params) => {
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
			val content = properties.collect({
				case p @ JsProperty(_,_,_,rhs,_) if !rhs.isInstanceOf[JsLiteral] => p
			}).map((p) => p match {
				case JsProperty(owner, name, tpe, rhs, mods) => "this." + name + " = " + print(rhs)
			}).mkString("\n") + "\n"
			
			val close = "};\n" 
			val ext = superClass.map( (s) => "goog.inherits("+print(owner)+", "+s.toString+");\n" ).getOrElse("")
			
			jsdoc + sig + indent(getDefaultParamsInit(params)) + "\n" + indent(body) + indent(content) + close + ext + "\n"
		}
	}
	
	def getLastStatement (tree:JsTree) : JsTree = tree match {
		case JsMethod(_,_, _, children, _) => {
			children.last match {
				case a:JsBlock => getLastStatement(a)
				case a => a
			}
		}
		case JsBlock (children) => {
			children.last match {
				case a:JsBlock => getLastStatement(a)
				case a => a
			}
		}
	}
	
	def printProp (prop:JsProperty) = prop match {
		case JsProperty (owner @ JsSelect(_,_, t), name, tpt, rhs, mods) => {		
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
		
	def printWithReturn (tree : JsTree) : String = tree match {
		case b:JsMethod => {
			(b.children.init map printWithSemiColon).mkString("") +printWithReturn(b.children.last) 
		}
		case b:JsBlock => {
			(b.children.init map printWithSemiColon).mkString("") +printWithReturn(b.children.last) 
		}
		case x => "return " + printWithSemiColon (x)
	}
	
	def findProvides (tree : JsTree) : List[String] = {
		val l = new ListBuffer[String]
		
		def find (tree:JsTree) : JsTree = {
			tree match {
				case JsClass(owner, name, _,_,_,_) => {
					l.append(print(owner)+"."+name)
					
					JsASTUtil visitAST ( tree, find )
				}
				
				case JsModule(owner, name, _,_,_,_) => {
					l.append(print(owner)+"."+name)
					
					JsASTUtil visitAST ( tree, find )
				}
				
				// check everything else
				case _ => {
					JsASTUtil visitAST ( tree, find )
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
			tree match {
				// ignore members
				case JsSelect(JsThis(), _, _) => ()
				
				// ignore applications on members
				case JsSelect(JsSelect(JsThis(),_,_),_,_) => ()
				
				// ignore super calls
				case JsApply(JsSelect(JsSuper(),_,_),_) => ()
				
				// ignore applications on local variables
				case JsSelect(JsIdent(_),_,_) => ()
				
				// ignore right side of JsProperty and JsVar if it's a select, but add the tpe
				case JsVar(id, tpe @ JsSelect(_,_,_), JsSelect(_,_,_)) => {
					l.append(print(tpe))
				}
				case JsProperty(owner, name, tpt @ JsSelect(_,_,_), JsSelect(_,_,_), mods) => {
					l.append(print(tpt))
				}
				
				// select class
				case s @ JsSelect(_,_,JsSelectType.Class) => {
					l.append(print(s))
				}
				case s @ JsSelect(_,_,JsSelectType.Module) => {
					l.append(print(s))
				}
				
				// method calls, print qualifier
				/*
				case JsSelect(q @ JsSelect(_,_,_), _, t) => {
					l.append( print(q) )
				}
				*/
				
				// add other selects
				case x:JsSelect => {
					val j = x
					val s = print(x)
					
					//l.append( print(x) )
					JsASTUtil visitAST ( tree, find )
				}
				
				// check everything else
				case _ => {
					JsASTUtil visitAST ( tree, find )
				}
			}
			tree
		}
		
		find(tree)
		
		l.toList
		/*
		def findInList (l : List[JsTree]) = l flatMap findRequires
		
		tree match {
			case JsSourceFile(_,_,classes) => findInList(classes)
			case JsClass(_,_,parents,JsConstructor(_,_,constructorBody,classBody),_,methods) => findInList(parents) ::: findInList(constructorBody) ::: findInList(classBody) ::: findInList(methods)
			case JsMethod(_,params, children,_) => params:::children flatMap findRequires //(params flatMap findRequires) ::: (children flatMap findRequires)
			case JsBlock(children) => children flatMap findRequires
			case JsVar(_,_,rhs) => findRequires(rhs)
			
			case JsApply(qual,_) => {
				findRequires(qual)
			}
			case JsNew(s) => findRequires(s)
			
			case JsParam (_, tpe) => findRequires(tpe)
			
			// methods on "object"s
			case JsSelect( s @ JsSelect(_,_,_), _, JsSelectType.Method ) => List(print(s))
			
			// params
			case JsSelect(_,_,JsSelectType.ParamAccessor) => Nil
			
			// predef
			case JsSelect(JsSelect(JsThis(), "Predef", _), _, _) => Nil
			
			// scala (predef)
			case JsSelect(JsIdent("scala"),_,_) => Nil
			
			case x:JsSelect => {
				
				List(print(x))
			}
			case JsVoid() => Nil
			
			case x => {
				println("Search for selects? "+x.getClass)
				Nil
			}
		}
		*/
	}
	
}
