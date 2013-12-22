import org.scalatest._, matchers.ShouldMatchers._
import com.gravitydev.s2js.Printer
import com.gravitydev.s2js.ast._

class SourcePrinterSpec extends FlatSpec with BeforeAndAfter {
  val trueLit = Literal("true", Types.BooleanT)
  
  "Pretty printing" should "produce correct output" in {
    import Printer.print
    
    // literal
    print(trueLit) should be ("true")
    
    // assign
    print(Var("check", Types.BooleanT, trueLit)) should be ("var check = true;")
    
    // array
    print(Array(List(trueLit, trueLit))) should be ("[true, true]")
    
    // module
    print(Module("Stuff", Nil, Nil, Nil, Nil)) should be ("Stuff = {};")
  }
  
}
