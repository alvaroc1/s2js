package pages

import browser._
import goog.ui.{Dialog, HsvaPalette}
import goog.events.Event

object demo {
	def main {
		val d = new Dialog
		d.setTitle("Welcome!")
		d.setContent("This is my welcome dialog")
		
		val button = goog.dom.getElement("say-welcome-button")
		
		goog.events.listen(button, "click", (e:Event) => {
			d.setVisible(true)
		})
		
		val palette = new HsvaPalette()
		palette.render(goog.dom.getElement("demo"))
	}
}
