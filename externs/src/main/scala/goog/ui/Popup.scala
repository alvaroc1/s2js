package goog.ui

import browser._
import goog.math.Box

class Popup (opt_element:Element=null, opt_position:goog.positioning.AbstractPosition=null) extends PopupBase {
  def getPosition ():goog.positioning.AbstractPosition = null
  def setPosition (position:goog.positioning.AbstractPosition) {}
  def setPinnedCorner (corner:goog.positioning.Corner) {}
  
/**
 * Returns the margin to place around the popup.
 *
 * @return The margin.
 */
  def getMargin(): Box = null
  
  /**
   * Sets the margin to place around the popup.
   *
   * @param arg1 Top value or Box.
   * @param opt_arg2 Right value.
   * @param opt_arg3 Bottom value.
   * @param opt_arg4 Left value.
   */
  def setMargin (arg1: Any, opt_arg2: Int = 0, opt_arg: Int = 0, opt_arg4: Int = 0) {}
}
