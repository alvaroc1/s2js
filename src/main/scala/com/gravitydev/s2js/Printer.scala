package com.gravitydev.s2js

import ast._
import StringUtil._

object Printer {
  def print (s:SourceFile) = {
    val provides = findProvides(s) map {"goog.provide('"+_+"');\n"} mkString "" + "\n"
    
    val pkg = printPackage(s.pkg)
    
    val code = provides + pkg
    
    code
  }
  
  def printTree (t:Tree) :String = t match {
    case Void => ""
    case Return(expr) => "return " + printTree(expr) + ";\n"
    case Ident(name) => name
    case i:If => printIf(i)
    case Literal(a,_) => a.toString
    case _ => {
      println(t)
      sys.error("not implemented")
    }
  }
  
  def printIf (i:If) = i match {
    case If (cond, thenp, elsep) => {			
      val condition = "if (" + printTree(cond) + ") {\n"
      val body = indent(printWithSemiColon(thenp))
      val elseline = "} else {\n"
      val e = indent(printWithSemiColon(elsep))
      val last = "}\n"

      condition + body + (if (elsep == JsType.VoidT) "" else elseline + e) + last
    }
  }
  
  def printPackage (p:Package) = {
    p.units map {printCompilationUnit(p.name, _)} mkString ""
  }
  
  def printCompilationUnit (pkg:String, cu:CompilationUnit) = cu match {
    case m:Module => printModule(pkg, m)
    case c:Class => printClass(pkg, c)
  }
  
  def printClass (pkg:String, m:Class) = {
    val lhs = (if (pkg != "_default_") pkg+"." else "") + m.name
    
    lhs + " = function () {};\n"
  }
  
  def printModule (pkg:String, m:Module) = {
    val prefix = (if (pkg != "_default_") pkg+"." else "") + m.name
    
    // methods
    val methods = m.methods map {printMethod(prefix, _, false)} mkString "\n"
    
    methods
  }
  
  def printMethod (prefix:String, m:Method, isClassMethod:Boolean) = {
    /*
     * 			case m @ JsMethod (owner, name, params, body, ret) => {
				// jsdoc
				val l = new ListBuffer[String]()
				params foreach ((p) => l += getParamDoc(p))
				if (ret != JsType.VoidT) l += "@return {"+print(ret)+"}"
				val jsdoc = doc(l.toList)
				
				val start = owner.name + "." + (if (owner.isInstanceOf[JsClassRef]) "prototype." else "") + name + " = function (" + printParamList(params) + ") {\n"
				
				val middle = indent(
					print(body)
				)
				val end = "};\n"
					
				jsdoc + start + indent(getDefaultParamsInit(params)) + middle + end + "\n"
			}
     */
    // jsdoc
    val returnDoc = if (m.ret != Types.VoidT) Seq("@return {"+printType(m.ret)+"}") else Seq()
    val jsdoc = doc( (m.params map getParamDoc) ++ returnDoc )
    
    val lhs = prefix + "." + (if (isClassMethod) "prototype." else "") + m.name
    
    val body = printTree(m.body)
    
    val rhs = " = function (" + printParamList(m.params) + ") {" + indent(body) + "};"
    
    jsdoc + lhs + rhs
  }
  
  def getParamDoc (node:Param) = {		
    "@param " + printTypeAnnotation(node) + " " + getParamName(node)
  }
  
  def printType (t:Type) = t match {
    case Types.StringT => "string"
    case Types.BooleanT => "boolean"
    case Types.NumberT => "number"
    case Types.AnyT => "Object"
    case Types.ArrayT => "Array"
    case Types.UnknownT  => "UNKNOWN"
  }
  
  def printTypeAnnotation (node:Tree) :String = node match {
    case Param(_,tpe,default) => {
      "{" + printTypeForAnnotation(tpe) + default.map((a)=>"=").mkString("") + "}" // annotate default param
    }
  }
  
  def printTypeForAnnotation (node:Tree) = {
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
 
  /**
   * Get the name of a param, prepend opt_ if it has default value
   */
  def getParamName (p:Param) = p.default match {
    case Some(x) => "opt_" + p.name
    case None => p.name
  }
  
  private def printParamList (params:Seq[Param]) = {		
    params map getParamName mkString ", "
  }
  
  private def findProvides (node :Tree) :Set[String] = {
    def findProvidesInChildren (b:Branch) = (b.children map {findProvides _}).foldLeft(Set[String]()){_++_}
    
    node match {
      case m @ Module(name, _, _, _, _) => findProvidesInChildren(m) + name
      case c @ Class(name, _, _, _, _) => findProvidesInChildren(c) + name
      case p @ Package("_default_", _) => findProvidesInChildren(p)
      case p:Package => findProvidesInChildren(p) map {p.name + "." + _}
      case b:Branch => findProvidesInChildren(b)
      case _:Leaf => Set()
    }
  }
  
  private def doc (annotations:Seq[String]) = {
    if (annotations.isEmpty) 
      "" 
    else
      "/**\n" + 
      (annotations map {" * "+ _ +"\n"} mkString("")) +
      " */\n"
  }
  
  private def printWithSemiColon (tree : Tree): String = {
    tree match {
      //case a:Apply => printApply(a) + ";\n"
      //case a:Select => printSelect(a) + ";\n"
      //case a:Ident => printIdent(a) + ";\n"
      case x => printTree(x)
    }
  }
}
