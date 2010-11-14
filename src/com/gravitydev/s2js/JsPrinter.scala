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
				val provides = p.map( (id:String) => "goog.provide('"+id+");\n" ).mkString("") + "\n"
				
				// requires
				// find all JsSelect
				val reqs = findRequires(tree).toSet.filter( !p.contains(_) ).map( (id:String) => "goog.require('"+id+"');\n" ).mkString("") + "\n"
				
				// classes
				val content = classes.map(print).mkString("\n") 
				
				provides + reqs + content
			}
			
			case c @ JsClass(owner, name, superClass, constructor, properties, methods) => {
				
				val const = printConstructor(c, constructor)
				
				val props = properties.map(printProp(c, _)+"\n").mkString("")
				
				val methds = methods.map(print).mkString("")
				
				const + props + methds
			}
			
			case m @ JsMethod (owner, name, params, children, ret) => {
				// jsdoc
				val l = new ListBuffer[Pair[String,String]]()
				params foreach ((p) => l += (("param", "{"+ print(p.tpe) +"} " + p.name)))
				if (ret != JsVoid()) l += (("return", "{"+print(ret)+"}"))
				val jsdoc = doc(l.toList)
				
				val start = print(owner) + ".prototype." + name + " = function (" + params.map(_.name).mkString(", ") + ") {\n"
				val middle = indent(
					// weird looking code, will have to refactor later
					if (ret == JsVoid()) children.map(printWithSemiColon).mkString("") else printWithReturn(m)
				)
				val end = "};\n"
					
				jsdoc + start + middle + end + "\n"
			}
			
			case JsLiteral (value, tpe) => {
				value
			}
			
			case JsNew ( s @ JsSelect(qualifier, name, _) ) => "new " + print(s)
			
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
			
			case JsParam (name, tpe) => name
			
			case JsBuiltInType (t) => t match {
				case JsBuiltInType.StringT => "string"
				case JsBuiltInType.BooleanT => "boolean"
				case JsBuiltInType.NumberT => "number"
				case JsBuiltInType.AnyT => "Object"
				case JsBuiltInType.UnknownT  => "UNKNOWN"
			}
			
			case JsModule (name, pkg, body) => {
				body.filter(!_.isInstanceOf[JsMethod]).map(print).mkString("\n")
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
	
	def printConstructor (c:JsClass, const:JsConstructor) = const match {
		case JsConstructor(name, params, constructorBody, classBody) => {
			val superClass = c.parents match {
				case Nil => None
				case x => Some(print(x.head))
			}
			
			val parent = superClass.map( (s) => ("extends", "{"+s+"}") )
			
			val jsdoc = doc(
				("constructor", "") :: params.map((p) => ("param", "{"+ print(p.tpe) +"} " + p.name)) ++ parent 
			)
			
			val sig = name + " = function (" + params.map(_.name).mkString(", ") + ") {\n"
			
			/*			
			// remove void return
			val constructorBody2 = constructorBody.dropRight(1)
			
			val body = constructorBody2 match {
				case JsApply(fun, params) :: tail => {
					// the first one is always a call to the super constructor, 
					// even if there isn't one
					val first = superClass match {
						case Some(s) => s + ".call(this, " + params.map(print).mkString(", ") + ");\n"
						case None => ""//printWithSemiColon(JsApply(fun, params))
					}
					val rest = tail.map(printWithSemiColon).mkString("\n") + "\n"
					
					first + rest
				}
				case _ => throw new Exception("what!")
			}
			
			// get assignments that are not literal or empty
			val content = classBody.collect({ case JsVar(id, tpe, rhs @ JsSelect(_, _)) => {
				// ommit 'this' qualifier
				"this." + id + " = " + print(rhs) + ";\n"
			} }).mkString("")
			*/
			
			val body = constructorBody.map(_ match {
				// if calling the super constructor
				case JsApply( JsSelect( JsSuper(), "<init>", _ ), params) => {
					// if there is a superclass
					superClass.map(_+".call(this" + (if (params.length>0) ", " + params.map(print).mkString(", ") else "") + ");").mkString("")
				}
				case x => print(x)
				
			}).mkString("\n")
			
			// get assignments that are not literal or empty
			// class variables must be prefixed with "this."
			val content = classBody.collect({
				case JsVar(id, tpe, rhs @ JsSelect(_,_,_)) => {
					"this."+id+" = " + print(rhs)+";"
				}
			}).mkString("\n") + "\n"
			
			val close = "};\n" 
			val ext = superClass.map( (s) => "goog.inherits("+name+", "+s.toString+");\n" ).getOrElse("")
			
			jsdoc + sig + indent(body) + indent(content) + close + ext + "\n"
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
	
	def printProp (c:JsClass, prop:JsProperty) = prop match {
		case JsProperty (mods, name, tpt, rhs) => {		
			val docs = List(
				//private
				if (mods.isPrivate) Some(("private", "")) else None,
				
				// type
				Some(("type", "{"+print(tpt)+"}"))
				
				// collapse 
			).flatMap(_.toList.flatMap(List(_)))
			
			val jsdoc = doc(docs)
		
			// right hand side
			val r = rhs match {
				case JsLiteral(value, tpe) => value
				case _ => "null"
			}
			
			val prop = print(c.owner)+"."+ c.name + ".prototype." + name + " = " + r + ";\n"
			
			jsdoc + prop + "\n"
		}
	}
	
	def doc (annotations:List[Pair[String,String]]) = {		
		"/**\n" + 
		annotations.map((a) => " * @"+a._1+" "+a._2+"\n").mkString("") +
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
				
				// method calls, print qualifier
				case JsSelect(q, _, JsSelectType.Method) => {
					l.append( print(q) )
				}
				
				// add other selects
				case x:JsSelect => {
					val s = print(x)
					
					l.append( print(x) )
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
		/*
		def findInList (l : List[JsTree]) = l flatMap findRequires
		
		tree match {
			case JsSourceFile(_,_,classes) => findInList(classes)
			case JsClass(_,_,parents,JsConstructor(_,_,constructorBody,classBody),_,methods) => findInList(parents) ::: findInList(constructorBody) ::: findInList(classBody) ::: findInList(methods)
			case JsMethod(_,params, children,_) => params:::children flatMap findRequires //(params flatMap findRequires) ::: (children flatMap findRequires)
			case JsBlock(children) => children flatMap findRequires
			case JsVar(_,_,rhs) => findRequires(rhs)
			
			// ignore super calls
			case JsApply(JsSelect(JsSuper(),_,_),_) => Nil
			
			case JsApply(qual,_) => {
				findRequires(qual)
			}
			case JsNew(s) => findRequires(s)
			
			case JsParam (_, tpe) => findRequires(tpe)
			
			// methods on "object"s
			case JsSelect( s @ JsSelect(_,_,_), _, JsSelectType.Method ) => List(print(s))
			
			// params
			case JsSelect(_,_,JsSelectType.ParamAccessor) => Nil
			
			// members
			case JsSelect(JsThis(), _, _) => Nil
			
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
