import org.scalatest._, matchers.ShouldMatchers._
import com.gravitydev.s2js.Processor
import com.gravitydev.s2js.ast._

class ProcessorSpec extends FlatSpec with BeforeAndAfter {
  import Processor.process
  val trueLit = Literal("true", Types.BooleanT)
  
  "Processing" should "produce correct output" in {
    process(Literal("true", Types.StringT)) should be (Literal("true", Types.StringT))
  }
  
}
