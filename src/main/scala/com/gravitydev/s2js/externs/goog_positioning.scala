package goog.positioning

import browser._

class Corner 

object Corner {
	object TOP_LEFT extends Corner
	object TOP_RIGHT extends Corner
	object BOTTOM_LEFT extends Corner
	object BOTTOM_RIGHT extends Corner
	object TOP_START extends Corner
	object TOP_END extends Corner
	object BOTTOM_START extends Corner
	object BOTTOM_END extends Corner
}

abstract class AbstractPosition {
	def reposition (movableElement:Element, corner:Corner, opt_margin:goog.math.Box=null, opt_preferredSize:goog.math.Size=null) {}
}

class AnchoredPosition (anchorElement:Element, corner:Corner) extends AbstractPosition

class AnchoredViewportPosition (anchorElement:Element, corner:Corner, opt_adjust:Boolean = false) extends AnchoredPosition (anchorElement, corner)
