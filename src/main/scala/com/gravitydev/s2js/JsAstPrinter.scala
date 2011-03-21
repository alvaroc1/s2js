package com.gravitydev.s2js

import StringUtil._

object JsAstPrinter {
	
	
	def print (tree:JsTree):String = tree match {
		case JsSourceFile (path, name, classes) => {
			"JsSourceFile (\n" +
			indent(
				"path: " + path + "\n" +
				"name: " + name + "\n" +
				"classes: " + printList(classes) + "\n"
			) +
			")"
		}
		case JsClass (owner, name, parents, constructor, properties, methods) => {
			"JsClass (\n" +
			indent(
				"owner: " + owner + "\n" +
				"name: " + name + "\n" +
				"parents: " + parents + "\n" +
				"constructor: " + print(constructor) + "\n" +
				"properties: " + printList(properties) + "\n" +
				"methods: " + printList(methods) + "\n" 
			) +
			")"
		}
		case JsConstructor (owner, params, constructorBody, classBody) => {
			"JsConstructor (\n" +
			indent(
				"owner: " + print(owner) + "\n" +
				"params: " + params + "\n" +
				"constructorBody: " + printList(constructorBody) + "\n" +
				"classBody: " + printList(classBody) + "\n" 
			) +
			")"
		}
		case JsApply (fun, params) => {
			"JsApply (\n" +
			indent(
				"fun: " + print(fun) + "\n" +
				"params: " + printList(params) + "\n"
			) +
			")"
		}
		case JsSelect(qualifier, name, t, tpe) => {
			"JsSelect(\n" +
			indent(
				"qualifier: " + print(qualifier) + ",\n" + 
				"name: " + name + "\n" +
				"type: " + t + "\n" +
				"returnType: " + tpe 
			) +
			")"
		}
		case JsMethod (owner, name, params, body, ret) => {
			"JsMethod(\n" +
			indent(
				"owner: " + print(owner) + "\n" +
				"name: " + name + "\n" +
				"params: " + params + "\n" +
				"children: " + print(body) + "\n" +
				"ret: " + ret + "\n" 
			) +
			")"
		}
		case JsBlock (stats,expr) => {
			"JsBlock(\n" + 
			indent(
				"stats: " + printList(stats) + "\n" +
				"expr: " + print(expr) + "\n" 
			) +
			")"
		}
		case JsIf (cond, then, elsep) => {
			"JsIf(\n" +
			indent(
				"cond: " + print(cond) + "\n" +
				"then: " + print(then) + "\n" +
				"else: " + print(then) + "\n" 
			) +
			")"
		}
		
		case JsVar (id, tpe, rhs) => {
			"JsVar(\n" +
			indent(
				"id: " + id + "\n" +
				"tpe: " + tpe + "\n" +
				"rhs: " + print(rhs) + "\n"
			) +
			")"
		}
		case JsTypeApply (fun, args) => {
			"JsTypeApply(\n" +
			indent(
				"fun: " + print(fun) + "\n" +
				"args: " + printList(args) + "\n"
			) +
			")"
		}
		case JsAssign (lhs, rhs) => {
			"JsAssign(\n" +
			indent(
				"lhs: " + print(lhs) + "\n" +
				"rhs: " + print(rhs) + "\n" 
			) +
			")"
		}
		case JsModule (owner, name, body, props, methods, classes, modules) => {
			"JsModule(\n" +
			indent(
				"owner: " + print(owner) + "\n" +
				"name: " + name + "\n" +
				"body: " + printList(body) + "\n" +
				"props: " + printList(props) + "\n" +
				"methods: " + printList(methods) + "\n" +
				"classes: " + printList(classes) + "\n" +
				"modules: " + printList(modules) + "\n"
			) +
			")"
		}
		case JsProperty (owner, name, tpt, rhs, mods) => {
			"JsProperty(\n" +
			indent(
				"owner: " + print(owner) + "\n" +
				"name: " + name + "\n" +
				"tpt: " + print(tpt) + "\n" +
				"rhs: " + print(rhs) + "\n" +
				"mods: " + mods + "\n"
			) +
			")"
		}
		case JsFunction (params, body) => {
			"JsFunction(\n" +
			indent(
				"params: " + printList(params) + "\n" +
				"body: " + print(body) 
			) +
			")"
		}
		case JsPackage (name, children) => {
			"JsPackage(\n" +
			indent(
				"name: " + name + "\n" +
				"children: " + printList(children) + "\n"
			) +
			")"
		}
		case JsReturn (expr) => {
			"JsReturn(\n" +
			indent(
				"expr: " + print(expr) + "\n"	
			) +
			")"
		}
		case JsCast (subject, tpe) => {
			"JsCast(\n" +
			indent(
				"subject: " + print(subject) + "\n" +
				"tpe: " + print(tpe) + "\n"
			) +
			")"
		}
		case x => x.toString
	}
	
	def printList (l: List[JsTree]) = {
		"[\n" +
		indent( l.map(print).mkString(",\n") ) + "\n" +
		"]"
	}

}

