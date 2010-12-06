package goog.structs 

object `package` {
	def forEach (m:Map, fn:(String, String) => Unit) {}
	
	def map [T <: Any, R <:Any] (m:scala.collection.immutable.Map[String,T], fn:(T, String)=>R) : scala.collection.immutable.Map[String, R] = null
	
	def getValues [T <:Any](m:scala.collection.immutable.Map[String, T]) : List[T] = null
}

class Map {
	def set (key:String, value:String):this.type = null
}
