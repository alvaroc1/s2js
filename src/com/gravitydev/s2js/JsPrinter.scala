package com.gravitydev.s2js

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
		
		case JsMethod (name, params, children) => {			
			val jsdoc = doc(
				params.map((p) => ("param", "{"+p.tpe+"} " +p.name))
			)
			
			// get return statement
			val ret = children.reverse.head
			
			// remove return if void
			val body = ret match {
				case JsVoid() => children.reverse.tail.reverse
				case _ => children
			}
			
			val start = name + " = function (" + params.map(_.name).mkString(", ") + ") {\n"
			val middle = indent(
				body.map(
					(a) => a match {
						// end invocations with semi-colon and newline
						case a:JsApply => print(a) + ";\n"
						case _ => print(a)
					}
				).mkString("")
			)
			val end = "};\n"
				
			jsdoc + start + middle + end
		}
		
		case JsLiteral (value, tpe) => {
			value
		}
		
		// assignment
		case JsApply (JsSelect(qualifier, name), params) if name.endsWith("_$eq") => {
			qualifier + "." + name.substring(0, name.length-4) + " = " + params.map(print).mkString("")
		}
		
		// not equals comparison
		case JsApply (JsSelect(qualifier, name), params) if name == "$bang$eq" => {
			qualifier + " != " + params.map(print).mkString("")
		}
		
		// not sure if this one is necessary
		case JsApply (fun, params) => {
			print(fun) + "(" + params.map(print).mkString("") + "); \n"
		}
		
		case JsSelect (qualifier, name) => {
			qualifier + "." + name
		}
		
		case JsIdent (name)  => {
			name
		}
		
		case JsIf (cond, thenp, elsep) => {			
			val condition = "if (" + print(cond) + ") {\n"
			val body = indent(print(thenp))
			val elseline = "} else {\n"
			val e = indent(print(elsep))
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
			
			"var "+id+" = " + tp + print(rhs)+";"
		}
		case JsBlock (children) => {
			children.map(
				(a) => a match {
					// end invocations with semi-colon and newline
					case a:JsApply => print(a) + ";\n"
					case _ => print(a)
				}
			).mkString("")
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
			val constructorBody2 = constructorBody.reverse.tail.reverse
			
			val body = constructorBody2 match {
				case JsApply(fun, params) :: tail => {
					val first = c.superClass match {
						case Some(superClass) => c.superClass.get + ".call(this, " + params.map(print).mkString("") + ");\n"
						case None => print(JsApply(fun, params))
					}
					val rest = tail.map(print).mkString("\n") + "\n"
					
					first + rest
				}
				case _ => throw new Exception("what!")
			}
			
			//val body = constructorBody2.map(print(_, i+1)).mkString("") + "\n"
			
			val content = (for (child @ JsVar(id, tpe, rhs) <- classBody if !rhs.isInstanceOf[JsEmpty] && !rhs.isInstanceOf[JsLiteral]) yield {
				"this."+id+" = " + print(rhs) + "\n"
			}).mkString("")
			
			val close = "};\n" 
			val ext = c.superClass.map( (s) => "goog.inherits("+name+", "+s.toString+");\n" ).getOrElse("")
			
			jsdoc + sig + indent(body) + indent(content) + close + ext + "\n"
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
	
}
