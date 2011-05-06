package browser 

object `package` {
	def alert (s:Any) {}
	def confirm (s:String):Boolean = false
	def decodeURIComponent (s:String) = ""
	def encodeURIComponent (s:String) = ""
	def parseInt (s:String, base:Int):Int = 0
}

// represents a javascript object literal
class Object {
	// not sure about these, they'll have to do for now
	def get (a:String):Any = null
	def set (a:String, b:Any) {}
}
object Object {
	def apply (map:(String,Any)*) = new Object
}

class Date {
	def getTimezoneOffset():Int = 0
}

class Window extends Object {
	object location {
		var href = ""
	}
	def focus () {}
	
	def setTimeout (fn:()=>Unit, milliseconds:Int) {}
}

object window extends Window {}

class Node {
	var parentNode:Node = _
}

class Element extends Node {
	val id :String = ""
	var innerHTML = ""
	var className = ""
	
	val style:HTMLElementStyle = null
	
	var scrollTop = 0
	
	var offsetHeight = 0
	
	def setAttribute(name:String, value:String) {}
	def getAttribute(name:String):String = ""
}

class Text extends Node

// what is the actual class name for this?
class HTMLElementStyle {
	// TODO: fill all these out
	var position:String = _
	var display:String = _
	var width:String = _
	var height:String = _
	var top:String = _
	var right:String = _
	var bottom:String = _
	var left:String = _
	
	var opacity:Float = _
	
	var backgroundColor = ""
	var outline = ""
	
	// TODO: what should be done with these?
	var MozBorderRadius:String = _
	var WebkitBorderRadius:String = _
}

class HTMLAnchorElement extends Element
class HTMLDivElement extends Element

class HTMLFormElement extends Element {
	def submit () {}
}

class HTMLIFrameElement extends Element 

trait HTMLControl { // not standard, just for convenience
	var value = ""
	//def value = ""
	//def value_= (value:Any) {} // i don't remember why i needed this
		
	var disabled = false
	def focus () {}
}
class HTMLInputElement extends Element with HTMLControl {
	def select () {}
	var checked = false
}
class HTMLTextAreaElement extends Element with HTMLControl
class HTMLButtonElement extends Element with HTMLControl
class HTMLSelectElement extends Element with HTMLControl

class Document {
	val body:Element = null
	def execCommand(command:String, showDefaultUI:Boolean, value:String) {}
}

object document extends Document

class Range {
	def insertNode (n:Node) {}
	def surroundContents (n:Node) {}
}
