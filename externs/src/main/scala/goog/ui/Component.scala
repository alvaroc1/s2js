package goog.ui

import browser._
import goog.dom.DomHelper

class Component (opt_domHelper:DomHelper=null) extends goog.events.EventTarget {
  def createDom () {}
  def getDomHelper () : DomHelper = null
  def getElement () : Element = null
  def getElementByFragment (idFragment:String) :Element = null
  def getId () : String = ""
  def isInDocument () : Boolean = false
  def makeId (idFragment:String) = ""
  def setElementInternal (el:Element) {}
  def render (opt_parentElement:Element = null) {}
  def enterDocument () {}
  
  /**
   * Returns the event handler for this component, lazily created the first time
   * this method is called.
   * @return Event handler for this component.
   */
  protected def getHandler (): goog.events.EventHandler = null

  /**
   * Decorates the element for the UI component.
   * @param element Element to decorate.
   */
  def decorate (element:Element) {}

  /**
   * Returns the 0-based index of the given child component, or -1 if no such
   * child is found.
   * @param child The child component.
   * @return 0-based index of the child component; -1 if not found.
   */
  def indexOfChild (child: Component) = 0 
  

/**
 * Adds the specified component as the last child of this component.  See
 * {@link goog.ui.Component#addChildAt} for detailed semantics.
 *
 * @see goog.ui.Component#addChildAt
 * @param child The new child component.
 * @param opt_render If true, the child component will be rendered
 *    into the parent.
 */
  def addChild (child: Component, opt_render: Boolean = false) {}
  
  /**
   * Adds the specified component as a child of this component at the given
   * 0-based index.
   *
   * Both {@code addChild} and {@code addChildAt} assume the following contract
   * between parent and child components:
   *  <ul>
   *    <li>the child component's element must be a descendant of the parent
   *        component's element, and
   *    <li>the DOM state of the child component must be consistent with the DOM
   *        state of the parent component (see {@code isInDocument}) in the
   *        steady state -- the exception is to addChildAt(child, i, false) and
   *        then immediately decorate/render the child.
   *  </ul>
   *
   * In particular, {@code parent.addChild(child)} will throw an error if the
   * child component is already in the document, but the parent isn't.
   *
   * Clients of this API may call {@code addChild} and {@code addChildAt} with
   * {@code opt_render} set to true.  If {@code opt_render} is true, calling these
   * methods will automatically render the child component's element into the
   * parent component's element.  However, {@code parent.addChild(child, true)}
   * will throw an error if:
   *  <ul>
   *    <li>the parent component has no DOM (i.e. {@code parent.getElement()} is
   *        null), or
   *    <li>the child component is already in the document, regardless of the
   *        parent's DOM state.
   *  </ul>
   *
   * If {@code opt_render} is true and the parent component is not already
   * in the document, {@code enterDocument} will not be called on this component
   * at this point.
   *
   * Finally, this method also throws an error if the new child already has a
   * different parent, or the given index is out of bounds.
   *
   * @see goog.ui.Component#addChild
   * @param child The new child component.
   * @param index 0-based index at which the new child component is to be
   *    added; must be between 0 and the current child count (inclusive).
   * @param opt_render If true, the child component will be rendered
   *    into the parent.
   */
  def addChildAt (child: Component, index: Int, opt_render: Boolean = false) {}
  
  /**
   * Removes the given child from this component, and returns it.  Throws an error
   * if the argument is invalid or if the specified child isn't found in the
   * parent component.  The argument can either be a string (interpreted as the
   * ID of the child component to remove) or the child component itself.
   *
   * If {@code opt_unrender} is true, calls {@link goog.ui.component#exitDocument}
   * on the removed child, and subsequently detaches the child's DOM from the
   * document.  Otherwise it is the caller's responsibility to clean up the child
   * component's DOM.
   *
   * @see goog.ui.Component#removeChildAt
   * @param child The ID of the child to remove,
   *    or the child component itself.
   * @param opt_unrender If true, calls {@code exitDocument} on the
   *    removed child component, and detaches its DOM from the document.
   * @return The removed component, if any.
   */
  def removeChild (child: AnyRef, opt_unrender: Boolean = false): Component = null
  
}

object Component {
  class EventType
  object EventType {      
    val BEFORE_SHOW = ""
    val SHOW = ""
    val HIDE = ""
    val DISABLE = ""
    val ENABLE = ""
    val HIGHLIGHT = ""
    val UNHIGHLIGHT = ""
    val ACTIVATE = ""
    val DEACTIVATE = ""
    val SELECT = ""
    val UNSELECT = ""
    val CHECK = ""
    val UNCHECK = ""
    val FOCUS = ""
    val BLUR = ""
    val OPEN = ""
    val CLOSE = ""
    val ENTER = ""
    val LEAVE = ""
    val ACTION = ""
    val CHANGE = ""
  }
  
  object Error {
    val ALREADY_RENDERED = ""
  }
}