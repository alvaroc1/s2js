package com.gravitydev.s2js.ast

sealed trait Tree
trait Branch extends Tree {
  def children :Seq[Tree]
}
trait Leaf extends Tree

trait CompilationUnit extends Branch

case class SourceFile (path:String, name:String, pkg:Package) extends Branch {
  def children = Seq(pkg)
}

case class Package (name :String, units :Seq[CompilationUnit]) extends Branch {
  def children = units
}

case class Module (name:String, properties:Seq[Property], methods:Seq[Method], classes:Seq[Class], modules:Seq[Module]) extends CompilationUnit {
  def children = properties ++ methods ++ classes ++ modules
}

case class Class (name:String, supers:Seq[String], constructor:Method, props:Seq[Property], methods:Seq[Method]) extends CompilationUnit {
  def children = Seq(constructor) ++ props ++ methods
}

case class Property () extends Leaf

case class Method (name:String, params:Seq[Param], body:Tree, ret:Type) extends Branch {
  def children = params ++ Seq(body, ret)
}

case class Block (stats :Seq[Tree]) extends Branch {
  def children = stats
}

case class If (cond:Tree, thenp:Tree, elsep:Tree) extends Branch {
  def children = Seq(cond, thenp, elsep)
}

case class Literal (value:String, tpe:Type with BuiltInType) extends Branch {
  def children = Seq(tpe)
}

case class Param (name:String, tpe:Tree, default:Option[Tree]) extends Branch {
  def children = Seq(tpe) ++ default.toSeq
}

case class Return (expr:Tree) extends Branch {
  def children = Seq(expr)
}

/*
case class Apply (fun:Tree, params:Seq[Tree]) extends Branch {
  def children = Seq(fun) ++ params
}
*/
case class Apply () extends Leaf

/*
case class Ident (name:String, tpe:Type=null) extends Branch {
  def children = Seq(tpe)
}
*/
case class Ident (name:String) extends Leaf

case object Void extends Leaf

case class Type (name:String, typeParams:List[String]=Nil) extends Leaf
trait BuiltInType
object Types {
	object StringT extends Type("String") with BuiltInType
	object ArrayT extends Type("Array") with BuiltInType
	object ObjectT extends Type("Object") with BuiltInType
	object BooleanT extends Type("Boolean") with BuiltInType
	object NumberT extends Type("Number") with BuiltInType // i don't remember why there's Int AND Number, I think we just need Number
	object FunctionT extends Type("Function") with BuiltInType
	object UnknownT extends Type("UNKOWN") with BuiltInType // probably not built-in?
	object AnyT extends Type("Any") with BuiltInType		// probably not built-in?
	object VoidT extends Type("Void") with BuiltInType
	object PackageT extends Type("Package") with BuiltInType
	object NullT extends Type("Null") with BuiltInType
}
