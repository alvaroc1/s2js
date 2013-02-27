package goog.array

object `package` {
	def forEach [T] (arr:List[T], fn:(T)=>Unit) {}
	
	def filter [T](arr:List[T], fn:(T)=>Boolean, opt_obj:AnyRef=null):List[T] = null
	
	def map [T,V](arr:List[T], fn:(T)=>V, opt_obj:AnyRef=null):List[V] = null
	
	def contains [T](arr:List[T], obj:T):Boolean = false
	
	def find [T](arr:List[T], fn:(T)=>Boolean, opt_obj:AnyRef=null):T = arr.head
	
	def some [T](arr:List[T], fn:(T)=>Boolean, opt_obj:AnyRef=null):Boolean = false
}
