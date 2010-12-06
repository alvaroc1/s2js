package goog.net 

object XhrIo {
	def send (url:String, callback:()=>Unit = null, method:String="", content:String="", headers:Map[String, String]=null, timeoutInterval:Int=0) {}
}