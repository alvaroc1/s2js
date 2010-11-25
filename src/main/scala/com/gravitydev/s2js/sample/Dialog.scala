/**
 * Porting of goog.ui.Dialog to scala as an exercise to make sure
 * the transformation is working correctly.
 * Work in progress.
 * @see http://closure-library.googlecode.com/svn/docs/closure_goog_ui_dialog.js.source.html
 */

package gravity

import goog.ui.Component
import goog.dom.DomHelper
import goog.events.FocusHandler
import goog.fx.Dragger
import browser.Element

class Dialog (
	val class_ :String = goog.getCssName("modal-dialog"), 
	val useIframeMask_ :Boolean = false, 
	domHelper:DomHelper = null
) extends Component (domHelper) {
	
	/** Button set. Default: Ok/Cancel */
	private var buttons_ = Dialog.ButtonSet.OK_CANCEL
		
	/** Focus handler. It will be initialized in enterDocument */
	private val focusHandler_ : FocusHandler = null
	
	/** Whether the escape key closes this dialog */
	private var escapeToCancel_ = true
	
	/** Whether the dialog should use an iframe as the background element to work */
	private var hasTitleClosButton_ = true
	
	/** Whether the dialog is modal. Defaults to true */
	private var modal_ = true
	
	/** Whether the dialog is draggable. Defaults to true */
	private var draggable_ = true
	
	/** Opacity for background mask. Defaults to 50% */
	private var backgroundElementOpacity_ = 0.50

	/** Dialog's title */
	private var title_ = ""
	
	/** Dialog's content (HTML) */
	private var content_ = ""
		
	/** Dragger */
	private var dragger_ : goog.fx.Dragger = null
	
	/** Whether dialog is visible */
	private var visible_ = false
	
	/** Wether the dialog should be disposed when it is hidden */
	private var disposeOnHide_ = false
	
	/** Element for the background which obscures the UI and blocks events */
	private var bgEl_ : Element = null
	
	/** Iframe element that is only used for IE as a workaround to keep select-type */
	private var bgIframeEl_ : Element = null
	
	/** Element for the title bar */
	private var titleEl_ : Element = null
	
	/** Element for the text area of the title bar */
	private var titleTextEl_ : Element = null
	
	/** Id of element fo the text area of the title bar */
	private var titleId_ : String = null
	
	/** Element for the close box area of the title bar */
	private var titleCloseEl_ : Element = null
	
	/** Element for the content area */
	private var contentEl_ : Element = null
	
	/** Element for the button bar */
	private var buttonEl_ : Element = null
	
	private var tabCatcherEl_ : Element = null
	
	/** Sets the title */
	def setTitle (title:String) {
		title_ = title
		if (titleTextEl_ != null) {
			goog.dom.setTextContent(titleTextEl_, title)
		}
	}
	
	/** Gets the title */
	def getTitle = title_
	
	/** Allows arbitrary HTML to be set in the content element */
	def setContent (html:String) {
		content_ = html
		if (contentEl_ != null) {
			contentEl_.innerHTML = html
		}
	}
	
	/** Gets the content HTML of the content element */
	def getContent = content_
	
	/** Renders if the DOM is not created */
	private def renderIfNoDom_() {
		if (getElement != null) {
			// TODO(user): Ideally we'd only create the DOM, but many applications
			// are requiring this behavior.  Eventually, it would be best if the
			// element getters could return null if the elements have not been
			// created.
			render()
		}
	}
	
	/**
	 * Returns the content element so that more complicated things can be done with
	 * the content area. Renders if the DOM is not yet created.
	 */
	def getContentElement = {
		renderIfNoDom_()
		contentEl_
	}
	
	/**
	 * Returns the content element so that more complicated things can be done with
	 * the title. Renders if the DOM is not yet created
	 */
	def getTitleElement = {
		renderIfNoDom_()
		titleEl_
	}
	
	/**
	 * Returns the title text element so that more complicated things can be done
	 * with the text of the title. Renders if the DOM is not yet created.
	 */
	def getTitleTextElement = {
		renderIfNoDom_()
		titleTextEl_
	}
	
	/**
	 * Returns the title close element so that more complicated things can be done
	 * with the close area of the title. Renders if the DOM is not yet created.
	 */
	def getTitleCloseElement = {
		renderIfNoDom_()
		titleCloseEl_
	}
	
	def getButtonElement = {
		renderIfNoDom_()
		buttonEl_
	}
	
	def getDialogElement = {
		renderIfNoDom_()
		getElement
	}
	
	def getBackgroundElement = {
		renderIfNoDom_()
		bgEl_
	}
	
	def getBackgroundElementOpacity = backgroundElementOpacity_
	
	def setBackgroundElementOpacity (opacity:Double) {
		backgroundElementOpacity_ = opacity
		
		if (bgEl_ != null) {
			goog.style.setOpacity(bgEl_, backgroundElementOpacity_)
		}
	}

	def setModal (modal:Boolean) {
		modal_ = modal
		manageBackgroundDom_()
		val dom = getDomHelper
		
		if (isInDocument && modal && isVisible) {
			if (bgIframeEl_ != null) {
				dom.insertSiblingBefore(bgIframeEl_, getElement)
			}
			if (bgEl_ != null) {
				dom.insertSiblingBefore(bgEl_, getElement)
			}
		}
		resizeBackground_()
	}
	
	def getModal = modal_
	
	// this might be a slight problem
	def $getClass = class_
	
	def setDraggable (draggable:Boolean) {
		draggable_ = draggable
		
		if (draggable_ && dragger_ == null && getElement != null) {
			dragger_ = createDraggableTitleDom_
		} else if (!draggable_ && dragger_ != null) {
			if (getElement != null) {
				goog.dom.classes.remove(titleEl_, goog.getCssName(class_, "title-draggable"))
			}
			dragger_.dispose
			dragger_ = null
		}
	}
	
	def createDraggableTitleDom_ () : Dragger = {
		val dragger = new Dragger (getElement, titleEl_)
		goog.dom.classes.add(titleEl_, goog.getCssName(class_, "title-draggable"))
		dragger
	}
	
	def getDraggable = draggable_
	
	def createDom () {
		manageBackgroundDom_()
		
		val dom = getDomHelper
		
		titleTextEl_ = dom.createDom("span", goog.getCssName(class_, "title-text"), title_)
		titleCloseEl_ = dom.createDom("span", goog.getCssName(class_, "title-close"))
		
		titleEl_ = dom.createDom(
			"div",
			Map("className"->goog.getCssName(class_, "title"), "id"->getId),
			titleTextEl_,
			titleCloseEl_
		)
		
		contentEl_ = dom.createDom("div", goog.getCssName(class_, "content"))
		
		buttonEl_ = dom.createDom("div", goog.getCssName(class_, "buttons"))
		
		tabCatcherEl_ = dom.createDom("span", Map("tabIndex"->0))
		
		setElementInternal(
			dom.createDom(
				"div",
				Map("className" -> class_, "tabIndex" -> 0),
				titleEl_,
				contentEl_,
				buttonEl_,
				tabCatcherEl_
			)
		)
		
		titleId_ = titleEl_.id
		goog.dom.a11y.setRole(getElement, "dialog")
		goog.dom.a11y.setState(getElement, "labelledby", titleId_)
		
		// If setContent() was called before createDom(), make sure the inner HTML of
		// the content elmenet is initialized
		if (this.content_ != null) {
			contentEl_.innerHTML = content_
		}
		goog.style.showElement(getElement, false)
		
		if (buttons_ != null) {
			buttons_.attachToElement(buttonEl_)
		}
	}
	
	/** Creates and disposes of the DOM for background mask elements. */
	def manageBackgroundDom_ () {
		if (useIframeMask_ && modal_ && bgIframeEl_ == null) {
		    // IE renders the iframe on top of the select elements while still
		    // respecting the z-index of the other elements on the page.  See
		    // http://support.microsoft.com/kb/177378 for more information.
		    // Flash and other controls behave in similar ways for other browsers
			bgIframeEl_ = goog.dom.iframe.createBlank(getDomHelper)
			bgIframeEl_.className = goog.getCssName(class_, "bg")
			goog.style.showElement(bgIframeEl_, false)
			goog.style.setOpacity(bgIframeEl_, 0)
			
		// Removes the iframe mask if it exists and we don't want it to
		} else if ((!useIframeMask_ || !modal_) && bgIframeEl_ != null) {
			goog.dom.removeNode(bgIframeEl_)
			bgIframeEl_ = null
		}
		
		// Create the backgound mask, initialize its opacity, and make sure it's
		// hidden.
		if (modal_ && bgEl_ == null) {
			bgEl_ = getDomHelper.createDom("div", goog.getCssName(class_, "bg"))
			goog.style.setOpacity(bgEl_, backgroundElementOpacity_)
			goog.style.showElement(bgEl_, false)
				
		// Removes the background mask if it exists and we don't want it to
		} else if (!modal_ && bgEl_ != null) {
			goog.dom.removeNode(bgEl_)
			bgEl_ = null
		}
	}
	
	def render (parent:Element = getDomHelper.getDocument.body) {
		if (isInDocument) {
			throw new Exception(goog.ui.Component.Error.ALREADY_RENDERED)
		}
		
		if (getElement == null) {
			createDom()
		}
	}
	
	
	def resizeBackground_ () {
		// TODO
	}
	
	def isVisible = visible_
}

object Dialog {
	class Event (val key:String, val caption:String) extends goog.events.Event {
		val `type` = EventType.SELECT
	}
	
	object EventType {
		val SELECT = "dialogselect"
		val AFTER_HIDE = "afterhide"
	}
	
	class ButtonSet (val dom_ :DomHelper = goog.dom.getDomHelper()) extends goog.structs.Map {
		var element_ :Element = null
		
		def attachToElement (el:Element) {
			element_ = el
			render()
		}
		
		def render () {
			if (element_ != null) {
				element_.innerHTML = ""
				val domHelper = goog.dom.getDomHelper(element_)
				goog.structs.forEach(this, (caption, key) => {
					var button = domHelper.createDom("button", Map("name"->key), caption)
				})
			}
		}
	}
	
	object ButtonSet {
		val OK_CANCEL = new ButtonSet
	}
}

