package goog.editor

import browser._

class Field (id:String, opt_doc:Document = null) {
  val originalElement:Element = null
  def registerPlugin (plugin:Plugin) {}
  def setHtml (addParas:Boolean, html:String, dontFireDelayedChange:Boolean=false, applyLorem:Boolean=false) {}
  def makeEditable (iframeSrc:String="") {}
  def makeUneditable (skipRestore:Boolean=false) {}
  def getCleanContents () = ""
  def getRange () : goog.dom.AbstractRange = null
  def isSelectionEditable () = false
}
