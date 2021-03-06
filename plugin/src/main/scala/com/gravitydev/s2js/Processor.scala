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
    topdown(cleanUpBlocks) <*
    removeInheritanceFromObject <*
    removeSuperCalls <*
    cleanupConstructors <*
    stdLibReplacements
  )(ast).map(_.asInstanceOf[T]).get

  
  val basicReplacements = attempt {
    rule {
      // clean up function
      case Function (params, stats, tpe) if stats.nonEmpty => {
        val last = stats.last
        val init = stats.init
        
        // remove unnecessary returns
        val newStats = init ++ Seq(
          last match {
            case Return(x) if tpe == Types.VoidT => x
            
            // don't mess with super constructor
            case a @ Apply(s, _, _) if Inspector.isSuperSelect(s) => a
            
            case Return(Void) => Void
            case x: Typed if tpe != Types.VoidT => Return(x)
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
      case Apply(Select(a @ Apply(Select(_,_,_),_,_),"length",SelectType.Method), _, Types.NumberT) => {
        PropRef(Select(a, "length", SelectType.Prop), Types.NumberT)
      }
      case Apply(Select(i @ Ident(_, Types.StringT), "length", SelectType.Method), _, _) => {
        PropRef(Select(i, "length", SelectType.Prop), Types.NumberT)
      }
      
      // List(1,2,3) => [1,2,3]
      case Apply(Select(Select(CollectionImmutablePkg, "List", SelectType.Module), "apply", SelectType.Method), args, _) => {
        Array(args)
      }
      
      // normalize 
      case Type("scala", Nil) => ScalaType
    }
  }
  
  val cleanUpBlocks = attempt {
    rule {
      // block in function
      case Function (params, List(Block(stats)), tpe) => {
        Function(params, stats, tpe)
      }
    }
  }
  
  val collectionRewrites = attempt {
    rule {
      // List foreach
      case a @ Apply(Select(arr @ Array(_), "foreach", SelectType.Method), args, Types.VoidT) => {
        Apply(Select(Select(Ident("goog", Types.PackageT), "array", SelectType.Module), "forEach", SelectType.Method), Seq(arr) ++ args, Types.VoidT)
      }
      
      // Map[String,*] apply (a bit hacky, but seems to work)
      case a @ Apply(s @ Select(s2 @ PredefMap(), "apply",SelectType.Method),params,tpe) if tpe.name startsWith "scala.collection.immutable.Map[String," => {
        Object {
          params map {case Apply(Select(s @ Apply(_, List(Literal(name, Types.StringT)),_), "$minus$greater", SelectType.Method), ps, tpe) =>
            ObjectItem(name.stripPrefix("\"").stripSuffix("\""), ps.head) 
          }
        }
      }
    }
  }
  
  val removeInheritanceFromObject = topdown {
    attempt {
      rule {
        case c @ Class(_, Some(Types.AnyT), _, _, _) => {
          c.copy(sup = None)
        }
      }
    }
  }
  
  val operators = attempt {
    rule {
      // unary
      case Apply(Select(qualifier, name, t), Nil, _) if operatorMaps.prefix contains name => UnaryOp(qualifier, operatorMaps.prefix(name), Prefix)
      
      // prop assign
      case Apply(Select(sel, m, SelectType.Method), List(param), _) if m endsWith "_$eq" => {
        Assign(Select(sel, m stripSuffix "_$eq", SelectType.Prop), param)
      }
      
      // List.apply: Nil(0) --> [][0]
      case Apply(s @ Select(a @ Array(_), "apply", SelectType.Method), List(key), tpe) => {
        ArrayItemGet(a, key)
      }
      
      // Map.apply: {}.apply("a") --> {}["a"]
      case Apply(s @ Select(o @ ObjectTyped(), "apply", SelectType.Method), List(key), tpe) => {
        ObjectItemGet(o, key)
      }
      
      // Object.get("a") --> {...}["a"]
      case a @ Apply(Select(s: Tree with Typed, "get", SelectType.Method), List(key), _) if s.tpe == Type("Object") => {
        ObjectItemGet(s, key)
      }
    }
  }
  
  val removeSuperCalls = {
    // remove super calls from any blocks
    val remove = attempt {
      rule {
        case Function(params, stats, tpe) => {
          Function (
            params, 
            stats filter {_ match {
              case Return(Apply(Select(Super(This), "<init>", SelectType.Method), _, _)) => false
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
  
  val cleanupConstructors = topdown {
    attempt {
      rule {
        // remove returns from constructor
        case c @ Class(_,_,const @ Method(_,fn,_),_,_) => {
          c.copy(
            constructor = const.copy(
              fun = fn.copy(
                stats = fn.stats flatMap {
                  case Return(x) => Seq(x)
                  case x => Seq(x) 
                }
              )
            ) 
          )
        }
      }
    }
  }
  
  val stdLibReplacements = topdown {
    attempt {
      rule {
        // sys.error -> throw Error
        case a @ Apply(s @ Select(Select(ScalaPkg, "sys", SelectType.Module),"error",SelectType.Method),params,tpe @ Type("Nothing", Nil)) => {
          Throw(Apply(New(Ident("Error", Type("Error"))), params, tpe))
        }
        // StringOps.codePointAt -> string.charCodeAt
        case a @ Apply(fun @ Select(qual, "codePointAt", SelectType.Method),List(num),Type("Number", Nil)) => {
          a.copy(fun = fun.copy(name = "charCodeAt"))
        }
      }
    }
  }
  
  // extractors
  private val ScalaType = Type("_scala_", Nil)
  private val ScalaPkg = Ident("scala", ScalaType)
  private val Predef = Select(ScalaPkg, "Predef", SelectType.Module)
  private val CollectionPkg = Select(ScalaPkg, "collection", SelectType.Module)
  private val CollectionImmutablePkg = Select(CollectionPkg, "immutable", SelectType.Module)
  private object ScalaNil {
    def unapply (x: Tree) = {
      x match {
        case Select(CollectionImmutablePkg, "Nil", SelectType.Module) => Some(())
        case _ => None
      }
    }
  }
  
  object PredefMap {
    def unapply (x: Tree) = {
      x match {
        case Select(Predef, "Map",_) => Some(())
        case _ => None
      }
    }
  }
  
  object ObjectTyped {
    def unapply (x: Tree) = x match {
      case Object(items) => Some(())
      case x: Tree with Typed if x.tpe.name.stripSuffix("scala.collection.immutable.") startsWith "Map[String," => Some(())
      case _ => None
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
