
package goog {
	import browser._
	
	object dom {
		def $ (id:String):Element = null
		def getElement (id:String) : Element = null
		def removeNode (node:Element) {}
		def getDomHelper (element:Element = null) : DomHelper = null
		
		def setTextContent(el:Element, content:String) {}
		
		class DomHelper {
			def insertSiblingBefore (el1:Element, el2:Element) {}
			def createDom (tag:String, others:Object*):Element = null
			def getDocument () = new Document
		}
		
		object classes {
			def add (el:Element, cls:String) {}
			def remove (el:Element, cls:String) {}
		}
		
		object a11y {
			def setRole (element:Element, roleName:String) {}
			def setState (element:Element, state:String, value:Any) {}
		}

		object iframe {
			def createBlank (dh:dom.DomHelper) : Element = null
		}
	}
	
	object css {
		def getCssName (n:String) = "test"
	}
	
	object style {
		def setOpacity (el:Element, opacity:Double) {}
		def showElement (el:Element, show:Boolean) {}
	}
	
	object structs {
		class Map
		
		def forEach (m:Map, fn:(String, String) => Unit) {
			
		}
	}
}

package object goog {
	def getCssName (className:String, modifier:String = ""):String = ""
}

package goog.events {
	class Event
	
	class EventHandler {
		def listen (obj:AnyRef, events:List[goog.ui.Component.EventType], fn:()=>Unit) {}
	}
	
	class FocusHandler
}

package goog.fx {
	class Dragger (el:browser.Element, el2:browser.Element){
		def dispose () {}
	}
}
