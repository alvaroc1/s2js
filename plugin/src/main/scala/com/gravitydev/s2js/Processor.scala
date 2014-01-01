package com.gravitydev.s2js

import com.gravitydev.s2js.ast._
import org.kiama._
import org.kiama.rewriting.{Strategy, Rewriter}

object Processor extends (SourceFile => SourceFile) with Rewriter {
  
  def apply (source: SourceFile) = process(source)
  
  def process [T <: Tree](ast: T): T = (
    topdown(basicReplacements) <* 
    topdown(collectionRewrites) <*
    topdown(operators) <* 
    removeSuperCalls
  )(ast).map(_.asInstanceOf[T]).get

  
  val basicReplacements = rule {
    // remove void returns
    //case Function(params, Return(Void)) => Function(params, Block(Nil))
    
    // clean up function
    case Function (params, stats, tpe) if stats.nonEmpty => {
      val last = stats.last
      val init = stats.init
      
      // remove unnecessary returns
      val newStats = init ++ Seq(
        last match {
          case Return(x) if tpe == Types.VoidT => x
          case Return(Void) => Void
          case x => x
        }
      )
      
      // remove void returns
      Function (params, newStats filter (_ != Void), tpe)
    }
    
    // block with only one thing
    case Block(List(x)) => x
    
    case Block(stats) => Block(stats filter (_ != Void))
    
    // Nil to empty array
    case ScalaNil() => Array(Nil)
    
    // infix ops
    case Apply( Select(q, name, t), args, _) if operatorMaps.infix contains name =>  {
      InfixOp(q, args(0), operatorMaps.infix(name))
    }
    
    // turn method String.length() into property
    case Apply(Select(l @ Literal(value, Types.StringT), "length", SelectType.Method), _, _) => {
      PropRef(Select(l, "length", SelectType.Prop), Types.NumberT)
    }
    case Apply(Select(Apply(s @ Select(_,_,_),_,_),"length",SelectType.Method), _, Types.StringT) => {
      PropRef(Select(s, "length", SelectType.Prop), Types.NumberT)
    }
    case Apply(Select(i @ Ident(_, Types.StringT), "length", SelectType.Method), _, _) => {
      PropRef(Select(i, "length", SelectType.Prop), Types.NumberT)
    }
    
    // List(1,2,3) => [1,2,3]
    case Apply(Select(Select(CollectionImmutablePkg, "List", SelectType.Module), "apply", SelectType.Method), args, _) => {
      Array(args)
    }
    
    case x => {
      x
    }
  }
  
  val collectionRewrites = rule {
    // List foreach
    case a @ Apply(Select(arr @ Array(_), "foreach", SelectType.Method), args, Types.VoidT) => {
      Apply(Select(Select(Ident("goog", Types.PackageT), "array", SelectType.Module), "forEach", SelectType.Method), Seq(arr) ++ args, Types.VoidT)
    }
    case x => x
  }
  
  val operators = rule {
    // unary
    case Select(qualifier, name, t) if operatorMaps.prefix contains name => UnaryOp(qualifier, operatorMaps.prefix(name), Prefix)
    
    // prop assign
    case Apply(Select(sel, m, SelectType.Method), List(param), _) if m endsWith "_$eq" => {
      println(sel)
      Assign(Select(sel, m stripSuffix "_$eq", SelectType.Prop), param)
    }
    
    case x => x
  }
  
  val removeSuperCalls = {
    // remove super calls from any blocks
    val remove = attempt {
      rule {
        case Function(params, stats, tpe) => {
          println(stats)
          Function (
            params, 
            stats filter {_ match {
              case Apply(Select(Super(This), "<init>", SelectType.Method), args, tpe) => false
              case _ => true
            }},
            tpe
          )
        }
      }
    }
    
    // only remove from classes with no parents
    topdown {
      attempt {
        rule {
          case c @ Class(_, None, x, _, _) => {
            some(topdown(remove))(c).get
          }
        }
      }
    }
  }
  
  // extractors
  private val ScalaType = Type("_scala_", Nil)
  private val ScalaPkg = Ident("scala", ScalaType)
  private val CollectionPkg = Select(ScalaPkg, "collection", SelectType.Module)
  private val CollectionImmutablePkg = Select(CollectionPkg, "immutable", SelectType.Module)
  object ScalaNil {
    def unapply (x: Tree) = {
      x match {
        case Select(CollectionImmutablePkg, "Nil", SelectType.Module) => Some(())
        case _ => None
      }
    }
  }
  
  object operatorMaps {
    val prefix = Map(
      "unary_$bang" -> "!"
    )
    val infix = Map(
      "$eq$eq"      -> "==",
      "$bang$eq"    -> "!=",
      "$greater"    -> ">",
      "$greater$eq" -> ">=",
      "$less"       -> "<",
      "$less$eq"    -> "<=",
      "$amp$amp"    -> "&&",
      "$bar$bar"    -> "||",
      "$plus"       -> "+",
      "$minus"      -> "-"
    )
  }
}
