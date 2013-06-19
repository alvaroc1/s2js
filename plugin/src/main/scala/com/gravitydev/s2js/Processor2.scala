package com.gravitydev.s2js

import scala.tools.nsc.{Global, Phase}
import language.postfixOps

class Processor2 (val global: Global) extends Extractors {
  import global._
  
  import definitions.{ BooleanClass, IntClass, DoubleClass, StringClass, ObjectClass, UnitClass, AnyClass, AnyRefClass, 
    FunctionClass, BoxedUnit_UNIT, BoxedUnitModule }
  
    
  def process (unit : CompilationUnit) = {
    import java.io._
    
    // get the package
    var pkg = ""
    unit.body match {
      case PackageDef(pid,_) => pkg = pid.toString
    }
    
    val name = "somefile"
      
    // transform to Js AST
    lazy val parsedUnit = getSourceFile(unit)
    
    // process the AST
    //processAST(parsedUnit)
    
    parsedUnit
  }
 
  // for debugging
  def inspect (t:Tree):Unit = treeBrowser.browse(t)
  
  protected def getMethods (body:List[AnyRef]) :List[DefDef] = body.collect({ 
    case x:DefDef if !x.symbol.isGetter && !x.symbol.isSetter && !x.symbol.isConstructor => x 
  })
  
  def getSourceFile (u:CompilationUnit) = {
    // output paths
    val path = Some(u.body.symbol.fullName.replace('.', '/')) filterNot {_ == "<empty>"} getOrElse ""
    val name = u.source.file.name.stripSuffix(".scala").toLowerCase
    
    ast.SourceFile(
      path,
      name,
      getPackage(u.body.asInstanceOf[PackageDef]) 
    )
  }
  
  def getPackage (p:PackageDef) = ast.Package(
    Some(p.pid.toString) filterNot {_ == "<empty>"} getOrElse "_default_",
    p.stats filterNot {_.isInstanceOf[Import]} map {getCompilationUnit(_)},
    findExports(p)
  )
  
  def findExports (p: PackageDef) = {
    p.stats collect {
      case m @ ModuleDef(_,_, Template(_,_,body)) => body collect {
        // annotations are in the symbol, not on mods
        case d: DefDef if d.symbol.annotations.find(_.tpe.safeToString == "com.gravitydev.s2js.export").isDefined => p.name.toString + "." + m.name+"."+d.name
      }
    } reduceLeft (_ ++ _) toSet
  }
  
  def getCompilationUnit (tree :Tree) :ast.CompilationUnit = tree match {
    case m:ModuleDef => getModule(m)
    case c:ClassDef => getClazz(c)
    case _ => sys.error("Unhandled")
  }

  def getModule (m :ModuleDef) :ast.Module = m match {
    /*
    // App
    case m @ ModuleDef (mods, name, Template(_ :: a :: _, self, body)) if a.symbol.toString == "trait App" => {
      val s = a.symbol
      
      val b = s.toString()
      
      JsEmpty()
    }
  */
    case m @ ModuleDef (mods, name, Template(parents, self, body)) => {
      val bodyContents = body.groupBy(_ match {
        case x:ValDef if !x.symbol.isParamAccessor && !x.symbol.isValueParameter => "properties"
        case x:DefDef if /*!x.mods.isAccessor*/ !x.mods.isSynthetic && !x.symbol.isConstructor => "methods"
        case x:ClassDef => "classes"
        case x:ModuleDef => "modules"
        case EmptyTree => "ignore"
        case _ => "expressions"
      })
      
      val methods = getMethods(body)
      
      val properties = bodyContents.get("properties").getOrElse(Nil).map(_.asInstanceOf[ValDef])
      val classes = bodyContents.get("classes").getOrElse(Nil).map(_.asInstanceOf[ClassDef])
      val modules = bodyContents.get("modules").getOrElse(Nil).map(_.asInstanceOf[ModuleDef])
      
      // get the expressions in the body of the <init> method, remove the constructor
      val expressions = bodyContents.get("expressions").map(_.filter({
        case x:DefDef if x.mods.isSynthetic => false
        case x:DefDef if x.name.toString == "<init>" => false
        case x if x.symbol.isGetter => false
        case _ => true
      })) getOrElse Nil
      
      /*
      JsModule(
        getJsRef(m.symbol.owner.tpe),
        m.symbol.name.toString,
        expressions map getJsTree,
        properties map getProperty, 
        methods map (getJsTree(_).asInstanceOf[JsMethod]),
        classes map (getJsTree(_).asInstanceOf[JsClass]),
        modules map (getJsTree(_).asInstanceOf[JsModule])
      )
      */
      ast.Module(
        m.symbol.name.toString, 
        properties map {getProperty(_)}, 
        methods map {getMethod(_)}, 
        classes map {getClazz(_)},
        modules map {getModule(_)})
    }
  }
  
