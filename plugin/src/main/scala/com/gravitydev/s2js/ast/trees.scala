package com.gravitydev.s2js.ast

sealed trait Tree extends scala.util.parsing.input.Positional

trait Typed {self: Tree =>
  def tpe: Type
}

trait CompilationUnit extends Tree {
  def name: String
}

case class SourceFile (path:String, name:String, pkg:Package) extends Tree

case class Package (name: String, units: Seq[CompilationUnit], exports: Set[String]) extends Tree

case class Module (name:String, properties:Seq[Property], methods:Seq[Method], classes:Seq[Class], modules:Seq[Module]) extends CompilationUnit

case class Array (elements: Seq[Tree]) extends Tree

case class ArrayItemGet (sel: Tree, key: Tree) extends Tree

case class New (tpt: Tree) extends Tree

case class Class (name:String, sup: Option[Type], constructor:Method, props:Seq[Property], methods:Seq[Method]) extends CompilationUnit

case class Cast(subject: Tree, tpe: Type) extends Tree with Typed

case class Property () extends Tree

case class Method (name:String, fun: Function) extends Tree

case class Block (stats: Seq[Tree]) extends Tree

case class If (cond: Tree, thenp: Tree, elsep: Tree) extends Tree

case class Literal (value:String, tpe: Type with BuiltInType) extends Tree with Typed

case class Param (name:String, tpe: Type, default:Option[Tree]) extends Tree

case class Return (expr:Tree) extends Tree

case class Var (id: String, tpe: Type, rhs: Tree) extends Tree

case class Assign(sel: Tree, rhs: Tree) extends Tree

case class Apply (fun: Tree, params:Seq[Tree], tpe: Type) extends Tree with Typed

case class Function (params: Seq[Param], stats: Seq[Tree], ret: Type) extends Tree

case class Ident (name:String, tpe: Type) extends Tree with Typed

case class InfixOp (operand1: Tree, operand2: Tree, op:String) extends Tree

case class UnaryOp (operand: Tree, op: String, opPos: OpPos) extends Tree

case class ObjectItemGet(sel: Tree, key: Tree) extends Tree

case class Object (elements: Seq[ObjectItem]) extends Tree

case class ObjectItem(key: String, value: Tree) extends Tree

trait OpPos
case object Prefix extends OpPos

case class Unknown (name: String) extends Tree

case object Void extends Tree
case object Null extends Tree

case class Super(qual: Tree) extends Tree
case object This extends Tree

case class Select (qualifier: Tree, name: String, selectType: SelectType) extends Tree

case class PropRef (sel: Select, tpe: Type) extends Tree

sealed trait SelectType
object SelectType {
  case object Method          extends SelectType
  case object ParamAccessor   extends SelectType 
  case object Prop            extends SelectType
  case object Module          extends SelectType
  case object Class           extends SelectType
}

case class Type (name:String, typeParams:List[String]=Nil) {
  override def toString = ":" + name + (if (typeParams.nonEmpty) typeParams.mkString("[", ",", "]") else "")
}
sealed trait BuiltInType
object Types {
  object StringT   extends Type("String")    with BuiltInType
	object ArrayT    extends Type("Array")     with BuiltInType
	object ObjectT   extends Type("Object")    with BuiltInType
	object BooleanT  extends Type("Boolean")   with BuiltInType
	object NumberT   extends Type("Number")    with BuiltInType
	object FunctionT extends Type("Function")  with BuiltInType
	//object UnknownT  extends Type("UNKOWN")    with BuiltInType // probably not built-in?
	object AnyT      extends Type("Any")       with BuiltInType // probably not built-in?
	object VoidT     extends Type("Void")      with BuiltInType
	object PackageT  extends Type("Package")   with BuiltInType
	object NullT     extends Type("Null")      with BuiltInType
}

object zipper {
  trait Context
  case object Top extends Context
  case class TreeContext (left: List[Tree], ctx: Context, right: List[Tree]) extends Context
}
