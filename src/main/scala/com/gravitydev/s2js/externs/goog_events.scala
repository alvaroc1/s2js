
package goog {
	package object events {
		// is there a better way to overload these?
		def listen (obj:AnyRef, events:List[String], fn:(Event)=>Unit) {}
		def listen (obj:AnyRef, event:String, fn:(Event)=>Unit) {}
		def listen (obj:AnyRef, events:List[String], fn:()=>Unit) {}
		def listen (obj:AnyRef, event:String, fn:()=>Unit) {}
	}
}

package goog.events {

	class Event
	
	class EventHandler {
		def listen (obj:AnyRef, events:List[goog.ui.Component.EventType], fn:()=>Unit) {}
	}
	
	class FocusHandler

}
