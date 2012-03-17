import scala.tools.nsc.Settings
import scala.tools.nsc.Global
import scala.tools.nsc.util.BatchSourceFile
import scala.tools.nsc.reporters.{ConsoleReporter, Reporter}
import com.gravitydev.s2js.{S2JSProcessor, Printer}

object S2JSParser {
  def parse (code :String) = {
    val settings = new Settings

	val scalaLib = "/home/alvaro/.sbt/boot/scala-2.9.1/lib/scala-library.jar"
	settings.classpath.tryToSet(scalaLib :: Nil)
    
    val parser = new S2JSParser(settings)

    val ast = parser.parse(code)
    
    Printer.print(ast)
  }
  def main (args :Array[String]) {
    specs2.run(new BasicCompilerSpec)
  }
}


class S2JSParser (settings:Settings, reporter:Reporter) extends Global(settings, reporter) with S2JSProcessor {
  def this(settings:Settings) = this(settings, new ConsoleReporter(settings))

  def parse (code :String) = {
    val run = new Run() {
      def compileUnit(unit :CompilationUnit) {
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
    
    process(unit)
  }
}
