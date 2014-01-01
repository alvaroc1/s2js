package com.gravitydev.s2js

import ast._
import StringUtil._
import org.kiama.output.PrettyPrinter
import org.kiama.rewriting.Rewriter
import scala.collection.mutable.ListBuffer
import language.postfixOps

object Printer extends PrettyPrinter {
  import Inspector.{findProvides, findRequires}
  
  def show (t: Tree): Doc = {
    t match {
      case SourceFile(path,name,pkg) => {
        val provides = findProvides(pkg).toSeq
        val requires = findRequires(pkg).toSeq filterNot (provides contains _)
        
        ssep(provides map (x => "goog.provide('" <> x <> "');"), line) <> line <> line <>
        (if (requires.nonEmpty) ssep(requires map (x => "goog.require('" <> x <> "');"), line) <> line <> line else "") <> 
        show(pkg)
      }
      case pkg @ Package(name,units,exports) => {
        ssep(units map (printCompilationUnit(pkg,_)), line) <> line <>
        ssep(exports.toSeq map (x => "goog.exportSymbol('" <> x <> "', " <> x <> ");"), line)
      }
      case Null => "null"
      case Literal(v,_)               => v
      
      //case Void                       => ""
      
      // selection
      case Select(qual, name, stpe) => show(qual) <> "." <> name  
      
      case Ident(name, _)             => name
      
      case x => "UNKNOWN: " + x.toString
    }
  }
  
  private def bracesIfBlock (tree: Tree)(treeDoc: Doc) = tree match {
    case _:Block => braces( nest( line <> treeDoc, 2 )  <> line )
    case _ => nest( treeDoc, 2 )
  }
  
  private def maybeSemi (tree: Tree)(treeDoc: Doc) = tree match {
    //case x:If => treeDoc // don't put semi-colons on IF
    case x => treeDoc <> semi
  }
  
  def showExpr (pkg: Package, unit: CompilationUnit, method: Method, expr: Tree): Doc = {
    def showInner (t: Tree) = showExpr(pkg, unit, method, t)
    
    expr match {
      case Var(name, tpe, rhs)        => "var" <+> name <+> "=" <+> showInner(rhs)
      case Array(elems)               => "[" <> nest( ssep(elems map showInner, ", ") ) <> "]"
      case Return(expr)               => "return" <+> showInner(expr)
      
      case If (cond, thenp, elsep)    => {
        // if else *returns* void, then the whole thing is void
        val (thenxp, elsexp) = (thenp, elsep) match {
          case (Return(x), Return(Void)) => x -> Void
          case _ => thenp -> elsep
        }
        
        "if" <+> parens( showInner(cond) ) <+> 
          bracesIfBlock(thenxp)( showInner( thenxp) ) <>
          (if (elsexp != Void) "" <+> "else" <+> bracesIfBlock(elsexp)( showInner(elsexp) ) else "")
      }
      
      case Cast (subject, tpe) => {
        "/** @type {" <> printTypeForAnnotation(tpe) <> "} */(" <> showInner(subject) <> ")"
      }
      case InfixOp(operand1, operand2, operator) => showInner(operand1) <+> operator <+> showInner(operand2)
      
      case UnaryOp(operand, op, Prefix) => "!" <> showInner(operand)
        
      // block
      case Block(stats) => {
        ssep(stats map {x => maybeSemi(x)(showInner(x))}, line)
      }
      
      // super constructor application
      case Apply(Select(Super(This), "<init>", SelectType.Method), args, tpe) => {
        print(unit.asInstanceOf[Class].sup.get) <> "." <> "call" <> parens( ssep(Seq("this": Doc) ++ (args map showInner), ", ") )
      }
      
      // application
      case Apply(fun, params, tpe) => {
        showInner(fun) <> parens( ssep(params map showInner, ", ") )
      }
      
      // assign
      case Assign(sel, rhs) => {
        showInner(sel) <+> "=" <+> showInner(rhs)
      }
      
      // prop ref
      case PropRef(sel, _) => showInner(sel)
      
      case Select(New(sel), "<init>", SelectType.Method) => {
        "new" <+> showInner(sel)
      }
      
      // top-level selects
      case Select(Ident("$toplevel", Type("_scala_", Nil)), name, _) => name
      
      // selection
      case Select(qual, name, stpe) => showInner(qual) <> "." <> name  
      
      case fn @ Function(params, body, _) => printFunction(pkg, unit, method, fn)
      
      case x => show(expr)
    }
  }
  
