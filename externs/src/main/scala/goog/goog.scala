package goog 

import browser._
	

object `package` {
	def getCssName (className:String, modifier:String = ""):String = ""
}

	
object css {
	def getCssName (n:String) = "test"
}

object string {
	def createUniqueString ():String = ""
}

class History (opt_invisible:Boolean = false, opt_blankPageUrl:String = "", opt_input:HTMLInputElement = null, opt_iframe:HTMLIFrameElement = null ) extends goog.events.EventTarget {
	def setEnabled (enabled:Boolean) {}
}

object History {
	object EventType {
		val NAVIGATE = ""
	}
}
