package vanity2.util
	
object `package` {
	def buildHttpQuery (params:Map[String, String]) = {
		//params.map( (p) => p._1 +"="+ p._2 ).mkString("&")
		
		goog.structs.map[String, String](params, (value, key) => key+"="+value).mkString("&")
	}
}
