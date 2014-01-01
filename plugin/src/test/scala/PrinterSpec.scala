import org.scalatest._, matchers.ShouldMatchers._
import com.gravitydev.s2js.Printer
import com.gravitydev.s2js.ast._

class PrinterSpec extends FlatSpec with BeforeAndAfter {
  val trueLit = Literal("true", Types.BooleanT)
  
  "Pretty printing" should "produce correct output" in {
    import Printer.print
    
    // literal
    print(trueLit) should be ("true")
    
    // assign
    print(Var("check", Types.BooleanT, trueLit)) should be ("var check = true")
    
    // array
    print(Array(List(trueLit, trueLit))) should be ("[true, true]")
    
    // application
    print(Apply(Ident("test", Type("_scala_")), Seq(Literal("\"s\"", Types.StringT)), Types.NumberT)) should be ("test(\"s\")")
    
    // module
    //print(Package("check", List(Module("Stuff", Nil, Nil, Nil, Nil)), Set())) should be ("check.Stuff = {};")
  }
  
}
