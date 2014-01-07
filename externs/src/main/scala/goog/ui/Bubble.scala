package goog.ui

import browser._
import goog.positioning.AbstractPosition

/**
 * The Bubble provides a general purpose bubble implementation that can be
 * anchored to a particular element and displayed for a period of time.
 *
 * @param message HTML string or an element to display inside
 *     the bubble.
 * @param opt_config The configuration
 *     for the bubble. If not specified, the default configuration will be
 *     used. {@see goog.ui.Bubble.defaultConfig}.
 * @param opt_domHelper Optional DOM helper.
 */
class Bubble (message: AnyRef, opt_config: Object = null, opt_domHelper: goog.dom.DomHelper = null) extends Component {

  
  /**
   * Sets whether the bubble should be automatically hidden whenever user clicks
   * outside the bubble element.
   *
   * @param autoHide Whether to hide if user clicks outside the bubble.
   */
  def setAutoHide (autoHide: Boolean) {}
  
  
  /**
   * Sets the position of the bubble. Pass null for corner in AnchoredPosition
   * for corner to be computed automatically.
   *
   * @param position The position of the
   *     bubble.
   */
  def setPosition (position: AbstractPosition) {}
  
  
  /**
   * Sets the timeout after which bubble hides itself.
   *
   * @param timeout Timeout of the bubble.
   */
  def setTimeout (timeout: Int) {}
  
  
  /**
   * Attaches the bubble to an anchor element. Computes the positioning and
   * orientation of the bubble.
   *
   * @param anchorElement The element to which we are attaching.
   */
  def attach (anchorElement: Element) {}
  
  
  /**
   * Sets whether the bubble should be visible.
   *
   * @param visible Desired visibility state.
   */
  def setVisible (visible: Boolean) {}
        
}
