package gravity.ui

import browser.HTMLInput

class Test {
	def main {
		val i = new HTMLInput
		i.value = if (true) "yes" else "no"
	}
}