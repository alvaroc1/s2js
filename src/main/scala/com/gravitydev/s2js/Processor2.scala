package com.gravitydev.s2js

import scala.tools.nsc.{Global, Phase}

trait Processor2 { self :S2JSProcessor with Global => 
  import definitions.{ BooleanClass, IntClass, DoubleClass, StringClass, ObjectClass, UnitClass, AnyClass, AnyRefClass, 
    FunctionClass, BoxedUnit_UNIT, BoxedUnitModule }
  
  def getJsSourceFile (u:CompilationUnit) = {
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
    p.stats filterNot {_.isInstanceOf[Import]} map {getCompilationUnit(_)}
  )
  
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
        case x:ValDef if !x.symbol.isParamAccessor && !x.symbol.isParameter => "properties"
        case x:DefDef if /*!x.mods.isAccessor*/ !x.mods.isSynthetic && !x.symbol.isConstructor => "methods"
        case x:ClassDef => "classes"
        case x:ModuleDef => "modules"
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
    case l @ Literal(Constant(x)) => ast.Literal(x.toString, getType(l.tpe).asInstanceOf[ast.Type with ast.BuiltInType])
    
    case b:Block => getBlock(b)
    case a:Apply => getApply(a)
    case i:Ident => getIdent(i)
    case i:If => getIf(i)
    case _ => {
      println("test")
      sys.error("not implemented")
    }
  }
  
  def getApply (a:Apply) = {
    ast.Apply(
      
    )
  }
  
  def getIf (i:If) = {
    ast.If(getTree(i.cond), getTree(i.thenp), getTree(i.elsep))
  }
  
  def getIdent (i:Ident) = {
    ast.Ident(i.name.toString)
  }
  
  def getBlock (b:Block) = {
    ast.Block(
      b.stats map getTree  
    )
  }
  
  def getType (tpe:Type):ast.Type = {
    import ast.Types._
    
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
  
  def addReturn (tree:ast.Tree):ast.Tree = tree match {
    case ast.If(cond, thenExpr, elseExpr) => ast.If(cond, addReturn(thenExpr), addReturn(elseExpr))
    /* case JsBlock(stats, expr) => JsBlock(stats, addReturn(expr))
    case JsType.VoidT => JsType.VoidT */
    case x => ast.Return(tree)
  }
}   

// vim: set ts=2 sw=2 et:
