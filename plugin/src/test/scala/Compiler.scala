import com.gravitydev.s2js.ast
import org.scalatest.Assertions

abstract class Compiler extends Assertions {
    
  def check (code: String, expected: String, message: String) = {
    /*
    val ex = cleanWhitespace(processExpected(expected.stripMargin('|')))
    val res = cleanWhitespace(S2JSParser.parse(processSource((code.stripMargin('|')))))
    
    assert(res === ex, message + ", Expected: " + ex + ", Actual: " + res)
    */
    //val ast = filter( S2JSParser.parseAST(code) )
    
    val ex = cleanWhitespace(expectedPrefix + expected.stripMargin('|') + expectedSuffix)
    val res = cleanWhitespace(S2JSParser.parse(sourcePrefix + code.stripMargin('|') + sourceSuffix))
    
    val ex2 = ex.stripPrefix(expectedPrefix).stripSuffix(expectedSuffix)
    val res2 = res.stripPrefix(expectedPrefix).stripSuffix(expectedSuffix)
    
    assert(
      res2 === ex2, 
      message + ". Expected: " + ex2 + ", Actual: " + res2
    )
  }
  
  def sourcePrefix: String
  def sourceSuffix: String
  
  def expectedPrefix: String
  def expectedSuffix: String
  
  def processSource (code: String) = sourcePrefix + code + sourceSuffix
    
  def processExpected (expected: String) = sourcePrefix + expected + sourceSuffix

  // TODO: I know this is horrible... to lazy to fix
  private def cleanWhitespace (s:String) = s.stripMargin('|').trim
      .replace("\n\n", "\n").replace("\n", " ").replace("  ", " ")
      .replace("  ", " ").replace("  ", " ").replace("  ", " ")
      .replace(" ;", ";")
  

  private def matchCompiled (code:String, expected:String, filter: ast.Tree => ast.Tree) = {
    //val ast = filter( S2JSParser.parseAST(code) )
    
    val ex = cleanWhitespace(expectedPrefix + expected.stripMargin('|') + expectedSuffix)
    val res = cleanWhitespace(S2JSParser.parse(sourcePrefix + code.stripMargin('|') + sourceSuffix))
    
    val ex2 = ex.stripPrefix(expectedPrefix).stripSuffix(expectedSuffix)
    val res2 = res.stripPrefix(expectedPrefix).stripSuffix(expectedSuffix)
    
    println(res2)
    /*
    ???
    
    assert(
      res2 === ex2, 
      "Expected: " + ex2 + ", Actual: " + res2
    )
    * 
    */
  }
}

class UnitCompiler extends Compiler {
  val sourcePrefix = ""
  val sourceSuffix = ""
  val expectedPrefix = ""
  val expectedSuffix = ""
}

class ExpressionCompiler extends Compiler {
  val sourcePrefix = "object Z { def z = { val x = "
  val sourceSuffix = " } }"
  val expectedPrefix = "goog.provide('Z'); Z.z = function () { var x = "
  val expectedSuffix = "; };"
}

class StatementCompiler extends Compiler {
  val sourcePrefix = "object Z { def z = { "
  val sourceSuffix = "; () } }"
  val expectedPrefix = "goog.provide('Z'); Z.z = function () { "
  val expectedSuffix = " };"
}
