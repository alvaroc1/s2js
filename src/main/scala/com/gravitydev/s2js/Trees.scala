package com.gravitydev.s2js

trait JsTree 
case class JsSourceFile (path:String, name:String, classes:List[JsTree]) extends JsTree

case class JsClass (owner:JsTree, name:String, parents:List[JsSelect], constructor:JsConstructor, properties:List[JsProperty], methods:List[JsMethod]) extends JsTree

case class JsModule (owner:JsTree, name:String, properties:List[JsProperty], methods:List[JsMethod], classes:List[JsClass], modules:List[JsModule]) extends JsTree

case class JsMethod (owner:JsTree, name:String, params:List[JsParam], children:List[JsTree], ret:JsTree) extends JsTree

case class JsConstructor (owner:JsTree, params:List[JsParam], constructorBody:List[JsTree], classBody:List[JsTree]) extends JsTree

case class JsVar (id:String, tpe:JsTree, rhs:JsTree) extends JsTree
case class JsApply (fun:JsTree, params:List[JsTree]) extends JsTree
case class JsBlock (children:List[JsTree]) extends JsTree
case class JsLiteral (value:String, tpe:String) extends JsTree
case class JsVoid () extends JsTree
case class JsOther (clazz:String, children:List[JsTree]) extends JsTree
case class JsProperty (owner:JsTree, name:String, tpt:JsTree, rhs:JsTree, mods:JsModifiers) extends JsTree

case class JsParam (name:String, tpe:JsTree, default:Option[JsTree]) extends JsTree

case class JsSelect (qualifier:JsTree, name:String, selectType:JsSelectType.Value = JsSelectType.Other ) extends JsTree

case class JsIdent (name:String) extends JsTree
case class JsThis () extends JsTree
case class JsIf (cond:JsTree, thenp:JsTree, elsep:JsTree) extends JsTree
case class JsAssign (lhs:JsTree, rhs:JsTree) extends JsTree
case class JsComparison (lhs:JsTree, operator:String, rhs:JsTree) extends JsTree

case class JsEmpty () extends JsTree

case class JsNew (tpt:JsTree) extends JsTree

case class JsSuper () extends JsTree

case class JsTypeApply (fun:JsTree, params:List[JsTree]) extends JsTree

case class JsMap (elements:List[JsMapElement]) extends JsTree

case class JsMapElement(key:String, value:JsTree) extends JsTree

case class JsPredef () extends JsTree

case class JsUnaryOp (subject:JsTree, op:String) extends JsTree

case class JsThrow (expr:JsTree) extends JsTree

case class JsModifiers (
	isPrivate:Boolean
)

case class JsBuiltInType (t:JsBuiltInType.Value) extends JsTree

object JsSelectType extends Enumeration {
	type JsSelectType = Value
	val Method, ParamAccessor, Prop, Module, Class, Package, Other = Value
}

object JsBuiltInType extends Enumeration {
	type JsBuiltInType = Value
	val AnyT, StringT, BooleanT, NumberT, UnknownT = Value
}
