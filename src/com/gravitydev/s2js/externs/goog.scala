
package goog {
	import browser._
	
	object dom {
	
		def $ (id:String):Element = null
		
		class DomHelper 
	
		def setTextContent(el:Element, content:String) {}
	}
	
	object css {
		def getCssName (n:String) = "test"
	}
}

package goog.ui {
	import browser._
	
	class Dialog (opt_class:String)
	
	class Component (opt_domHelper:goog.dom.DomHelper) {
		def getElement () : Element = null
	}
	
	object Component {
		class EventType
		object EventType {
			case object CHECK extends EventType
			case object UNCHECK extends EventType
		}
	}
	
	class Container (opt_orientation:Container.Orientation, opt_renderer:ContainerRenderer, opt_domHelper:goog.dom.DomHelper) {
		def this () = this(null, null, null)
		def this (opt_orientation:Container.Orientation) = this(opt_orientation, null, null)
		def this (opt_orientation:Container.Orientation, opt_renderer:ContainerRenderer) = this(opt_orientation, opt_renderer, null)
		
		def setModel (obj:AnyRef) {}
		def setFocusable (focusable:Boolean) {}
		def enterDocument () {}
		def getHandler:goog.events.EventHandler = new goog.events.EventHandler
		
		def forEachChild (fn:(AnyRef, Int)=>Unit) {}
	}
	
	object Container {
		class Orientation
		object Orientation {
			case object VERTICAL extends Orientation
			case object HORIZONTAL extends Orientation
		}
	}
	
	class ContainerRenderer
}

package goog.events {
	class EventHandler {
		def listen (obj:AnyRef, events:List[goog.ui.Component.EventType], fn:()=>Unit) {}
	}
	
	class FocusHandler
}

package goog.fx {
	class Dragger
}
