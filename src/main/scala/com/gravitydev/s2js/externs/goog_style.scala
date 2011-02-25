package goog.style

import browser._

object `package` {
	def getComputedPosition (element:Element):String = ""
	def getComputedStyle (element:Element, property:String):String = ""
	def isRightToLeft(el:Element) = false
	def setOpacity (el:Element, opacity:Double) {}
	def showElement (el:Element, show:Boolean) {}
}
