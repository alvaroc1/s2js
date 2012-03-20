import org.specs2.mutable._
import S2JSParser.parse
import org.specs2.main.SmartDiffs

class BasicCompilerSpec extends Specification {
  
  "Compiler" should {
    def matchCompiled (code:String, expected:String) = {
      val ex = cleanWhitespace(expected.stripMargin('|'))
      val res = cleanWhitespace(parse(code.stripMargin('|')))
    
      println("Expected: ")
      println(ex)
      println("Actual: ")
      println(res)
      
      res mustEqual ex
    }
    def cleanWhitespace (s:String) = s.stripMargin('|').trim.replace("\n\n", "\n").replace("\n", " ").replace("  ", " ").replace("  ", " ")
    
    val map = {
      val l = {
        io.Source.fromFile("/Users/alvarocarrasco/workspace/s2js/src/test/resources/map.txt").getLines mkString("\n") split("####") toList
      }
      l map {s => 
        val r = s split ("----")
        (r(0), r(1))
      }
    }
    
    "parse correctly" in {
      map map {code => 
        matchCompiled(code._1, code._2)
      } last
    }
  }
}
