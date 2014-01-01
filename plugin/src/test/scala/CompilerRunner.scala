import scala.tools.nsc.Settings
import scala.tools.nsc.Global
import scala.tools.nsc.util.BatchSourceFile
import scala.tools.nsc.reporters.{ConsoleReporter, Reporter}
import com.gravitydev.s2js.{Translator, Processor, Printer, ast}

class S2JSParser (settings: Settings, reporter: Reporter, process: ast.SourceFile => ast.SourceFile) extends Global(settings, reporter) {
  def this(settings:Settings, process: ast.SourceFile => ast.SourceFile = Processor) = this(settings, new ConsoleReporter(settings), process)

  def parse (code :String) = {
    
    val run = new Run() {
      def compileUnit(unit: CompilationUnit) {
        import scala.tools.nsc.NoPhase
        val s = syntaxAnalyzer.newPhase(NoPhase)
        val n = analyzer.namerFactory.newPhase(s)
        val p = analyzer.packageObjects.newPhase(n)
        val t = analyzer.typerFactory.newPhase(p)
        s(unit)
        n(unit)
        p(unit)
        t(unit)
      }
    }
    
    val unit = new CompilationUnit(new BatchSourceFile("somefile.scala", code))
    run.compileUnit(unit)
    
    val translator = new Translator(this)
    val jsAst = translator.translate(unit.asInstanceOf[translator.global.CompilationUnit])
    
    process(jsAst)
  }
}
