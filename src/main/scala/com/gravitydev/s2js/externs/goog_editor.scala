package goog.editor

import browser._

class Field (id:String, opt_doc:Document = null) {
	val originalElement:Element = null
	def registerPlugin (plugin:Plugin) {}
	def setHtml (addParas:Boolean, html:String, dontFireDelayedChange:Boolean=false, applyLorem:Boolean=false) {}
	def makeEditable (iframeSrc:String="") {}
	def makeUneditable (skipRestore:Boolean=false) {}
	def getCleanContents () = ""
	def getRange () : goog.dom.AbstractRange = null
	def isSelectionEditable () = false
}

class SeamlessField (id:String, opt_doc:Document = null) extends Field (id) {
	def setMinHeight (height:Int) {}
}

class Plugin {
	val fieldObject:Field = null
	def getFieldDomHelper () :goog.dom.DomHelper = null
}

object Command {
  // Prepend all the strings of built in execCommands with a plus to ensure
  // that there's no conflict if a client wants to use the
  // browser's execCommand.
	val UNDO = "+undo"
	val REDO= "+redo"
	val LINK= "+link"
	val FORMAT_BLOCK = "+formatBlock"
	val INDENT = "+indent"
	val OUTDENT = "+outdent"
	val REMOVE_FORMAT = "+removeFormat"
	val STRIKE_THROUGH = "+strikeThrough"
	val HORIZONTAL_RULE = "+insertHorizontalRule"
	val SUBSCRIPT = "+subscript"
	val SUPERSCRIPT = "+superscript"
	val UNDERLINE = "+underline"
	val BOLD = "+bold"
	val ITALIC = "+italic"
	val FONT_SIZE = "+fontSize"
	val FONT_FACE = "+fontName"
	val FONT_COLOR = "+foreColor"
	val EMOTICON = "+emoticon"
	val BACKGROUND_COLOR = "+backColor"
	val ORDERED_LIST = "+insertOrderedList"
	val UNORDERED_LIST = "+insertUnorderedList"
	val TABLE = "+table"
	val JUSTIFY_CENTER = "+justifyCenter"
	val JUSTIFY_FULL = "+justifyFull"
	val JUSTIFY_RIGHT = "+justifyRight"
	val JUSTIFY_LEFT = "+justifyLeft"
	val BLOCKQUOTE = "+BLOCKQUOTE" // This is a nodename. Should be all caps.
	val DIR_LTR = "ltr" // should be exactly 'ltr' as it becomes dir attribute value
	val DIR_RTL = "rtl" // same here
	val IMAGE = "image"
	val EDIT_HTML = "editHtml"

	// queryCommandValue only: returns the default tag name used in the field.
	// DIV should be considered the default if no plugin responds.
	val DEFAULT_TAG = "+defaultTag"

	// TODO(nicksantos): Try to give clients an API so that they don't need
	// these execCommands.
	val CLEAR_LOREM = "clearlorem"
	val UPDATE_LOREM = "updatelorem"
	val USING_LOREM = "usinglorem"

	// Modal editor commands (usually dialogs).
	val MODAL_LINK_EDITOR = "link"
}

object node {
	def isEditable (node:Element):Boolean = false
}

class Link (anchor:HTMLAnchorElement, isNew:Boolean) {
	def getCurrentText ():String = ""
	def isNew () = false
}

object Link {
	def isMailto (url:String) = false
	def isLikelyUrl(str:String) = false
	def isLikelyEmailAddress (str:String) = false
}
