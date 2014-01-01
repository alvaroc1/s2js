package com.gravitydev.s2js

trait JsTree
trait JsMod extends JsTree {
	val body :List[JsTree]
}

case class JsSourceFile (path:String, name:String, children:List[JsTree]) extends JsTree

case class JsApp (owner :JsRef, name :String, body :List[JsTree]) extends JsMod

case class JsClass (owner:JsRef, name:String, parents:List[JsSelect], constructor:JsConstructor, properties:List[JsProperty], methods:List[JsMethod]) extends JsTree

case class JsModule (owner:JsRef, name:String, body:List[JsTree], properties:List[JsProperty], methods:List[JsMethod], classes:List[JsClass], modules:List[JsModule]) extends JsMod

case class JsMethod (owner:JsRef, name:String, params:List[JsParam], body:JsTree, ret:JsTree) extends JsTree

case class JsConstructor (owner:JsRef, params:List[JsParam], constructorBody:List[JsTree], classBody:List[JsTree]) extends JsTree

case class JsVar (id:String, tpe:JsTree, rhs:JsTree) extends JsTree
case class JsBlock (stats:List[JsTree], expr:JsTree) extends JsTree
case class JsLiteral (value:String, tpe:JsBuiltInType) extends JsTree
case class JsOther (clazz:String, children:List[JsTree]) extends JsTree
case class JsProperty (owner:JsTree, name:String, tpt:JsTree, rhs:JsTree, mods:JsModifiers) extends JsTree

case class JsParam (name:String, tpe:JsTree, default:Option[JsTree]) extends JsTree

/* String for the type will have to do until i can figure out how to get an actual type */
case class JsSelect (qualifier:JsTree, name:String, selectType:JsSelectType.Value, tpe:JsType=null ) extends JsTree
case class JsIdent (name:String, tpe:JsType=null) extends JsTree
case class JsApply (fun:JsTree, params:List[JsTree]) extends JsTree

case class JsType (name:String, typeParams:List[String]=Nil) extends JsTree
trait JsBuiltInType
object JsType {
	object StringT extends JsType("String") with JsBuiltInType
	object ArrayT extends JsType("Array") with JsBuiltInType
	object ObjectT extends JsType("Object") with JsBuiltInType
	object BooleanT extends JsType("Boolean") with JsBuiltInType
	object NumberT extends JsType("Number") with JsBuiltInType // i don't remember why there's Int AND Number, I think we just need Number
	object FunctionT extends JsType("Function") with JsBuiltInType
	object UnknownT extends JsType("UNKOWN") with JsBuiltInType // probably not built-in?
	object AnyT extends JsType("Any") with JsBuiltInType		// probably not built-in?
	object VoidT extends JsType("Void") with JsBuiltInType
	object PackageT extends JsType("Package") with JsBuiltInType
	object NullT extends JsType("Null") with JsBuiltInType
}

trait JsRef extends JsTree {
	val name:String
}
case class JsClassRef (name:String) extends JsRef
case class JsModuleRef (name:String) extends JsRef
case class JsPackageRef (name:String) extends JsRef
case class JsMethodRef (name:String) extends JsRef
case class JsUnknownRef (name:String) extends JsRef

object DefaultModuleRef extends JsModuleRef ("_default_")

case class JsThis () extends JsTree
case class JsIf (cond:JsTree, thenp:JsTree, elsep:JsTree) extends JsTree
case class JsTernary (cond:JsTree, thenp:JsTree, elsep:JsTree) extends JsTree
case class JsAssign (lhs:JsTree, rhs:JsTree) extends JsTree
case class JsComparison (lhs:JsTree, operator:String, rhs:JsTree) extends JsTree

case class JsEmpty () extends JsTree

//case class JsNew (tpt:JsClassRef) extends JsTree
case class JsNew (tpt:JsTree) extends JsTree

case class JsSuper (qualifier:JsSelect) extends JsTree

case class JsTypeApply (fun:JsTree, params:List[JsTree]) extends JsTree

case class JsMap (elements:List[JsMapElement]) extends JsTree

case class JsMapElement(key:String, value:JsTree) extends JsTree

case class JsPredef () extends JsTree

case class JsUnaryOp (subject:JsTree, op:String) extends JsTree

case class JsInfixOp (operand1:JsTree, operand2:JsTree, op:String) extends JsTree

case class JsThrow (expr:JsTree) extends JsTree

case class JsFunction (params:List[JsParam], body:JsTree) extends JsTree

case class JsReturn (expr:JsTree) extends JsTree

case class JsPackage (name:String, children:List[JsTree]) extends JsTree

case class JsArray (elements:List[JsTree]) extends JsTree

case class JsArrayAccess (array:JsTree, index:JsTree) extends JsTree

case class JsCast (subject:JsTree, tpe:JsTree) extends JsTree

case class JsModifiers (
	isPrivate:Boolean
)

object JsSelectType extends Enumeration {
	type JsSelectType = Value
	val Method, ParamAccessor, Prop, Module, Class, Package, Other = Value
}
