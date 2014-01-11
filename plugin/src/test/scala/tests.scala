import org.scalatest.FunSpec
import com.gravitydev.s2js.ast
import scalax.io.Resource
import scala.language.postfixOps;

class BasicCompilerSpec extends FunSpec {
  
  private def parseFile (file: String): List[(String,String,String)] = {
    val l = (Resource.fromFile(file).string() split "####" toList).tail
    val parsed = l map {s => 
      val r = (s split ("----") toList)
      val title :: rest = r(0) split "\n" toList;
      (title.stripPrefix("#### "), rest.mkString("\n"), r(1))
    }
    parsed
  }
  
  describe("Expressions Compiler") {
    val basePath = "/Users/alvarocarrasco/workspace/s2js/plugin/src/test/resources"
      
    parseFile(basePath + "/expressions.txt") map {case (title, sc, js) => ExpressionCompiler.checkCode(sc, js, title)}
    parseFile(basePath + "/browser.txt") map {case (title, sc, js) => StatementCompiler.checkCode(sc, js, title)}
    parseFile(basePath + "/collections.txt") map {case (title, sc, js) => ExpressionCompiler.checkCode(sc, js, title)}
    parseFile(basePath + "/statements.txt") map {case (title, sc, js) => StatementCompiler.checkCode(sc, js, title)}
    parseFile(basePath + "/modules.txt") map {case (title, sc, js) => CompilationUnitCompiler.checkCode(sc, js, title)}
    parseFile(basePath + "/classes.txt") map {case (title, sc, js) => CompilationUnitCompiler.checkCode(sc, js, title)}

    parseFile(basePath + "/temp.txt") map {case (title, sc, js) => ExpressionCompiler.checkCode(sc, js, title)}
    
    //parseFile(basePath + "/map2.txt") map {case (title, sc, js) => CompilationUnitCompiler.checkCode(sc, js, title)}
      
  }
}
