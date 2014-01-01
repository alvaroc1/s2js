import org.scalatest._, matchers.ShouldMatchers._
import scalax.io.Resource
import com.gravitydev.s2js.ast._

class TranslatorSpec extends FunSpec {
  
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
      
    ExpressionCompiler.checkAST("true", Literal("true", Types.BooleanT), "Bolean")
    ExpressionCompiler.checkAST("() => ()", Function(List(), Nil, Type("Function")), "Empty function")
  }
}
