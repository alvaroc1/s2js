package com.gravitydev.s2js

import scala.tools.nsc.{Global, Phase}
import language.postfixOps

class Translator (val global: Global) {
  import global._
  
  import definitions.{ BooleanClass, IntClass, DoubleClass, StringClass, ObjectClass, UnitClass, AnyClass, AnyRefClass, 
    FunctionClass, BoxedUnit_UNIT, BoxedUnitModule }
  
    
  def translate (unit: CompilationUnit): ast.SourceFile = {
    import java.io._
    
    // get the package
    var pkg = ""
    unit.body match {
      case PackageDef(pid,_) => pkg = pid.toString
    }
    
    val name = "somefile"
      
    // transform to Js AST
    lazy val parsedUnit = getSourceFile(unit)
    
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
  
  private def pos (p: Tree) = new scala.util.parsing.input.Position {
    val column = p.pos.column
    val line = p.pos.line
    val lineContents = p.pos.lineContent
  }
  
  def getPackage (p:PackageDef) = ast.Package(
    Some(p.pid.toString) filterNot {_ == "<empty>"} getOrElse "_default_",
    p.stats filterNot {_.isInstanceOf[Import]} map {getCompilationUnit(_)},
    findExports(p)
  ).setPos(pos(p))
  
  def findExports (p: PackageDef) = {
    p.stats.collect {
      case m @ ModuleDef(_,_, Template(_,_,body)) => body collect {
        // annotations are in the symbol, not on mods
        case d: DefDef if d.symbol.annotations.find(_.tpe.safeToString == "com.gravitydev.s2js.export").isDefined => p.name.toString + "." + m.name+"."+d.name
      }
    }.foldLeft(Seq.empty[String])(_ ++ _) toSet
  }
  
  def getCompilationUnit (tree :Tree) :ast.CompilationUnit = tree match {
    case m:ModuleDef => getModule(m)
    case c:ClassDef => getClazz(c)
    case _ => sys.error("Unhandled")
  }

  def getModule (m :ModuleDef) :ast.Module = m match {
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
      
      ast.Module(
        m.symbol.name.toString, 
        properties map {getProperty(_)}, 
        methods map {getMethod(_)}, 
        classes map {getClazz(_)},
        modules map {getModule(_)})
    }
  }
  
  def getClazz (c: ClassDef) = {
    val ClassDef(mods, name, tparams, Template(parents, self, body)) = c
    
    val primary = body.collect({ case d:DefDef if d.symbol.isPrimaryConstructor => d }) head

    val params = primary.vparamss.flatten
    
    val constructorInitializedProps = body.collect {
      case v @ ValDef(_,name,_, s @ Select(_, param: Name)) if !v.symbol.isParamAccessor => {
        // must trim name, they come with a trailing space
        name.toString.trim -> ast.Assign(ast.Select(ast.This, name.toString.trim, ast.SelectType.Prop), ast.Ident(param.toString, getType(s.tpe)))
      }
    }
    
    val constStatements = body.collect {
      case a @ Apply(_,_) => getTree(a)
    }

    // get properties
    // all valdefs that have a corresponding accessor
    val accessorNames = body.collect({ case x:DefDef if x.symbol.isGetter => x.name.toString })
    val properties = body.collect({
      // don't know why but valdef's name has a space at the end
      // trim it
      case x:ValDef if accessorNames.contains(x.name.toString.trim) && !constructorInitializedProps.map(_._1).contains(x.name.toString.trim) => {
        println(x.name.toString)
        println(constructorInitializedProps.map(_._1))
        x
      }
    })

    val methods = getMethods(body)
    
    val superTypes = parents map (_.tpe) map getType 
    
    val const = getMethod(primary)
    
    def removeVoidReturn (fun: ast.Function) = {
      fun.copy(
        stats = fun.stats.filter {
          case ast.Return(ast.Void) => false
          case _ => true
        }
      )
    }
    
    ast.Class(
      c.symbol.name.toString,
      superTypes.headOption,
      
      // rebuild constructor with initialized props
      const.copy(
        fun = const.fun.copy(
          stats = removeVoidReturn(const.fun).stats ++ constructorInitializedProps.map(_._2) ++ constStatements
        )
      ),
      properties map getProperty,
      methods map getMethod
    )
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
    ast.Property(
      prop.name.toString,
      getTree(prop.rhs),
      getType(prop.tpt.tpe),
      ast.Modifiers(
        isPrivate = prop.symbol.isPrivate
      )
    )
  }
  