  def getClazz (c:ClassDef) = c match {
    case ClassDef(mods, name, tparams, Template(parents, self, body)) => {
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

      ast.Class(
        c.symbol.name.toString,
    //for (s @ Select(qualifier, name) <- parents) yield JsSelect( getJsTree(qualifier), name.toString, JsSelectType.Class, JsType.UnknownT ),
        Nil,
    //getConstructor(c, primary),
        getMethod(primary),
    properties map getProperty,
    methods map getMethod
      )
    }
  }
  
  def getProperty (prop:ValDef) = {      
    /*
    JsProperty(
      getType(prop.symbol.owner),
      prop.name.toString,
      getJsType(prop.tpt.tpe),
      prop match {
        // if it's a param accessor
        case v if v.symbol.isParamAccessor => JsIdent(v.name.toString)
        // if rhs is a param accessor
        case ValDef(_,_,_,rhs @ Select(_,_)) if rhs.symbol.isParamAccessor => JsIdent(rhs.name.toString)
        // other wise
        case v => getJsTree(v.rhs)
      },
      JsModifiers(
        prop.mods.isPrivate  
      )
    )
    */
    ast.Property()
  }
  
  def getMethod (m:DefDef) = {
    val y = m.tpt.tpe
    val x = UnitClass.tpe
    val a = m.tpt == UnitClass
    ast.Method(
      m.name.toString,
      for (v @ ValDef(mods, name, tpt, rhs) <- m.vparamss.flatten) yield getParam(v),
      if (m.tpt.tpe == UnitClass.tpe) getTree(m.rhs) else addReturn(getTree(m.rhs)),
      getType(m.tpt.tpe)
    )
  }
  
  def getParam (v:ValDef) = {
    ast.Param(v.name.toString, getType(v.tpt.tpe), v.rhs match {
      case EmptyTree => None
      case p => Some(getTree(p))
    })  
  }
  
  def getTree (n:Tree) :ast.Tree = n match {
    // unit
    case Literal(Constant(())) => ast.Void
    
    // literals
    case l @ Literal(Constant(x)) => {
      if (x == null) ast.Null 
      else ast.Literal(x.toString, getType(l.tpe).asInstanceOf[ast.Type with ast.BuiltInType])
    }
    
    case b:Block  => getBlock(b)
    case a:Apply  => getApply(a)
    case i:Ident  => getIdent(i)
    case i:If     => getIf(i)
    case v: ValDef => getValDef(v)
    //case d: DefDef => getDefDef(d)
    
    // println
    case Select (Select(This(q),subject), name) if q.toString == "scala" && subject.toString == "Predef" && name.toString == "println" => {
      ast.Select(ast.Ident("console", ast.Type("_scala_")), "log", ast.SelectType.Method, ast.Type("method"))
    }
    
    case Select(qual, name) if name.toString == "package" && qual.toString == "browser" => ast.Ident("<toplevel>", ast.Type("_scala_"))
    
    // collapse package selections
    //case Select(qual, "package") => getTree(qual)
    case Select(qual, name) if name.toString == "package" => getTree(qual) // TODO: make it not rely on toString
    case s: Select => {
      getSelect(s)
    }
    case New(tpt) => {
      ast.New(getTree(tpt))
    }
    
    // cast
    case TypeApply (Select(q,n), tpe :: Nil) if n.toString == "asInstanceOf" => {
      ast.Cast(getTree(q), ast.Type(tpe.toString))
    }
    
    // typeapply - remove it
    case TypeApply (s, _) => getTree(s)
    
    // this is a standalone function
    case f @ DefDef (_,name,_,params,_,rhs) => {
      val tpe = getType(rhs.tpe)
      ast.Var(
        name.toString, 
        getType(rhs.tpe), 
        ast.Function(
          // TODO: this is flattening curried params, not good
          for (v @ ValDef(_,_,_,_) <- params.flatten) yield getParam(v),
          tpe match {
            case _ => addReturn(getTree(rhs))
          }
        )
      )
    }
      
    // TODO: this code should share some code with DefDef
    case f @ Function (vparams, body) => {
      // is there a better way to get a function's return type?
      val tpe = getType(body.tpe)
      ast.Function (
        for (v @ ValDef(mods, name, tpt, rhs) <- vparams) yield getParam(v),
        tpe match {
          // case ast.Void => getTree(body) // why are the types not matching?
          case _ => addReturn(getTree(body))
        }
      ) 
    }
    
    case t @ This(_) if t.tpe.typeSymbol.isModuleClass => {
      val tp = getType(t.tpe)
      
      val parts = tp.name.split('.').toList
      
      parts.tail.foldLeft(ast.Ident(parts.head, ast.Types.UnknownT): ast.Tree) {(a,b) => ast.Select(a, b, ast.SelectType.Other, ast.Types.UnknownT)}
    }
    
    // ignore imports
    case Import(_, _) => ast.Void
    
    case x => {
      //sys.error("not implemented for " + x.getClass)
      println(x) 
      ast.Unknown("Not Implemented! : " + x.toString)
    }
  }
  
  def getApply (a:Apply) = {
    ast.Apply(
      getTree(a.fun),
      a.args filter (x => !(x.toString contains "$default$")) map getTree, // TODO: clean up this hack to remove default params
      getType(a.fun.tpe)
    )
  }
  def getSelect (s: Select) = {
    ast.Select(
      getTree(s.qualifier),
      s.name.toString,
      s.symbol match {
        case _: ModuleSymbol  => ast.SelectType.Module
        case _: ClassSymbol   => ast.SelectType.Class 
        case _: MethodSymbol  => ast.SelectType.Method
        case _                => ast.SelectType.Other
      },
      ast.Type(s.tpe.resultType.toString.stripPrefix("java.lang."))
    )
  }
  
  def getIf (i:If) = {
    ast.If(getTree(i.cond), getTree(i.thenp), getTree(i.elsep))
  }
  
  def getIdent (i:Ident) = {
    ast.Ident(i.name.toString, getType(i.tpe))
  }
  
  def getBlock (b:Block) = {
    ast.Block(
      (b.stats ++ Seq(b.expr)) map getTree  
    )
  }
  
  def getValDef (v: ValDef) = {
   ast.Var(
     v.name.toString,
     getType(v.tpt.tpe),
     getTree(v.rhs)
   )
  }
  
  def getSelect (symbol: Symbol) = {
    //ast.Select(null, symbol.in)
  }
  
  def getTypeName (tpe: TypeName) = {
    println(tpe)
  }
  
  def getType (tpe:Type):ast.Type = {
    import ast.Types._
    
    val tSymbol = tpe.typeSymbol
    
    tpe.typeSymbol match {
      case IntClass|DoubleClass => NumberT
      case StringClass => StringT
      case AnyClass|AnyRefClass|ObjectClass => AnyT
      case BooleanClass => BooleanT
      case UnitClass => VoidT
      case x if FunctionClass.contains(x) => FunctionT
      case x if x.isModuleClass => ast.Type(x.asInstanceOf[ModuleClassSymbol].fullName)
    
      case _ => tpe.typeConstructor.toString match {
        case func if func.startsWith("Function") => FunctionT
        case "List"|"Array" => ArrayT
        case "browser.Object" => ObjectT

        // lame, maybe we don't need this
        case _ => ast.Type(tpe.toString)
      }
    }
  }
  
  def addReturn (tree:ast.Tree):ast.Tree = {
    tree match {
      case ast.If(cond, thenExpr, elseExpr) => ast.If(cond, addReturn(thenExpr), addReturn(elseExpr))
      case ast.Block(stats) => ast.Block(
        if (stats.isEmpty) stats else stats.init ++ (if (stats.last == ast.Void) Seq() else Seq(addReturn(stats.last)))
      )
      case x => ast.Return(tree)
    }
  }
}

object S2JSProcessor {
  
  // TODO figure out how to do parenthesized comparisons
  val comparisonMap = Map(
    "$eq$eq"      -> "==",
    "$bang$eq"    -> "!=",
    "$greater"    -> ">",
    "$greater$eq" -> ">=",
    "$less"       -> "<",
    "$less$eq"    -> "<=",
    "$amp$amp"    -> "&&",
    "$bar$bar"    -> "||"
  )
  
  // TODO: comparison operators should brobably be here
  val infixOperatorMap = Map(
    "$plus"       -> "+",
    "$minus"      -> "-"
  )
}

