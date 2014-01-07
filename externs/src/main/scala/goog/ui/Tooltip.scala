package goog.ui

import browser._
import goog.dom.DomHelper

/**
 * Tooltip widget. Can be attached to one or more elements and is shown, with a
 * slight delay, when the the cursor is over the element or the element gains
 * focus.
 *
 * @param opt_el Element to display tooltip for, either
 *     element reference or string id.
 * @param opt_str Text message to display in tooltip.
 * @param opt_domHelper Optional DOM helper.
 */
class Tooltip (opt_el: AnyRef=null, opt_str:String="", opt_domHelper:DomHelper=null) extends Popup {
  var className = ""
  
  def setElement (el:Element) {}
  
  
  /**
   * Sets tooltip message as HTML markup.
   *
   * @param str HTML message to display in tooltip.
   */
  def setHtml (str: String) {}
}
