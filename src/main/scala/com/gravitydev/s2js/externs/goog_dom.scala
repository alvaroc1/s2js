package goog.dom

import browser._

object `package` {
	def getDomHelper (element:Element = null) : DomHelper = null
	def getDocument (): Document = null
	def getElement (id:String) : Element = null
	def getElementsByTagNameAndClass (opt_tag:String="", opt_class:String="", opt_el:Element=null):List[Element] = null
	def getElementsByClass (className:String, opt_el:Element = null):{val length:Int} = null
	def getElementByClass (className:String, opt_el:Element = null): Element = null
	def setProperties (elements:Element, properties:AnyRef) {}
	
	def getViewportSize (opt_window:Window = null):goog.math.Size = null
	def getDocumentHeight ():Int = 0
	def getPageScroll (opt_window:Window = null):goog.math.Coordinate = null
	def getDocumentScroll ():goog.math.Coordinate = null
	def getDocumentScrollElement (doc:Document):Element = null
	def getWindow (opt_doc:Document=null):Window = null
	def createDom (tag:String, others:AnyRef*):Element = null
	def createElement (name:String):Element = null
	def createTextNode (content:String):Text = null
	def createTable (rows:Int, columns:Int, opt_fillWithNbsp:Boolean):Element = null
	def isCss1CompatMode():Boolean = false
	def canHaveChildren (node:Node):Boolean = false
	def appendChild (parent:Node, child:Node) {}
	def append (parent:Node, var_args:AnyRef*) {}
	def removeChildren (node:Node) {}
	def insertSiblingBefore (el1:Element, el2:Element) {}
	def insertSiblingAfter (el1:Element, el2:Element) {}
	def removeNode (node:Element) {}
	def replaceNode (newNode:Node, oldNode:Node) {}
	def flattenElement (element:Element):Element = null
	def getChildren (element:Element):List[Node] = null
	def getFirstElementChild (node:Node):Element = null
	def getLastElementChild (node:Node):Element = null
	def getNextElementSibling (node:Node):Element = null
	def getPreviousElementSibling (node:Node):Element = null
	def getNextElementNode (node:Node, forwared:Boolean):Element = null
	def getNextNode (node:Node):Node = null
	def getPreviousNode (node:Node):Node = null
	def isNodeLike (obj:AnyRef):Boolean = false
	
	def getFrameContentDocument (frame:Element):Document = null
	def getFrameContentWindow (frame:HTMLIFrameElement):Window = null
 	
	def setTextContent(el:Element, content:String) {}
	def getAncestorByTagNameAndClass (element:Element, tag:String="", class_ :String = "") :Node = null
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
	def has (element:Element, className:String):Boolean = false
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