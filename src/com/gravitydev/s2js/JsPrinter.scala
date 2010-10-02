package com.gravitydev.s2js

object JsPrinter {
	
	/**
	 * Print a JS AST
	 */
	def print (tree:JsTree, i:Int):String = tree match {
		
		case f @ JsSourceFile(path,name,classes) => classes.map(print(_, i)).mkString("\n")
		
		case c @ JsClass(name, superClass, constructor, properties, methods) => {
			val p = indent(i) _
		
			val provide = p("goog.provide('"+name+"');") + "\n"
			
			val const = printConstructor(c, constructor, i)
			
			val props = properties.map(printProp(c, _, i)).mkString("")
			
			val methds = methods.map(print(_, i)).mkString("")
			
			provide + const + props + methds
		}
		
		case JsMethod (name, params, children) => {
			// partially apply indent
			val p = indent(i) _
			
			val jsdoc = doc(i) (
				params.map((p) => ("param", p.tpe+" " +p.name))
			)
			
			// get return statement
			val ret = children.reverse.head
			
			// remove return if void
			val body = ret match {
				case JsVoid() => children.reverse.tail.reverse
				case _ => children
			}
			
			/*
			val content = body match {
				case l:List[JsTree] => l.map(print(_, i+1)).mkString("")
				case t:JsTree => print(t, i+1)
			}
			*/
			
			val s = p(name + " = function (" + params.map(_.name).mkString(", ") + ") {") +
				body.map(print(_, i+1)).mkString("") +
				p("};") +
				"\n"
				
			jsdoc + s
		}
		
		case JsLiteral (value, tpe) => {
			value
		}
		
		case JsApply (JsSelect(qualifier, name), params) => {
			indent(i) (
				if (name.endsWith("_$eq")) {
					qualifier+"."+name.substring(0, name.length-4) + " = " + params.map(print(_, i)).mkString("") + ";"
				} else {
					qualifier+"."+name+"(" + params.map(print(_, i)).mkString("") + ");"
				}
			)
		}
		
		// not sure if this one is necessary
		case JsApply (fun, params) => {
			print(fun, i) + "(" + params.map(print(_, i)).mkString("") + "); \n"
		}
		
		case JsSelect (qualifier, name) => {
			indent(i)(qualifier + "." + name)
		}
		
		case JsIdent (name)  => {
			name
		}
		
		case JsIf (cond, thenp, elsep) => {			
			val condition = indent(i)("if (" + print(cond, i) + ") {")
			val body = print(thenp, i+1)
			val elseline = indent(i)("} else {")
			val e = print(elsep, i+1)
			val last = indent(i)("}")
			
			condition + body + elseline + e + last
		}
		
		case JsThis () => "this"
		
		case JsVoid () => "{void}"
		
		case JsVar (id,tpe,rhs) => {
			// only annotate type for non-literals
			val tp = rhs match {
				case l:JsLiteral => ""
				case _ => "/** @type {"+tpe+"} */ "
			}
			
			val s = indent(i) (
				"var "+id+" = " + tp + print(rhs, i)+";"
			)
			
			s
		}
		case JsBlock (children) => {
			(for (child <- children) yield print(child, i)).mkString("")
		}
		
		case JsOther (clazz,children) => {
			"## "+clazz+" ##"
		}
		
		case JsEmpty () => "EMPTY"
		
		case JsParam (name, tpe) => name
	}
	
	def printConstructor (c:JsClass, const:JsConstructor, i:Int) = const match {
		case JsConstructor(name, params, constructorBody, classBody) => {
			val p = indent(i) _
			
			val parent = c.superClass.map( (s) => ("extends", "{"+s.toString+"}") )
			
			val jsdoc = doc(i)(
				("constructor", "") :: params.map((p) => ("param", p.tpe+" " + p.name)) ++ parent 
			)
			
			val sig = p(name + " = function (" + params.map(_.name).mkString(", ") + ") {")
			
			// remove void return
			val constructorBody2 = constructorBody.reverse.tail.reverse
			
			val body = constructorBody2 match {
				case JsApply(fun, params) :: tail => {
					val first = c.superClass match {
						case Some(superClass) => indent(i+1) (c.superClass.get + ".call(this, " + params.map(print(_, i)).mkString("") + ");")
						case None => print(JsApply(fun, params), i+1)
					}
					val rest = tail.map(print(_, i+1)).mkString("") + "\n"
					
					first + rest
				}
				case _ => throw new Exception("what!")
			}
			
			//val body = constructorBody2.map(print(_, i+1)).mkString("") + "\n"
			
			val content = (for (child @ JsVar(id, tpe, rhs) <- classBody if !rhs.isInstanceOf[JsEmpty] && !rhs.isInstanceOf[JsLiteral]) yield {
				indent(i+1)("this."+id+" = " + print(rhs, i))
			}).mkString("")
			
			val close = p("};") 
			val ext = c.superClass.map( (s) => "goog.inherits("+name+", "+s.toString+");\n" ).getOrElse("")
			
			jsdoc + sig + body + content + close + ext + "\n"
		}
	}
	
	def printProp (c:JsClass, prop:JsProperty, i:Int) = prop match {
		case JsProperty (mods, name, tpt, rhs) => {
			val p = indent(i) _
			
			
			
			val docs = List(
				//private
				if (mods.isPrivate) Some(("private", "")) else None,
				
				// type
				Some(("type", tpt))
				
				// collapse 
			).flatMap(_.toList.flatMap(List(_)))
			
			val jsdoc = doc(i)(docs)
		
			// right hand side
			val r = rhs match {
				case JsLiteral(value, tpe) => value
				case _ => "null"
			}
			
			val prop = p(c.name + ".prototype." + name + " = " + r + ";")
			
			jsdoc + prop + "\n"
		}
	}
	
	
	/**
	 * Indent a string [margin] number of times
	 */
	def indent (margin:Int)(line:String*) = {
		line.map(("  "*margin)+_).mkString("\n") + "\n"
	}
	
	
	
	def doc (margin:Int)(annotations:List[Pair[String,String]]) = {
		val p = indent(margin) _
		
		p("/**") + 
		annotations.map((a) => p(" * @"+a._1+" "+a._2)).mkString("") +
		p(" */")
	}
	
}
