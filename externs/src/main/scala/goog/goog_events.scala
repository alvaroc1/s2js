import goog.Disposable

package goog {
	package object events {
		// is there a better way to overload these?
		def listen [T<:Event](obj:AnyRef, events:List[String], fn:(T)=>Any) {}
		def listen [T<:Event](obj:AnyRef, event:String, fn:(T)=>Any) {}
		def listen (obj:AnyRef, events:List[String], fn:()=>Any) {}
		def listen (obj:AnyRef, event:String, fn:()=>Any) {}
		
		def listenOnce [T<:Event](obj:AnyRef, events:List[String], fn:(T)=>Any) {}
		def listenOnce [T<:Event](obj:AnyRef, event:String, fn:(T)=>Any) {}
		def listenOnce (obj:AnyRef, events:List[String], fn:()=>Any) {}
		def listenOnce (obj:AnyRef, event:String, fn:()=>Any) {}
	}
}

package goog.events {
	
	class BrowserEvent (opt_e:Event=null, opt_currentTarget:browser.Node=null) extends Event ("") {
		val keyCode:Int = 0
	}

	
	class EventHandler (opt_handler:AnyRef = null){
		def listen [T<:Event](obj:AnyRef, events:List[String], fn:(T)=>Any) {}
		def listen [T<:Event](obj:AnyRef, event:String, fn:(T)=>Any) {}
		def listen (obj:AnyRef, events:List[String], fn:()=>Any) {}
		def listen (obj:AnyRef, event:String, fn:()=>Any) {}
	}
	
	class FocusHandler
	
	class EventTarget extends Disposable {
		def dispatchEvent (e :String) :Boolean = false
		def dispatchEvent (e :Event) :Boolean = false
	}
	
	object EventType {
		// Mouse events
		val CLICK = "click"
		val DBLCLICK = "dblclick"
		val MOUSEDOWN = "mousedown"
		val MOUSEUP = "mouseup"
		val MOUSEOVER = "mouseover"
		val MOUSEOUT = "mouseout"
		val MOUSEMOVE = "mousemove"
		val SELECTSTART = "selectstart" // IE, Safari, Chrome

		// Key events
		val KEYPRESS = "keypress"
		val KEYDOWN = "keydown"
		val KEYUP = "keyup"

		// Focus
		val BLUR = "blur"
		val FOCUS = "focus"
		val DEACTIVATE = "deactivate" // IE only
		// NOTE = The following two events are not stable in cross-browser usage.
		//     WebKit and Opera implement DOMFocusIn/Out.
		//     IE implements focusin/out.
		//     Gecko implements neither see bug at
		//     https://bugzilla.mozilla.org/show_bug.cgi?id=396927.
		// The DOM Events Level 3 Draft deprecates DOMFocusIn in favor of focusin:
		//     http://dev.w3.org/2006/webapi/DOM-Level-3-Events/html/DOM3-Events.html
		// You can use FOCUS in Capture phase until implementations converge.
		val FOCUSIN = "focusin"
		val FOCUSOUT = "focusout"

		// Forms
		val CHANGE = "change"
		val SELECT = "select"
		val SUBMIT = "submit"
		val INPUT = "input"
		val PROPERTYCHANGE = "propertychange" // IE only

		// Drag and drop
		val DRAGSTART = "dragstart"
		val DRAGENTER = "dragenter"
		val DRAGOVER = "dragover"
		val DRAGLEAVE = "dragleave"
		val DROP = "drop"

		// WebKit touch events.
		val TOUCHSTART = "touchstart"
		val TOUCHMOVE = "touchmove"
		val TOUCHEND = "touchend"
		val TOUCHCANCEL = "touchcancel"

		// Misc
		val CONTEXTMENU = "contextmenu"
		val ERROR = "error"
		val HELP = "help"
		val LOAD = "load"
		val LOSECAPTURE = "losecapture"
		val READYSTATECHANGE = "readystatechange"
		val RESIZE = "resize"
		val SCROLL = "scroll"
		val UNLOAD = "unload"

		// HTML 5 History events
		// See http://www.w3.org/TR/html5/history.html#event-definitions
		val HASHCHANGE = "hashchange"
		val PAGEHIDE = "pagehide"
		val PAGESHOW = "pageshow"
		val POPSTATE = "popstate"

		// Copy and Paste
		val COPY = "copy"
		val PASTE = "paste"
		val CUT = "cut"

		// HTML 5 worker events
		val MESSAGE = "message"
		val CONNECT = "connect"
	}


}
