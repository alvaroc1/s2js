package goog.ui 

import browser._
import goog.dom.DomHelper

class Button (content:AnyRef /* ControlContent*/, renderer:ButtonRenderer=null, domHelper:DomHelper=null) extends Control (content) {
	// this doesn't exist but it is used on goog.editor
	var queryable = false
}

class ButtonRenderer extends ControlRenderer

class Component (opt_domHelper:DomHelper=null) {
	def createDom () {}
	def getElement () : Element = null
	def getDomHelper () : DomHelper = null
	def isInDocument () : Boolean = false
	def setElementInternal (el:Element) {}
	def getId () : String = ""
	def render (opt_parentElement:Element = null) {}
	def enterDocument () {}
}

object Component {
	class EventType
	object EventType {			
		val BEFORE_SHOW = ""
		val SHOW = ""
		val HIDE = ""
		val DISABLE = ""
		val ENABLE = ""
		val HIGHLIGHT = ""
		val UNHIGHLIGHT = ""
		val ACTIVATE = ""
		val DEACTIVATE = ""
		val SELECT = ""
		val UNSELECT = ""
		val CHECK = ""
		val UNCHECK = ""
		val FOCUS = ""
		val BLUR = ""
		val OPEN = ""
		val CLOSE = ""
		val ENTER = ""
		val LEAVE = ""
		val ACTION = ""
		val CHANGE = ""
	}
	
	object Error {
		val ALREADY_RENDERED = ""
	}
}

class Container (opt_orientation:Container.Orientation, opt_renderer:ContainerRenderer, opt_domHelper:goog.dom.DomHelper) extends Component {
	def this () = this(null, null, null)
	def this (opt_orientation:Container.Orientation) = this(opt_orientation, null, null)
	def this (opt_orientation:Container.Orientation, opt_renderer:ContainerRenderer) = this(opt_orientation, opt_renderer, null)
	
	def setModel (obj:AnyRef) {}
	def setFocusable (focusable:Boolean) {}
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

class Control (content:AnyRef /* ControlContent */, renderer:ControlRenderer = null, domHelper:DomHelper = null) extends Component (domHelper) {
	def setOpen (open:Boolean) {}
}

class ControlContent

class ControlRenderer

class CustomButtonRenderer extends ButtonRenderer

object CustomButtonRenderer {
	def getInstance ():CustomButtonRenderer = null
}

class Dialog (class_ :String = "", useIframeMask:Boolean = false, domHelper:DomHelper = null) extends Component (domHelper) {
	var titleEl_ : Element = _
	
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

class FilteredMenu (opt_renderer:MenuRenderer=null, opt_domHelper:goog.dom.DomHelper=null) extends Menu {
	def getFilterInputElement ():Element = null
}

class FlatButtonRenderer extends ButtonRenderer
object FlatButtonRenderer {
	def getInstance ():FlatButtonRenderer = null
}
class FlatMenuButtonRenderer extends FlatButtonRenderer
object FlatMenuButtonRenderer {
	def getInstance ():FlatMenuButtonRenderer = null
}

class ToggleButton (content:AnyRef /* ControlContent */, renderer:ButtonRenderer=null, domHelper:DomHelper=null) extends Button (content, renderer, domHelper)

class Menu (opt_domHelper:goog.dom.DomHelper=null, opt_renderer:MenuRenderer=null) extends Container {
	def addItem (item:MenuItem) {}
	def addItem (item:MenuHeader) {}
	def addItem (item:MenuSeparator) {}
}

class MenuButton (content:String /* ControlContent */, opt_menu:Menu=null, opt_renderer:MenuButtonRenderer=null, opt_domHelper:goog.dom.DomHelper=null) extends Button (content) {
	def setOpen (open:Boolean, opt_e:goog.events.Event=null) {}
	def getMenu ():Menu = null
	def setFocusablePopupMenu (focusable:Boolean) {}
}

class MenuButtonRenderer extends CustomButtonRenderer

object MenuButtonRenderer {
	def getInstance () :MenuButtonRenderer = null
}

class MenuHeader (content:String /* ControlContent */, opt_domHelper:goog.dom.DomHelper=null, opt_renderer:MenuHeaderRenderer=null)

class MenuHeaderRenderer extends ControlRenderer 

object MenuHeaderRenderer {
	def getInstance ():MenuHeaderRenderer = null
}

class MenuItem (content:String /* ControlContent */, opt_model:AnyRef=null, opt_domHelper:goog.dom.DomHelper=null, opt_renderer:MenuItemRenderer=null) extends Control(content)

class MenuItemRenderer extends ControlRenderer

object MenuItemRenderer {
	def getInstance ():MenuItemRenderer = null
}

class MenuRenderer extends ContainerRenderer

object MenuRenderer {
	def getInstance ():MenuRenderer = null
}

class MenuSeparator (opt_domHelper:goog.dom.DomHelper=null) extends Separator

class MenuSeparatorRenderer extends ControlRenderer

object MenuSeparatorRenderer {
	def getInstance():MenuSeparatorRenderer = null
}

class PopupMenu (opt_domHelper:goog.dom.DomHelper = null, opt_renderer:MenuRenderer = null) extends Menu {
	def attach (element:Element, opt_targetCorner:goog.positioning.Corner=null, opt_menuCorner:goog.positioning.Corner=null, opt_contextMenu:Boolean=false, opt_margin:goog.math.Box=null) {}
}

class Select (caption:String /* ControlContent */, opt_menu:Menu=null, opt_renderer:ButtonRenderer=null, opt_domHelper:goog.dom.DomHelper=null) extends MenuButton (caption)

class Separator (opt_renderer:MenuSeparatorRenderer=null, op_domHelper:goog.dom.DomHelper=null) extends Control(null)

class Tab (content:goog.ui.ControlContent, opt_renderer:goog.ui.TabRenderer=null, opt_domHelper:goog.dom.DomHelper=null) extends Control (content, opt_renderer, opt_domHelper)

class TabRenderer extends ControlRenderer

class Toolbar (renderer:ToolbarRenderer, orientation:Any = null, domHelper:DomHelper = null)

class ToolbarRenderer
