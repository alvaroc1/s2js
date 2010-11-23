package goog.ui 

import browser._
import goog.dom.DomHelper

class Dialog (opt_class:String)

class Component (opt_domHelper:DomHelper) {
	def getElement () : Element = null
	def getDomHelper () : DomHelper = null
	def isInDocument () : Boolean = false
	def setElementInternal (element:browser.Element) {}
	def getId () : String = ""
}

object Component {
	class EventType
	object EventType {
		case object CHECK extends EventType
		case object UNCHECK extends EventType
	}
	object Error {
		val ALREADY_RENDERED = "Component already rendered"
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
