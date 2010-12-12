package browser 

object `package` {
	def alert (s:Any) {}
}

class Window {
	def get(key:String) = ""
	object location {
		var href = ""
	}
	def focus () {}
}

object window extends Window 

class Node 

class Element extends Node {
	val id :String = ""
	var innerHTML = ""
	var className = ""
		
	// TODO: should we have to cast here?
	// inputs 
	var value = ""
		
	// forms
	def submit () {}
}
class Document {
	val body:Element = null
	def execCommand(command:String, showDefaultUI:Boolean, value:String) {}
}

class Range {
	def insertNode (n:Node) {}
	def surroundContents (n:Node) {}
}
