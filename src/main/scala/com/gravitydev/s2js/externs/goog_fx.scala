package goog.fx

import browser._

class AbstractDragDrop extends goog.events.EventTarget {
	protected var dragClass_ :String = _
	
	def addTarget (target:AbstractDragDrop) {}
	def init () {}
	def isInitialized ():Boolean = false
}

class DragDropEvent (tpe:String, source:AbstractDragDrop, sourceItem:DragDropItem, opt_target:AbstractDragDrop=null, opt_targetItem:DragDropItem=null,
		opt_targetElement:Element=null, opt_clientX:Int=0, opt_clientY:Int=0, opt_x:Int=0, opt_y:Int=0, opt_subtarget:AnyRef=null) extends goog.events.Event(tpe) {
	
	val dragSource:AbstractDragDrop = null
	val dragSourceItem:DragDropItem = null
	val dropTarget:AbstractDragDrop = null
	val dropTargetItem:DragDropItem = null
	val dropTargetElement:Element = null
	val clientX:Int = 0
	val clientY:Int = 0
	val viewportX:Int = 0
	val viewportY:Int = 0
	val subtarget:AnyRef = null
}

class DragDropGroup extends AbstractDragDrop {
	def addItem (element:Element, opt_data:AnyRef = null) {}
	def addDragDropItem (item:DragDropItem) {}
	def removeItem (element:Element) {}
	def setSelection (list:List[DragDropItem]) {}
}

class DragDropItem extends goog.events.EventTarget {
	val element:Element = null
	val data:String = ""
}

class Dragger (el:browser.Element, el2:browser.Element){
	def dispose () {}
}
