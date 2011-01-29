package goog.ui 

import browser._
import goog.dom.DomHelper

class Button (content:AnyRef /* ControlContent*/, renderer:ButtonRenderer=null, domHelper:DomHelper=null) {
	// this doesn't exist but it is used on goog.editor
	var queryable = false
}

class ButtonRenderer extends ControlRenderer

class ToggleButton (content:AnyRef /* ControlContent */, renderer:ButtonRenderer=null, domHelper:DomHelper=null) extends Button (content, renderer, domHelper)

class Control (content:ControlContent, renderer:ControlRenderer = null, domHelper:DomHelper = null) extends Component (domHelper)

class ControlContent

class ControlRenderer

class Dialog (class_ :String = "", useIframeMask:Boolean = false, domHelper:DomHelper = null) extends Component (domHelper) {
	var titleEl_ : Element = _
	
	def createDom () {}
	def manageBackgroundDom_() {}
	def setTitle (title:String) {}
	def setContent (html:String) {}
	def setVisible (visible:Boolean) {}
	def setButtonSet (buttonSet:Dialog.ButtonSet) {}
}
object Dialog {
	class Event (val key:String, val caption:String) extends goog.events.Event ("") {
		val `type`:String = ""
	}
	
	class EventType
	object EventType {
		val SELECT = "select"
		val AFTER_HIDE = "after_hide"
	}
	class ButtonSet extends goog.structs.Map
}

class Component (opt_domHelper:DomHelper) {
	def getElement () : Element = null
	def getDomHelper () : DomHelper = null
	def isInDocument () : Boolean = false
	def setElementInternal (el:Element) {}
	def getId () : String = ""
	def render (opt_parentElement:Element = null) {}
}

object Component {
	class EventType
	object EventType {
		/*
		case object SELECT extends EventType
		case object UNSELECT extends EventType
		case object CHECK extends EventType
		case object UNCHECK extends EventType
		*/
		val SELECT = ""
		val UNSELECT = ""
		val CHECK = ""
		val UNCHECK = ""
	}
	object Error {
		val ALREADY_RENDERED = ""
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

class Tab (content:goog.ui.ControlContent, opt_renderer:goog.ui.TabRenderer=null, opt_domHelper:goog.dom.DomHelper=null) extends Control (content, opt_renderer, opt_domHelper)

class TabRenderer extends ControlRenderer

class Toolbar (renderer:ToolbarRenderer, orientation:Any = null, domHelper:DomHelper = null)

class ToolbarRenderer
