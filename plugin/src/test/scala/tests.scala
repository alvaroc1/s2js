import org.scalatest.FunSpec
import com.gravitydev.s2js.ast

class BasicCompilerSpec extends FunSpec {
  
  private def parseFile (file: String): List[(String,String)] = {
    val l = io.Source.fromFile(file).getLines mkString "\n" split "####" toList;
    l map {s => 
      val r = s split ("----") toList;
      (r(0), r(1))
    }
  }
  
  describe("Compiler") {
    val basePath = "/Users/alvarocarrasco/workspace/s2js/src/test/resources"
    val basic = parseFile(basePath + "/basic.txt")
    
    val filter = (tree: ast.Tree) => {
      tree match {
        case ast.SourceFile(_,_,ast.Package(_,ast.Module(_,_,ast.Method(_,_,ast.Block(unit::_),_)::Nil,_,_) :: Nil)) => unit
      }
    }
    
    basic map {case (sc, js) =>
      println(sc)
      // wrap
      matchCompiled(
        "object Z { def z = { " + sc + "; () } } ", 
        "goog.provide('Z'); Z.z = function () { " + js + " };", 
        filter
      )
    }
    
    //val codeMap = parseFile(basePath + "/map.txt")
    val codeMap = parseFile(basePath + "/map2.txt")
    
    codeMap map {case (sc, js) =>  
      it("parse correctly: " + sc.take(30)) {
        matchCompiled(sc, js, x=>x) 
      }
    }
  }

  private def matchCompiled (code:String, expected:String, filter: ast.Tree => ast.Tree) = {
    val ast = filter( S2JSParser.parseAST(code) )
    println(ast)
    
    val ex = cleanWhitespace(expected.stripMargin('|'))
    val res = cleanWhitespace(S2JSParser.parse(code.stripMargin('|')))
  
    println("Expected: ")
    println(ex)
    println("Actual: ")
    println(res)
    
    assert(res === ex)
  }
  
  // TODO: I know this is horrible... to lazy to fix
  private def cleanWhitespace (s:String) = s.stripMargin('|').trim
      .replace("\n\n", "\n").replace("\n", " ").replace("  ", " ")
      .replace("  ", " ").replace("  ", " ").replace("  ", " ")
  
}
