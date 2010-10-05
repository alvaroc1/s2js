package gravity

import goog.ui.Component
import goog.dom.DomHelper
import goog.events.FocusHandler
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
}

/*
object Dialog {
	
	class ButtonSet (private val cls:String = goog.css.getCssName("test"))
	
	object ButtonSet {
		val OK_CANCEL = new ButtonSet
	}
}
*/
