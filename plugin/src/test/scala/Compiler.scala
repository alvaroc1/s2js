import com.gravitydev.s2js.{ast, Printer, Processor}
import org.scalatest.Assertions
import scala.tools.nsc.Settings
import scala.tools.nsc.Global

abstract class Compiler extends Assertions {
  val settings = new Settings

  // must be absolute, can't use ~
  val home = "/Users/alvarocarrasco"
  val base = home + "/workspace/s2js"
    
  val scalaLib = home + "/.sbt/boot/scala-2.10.3/lib/scala-library.jar:" +
      base + "/externs/target/scala-2.10/s2js-externs_2.10-0.1-SNAPSHOT.jar:" +
      base + "/api/target/scala-2.10/s2js-api_2.10-0.1-SNAPSHOT.jar"
        
    settings.classpath.value = scalaLib
    
  lazy val parser = new S2JSParser(settings, process = process)
    
  def checkCode (code: String, expected: String, message: String) = {
    println("===== CODE =====")
    println(code)
    
    val ast = parser.parse(processSource((code.stripMargin('|'))))
    
    val ex = cleanWhitespace(processExpected(expected.stripMargin('|')))
    val s = Printer.print(ast)
    
    println("==== JS ====")
    println(s)
    val res = cleanWhitespace(s)
    
    assert(res === ex, message + ", Expected: " + ex + ", Actual: " + res)
  }
  
  def checkAST (code: String, expected: ast.Tree, message: String) = {
    val res = parser.parse(processSource((code.stripMargin('|'))))
    
    assert(
      res.pkg.units.head.asInstanceOf[ast.Module].methods
        .head.fun.stats
        .head.asInstanceOf[ast.Var].rhs == expected, message + ", Expected: " + expected + ", Actual: " + res)
  }
  
  def sourcePrefix: String
  def sourceSuffix: String
  
  def expectedPrefix: String
  def expectedSuffix: String
  
  def processSource (code: String) = sourcePrefix + code + sourceSuffix
    
  def processExpected (expected: String) = expectedPrefix + expected + expectedSuffix
  
  def trimTreeForPrinting (tree: ast.SourceFile): ast.Tree
  
  def process (tree: ast.SourceFile): ast.SourceFile = {
    println("======= BEFORE =======")
    println(trimTreeForPrinting(tree))
    val res = Processor(tree)
    
    println("======= AFTER ========")
    println(trimTreeForPrinting(res))
    
    res
  }

  // TODO: I know this is horrible... to lazy to fix
  private def cleanWhitespace (s:String) = s.stripMargin('|').trim
      .replace("\n\n", "\n").replace("\n", " ").replace("  ", " ")
      .replace("  ", " ").replace("  ", " ").replace("  ", " ")
      .replace(" ;", ";")
  
}

object CompilationUnitCompiler extends Compiler {
  val sourcePrefix = ""
  val sourceSuffix = ""
  val expectedPrefix = ""
  val expectedSuffix = ""
    
  def trimTreeForPrinting (tree: ast.SourceFile) = {
    tree.pkg
  }
}

object ExpressionCompiler extends Compiler {
  val sourcePrefix = "object Z { def z = { val x = "
  val sourceSuffix = " } }"
  val expectedPrefix = "goog.provide('Z'); Z.z = function () { var x = "
  val expectedSuffix = "; };"
    
  def trimTreeForPrinting (tree: ast.SourceFile) = {
    tree.pkg.units.head.asInstanceOf[ast.Module].methods.head.fun.stats.head.asInstanceOf[ast.Var].rhs
  }
}

object StatementCompiler extends Compiler {
  val sourcePrefix = "object Z { def z = { "
  val sourceSuffix = "; () } }"
  val expectedPrefix = "goog.provide('Z'); Z.z = function () { "
  val expectedSuffix = " };"
    
  def trimTreeForPrinting (tree: ast.SourceFile) = {
    tree.pkg.units.head.asInstanceOf[ast.Module].methods.head
  }
}
