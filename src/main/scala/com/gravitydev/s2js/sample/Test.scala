package vanity2.ui.editor

import goog.dom.TagName
import browser._

class LinkDialog (dialogDomHelper:goog.dom.DomHelper, link:goog.editor.Link) extends goog.ui.editor.LinkDialog (dialogDomHelper, link) {
	val dom = dialogDomHelper
	var textToDisplayDiv_ :HTMLDivElement = null
	var tabPane_ :goog.ui.editor.TabPane = null
	
	var eventHandler_ = new goog.events.EventHandler(this)

	override def createDialogControl () = {
		alert("create")
		textToDisplayDiv_ = buildTextToDisplayDiv_()
		
		val content = dom.createDom(TagName.DIV, null, textToDisplayDiv_)
		
		val builder = new goog.ui.editor.AbstractDialog.Builder(this)
		builder.setTitle(goog.ui.editor.messages.MSG_EDIT_LINK)
			.setContent(content)
	
		tabPane_ = new goog.ui.editor.TabPane(dom, goog.ui.editor.messages.MSG_LINK_TO)
		tabPane_.addTab(
			"linkdialog-internal",
			"Internal Page",
			"Internal Page",
			buildTabInternal()
		)
		tabPane_.addTab(
			goog.ui.editor.LinkDialog.Id_.ON_WEB_TAB,
			goog.ui.editor.messages.MSG_ON_THE_WEB,
			goog.ui.editor.messages.MSG_ON_THE_WEB_TIP,
			buildTabOnTheWeb_()
		)
		tabPane_.addTab(
			goog.ui.editor.LinkDialog.Id_.EMAIL_ADDRESS_TAB,
			goog.ui.editor.messages.MSG_EMAIL_ADDRESS,
			goog.ui.editor.messages.MSG_EMAIL_ADDRESS_TIP,
			buildTabEmailAddress_()
		)
		tabPane_.render(content)
		
		// add the "text" classes to the inputs
		val inputs = goog.dom.getElementsByTagNameAndClass("INPUT", null, content)
		goog.array.forEach[Element](inputs, (i) => {
			goog.dom.classes.add(i, "text")
		})
		
		eventHandler_.listen(
			tabPane_,
			goog.ui.Component.EventType.SELECT,
			onChangeTab_ _
		)
		
		builder.build()
	}
	
	private def buildTabInternal () = {
		goog.dom.createDom("DIV", null, "testing")
	}
	
	override def onChangeTab_ (e:goog.events.Event) {
		val tab = e.target.asInstanceOf[goog.ui.Tab]
		
		if (tab.getId() != "linkdialog-internal") {
			super.onChangeTab_(e)
		}
	}
	
}
