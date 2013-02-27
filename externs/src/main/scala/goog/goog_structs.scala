package goog.structs 

import browser._

object `package` {
	def forEach (m:Map, fn:(String, String) => Unit) {}
	
	def map [T <: Any, R <:Any] (m:Object, fn:(T, String)=>R) : Object = null
	
	def getValues [T <:Any](m:Object) : List[T] = null
}

class Map {
	def set (key:String, value:String):this.type = null
}