  def getMethod (m:DefDef) = {
    val y = m.tpt.tpe
    val x = UnitClass.tpe
    val a = m.tpt == UnitClass
        
    ast.Method(
      m.name.toString,
      ast.Function(
        for (v @ ValDef(mods, name, tpt, rhs) <- m.vparamss.flatten) yield getParam(v),
        m.rhs match {
          case x: Block => (x.stats map getTree) ++ Seq(ast.Return(getTree(x.expr)))
          case x => Seq(getTree(x))
        },
        getType(m.tpt.tpe)
      ),
      ast.Modifiers(
        isPrivate = m.symbol.isPrivate,
        isOverride = m.symbol.isOverridingSymbol
      )
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
      else {
        val tpe = getType(l.tpe).asInstanceOf[ast.Type with ast.BuiltInType]
        val value = if (tpe == ast.Types.StringT) "\"" + x.toString + "\"" else x.toString
        ast.Literal(value, tpe)
      }
    }
    
    case b:Block  => getBlock(b)
    case a:Apply  => getApply(a)
    case i:Ident  => getIdent(i)
    case i:If     => getIf(i)
    case v: ValDef => getValDef(v)
    //case d: DefDef => getDefDef(d)
    
    // TODO: move to Processor?
    // println
    case Select (Select(This(q),subject), name) if q.toString == "scala" && subject.toString == "Predef" && name.toString == "println" => {
      ast.Select(ast.Ident("console", ast.Type("_scala_")), "log", ast.SelectType.Method)
    }
    
    case Select(qual, name) if name.toString == "package" && qual.toString == "browser" => ast.Ident("$toplevel", ast.Type("_scala_"))
    
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
          Seq(getTree(rhs)),
          tpe
        )
      )
    }
      
    // TODO: this code should share some code with DefDef
    case f @ Function (vparams, body) => {
      // is there a better way to get a function's return type?
      val tpe = getType(body.tpe)
      ast.Function (
        for (v @ ValDef(mods, name, tpt, rhs) <- vparams) yield getParam(v),
        f.body match {
          case x:Block => (x.stats map getTree) ++ Seq(ast.Return(getTree(x.expr)))
          case x => Seq(getTree(x))
        },
        tpe
      ) 
    }
    
    case t @ This(_) if t.tpe.typeSymbol.isModuleClass => {
      val tp = getType(t.tpe)
      
      val parts = tp.name.split('.').toList
      
      parts.tail.foldLeft(ast.Ident(parts.head, ast.Type("_scala_")): ast.Tree) {(a,b) => ast.Select(a, b, ast.SelectType.Module)}
    }
    
    // ignore imports
    case Import(_, _) => ast.Void
    
    case Super(qual, _) => ast.Super(getTree(qual))
    
    case This(qual) => ast.This
    
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
      getType(a.tpe)
    )
  }
  def getSelect (s: Select) = {
    def getSelType (sym: Symbol) = sym match {
      case _: ModuleSymbol  => ast.SelectType.Module
      case _: ClassSymbol   => ast.SelectType.Class 
      case x: MethodSymbol  => {
        ast.SelectType.Method
      }
      case _                => ast.SelectType.Module
    }
    
    s.tpe match {
      // if the type is a TypeRef, that probably means this is a parameterless-method application
      case _: TypeRef if s.symbol.isAccessor => ast.PropRef(
        ast.Select(getTree(s.qualifier), s.name.toString, getSelType(s.symbol)),
        getType(s.tpe)
      )
      case _: TypeRef if !s.symbol.isAccessor => ast.Apply(
        ast.Select(
          getTree(s.qualifier),
          s.name.toString,
          getSelType(s.symbol)
        ),
        Nil,
        getType(s.tpe)
      )
      // else this is probably a MethodType
      case _ => ast.Select(
        getTree(s.qualifier),
        s.name.toString,
        getSelType(s.symbol)
      )
    }
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

