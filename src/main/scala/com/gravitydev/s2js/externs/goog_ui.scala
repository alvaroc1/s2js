package goog.ui 

import browser._
import goog.dom.DomHelper

class AdvancedTooltip (opt_el:Element=null, opt_str:String="", opt_domHelper:DomHelper=null) extends Tooltip {
	def setCursorTracking (b:Boolean) {}
}

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
	def decorate (element:Element) {}
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
	var title_ : String = _
	var content_ : String = _
	var buttons_ : Dialog.ButtonSet = _
	var titleEl_ : Element = _
	var titleTextEl_ : Element = _
	var titleId_ : String = _
	var titleCloseEl_ : Element = _
	var contentEl_ : Element = _
	var buttonEl_ : Element = _
	
	def manageBackgroundDom_() {}
	def setTitle (title:String) {}
	def setContent (html:String) {}
	def setVisible (visible:Boolean) {}
	def setButtonSet (buttonSet:Dialog.ButtonSet) {}
	def setDisposeOnHide(b:Boolean) {}
	def getDialogElement ():Element = null
	def getButtonSet () :Dialog.ButtonSet = null
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
	class ButtonSet extends goog.structs.Map {
		var class_ :String = _
		var defaultButton_ :String = _
		var element_ :Element = _
		var cancelButton_ :String = _
		
		def set (key:String, caption:String, opt_isDefault:Boolean = false, opt_isCancel:Boolean = false) = this
		def addButton (button:AnyRef, opt_isDefault:Boolean = false, opt_isCancel:Boolean = false) {}
		def attachToElement (el:Element) {}
		def render () {}
		def decorate (el:Element) {}
		def setAllButtonsEnabled(enabled:Boolean) {}
		def getButton (key:String):Element = null
	}
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

class HoverCard (isAnchor:AnyRef, opt_checkDescendants:Boolean=false, opt_domHelper:goog.dom.DomHelper = null) extends AdvancedTooltip {
	def getAnchorElement () :Element = null
}

object HoverCard {
	object EventType {
		val TRIGGER = ""
		val CANCEL_TRIGGER = ""
		val BEFORE_SHOW = ""
		val SHOW = ""
		val BEFORE_HIDE = ""
		val HIDE = ""
	}
}

class HsvPalette extends Component

class HsvaPalette (opt_domHelper:goog.dom.DomHelper=null, opt_color:String="", opt_alpha:Int=0, opt_class:String="") extends HsvPalette

class LabelInput (opt_label:String=null, opt_domHelper:goog.dom.DomHelper=null) extends Component {
	def getValue ():String = ""
	def clear () {}
}

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

class MenuItem (content:AnyRef /* ControlContent */, opt_model:AnyRef=null, opt_domHelper:goog.dom.DomHelper=null, opt_renderer:MenuItemRenderer=null) extends Control(content) {
	def getModel ():AnyRef = null
}

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

class Popup (opt_element:Element=null, opt_position:goog.positioning.AbstractPosition=null) extends PopupBase {
	def getPosition ():goog.positioning.AbstractPosition = null
	def setPosition (position:goog.positioning.AbstractPosition) {}
	def setPinnedCorner (corner:goog.positioning.Corner) {}
}

class PopupBase (opt_element:Element=null, opt_type:PopupBase.Type=null) extends goog.events.EventTarget {
	def getType ():PopupBase.Type = null
	def setType (tpe:PopupBase.Type) {}
	def isVisible ():Boolean = false
	def setVisible (visible:Boolean) {}
}
object PopupBase {
	trait Type
	object TOGGLE_DISPLAY extends Type
	object MOVE_OFFSCREEN extends Type
	
	object EventType {
		val BEFORE_SHOW = ""
		val SHOW = ""
		val BEFORE_HIDE = ""
		val HIDE = ""
	}
	
	var DEBOUNCE_DELAY_MS = 150
}

class PopupMenu (opt_domHelper:goog.dom.DomHelper = null, opt_renderer:MenuRenderer = null) extends Menu {
	def attach (element:Element, opt_targetCorner:goog.positioning.Corner=null, opt_menuCorner:goog.positioning.Corner=null, opt_contextMenu:Boolean=false, opt_margin:goog.math.Box=null) {}
	def setToggleMode (toggle:Boolean) {}
}

class Select (caption:String /* ControlContent */, opt_menu:Menu=null, opt_renderer:ButtonRenderer=null, opt_domHelper:goog.dom.DomHelper=null) extends MenuButton (caption)

class Separator (opt_renderer:MenuSeparatorRenderer=null, op_domHelper:goog.dom.DomHelper=null) extends Control(null)

class Tab (content:goog.ui.ControlContent, opt_renderer:goog.ui.TabRenderer=null, opt_domHelper:goog.dom.DomHelper=null) extends Control (content, opt_renderer, opt_domHelper)

class TabBar (opt_location:String="", opt_renderer:TabBarRenderer=null, opt_domHelper:goog.dom.DomHelper=null)

class TabBarRenderer extends ContainerRenderer

object TabBarRenderer {
	def getInstance():TabBarRenderer = null
}

class TabRenderer extends ControlRenderer

class ToggleButton (content:AnyRef /* ControlContent */, renderer:ButtonRenderer=null, domHelper:DomHelper=null) extends Button (content, renderer, domHelper)


class Toolbar (renderer:ToolbarRenderer, orientation:Any = null, domHelper:DomHelper = null)

class ToolbarRenderer

class Tooltip (opt_el:Element=null, opt_str:String="", opt_domHelper:DomHelper=null) extends Popup {
	var className = ""
	
	def setElement (el:Element) {}
}
