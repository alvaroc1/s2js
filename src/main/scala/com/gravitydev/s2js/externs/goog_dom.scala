package goog.dom

import browser._

object `package` {
	def $ (id:String):Element = null
	def getElement (id:String) : Element = null
	def removeNode (node:Element) {}
	def getDomHelper (element:Element = null) : DomHelper = null
	
	def setTextContent(el:Element, content:String) {}
}

class DomHelper {
	def insertSiblingBefore (el1:Element, el2:Element) {}
	def createDom (tag:String, others:Object*):Element = null
	def getDocument () = new Document
}

object classes {
	def add (el:Element, cls:String) {}
	def remove (el:Element, cls:String) {}
}

object a11y {
	def setRole (element:Element, roleName:String) {}
	def setState (element:Element, state:String, value:Any) {}
}

object iframe {
	def createBlank (dh:DomHelper) : Element = null
}
