
package goog {
	import browser._
		
	object css {
		def getCssName (n:String) = "test"
	}
	
	object style {
		def setOpacity (el:Element, opacity:Double) {}
		def showElement (el:Element, show:Boolean) {}
	}
}

package object goog {
	def getCssName (className:String, modifier:String = ""):String = ""
}

package goog.fx {
	class Dragger (el:browser.Element, el2:browser.Element){
		def dispose () {}
	}
}
