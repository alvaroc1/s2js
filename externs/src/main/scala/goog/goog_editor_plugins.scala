package goog.editor.plugins

import browser._

abstract class AbstractBubblePlugin extends goog.editor.Plugin {
	def getBubbleTargetFromSelection (selectedElement:Element):Element = null
	def getBubbleType ():String = null
	def getBubbleTitle () :String = null
	def createBubbleContents (bubbleContainer:Element) {}
}
class AbstractDialogPlugin extends goog.editor.Plugin

class EnterHandler extends goog.editor.Plugin

class BasicTextFormatter extends goog.editor.Plugin {
	def execCommandHelper_ (command:String, value:String = "", preserveDir:Boolean = false, styleWithCss:Boolean = false) {}
}

class HeaderFormatter extends goog.editor.Plugin
class TableEditor extends goog.editor.Plugin
class RemoveFormatting extends goog.editor.Plugin
class Blockquote (requiresClassNameToSplit:Boolean) extends goog.editor.Plugin

class LinkBubble extends AbstractBubblePlugin

class LinkDialogPlugin extends AbstractDialogPlugin {
	def createDialog (dialogDomHelper:goog.dom.DomHelper, link:goog.editor.Link) : goog.ui.editor.LinkDialog = null
}

class TagOnEnterHandler (tag:String) extends EnterHandler
