
package goog {
	import browser._
	
	object dom {
		def $ (id:String):Element = null
		def setTextContent(el:Element, content:String) {}
		
		class DomHelper {
			def insertSiblingBefore (el1:Element, el2:Element) {}
		}
		
		object classes {
			def add (el:Element, cls:String) {}
			def remove (el:Element, cls:String) {}
		}
	}
	
	object css {
		def getCssName (n:String) = "test"
	}
	
	object style {
		def setOpacity (el:Element, opacity:Double) {}
	}
	
}

package object goog {
	def getCssName (el:String, name:String):String = ""
}

package goog.ui {
	import browser._
	import goog.dom.DomHelper
	
	class Dialog (opt_class:String)
	
	class Component (opt_domHelper:DomHelper) {
		def getElement () : Element = null
		def getDomHelper () : DomHelper = null
		def isInDocument () : Boolean = false
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
	class Dragger {
		def dispose () {}
	}
}
