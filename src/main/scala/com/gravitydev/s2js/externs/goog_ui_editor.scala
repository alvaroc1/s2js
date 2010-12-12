package goog.ui.editor

import browser._


object DefaultToolbar {
	def makeToolbar (items:List[AnyRef], elem:Element, isRightToLeft:Boolean = false):goog.ui.Toolbar = null
}

class ToolbarController (field:goog.editor.Field, toolbar:goog.ui.Toolbar)



object ToolbarFactory {
	def makeButton (id:String, tooltip:String, caption:String, classNames:String="", renderer:goog.ui.ButtonRenderer=null, domHelper:goog.dom.DomHelper):goog.ui.Button = null
	def makeToggleButton (id:String, tooltip:String, caption:String, classNames:String="", renderer:goog.ui.ButtonRenderer=null, domHelper:goog.dom.DomHelper=null):goog.ui.ToggleButton = null
}

