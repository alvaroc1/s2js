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

	class Event (tpe:String, opt_target:AnyRef = null) {
		val target = opt_target
		val currentTarget:AnyRef = null
		def preventDefault() {}
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
	
	object KeyCodes {
		val MAC_ENTER = 3
		val BACKSPACE = 8
		val TAB = 9
		val NUM_CENTER = 12	// NUMLOCK on FF/Safari Mac
		val ENTER = 13
		val SHIFT = 16
		val CTRL = 17
		val ALT = 18
		val PAUSE = 19
		val CAPS_LOCK = 20
		val ESC = 27
		val SPACE = 32
		val PAGE_UP = 33	 // also NUM_NORTH_EAST
		val PAGE_DOWN = 34	 // also NUM_SOUTH_EAST
		val END = 35	 	 // also NUM_SOUTH_WEST
		val HOME = 36	 	 // also NUM_NORTH_WEST
		val LEFT = 37	 	 // also NUM_WEST
		val UP = 38	 	 	 // also NUM_NORTH
		val RIGHT = 39	 	 // also NUM_EAST
		val DOWN = 40	 	 // also NUM_SOUTH
		val PRINT_SCREEN = 44
		val INSERT = 45	 	 // also NUM_INSERT
		val DELETE = 46	 	 // also NUM_DELETE
		val ZERO = 48
		val ONE = 49
		val TWO = 50
		val THREE = 51
		val FOUR = 52
		val FIVE = 53
		val SIX = 54
		val SEVEN = 55
		val EIGHT = 56
		val NINE = 57
		val QUESTION_MARK = 63 // needs localization
		val A = 65
		val B = 66
		val C = 67
		val D = 68
		val E = 69
		val F = 70
		val G = 71
		val H = 72
		val I = 73
		val J = 74
		val K = 75
		val L = 76
		val M = 77
		val N = 78
		val O = 79
		val P = 80
		val Q = 81
		val R = 82
		val S = 83
		val T = 84
		val U = 85
		val V = 86
		val W = 87
		val X = 88
		val Y = 89
		val Z = 90
		val META = 91
		val CONTEXT_MENU = 93
		val NUM_ZERO = 96
		val NUM_ONE = 97
		val NUM_TWO = 98
		val NUM_THREE = 99
		val NUM_FOUR = 100
		val NUM_FIVE = 101
		val NUM_SIX = 102
		val NUM_SEVEN = 103
		val NUM_EIGHT = 104
		val NUM_NINE = 105
		val NUM_MULTIPLY = 106
		val NUM_PLUS = 107
		val NUM_MINUS = 109
		val NUM_PERIOD = 110
		val NUM_DIVISION = 111
		val F1 = 112
		val F2 = 113
		val F3 = 114
		val F4 = 115
		val F5 = 116
		val F6 = 117
		val F7 = 118
		val F8 = 119
		val F9 = 120
		val F10 = 121
		val F11 = 122
		val F12 = 123
		val NUMLOCK = 144
		val SEMICOLON = 186	 	 	 // needs localization
		val DASH = 189	 	 	 	  // needs localization
		val EQUALS = 187		  // needs localization
		val COMMA = 188	 	 	 	 // needs localization
		val PERIOD = 190	 	  // needs localization
		val SLASH = 191	 	 	 	 // needs localization
		val APOSTROPHE = 192	  // needs localization
		val SINGLE_QUOTE = 222	 	  // needs localization
		val OPEN_SQUARE_BRACKET = 219	// needs localization
		val BACKSLASH = 220	 	 	 // needs localization
		val CLOSE_SQUARE_BRACKET = 221 // needs localization
		val WIN_KEY = 224
		val MAC_FF_META = 224 // Firefox (Gecko) fires this for the meta key instead of 91
		val WIN_IME = 229

		// We've seen users whose machines fire this keycode at regular one
		// second intervals. The common thread among these users is that
		// they're all using Dell Inspiron laptops so we suspect that this
		// indicates a hardware/bios problem.
		// http://en.community.dell.com/support-forums/laptop/f/3518/p/19285957/19523128.aspx
		val PHANTOM = 255
	}

}
