package goog.ui

import browser._
import goog.dom.DomHelper
import goog.math.Box

/**
 * Advanced tooltip widget with cursor tracking abilities. Works like a regular
 * tooltip but can track the cursor position and direction to determine if the
 * tooltip should be dismissed or remain open.
 *
 * @param opt_el Element to display tooltip for, either
 *     element reference or string id.
 * @param opt_str Text message to display in tooltip.
 * @param opt_domHelper Optional DOM helper.
 */
class AdvancedTooltip (opt_el: AnyRef=null, opt_str:String="", opt_domHelper:DomHelper=null) extends Tooltip {  
  def setCursorTracking (b:Boolean) {}
  
  
  /**
   * Sets margin around the tooltip where the cursor is allowed without dismissing
   * the tooltip.
   *
   * @param opt_box The margin around the tooltip.
   */
  def setHotSpotPadding (opt_box: Box = null) {}
  
  /**
   * Sets delay in milliseconds before tooltips are hidden if cursor tracking is
   * enabled and the cursor is moving away from the tooltip.
   *
   * @param delay The delay in milliseconds.
   */
  def setCursorTrackingHideDelayMs (delay: Int) {}
  
  
  /**
   * Sets delay in milliseconds before tooltip is hidden once the cursor leavs
   * the element.
   *
   * @param delay The delay in milliseconds.
   */
  def setHideDelayMs (delay: Int) {}
}