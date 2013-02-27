package goog.net 

import goog.events.Event
import browser._

class XhrIo {
	def getResponseType () :XhrIo.ResponseType = null
	def getResponseText () :String = ""
	def getResponseXml () :browser.Document = null
	def getResponseJson (opt_xssiPrefix:String="") :Object = null
}

object XhrIo {
	def send [T<:Event](url:String, callback:(T)=>Unit = null, method:String="", content:String="", headers:Map[String, String]=null, timeoutInterval:Int=0) {}
	
	class ResponseType {
		
	}
}