  def print (s: Tree) = pretty(show(s)).trim
  
  private def printCompilationUnit (pkg: Package, unit: CompilationUnit) = {
    unit match {
      case c: Class => printClass(pkg, c)
      case m: Module => printModule(pkg, m)
    }
  }
    
  private def printClass (pkg: Package, m:Class) = {
    printConstructor(pkg, m, m.constructor) <> 
    (m.sup.map(x => "goog.inherits(" <> packagePrefix(pkg) <> m.name <> ", " <> print(x) <> ");\n") getOrElse "")
  }

  private def printModule (pkg: Package, mod: Module) = {
    val Module(name,props,methods,classes,modules) = mod
    
    val prefix = packagePrefix(pkg)
    
    ssep(methods map (printMethod(pkg, mod, _)), line)
  }
  
  private def printConstructor (pkg: Package, unit: Class, m:Method) = {
    // jsdoc
    val jsdoc = doc {
      Seq(
        "@constructor"
      ) ++ unit.sup.map(x => "@extends {" + print(x) + "}").toList ++
      (m.fun.params map getParamDoc)
    }
    
    val lhs = packagePrefix(pkg) <> unit.name
    
    val rhs = " = " <> printFunction(pkg, unit, m, m.fun) <> semi <> line
    
    jsdoc <> lhs <> rhs
  }
    
  private def printMethod (pkg: Package, unit: CompilationUnit, m:Method, isConstructor: Boolean = false) = {
    // jsdoc
    val returnDoc = if (m.fun.ret != Types.VoidT) Seq("@return {"+printType(m.fun.ret)+"}") else Seq()
    val jsdoc = doc( (m.fun.params map getParamDoc) ++ returnDoc )
    
    val lhs = if (isConstructor) packagePrefix(pkg) <> unit.name else memberPrefix(pkg, unit) <> m.name
    
    val rhs = " = " <> printFunction(pkg, unit, m, m.fun) <> semi <> line
    
    jsdoc <> lhs <> rhs
  }

  
  private def printType (t:Type) = t match {
    case Types.StringT  => "string"
    case Types.BooleanT => "boolean"
    case Types.NumberT  => "number"
    case Types.AnyT     => "Object"
    case Types.ArrayT   => "Array"
    //case Types.UnknownT => "UNKNOWN"
    case Type(name, _)  => name.stripPrefix("browser.")
  }
  
  private def doc (annotations:Seq[String]) = {
    if (annotations.isEmpty) 
      "" 
    else
      "/**\n" + 
      (annotations map {" * "+ _ +"\n"} mkString("")) +
      " */\n"
  }
 
  private def getParamDoc (node:Param) = {    
    "@param " + printTypeAnnotation(node) + " " + getParamName(node)
  }
 
  def printTypeAnnotation (node:Tree) :String = node match {
    case Param(_,tpe,default) => {
      "{" + printTypeForAnnotation(tpe) + default.map((a)=>"=").mkString("") + "}" // annotate default param
    }
  }
  
  private def packagePrefix (pkg: Package) = {
    (Option(pkg.name) filter (_ != "_default_") map(_+".") getOrElse "")
  }
  
  private def memberPrefix (pkg: Package, unit: CompilationUnit) = 
    packagePrefix(pkg) <> (unit match {
      case Module(name,_,_,_,_) => name
      case Class(name,_,_,_,_) => name <> "." <> "prototype"
    }) <> "."
  
  def printTypeForAnnotation (node: Type) = {
    import Types._
    node match {
      case FunctionT => "Function"

      /*
      // the previous one should catch everything, but just in case
      case Select(Ident("scala",_), "Function1", JsSelectType.Class,_) => {
        "Function"
      }
      */
      case t:Type => printType(t)
      case _ => sys.error("not implemented") //printTree(node)
    }
  }
  
