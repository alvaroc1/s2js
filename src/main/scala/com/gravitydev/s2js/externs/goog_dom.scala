package goog.dom

import browser._

object `package` {
	@deprecated("Use getElement")
	def $ (id:String):Element = null
	def getElement (id:String) : Element = null
	def removeNode (node:Element) {}
	def getDomHelper (element:Element = null) : DomHelper = null
	
	def setTextContent(el:Element, content:String) {}
	def createDom (tag:String, others:AnyRef*):Element = null
	def insertSiblingBefore (el1:Element, el2:Element) {}
	def getAncestorByTagNameAndClass (element:Element, tag:String="", class_ :String = "") :Node = null
	def flattenElement (element:Element):Element = null
	def appendChild (parent:Node, child:Node) {}
	def getElementsByTagNameAndClass (opt_tag:String="", opt_class:String="", opt_el:Element=null):List[Element] = null
}

class DomHelper {
	def insertSiblingBefore (el1:Element, el2:Element) {}
	def createDom (tag:String, opt_attributes:Any, args:Any*):Element = null
	def getDocument ():Document = null
	def getWindow ():Window = null
	def createElement (name:String):Element = null
	def getElement (id:String) : Element = null
	def getElement (id:Element) : Element = null
}

object classes {
	def add (el:Element, cls:String) {}
	def remove (el:Element, cls:String) {}
}

object a11y {
	def setRole (element:Element, roleName:String) {}
	def setState (element:Element, state:String, value:Any) {}
}

object iframe {
	def createBlank (dh:DomHelper) : Element = null
}

abstract class AbstractRange {
	def getContainerElement () : Element = null
	def isCollapsed () :Boolean
	def getTextRange (i:Int) :goog.dom.TextRange
}

class TextRange extends AbstractRange {
	def getTextRange (i:Int) :TextRange = null
	def isCollapsed () :Boolean = false
	def getBrowserRangeObject ():browser.Range = null
}

object TagName {
	val A = "A"
	val ABBR = "ABBR"
	val ACRONYM = "ACRONYM"
	val ADDRESS = "ADDRESS"
	val APPLET = "APPLET"
	val AREA = "AREA"
	val B = "B"
	val BASE = "BASE"
	val BASEFONT = "BASEFONT"
	val BDO = "BDO"
	val BIG = "BIG"
	val BLOCKQUOTE = "BLOCKQUOTE"
	val BODY = "BODY"
	val BR = "BR"
	val BUTTON = "BUTTON"
	val CAPTION = "CAPTION"
	val CENTER = "CENTER"
	val CITE = "CITE"
	val CODE = "CODE"
	val COL = "COL"
	val COLGROUP = "COLGROUP"
	val DD = "DD"
	val DEL = "DEL"
	val DFN = "DFN"
	val DIR = "DIR"
	val DIV = "DIV"
	val DL = "DL"
	val DT = "DT"
	val EM = "EM"
	val FIELDSET = "FIELDSET"
	val FONT = "FONT"
	val FORM = "FORM"
	val FRAME = "FRAME"
	val FRAMESET = "FRAMESET"
	val H1 = "H1"
	val H2 = "H2"
	val H3 = "H3"
	val H4 = "H4"
	val H5 = "H5"
	val H6 = "H6"
	val HEAD = "HEAD"
	val HR = "HR"
	val HTML = "HTML"
	val I = "I"
	val IFRAME = "IFRAME"
	val IMG = "IMG"
	val INPUT = "INPUT"
	val INS = "INS"
	val ISINDEX = "ISINDEX"
	val KBD = "KBD"
	val LABEL = "LABEL"
	val LEGEND = "LEGEND"
	val LI = "LI"
	val LINK = "LINK"
	val MAP = "MAP"
	val MENU = "MENU"
	val META = "META"
	val NOFRAMES = "NOFRAMES"
	val NOSCRIPT = "NOSCRIPT"
	val OBJECT = "OBJECT"
	val OL = "OL"
	val OPTGROUP = "OPTGROUP"
	val OPTION = "OPTION"
	val P = "P"
	val PARAM = "PARAM"
	val PRE = "PRE"
	val Q = "Q"
	val S = "S"
	val SAMP = "SAMP"
	val SCRIPT = "SCRIPT"
	val SELECT = "SELECT"
	val SMALL = "SMALL"
	val SPAN = "SPAN"
	val STRIKE = "STRIKE"
	val STRONG = "STRONG"
	val STYLE = "STYLE"
	val SUB = "SUB"
	val SUP = "SUP"
	val TABLE = "TABLE"
	val TBODY = "TBODY"
	val TD = "TD"
	val TEXTAREA = "TEXTAREA"
	val TFOOT = "TFOOT"
	val TH = "TH"
	val THEAD = "THEAD"
	val TITLE = "TITLE"
	val TR = "TR"
	val TT = "TT"
	val U = "U"
	val UL = "UL"
	val VAR = "VAR"
}