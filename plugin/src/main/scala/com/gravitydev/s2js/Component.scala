package com.gravitydev.s2js

import scala.tools.nsc.{Global, Phase}
//import scala.tools.nsc.symtab.Symbols
import scala.tools.nsc.plugins.PluginComponent
import scala.collection.mutable.ListBuffer

class Component (val global:Global, val plugin:S2JSPlugin) extends PluginComponent {
  import global._
  import definitions.{ BooleanClass, IntClass, DoubleClass, StringClass, ObjectClass, UnitClass, AnyClass, AnyRefClass, FunctionClass }
  import treeInfo.{ isSuperConstrCall }

  val runsAfter = List[String]("typer")
  
  val phaseName = "s2js"
    
  val outputDir = plugin.output
    
  def collect [T <: Tree] (tree: Tree)(pf: PartialFunction[Tree, T]): List[T] = {
    val lb = new ListBuffer[T]
    tree foreach (t => if (pf.isDefinedAt(t)) lb += pf(t))
    lb.toList
  }
    
  def newPhase (prev:Phase) = new StdPhase(prev) {
    
    val buffer = new StringBuffer
    
    override def name = phaseName
    
    override def apply (unit: CompilationUnit) {
      import java.io._
      
      // output paths
      val path = unit.body.symbol.fullName.replace('.', '/')
      val name = unit.source.file.name.stripSuffix(".scala").toLowerCase
      val dir = plugin.output + "/" + path
      
      // get the package
      var pkg = ""
      unit.body match {
        case PackageDef(pid,_) => pkg = pid.toString
      }
      
      if (plugin.packages contains pkg) {
        // create the directories
        new File(dir).mkdirs
      
        println(s"Compiling $name to $dir")
      
        // transform to Js AST
        val translator = new Translator(global)
        
        lazy val parsedUnit = translator.getSourceFile(unit.asInstanceOf[translator.global.CompilationUnit])
  
        val code = Printer print Processor(parsedUnit)
        
        var stream = new FileWriter(dir + "/" + name + ".js")
        var writer = new BufferedWriter(stream)
        writer write code
        writer.close()
      }
    }
  }
}

