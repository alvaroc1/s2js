package com.gravitydev.s2js

import scala.tools.nsc.{Global, Phase}
import scala.tools.nsc.symtab.Symbols
import scala.tools.nsc.plugins.PluginComponent
import scala.collection.mutable.ListBuffer
import StringUtil._
import S2JSProcessor._
import JsAstProcessor._

trait S2JSProcessor extends Processor2 { self :Global =>
  import definitions.{ BooleanClass, IntClass, DoubleClass, StringClass, ObjectClass, UnitClass, AnyClass, AnyRefClass, FunctionClass }
  import treeInfo.isSuperConstrCall

  val runsAfter = List[String]("typer")
  
  val phaseName = "s2js"
    
  def collect [T <: Tree] (tree: Tree)(pf: PartialFunction[Tree, T]): List[T] = {
    val lb = new ListBuffer[T]
    tree foreach (t => if (pf.isDefinedAt(t)) lb += pf(t))
    lb.toList
  }
    
  def process (unit : self.CompilationUnit) = {
    import java.io._
    
    // get the package
    var pkg = ""
    unit.body match {
      case PackageDef(pid,_) => pkg = pid.toString
    }
    
    val name = "somefile"
      
    // transform to Js AST
    lazy val parsedUnit = getJsSourceFile(unit)
    
    /*
    val processed = JsAstProcessor process parsedUnit
    
    println(processed)
    
    // print and save
    val code = JsPrinter print processed

    code
    */
    parsedUnit
  }

  // for debugging
  def inspect (t:Tree):Unit = treeBrowser.browse(t)
  
  def getJsType (tpe:Type):JsType = {
    val ts = tpe.typeSymbol
    
    tpe.typeSymbol match {
      case IntClass|DoubleClass => JsType.NumberT
      case StringClass => JsType.StringT
      case AnyClass|AnyRefClass|ObjectClass => JsType.AnyT
      case BooleanClass => JsType.BooleanT
      case x if FunctionClass.contains(x) => JsType.FunctionT
      case x if x.isModuleClass => JsType(x.asInstanceOf[ModuleClassSymbol].fullName)
      
      case _ => tpe.typeConstructor.toString match {
        case func if func.startsWith("Function") => JsType.FunctionT
        case "List"|"Array" => JsType.ArrayT
        case "browser.Object" => JsType.ObjectT
        
        // lame, maybe we don't need this
        case _ => JsType(tpe.toString)
      }
    }
  }
  
  def getJsRef (tpe:Type):JsRef = {      
    tpe.typeSymbol match {
      case x if x.isModuleClass => JsModuleRef(x.asInstanceOf[ModuleClassSymbol].fullName)
      case x if x.isClass => JsClassRef(x.asInstanceOf[ClassSymbol].fullName)
      case x if x.isPackage => JsPackageRef(x.fullName)
    }
  }
  
  def getJsRef (sym:Symbol):JsRef = {
    sym match {
      case x if x.isModuleClass => JsModuleRef(x.asInstanceOf[ModuleClassSymbol].fullName)
      case x if x.isClass => JsClassRef(x.asInstanceOf[ClassSymbol].fullName)
      case x if x.isPackage => JsPackageRef(x.nameString)
      case x if x.isMethod => JsMethodRef(x.fullName)
      case x => JsUnknownRef(x.fullName)
    }
  }
  
  /* p match {
    case PackageDef(pid, stats) => {
      
      val path = p.symbol.fullName.replace('.', '/')
      
      JsSourceFile(path, name, 
        stats.map(_ match {
          // discard imports
          case t:Import => null
          
          case x => getJsTree(x)
          
        }).filter(_ != null)
      )
    }
  }
  */
  
  def getJsTreeList[T <: JsTree] (l:List[Tree]):List[T] = l map (getJsTree(_).asInstanceOf[T])
  
  protected def getMethods (body:List[AnyRef]) :List[DefDef] = body.collect({ 
    case x:DefDef if !x.symbol.isGetter && !x.symbol.isSetter && !x.symbol.isConstructor => x 
  })
  
