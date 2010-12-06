package vanity2.editor

import browser._
import goog.editor.{Field, SeamlessField}

class SeamlessEditor (id:String, doc:Document=null) extends SeamlessField (id, doc) {
	val originalContent = originalElement.innerHTML
	
	def createToolbar () {
		setMinHeight(100)
		
		registerPlugin(new goog.editor.plugins.BasicTextFormatter)
		
		val buttons = List(
			goog.editor.Command.FONT_FACE,
			goog.editor.Command.FONT_SIZE,
			goog.editor.Command.BOLD,
			goog.editor.Command.ITALIC,
			goog.editor.Command.UNDERLINE,
			goog.editor.Command.FONT_COLOR,
			goog.editor.Command.BACKGROUND_COLOR,
			goog.editor.Command.UNORDERED_LIST,
			goog.editor.Command.ORDERED_LIST,
			goog.editor.Command.INDENT,
			goog.editor.Command.OUTDENT,
			goog.editor.Command.JUSTIFY_LEFT,
			goog.editor.Command.JUSTIFY_CENTER,
			goog.editor.Command.JUSTIFY_RIGHT,
			goog.editor.Command.STRIKE_THROUGH		
		)
	}
}

object SeamlessEditor {
	def createEditor (elementId:String, withToolbar:Boolean) = {
		val myField = new SeamlessEditor(elementId)
		
		myField.createToolbar()
		
		myField
	}
}
