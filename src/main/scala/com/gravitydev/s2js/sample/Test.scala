package vanity2.editor.plugins

import browser._
import goog.editor.Plugin

class CodeBlockPlugin extends Plugin {
	def getTrogClassId = "CBP"
		
	def isSupportedCommand (command:String) = command == "codeblock"
		
	def queryCommandValue (command:String) = {
		alert("queryCommandValue")
		isNodeInState (goog.dom.TagName.CODE)
	}
	
	private def isNodeInState (nodeName:String) = {
		alert("checking in state")
		val range = getRange
		
		var node:Element = null
		if (range != null) node = range.getContainerElement else node = null
		
		val ancestor = goog.dom.getAncestorByTagNameAndClass(node, nodeName)
		if (ancestor != null) goog.editor.node.isEditable(ancestor) else false
	}
	
	private def getRange () = fieldObject.getRange
	
}
