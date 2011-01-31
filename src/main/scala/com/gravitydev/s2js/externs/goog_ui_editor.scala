package goog.ui.editor

import browser._


object DefaultToolbar {
	def makeToolbar (items:List[AnyRef], elem:Element, isRightToLeft:Boolean = false):goog.ui.Toolbar = null
}

abstract class AbstractDialog (domHelper:goog.dom.DomHelper) extends goog.events.EventTarget {
	def createDialogControl ():goog.ui.Dialog
}
object AbstractDialog {
	class Builder (editorDialog:AbstractDialog) {
		def setTitle (title:String):Builder = null
		def setContent (contentElem:Element):Builder = null
		def build (): goog.ui.Dialog = null
	}
}

class LinkDialog (domHelper:goog.dom.DomHelper, link:goog.editor.Link) extends AbstractDialog (domHelper) {
	val targetLink_ = link
	def createDialogControl ():goog.ui.Dialog = null
	def buildTextToDisplayDiv_() = null
	def buildTabOnTheWeb_() = null
	def buildTabEmailAddress_() = null
	def onChangeTab_ (e:goog.events.Event) {}
	def selectAppropriateTab_ (text:String, url:String) {}
	def isNewLink_ ():Boolean = false
	def guessUrlAndSelectTab_ (text:String) {}
	def setAutogenFlag_(value:Boolean) {}
	def setAutogenFlagFromCurInput_ () {}
	def disableAutogenFlag_(autogen:Boolean) {}
}

object LinkDialog {
	object Id_ {
		val TEXT_TO_DISPLAY = ""
		val ON_WEB_TAB = ""
		val ON_WEB_INPUT = ""
		val EMAIL_ADDRESS_TAB = ""
		val EMAIL_ADDRESS_INPUT = ""
		val EMAIL_WARNING = ""
		val TAB_INPUT_SUFFIX = ""
	}
}

class ToolbarController (field:goog.editor.Field, toolbar:goog.ui.Toolbar)

object ToolbarFactory {
	def makeButton (id:String, tooltip:String, caption:String, classNames:String="", renderer:goog.ui.ButtonRenderer=null, domHelper:goog.dom.DomHelper):goog.ui.Button = null
	def makeToggleButton (id:String, tooltip:String, caption:String, classNames:String="", renderer:goog.ui.ButtonRenderer=null, domHelper:goog.dom.DomHelper=null):goog.ui.ToggleButton = null
}

class TabPane (dom:goog.dom.DomHelper, opt_caption:String = "") extends goog.ui.Component (dom) {
	def addTab (id:String, caption:String, tooltip:String, content:Element) {}
	def setSelectedTabId (id:String) {}
}

object messages {
	val MSG_LINK_CAPTION = ""
	val MSG_EDIT_LINK = ""
	val MSG_TEXT_TO_DISPLAY = ""
	val MSG_LINK_TO = ""
	val MSG_ON_THE_WEB = ""
	val MSG_ON_THE_WEB_TIP = ""
	val MSG_TEST_THIS_LINK = ""
	val MSG_TR_LINK_EXPLANATION = ""
	val MSG_WHAT_URL = ""
	val MSG_EMAIL_ADDRESS = ""
	val MSG_EMAIL_ADDRESS_TIP = ""
	val MSG_INVALID_EMAIL = ""
	val MSG_WHAT_EMAIL = ""
	val MSG_EMAIL_EXPLANATION = ""
	val MSG_IMAGE_CAPTION = ""
}