  private def printFunction (pkg: Package, unit: CompilationUnit, method: Method, fn: Function) = {
    "function (" <> printParamList(fn.params) <> ")" <+> "{" <> nest(
      line <> ssep(fn.stats map (x => maybeSemi(x)(showExpr(pkg, unit, method, x))), line),
      2
    ) <> line <> "}"
  }
  
  /**
   * Get the name of a param, prepend opt_ if it has default value
   */
  private def getParamName (p:Param) = p.default match {
    case Some(x) => "opt_" + p.name
    case None => p.name
  }
  
  private def printParamList (params:Seq[Param]) = {    
    params map getParamName mkString ", "
  }
  
}

object TreePrinter {
  
  def printTree (t:Tree): String = {
    /*
    t match {
      case i: If => printIf(i)
      case Literal(a,tpe) => tpe match {
        case Types.StringT => "\"" + a + "\""
        case _ => a
      }
      case Var(id, tpe, rhs) => {
        "var " + id + " = " + printWithSemiColon(rhs) + "\n"
      }
      case Block(stats) => 
        if (stats.length > 1) 
          "{\n" + indent(stats.map(printWithSemiColon).mkString("")) + "}\n"
        else
          printTree(stats.head)
          
      // constructor application
      case Apply(Select(New(q), "<init>", _, _), args, _) => {
        "new " + printTree(q) + "(" + args.map(printTree _).mkString + ")"
      }
      
      // scala
      case Select(Ident("_scala_", _), "println", SelectType.Method,_) => "console.log"
        
      case Select(Ident("<toplevel>",_), s: String, _, _) => s
      
      // operators
      case Apply(Select(q,"$eq$eq", _,_), args, _) => printTree(q) + " == " + args.map(printTree).mkString
      case Apply(Select(q,"$bang$eq", _,_), args, _) => printTree(q) + " != " + args.map(printTree).mkString
      
      // assignment
      case Apply(Select(q,n,SelectType.Method,_), args, _) if n endsWith "_$eq" => {
        printTree(q) + "." + n.stripSuffix("_$eq") + " = " + printTree(args.head)
      }
      
      // String.length
      case Apply (q @ Select(t: Typed, "length", SelectType.Method, _), _, _) if t.tpe == Types.StringT || t.tpe.name.endsWith(")String") => {
        printTree(q)
      }
      /*
      case Apply(q @ Select(Apply(_, _, Types.StringT), "length", SelectType.Method, _), _, _) if true => {
        printTree(q)
      }
      case Apply(q @ Select(Literal(_, tpe), "length", SelectType.Method, _), _, _) if tpe == Types.StringT => {
        printTree(q)
      }*/
          
      case Apply(select, args, _) => {
        printTree(select) + "(" + (args.map(printTree _).mkString(", ")) + ")"
      }
      
      case Select(qualifier, name, _, _) => printTree(qualifier) + "." + name
      case Null => "null"
      case Cast (qualifier, tpe) => "/** @type {" + printType(tpe) +"} */ (" + printTree(qualifier) + ")"
      case fn @ Function (_,_) => printFunction(fn)
      case _ => {
        //sys.error("not implemented: " + t)
        "(NOT IMPLEMENTED: "+t+")"
      }
    }
    * 
    */
    "printtree"
  }
    
    def printIf (i:If) = i match {
      case If (cond, thenp, elsep) => {
        
        // if else *returns* void, then the whole thing is void
        val (thenxp, elsexp) = (thenp, elsep) match {
          case (Return(x), Return(Void)) => x -> Void
          case _ => thenp -> elsep
        }
        
        val condition = "if (" + printTree(cond) + ") "
        val body = indent(printWithSemiColon(thenxp))
        val elseline = " else "
        val e = indent(printWithSemiColon(elsexp))
        val last = ""
  
        condition + body + (if (elsexp == Void) "" else elseline + e) + last
      }
    }
    
    def printType (t:Type) = t match {
      case Types.StringT  => "string"
      case Types.BooleanT => "boolean"
      case Types.NumberT  => "number"
      case Types.AnyT     => "Object"
      case Types.ArrayT   => "Array"
      //case Types.UnknownT => "UNKNOWN"
      case Type(name, _)  => name.stripPrefix("browser.")
    }


    
    /*
    def findRequires (tree: Tree) : List[String] = {
      val reqs: List[String] = tree match {
        // instantiations
        case Apply(Select(New(q), n, _, _), _, _) => {
          List(printTree(q))
        }
        // applications
        case Apply(Select(q @ Select(_,_, SelectType.Class|SelectType.Module, _), n, _, _), _, _) => {
          List(printTree(q))
        }
        /*-=0
        // ignore members
        case JsSelect(JsThis(), _, _, _) => ()
        
        // ignore applications on members
        case JsSelect(JsSelect(JsThis(),_,_, _),_,_, _) => ()
        
        // ignore super calls
        case JsApply(JsSelect(JsSuper(qualifier),_,_, _),_) => ()
        
        // ignore function types
        case JsSelect(JsIdent("scala",_),_,JsSelectType.Class,_) => ()
        
        // ignore right side of JsProperty and JsVar if it's a select, but add the tpe
        case JsVar(id, tpe @ JsSelect(_,_,_,_), JsSelect(_,_,_, _)) => {
          l.append(print(tpe))
        }
        case JsProperty(owner, name, tpt @ JsSelect(_,_,_,_), JsSelect(_,_,_,_), mods) => {
          l.append(print(tpt))
        }
        // TODO: these should be collapsed into one case with guards
        // select class
        case s @ JsSelect(_,_,JsSelectType.Class,_) => {
          l.append(print(s))
        }
        // select module
        case JsSelect(s @ JsSelect(_,_,JsSelectType.Module,_), _, JsSelectType.Method, _ ) => {
          l.append(print(s))
        }
        // select package
        case JsSelect( s @ JsSelect(_,_,JsSelectType.Package,_), _, JsSelectType.Method, _ ) => {
          l.append(print(s))
        }
        // select prop
        case JsSelect (s @ JsSelect(_,_,JsSelectType.Module,_),_,JsSelectType.Prop,_) => {
          l.append(print(s))
        }
        
        // TODO: had to move this one down to not conflict with the package one. Gotta figure out what the deal is
        // ignore applications on local variables
        case JsSelect(JsIdent(_,_),_,_,_) => ()
        
        case JsClassRef(name) => l.append(name)
        */
        case _ => Nil
      }
      
      // check children
      val childrenReqs = Seq.empty[String] /*tree match {
        case _: Leaf => Nil
        case b: Branch => b.children.foldLeft(List[String]())(_ ++ findRequires(_))
      }*/
      
      // remove anything that ends in underscore or starts with browser
      (reqs ++ childrenReqs).filterNot(_.endsWith("_")).filterNot(_.startsWith("browser.")).toList
    }
    * 
    */
    
    private def doc (annotations:Seq[String]) = {
      if (annotations.isEmpty) 
        "" 
      else
        "/**\n" + 
        (annotations map {" * "+ _ +"\n"} mkString("")) +
        " */\n"
    }
    
    private def stripOuterBraces (s: String): String = {
      val trimmed = s.trim
      if (trimmed startsWith "{") stripOuterBraces(trimmed stripPrefix "{" stripSuffix "}")
      else trimmed
    }
    
    private def printWithSemiColon (tree : Tree): String = {
      tree match {
        /*case Block(children) => {
          children.map(printWithSemiColon).mkString
        }*/
        //case a:Apply => printTree(a) + ";\n"
        //case a:Select => printSelect(a) + ";\n"
        //case a:Ident => printIdent(a) + ";\n"
  
        case x @ Void => printTree(x)
        case x: Block => printTree(x)
        case x: If => printTree(x)
        case x: Var => printTree(x)
        case x => printTree(x) + ";\n"
      }
    }
}
