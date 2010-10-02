package com.gravitydev.s2js

trait JsTree
case class JsSourceFile (path:String, name:String, classes:List[JsClass]) extends JsTree
case class JsClass (name:String, superClass:Option[String], constructor:JsConstructor, properties:List[JsProperty], methods:List[JsMethod]) extends JsTree
case class JsMethod (name:String, params:List[JsParam], children:List[JsTree]) extends JsTree

case class JsConstructor (name:String, params:List[JsParam], constructorBody:List[JsTree], classBody:List[JsTree]) extends JsTree

case class JsVar (id:String, tpe:String, rhs:JsTree) extends JsTree
case class JsApply (fun:JsTree, params:List[JsTree]) extends JsTree
case class JsBlock (children:List[JsTree]) extends JsTree
case class JsLiteral (value:String, tpe:String) extends JsTree
case class JsVoid () extends JsTree
case class JsOther (clazz:String, children:List[JsTree]) extends JsTree
case class JsProperty (mods:JsModifiers, name:String, tpt:String, rhs:JsTree) extends JsTree

case class JsParam (name:String, tpe:String) extends JsTree
case class JsSelect (qualifier:String, name:String) extends JsTree
case class JsIdent (name:String) extends JsTree
case class JsThis () extends JsTree
case class JsIf (cond:JsTree, thenp:JsTree, elsep:JsTree) extends JsTree

case class JsEmpty () extends JsTree

case class JsModifiers (
	isPrivate:Boolean
)