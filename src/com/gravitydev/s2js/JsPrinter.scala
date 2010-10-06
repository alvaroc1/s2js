package com.gravitydev.s2js

import scala.collection.mutable.ListBuffer

object JsPrinter {
	
	/**
	 * Print a JS AST
	 */
	def print (tree:JsTree):String = tree match {
		
		case f @ JsSourceFile(path,name,classes) => classes.map(print).mkString("\n")
		
		case c @ JsClass(name, superClass, constructor, properties, methods) => {		
			val provide = "goog.provide('"+name+"');\n\n"
			
			val const = printConstructor(c, constructor)
			
			val props = properties.map(printProp(c, _)+"\n").mkString("")
			
			val methds = methods.map(print).mkString("")
			
			provide + const + props + methds
		}
		
		case m @ JsMethod (name, params, children, ret) => {
			// jsdoc
			val l = new ListBuffer[Pair[String,String]]()
			params foreach ((p) => l += (("param", "{"+p.tpe+"} " + p.name)))
			if (ret != "void") l += (("return", "{"+ret+"}"))
			val jsdoc = doc(l.toList)
			
			val start = name + " = function (" + params.map(_.name).mkString(", ") + ") {\n"
			val middle = indent(
				// weird looking code, will have to refactor later
				if (ret == "void") children.map(printWithSemiColon).mkString("") else printWithReturn(m)
			)
			val end = "};\n"
				
			jsdoc + start + middle + end + "\n"
		}
		
		case JsLiteral (value, tpe) => {
			value
		}
		
		// not equals comparison
		case JsApply (JsSelect(qualifier, name, _), params) if name == "$bang$eq" => {
			qualifier + " != " + params.map(print).mkString("")
		}
		
		// not sure if this one is necessary
		case JsApply (fun, params) => {
			print(fun) + "(" + params.map(print).mkString(", ") + ")"
		}
		
		case JsSelect (qualifier, name, isParamAccessor) => {
			// parameters don't need to be qualified
			(if (!isParamAccessor) qualifier + "." else "") + name
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
		
		case JsIf (cond, thenp, elsep) => {			
			val condition = "if (" + print(cond) + ") {\n"
			val body = indent(printWithSemiColon(thenp))
			val elseline = "} else {\n"
			val e = indent(printWithSemiColon(elsep))
			val last = "}\n"
			
			condition + body + (if (elsep.isInstanceOf[JsVoid]) "" else elseline + e) + last
		}
		
		case JsThis () => "this"
		
		case JsVoid () => "{void}"
		
		case JsVar (id,tpe,rhs) => {
			// only annotate type for non-literals
			val tp = rhs match {
				case l:JsLiteral => ""
				case _ => "/** @type {"+tpe+"} */ "
			}
			
			"var "+id+" = " + tp + print(rhs)+";\n"
		}
		case JsBlock (children) => {
			children.map(printWithSemiColon).mkString("")
		}
		
		case JsOther (clazz,children) => {
			"## "+clazz+" ##"
		}
		
		case JsEmpty () => "EMPTY"
		
		case JsParam (name, tpe) => name
	}
	
	def printConstructor (c:JsClass, const:JsConstructor) = const match {
		case JsConstructor(name, params, constructorBody, classBody) => {			
			val parent = c.superClass.map( (s) => ("extends", "{"+s.toString+"}") )
			
			val jsdoc = doc(
				("constructor", "") :: params.map((p) => ("param", "{"+p.tpe+"} " + p.name)) ++ parent 
			)
			
			val sig = name + " = function (" + params.map(_.name).mkString(", ") + ") {\n"
			
			// remove void return
			val constructorBody2 = constructorBody.dropRight(1)
			
			val body = constructorBody2 match {
				case JsApply(fun, params) :: tail => {
					val first = c.superClass match {
						case Some(superClass) => c.superClass.get + ".call(this, " + params.map(print).mkString("") + ");\n"
						case None => printWithSemiColon(JsApply(fun, params))
					}
					val rest = tail.map(printWithSemiColon).mkString("\n") + "\n"
					
					first + rest
				}
				case _ => throw new Exception("what!")
			}
			
			// get assignments that are not literal or empty
			val content = classBody.collect({ case JsVar(id, tpe, rhs @ JsSelect(qualifier, name, isParamAccessor)) => {
				// ommit 'this' qualifier
				"this." + id + " = " + print(rhs) + ";\n"
			} }).mkString("")
			
			val close = "};\n" 
			val ext = c.superClass.map( (s) => "goog.inherits("+name+", "+s.toString+");\n" ).getOrElse("")
			
			jsdoc + sig + indent(body) + indent(content) + close + ext + "\n"
		}
	}
	
	def getLastStatement (tree:JsTree) : JsTree = tree match {
		case JsMethod(_, _, children, _) => {
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
				Some(("type", tpt))
				
				// collapse 
			).flatMap(_.toList.flatMap(List(_)))
			
			val jsdoc = doc(docs)
		
			// right hand side
			val r = rhs match {
				case JsLiteral(value, tpe) => value
				case _ => "null"
			}
			
			val prop = c.name + ".prototype." + name + " = " + r + ";\n"
			
			jsdoc + prop + "\n"
		}
	}
	
	def indent (text:String) = text.split("\n").map("  "+_).mkString("\n") + "\n"
	
	def doc (annotations:List[Pair[String,String]]) = {		
		"/**\n" + 
		annotations.map((a) => " * @"+a._1+" "+a._2+"\n").mkString("") +
		" */\n"
	}
	
	def printWithSemiColon (tree : JsTree): String = tree match {
		case a:JsApply => print(a) + ";\n"
		case a:JsSelect => print(a) + ";\n"
		case x => print(x)
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
	
}