  def getJsTree (node:Tree) : JsTree = node match {
    /*
    case c @ ClassDef(mods, name, tparams, Template(parents, self, body)) => {        
      val primary = body.collect({ case d:DefDef if d.symbol.isPrimaryConstructor => d }) head
      
      val params = primary.vparamss.flatten
      
      //val properties = body.collect({ case x:ValDef if !x.symbol.isParamAccessor && !x.symbol.isParameter => x })
            
      // get properties
      // all valdefs that have a corresponding accessor
      val accessorNames = body.collect({ case x:DefDef if x.symbol.isGetter => x.name.toString })
      val properties = body.collect({
        // don't know why but valdef's name has a space at the end
        // trim it
        case x:ValDef if accessorNames.contains(x.name.toString.trim) => x
      })
    
      val methods = getMethods(body)

      JsClass(
        getJsRef(c.symbol.owner.tpe),
        c.symbol.name.toString,
        for (s @ Select(qualifier, name) <- parents) yield JsSelect( getJsTree(qualifier), name.toString, JsSelectType.Class, JsType.UnknownT ),
        getConstructor(c, primary),
        properties map getProperty,
        methods map (getJsTree(_).asInstanceOf[JsMethod])
      )
    }
    */

    // property re-assignment
    // TODO: move to transform
    case a @ Apply(fun @ Select(qualifier, name), args) if name.toString.endsWith("_$eq") => {
      
      val n = name.toString stripSuffix "_$eq"
    
      val select = JsSelect (
        getJsTree(qualifier), 
        // remove the suffix
        name.toString stripSuffix "_$eq",
        JsSelectType.Other,
        JsType.UnknownT
      )
      
      JsAssign(select, getJsTree(args.head))
    }
    
    case Assign (lhs, rhs) => {
      JsAssign(getJsTree(lhs), getJsTree(rhs))
    }

    case New ( select ) => {
      JsNew( getJsRef(select.tpe) )
    }
    
    /*
    // application on super
    case Apply(Select(qualifier @ Super(qual, mix), name), args) => {
      JsApply( JsSelect( JsSuper(getType(qualifier.symbol)), name.toString, JsSelectType.Method ), getJsTreeList(args))
    }
    */
    
    // application (select)
    case Apply(fun @ Select(qualifier, name), args) => {
      JsApply( JsSelect( getJsTree(qualifier), name.toString, JsSelectType.Method, getJsType(fun.tpe.resultType) ), getJsTreeList(args))
    }
    
    case v @ ValDef(mods,name,tpt,rhs) => {
      //inspect(v)
      var t = getJsTree(rhs)
      
      JsVar(name.toString, getJsType(tpt.tpe), (if (rhs.isEmpty) JsEmpty() else getJsTree(rhs)))
    }
    case b @ Block(stats,expr) => {
      //println(b)
      JsBlock( 
        getJsTreeList[JsTree](
          // remove imports
          stats.filterNot(_.isInstanceOf[Import])
        ), 
        getJsTree(expr) 
      )
    }
    
    // Unit 
    case Literal(Constant(())) => JsType.VoidT
    
    case This(qual) => {
      JsThis()
    }
    
    case l @ Literal(Constant(value)) => {
      getLiteral(l)
    }
    
    //case c @ ClassDef(_,_,_,_) => getClass(c)
    
    case If(cond, thenp, elsep) => {
      JsIf(getJsTree(cond), getJsTree(thenp), getJsTree(elsep))
    }
    
    case Apply (select, args) => {
      
      JsApply(
        getJsTree(select), args map getJsTree
      )
    }
    
    case TypeApply(s, args) => {
      JsTypeApply(getJsTree(s), args map getJsTree)
    }
    
    case Throw (expr:Tree) => {
      JsThrow (getJsTree(expr))
    }
    
    /*
    // this code should share some code with DefDef
    case f @ Function (vparams, body) => {
      // is there a better way to get a function's return type?
      val tpe = getType(body.tpe.typeSymbol)
      JsFunction (
        for (v @ ValDef(mods, name, tpt, rhs) <- vparams) yield getParam(v),
        tpe match {
          case JsType.VoidT => getJsTree(body) 
          case _ => addReturn(getJsTree(body))
        }
      ) 
    }
    */
    
    case Return (expr) => {
      JsReturn (getJsTree(expr))
    }
    
    //case p:PackageDef => getPackage(p)

    case EmptyTree => {
      JsEmpty()
    }
    
    case t @ Typed (expr, tpt) => {
      JsBlock(Nil, getJsTree(expr))
    }
    
    case t:Tree => {
      println(t)
      JsOther(t.getClass.toString, for (child <- t.children) yield getJsTree(child))
    }
  }
  
  def getLiteral (node:Tree):JsLiteral = {
    node match {
      case Literal(Constant(value)) => value match {
        case v:String => JsLiteral("\""+v.toString.replace("\"", "\\\"")+"\"", JsType.StringT)
        case _ => JsLiteral( if (value != null) value.toString else "null", JsType.NullT )
      }
      case a @ _ => {
        JsLiteral("", JsType.UnknownT)
      }
    }
  }
  
  def getSuperClass (c:ClassDef):Option[String] = {
    val superClass = c.impl.parents.head
    if (superClass.toString == "java.lang.Object") None else Some(superClass.tpe.toString)
  }
  
  /*
  def addReturn (tree:JsTree):JsTree = tree match {
    case JsIf(cond, thenExpr, elseExpr) => JsIf(cond, addReturn(thenExpr), addReturn(elseExpr))
    case JsBlock(stats, expr) => JsBlock(stats, addReturn(expr))
    case JsType.VoidT => JsType.VoidT
    case x => JsReturn(tree)
  }
  */
}

object S2JSProcessor {
  
  // TODO figure out how to do parenthesized comparisons
  val comparisonMap = Map(
    "$eq$eq"    -> "==",
    "$bang$eq"     -> "!=",
    "$greater"    -> ">",
    "$greater$eq"   -> ">=",
    "$less"      -> "<",
    "$less$eq"    -> "<=",
    "$amp$amp"    -> "&&",
    "$bar$bar"    -> "||"
  )
  
  // TODO: comparison operators should brobably be here
  val infixOperatorMap = Map(
    "$plus" -> "+",
    "$minus" -> "-"
  )
}
