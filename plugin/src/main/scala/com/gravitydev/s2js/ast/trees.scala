package com.gravitydev.s2js.ast

import com.gravitydev.s2js.StringUtil.indent

sealed trait Tree
sealed trait Branch extends Tree {
  def children :Seq[Tree]
}
sealed trait Leaf extends Tree

trait Typed {self: Tree =>
  def tpe: Type
}

trait CompilationUnit extends Branch

case class SourceFile (path:String, name:String, pkg:Package) extends Branch {
  def children = Seq(pkg)
}

case class Package (name :String, units: Seq[CompilationUnit], exports: Set[String]) extends Branch {
  def children = units
}

case class Module (name:String, properties:Seq[Property], methods:Seq[Method], classes:Seq[Class], modules:Seq[Module]) extends CompilationUnit {
  def children = properties ++ methods ++ classes ++ modules
}

case class New (tpt: Tree) extends Branch {
  def children = Seq(tpt)
}

case class Class (name:String, supers:Seq[String], constructor:Method, props:Seq[Property], methods:Seq[Method]) extends CompilationUnit {
  def children = Seq(constructor) ++ props ++ methods
}

case class Cast(subject: Tree, tpe: Type) extends Branch {
  def children = Seq(subject, tpe)
}

case class Property () extends Leaf

case class Method (name:String, params:Seq[Param], body:Tree, ret:Type) extends Branch {
  def children = params ++ Seq(body, ret)
}

case class Block (stats :Seq[Tree]) extends Branch {
  def children = stats
  override def toString = "{\n" + children.map(c => indent(c.toString + ";\n")).mkString + "}\n"
}

case class If (cond:Tree, thenp:Tree, elsep:Tree) extends Branch {
  def children = Seq(cond, thenp, elsep)
}

case class Literal (value:String, tpe:Type with BuiltInType) extends Branch with Typed {
  def children = Seq(tpe)
  override def toString = tpe match {
    case Types.StringT => "\"" + value + "\""
    case _ => value
  }
}

case class Param (name:String, tpe:Tree, default:Option[Tree]) extends Branch {
  def children = Seq(tpe) ++ default.toSeq
}

case class Return (expr:Tree) extends Branch {
  def children = Seq(expr)
  override def toString = "return " + expr.toString + ";\n"
}

case class Var (id: String, tpe: Type, rhs: Tree) extends Branch {
  def children= Seq(tpe, rhs)
  override def toString = "var " + id + ":" + tpe.toString + " = " + rhs.toString 
}

case class Apply (fun:Tree, params:Seq[Tree], tpe: Type) extends Branch with Typed {
  def children = Seq(fun) ++ params
  override def toString = fun.toString + "(" + params.map(_.toString) + ")"
}

case class Function (params: Seq[Param], body: Tree) extends Branch {
  def children = params ++ Seq(body)
}

case class Ident (name:String, tpe: Type) extends Leaf with Typed {
  override def toString = name
}

case class Unknown (name: String) extends Leaf

case object Void extends Leaf
case object Null extends Leaf

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
case class Select (qualifier: Tree, name: String, selectType: SelectType, tpe: Type) extends Branch {
  def children = Seq(qualifier)
  override def toString = qualifier.toString + "." + name
}

case class Type (name:String, typeParams:List[String]=Nil) extends Leaf {
  override def toString = name + (if (typeParams.nonEmpty) typeParams.mkString("[", ", ", "]") else "")
}
sealed trait BuiltInType
object Types {
	object StringT   extends Type("String")    with BuiltInType
	object ArrayT    extends Type("Array")     with BuiltInType
	object ObjectT   extends Type("Object")    with BuiltInType
	object BooleanT  extends Type("Boolean")   with BuiltInType
	object NumberT   extends Type("Number")    with BuiltInType // i don't remember why there's Int AND Number, I think we just need Number
	object FunctionT extends Type("Function")  with BuiltInType
	object UnknownT  extends Type("UNKOWN")    with BuiltInType // probably not built-in?
	object AnyT      extends Type("Any")       with BuiltInType		// probably not built-in?
	object VoidT     extends Type("Void")      with BuiltInType
	object PackageT  extends Type("Package")   with BuiltInType
	object NullT     extends Type("Null")      with BuiltInType
}

/*
object `package` {
  type SelectType = SelectType.Value
}
* 
*/
