package vanity

import goog.events.Event

object pages {
	def main {
		val d = new goog.ui.Dialog()
		d.setTitle("Add Page")
		d.setContent("Content")
		
		
		val b = goog.dom.getElement("add-page-button")
		if (b != null) {
			goog.events.listen(b, "click", (e:Event) => {
				d.setVisible(true)
			})
		}
	}
}
