
package goog {
	package object events {
		// is there a better way to overload these?
		def listen [T<:Event](obj:AnyRef, events:List[String], fn:(T)=>Any) {}
		def listen [T<:Event](obj:AnyRef, event:String, fn:(T)=>Any) {}
		def listen (obj:AnyRef, events:List[String], fn:()=>Any) {}
		def listen (obj:AnyRef, event:String, fn:()=>Any) {}
	}
}

package goog.events {

	class Event (tpe:String, opt_target:AnyRef = null) {
		val target = opt_target
	}
	
	class EventHandler (opt_handler:AnyRef = null){
		def listen [T<:Event](obj:AnyRef, events:List[String], fn:(T)=>Any) {}
		def listen [T<:Event](obj:AnyRef, event:String, fn:(T)=>Any) {}
		def listen (obj:AnyRef, events:List[String], fn:()=>Any) {}
		def listen (obj:AnyRef, event:String, fn:()=>Any) {}
	}
	
	class FocusHandler
	
	class EventTarget

}
