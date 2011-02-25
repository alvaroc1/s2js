package browser 

object `package` {
	def alert (s:Any) {}
	def confirm (s:String):Boolean = false
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

class Window {
	def get(key:String) = ""
	object location {
		var href = ""
	}
	def focus () {}
	
	def setTimeout (fn:()=>Unit, milliseconds:Int) {}
}

object window extends Window {}

class Node 

class Element extends Node {
	val id :String = ""
	var innerHTML = ""
	var className = ""
	
	val style:HTMLElementStyle = null
	
	// TODO: move to subclass
	// forms
	def submit () {}
	
	def setAttribute(name:String, value:String) {}
	def getAttribute(name:String):String = ""
}

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
	
}

class HTMLAnchorElement extends Element
class HTMLDivElement extends Element

trait HTMLControl { // not standard, just for convenience
	var value = ""
	var disabled = false
	def focus () {}
}
class HTMLInputElement extends Element with HTMLControl {
	def select () {}
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
