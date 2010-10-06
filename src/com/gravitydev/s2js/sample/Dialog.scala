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

class Dialog (opt_class:String, opt_useIframeMask:Boolean, opt_domHelper:DomHelper) extends Component (opt_domHelper) {
	val class_ = opt_class
	val useIframeMask_ = opt_useIframeMask
	
	/** Button set. Default: Ok/Cancel */
	//private var buttons_ = Dialog.ButtonSet.OK_CANCEL
		
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
	private var buttonEl_ : Element = _
	
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
		val dragger = new Dragger ()
		goog.dom.classes.add(titleEl_, goog.getCssName(class_, "title-draggable"))
		dragger
	}
	
	def render () {
		// TODO
	}
	
	def manageBackgroundDom_ () {
		// TODO
	}
	
	def resizeBackground_ () {
		// TODO
	}
	
	def isVisible = visible_
}

/*
object Dialog {
	
	class ButtonSet (private val cls:String = goog.css.getCssName("test"))
	
	object ButtonSet {
		val OK_CANCEL = new ButtonSet
	}
}
*/
