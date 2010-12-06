package browser {
	class Window {
		def get(key:String) = ""
		object location {
			var href = ""
		}
	}
	
	object window extends Window 
	
	class Element {
		val id :String = ""
		var innerHTML = ""
		var className = ""
		val value = ""
	}
	class Document {
		val body:Element = null
	}
}
