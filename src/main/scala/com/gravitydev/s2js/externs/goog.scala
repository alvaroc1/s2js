
package goog 

import browser._
	

object `package` {
	def getCssName (className:String, modifier:String = ""):String = ""
}

	
object css {
	def getCssName (n:String) = "test"
}

object style {
	def setOpacity (el:Element, opacity:Double) {}
	def showElement (el:Element, show:Boolean) {}
	def isRightToLeft(el:Element) = false
}

object string {
	def createUniqueString ():String = ""
}
