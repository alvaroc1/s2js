package com.gravitydev.s2js.ast

import com.gravitydev.s2js.StringUtil.indent

sealed trait Tree

trait Typed {self: Tree =>
  def tpe: Type
}

trait CompilationUnit extends Tree

case class SourceFile (path:String, name:String, pkg:Package) extends Tree

case class Package (name: String, units: Seq[CompilationUnit], exports: Set[String]) extends Tree

case class Module (name:String, properties:Seq[Property], methods:Seq[Method], classes:Seq[Class], modules:Seq[Module]) extends CompilationUnit

case class Array (elements: Seq[Tree]) extends Tree

case class New (tpt: Tree) extends Tree

case class Class (name:String, supers:Seq[String], constructor:Method, props:Seq[Property], methods:Seq[Method]) extends CompilationUnit

case class Cast(subject: Tree, tpe: Type) extends Tree

case class Property () extends Tree

case class Method (name:String, params:Seq[Param], body:Tree, ret:Type) extends Tree

case class Block (stats :Seq[Tree]) extends Tree

case class If (cond:Tree, thenp:Tree, elsep:Tree) extends Tree

case class Literal (value:String, tpe:Type with BuiltInType) extends Tree with Typed

case class Param (name:String, tpe:Tree, default:Option[Tree]) extends Tree

case class Return (expr:Tree) extends Tree

case class Var (id: String, tpe: Type, rhs: Tree) extends Tree

case class Apply (fun:Tree, params:Seq[Tree], tpe: Type) extends Tree with Typed

case class Function (params: Seq[Param], body: Tree) extends Tree

case class Ident (name:String, tpe: Type) extends Tree with Typed

case class Unknown (name: String) extends Tree

case object Void extends Tree
case object Null extends Tree

sealed trait SelectType
object SelectType {
  case object Method          extends SelectType
  case object ParamAccessor   extends SelectType 
  case object Prop            extends SelectType
  case object Module          extends SelectType
  case object Class           extends SelectType
  case object Package         extends SelectType
  case object Other           extends SelectType
}

// TODO: remove tpe, since it probably belong only on Apply
case class Select (qualifier: Tree, name: String, selectType: SelectType, tpe: Type) extends Tree

case class Type (name:String, typeParams:List[String]=Nil) extends Tree
sealed trait BuiltInType
object Types {
	object StringT   extends Type("String")    with BuiltInType
	object ArrayT    extends Type("Array")     with BuiltInType
	object ObjectT   extends Type("Object")    with BuiltInType
	object BooleanT  extends Type("Boolean")   with BuiltInType
	object NumberT   extends Type("Number")    with BuiltInType
	object FunctionT extends Type("Function")  with BuiltInType
	object UnknownT  extends Type("UNKOWN")    with BuiltInType // probably not built-in?
	object AnyT      extends Type("Any")       with BuiltInType // probably not built-in?
	object VoidT     extends Type("Void")      with BuiltInType
	object PackageT  extends Type("Package")   with BuiltInType
	object NullT     extends Type("Null")      with BuiltInType
}
