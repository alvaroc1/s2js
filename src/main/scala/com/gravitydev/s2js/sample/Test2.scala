package pages

import browser._
import goog.dom.{getElement, getElementsByTagNameAndClass, getAncestorByTagNameAndClass}
import goog.events.{Event, BrowserEvent, EventType, KeyCodes}
import goog.net.XhrIo

import goog.ui.LabelInput

object labels {
	def main (
			createLabelUrl:String, 
			labelsUrl:String, 
			labelUrl:String, 
			deleteLabelUrl:String, 
			updateLabelUrl:String,
			
			addFieldUrl:String, 
			labelFieldsUrl:String, 
			deleteLabelFieldUrl:String,
			
			addTestUrl:String,
			labelTestsUrl:String,
			deleteLabelTestUrl:String,
			
			addTaskUrl:String,
			labelTasksUrl:String,
			deleteLabelTaskUrl:String
	) {
		
		val labelsDiv = getElement("labels")
		val button = getElement("new-label-button").asInstanceOf[HTMLButtonElement]
		
		val input = getElement("new-label-input")
		if (input != null) {
			val labelInput = new LabelInput("Create a new label")
			
			labelInput.decorate(input)
		
			goog.events.listen(input, "keyup", (e:BrowserEvent) => {
				if (e.keyCode != KeyCodes.ENTER) {
					println("test")
					button.disabled = labelInput.getValue.length == 0
				}
			})
		}
		
		def getLabelName (el:Element) = {
			val labelDiv = getAncestorByTagNameAndClass(el, "TR", null).asInstanceOf[Element]
			getElementsByTagNameAndClass("A", "label-link", labelDiv)(0).innerHTML
		}
		
		def wireLabels () {
			// label link
			val labels = getElementsByTagNameAndClass("A", "label-link", labelsDiv)
			for (link <- labels) {
				goog.events.listen(link, "click", (e:Event) => {
					selectLabel(link.innerHTML)
					
					e.preventDefault()
				})
			}
		}
		
		def wireLabel (name:String) {
			// edit
			val detailsDiv = getElement("label-details")
			val editButton = getElementsByTagNameAndClass("IMG", "edit-label-button", detailsDiv)(0)
			
			val nameSpan = getElementsByTagNameAndClass("SPAN", "label-name", detailsDiv)(0)
			val editSpan = getElementsByTagNameAndClass("SPAN", "label-name-edit", detailsDiv)(0)
			val input = getElementsByTagNameAndClass("INPUT", null, editSpan)(0).asInstanceOf[HTMLInputElement]
			
			// edit
			if (editButton != null) {
				
				// cancel edit
				def cancelEdit (e:Event) {
					nameSpan.style.display = ""
					editSpan.style.display = "none"
					getElementsByTagNameAndClass("DIV", "inline-error", detailsDiv)(0).innerHTML = ""
				}
				val cancelEditButton = getElementsByTagNameAndClass("BUTTON", "label-cancel-edit", detailsDiv)(0)
				
				val saveEditButton = getElementsByTagNameAndClass("BUTTON", "label-save-edit", detailsDiv)(0)
				
				// FIELDS
				val fieldNameInput = getElementsByTagNameAndClass("INPUT", "new-field-name", detailsDiv)(0).asInstanceOf[HTMLInputElement]
				val fieldTypeSelect = getElementsByTagNameAndClass("SELECT", "new-field-type", detailsDiv)(0).asInstanceOf[HTMLSelectElement]
				val newFieldButton = getElementsByTagNameAndClass("BUTTON", "new-field-button", detailsDiv)(0).asInstanceOf[HTMLButtonElement]
				
			
				// fields
				val addFieldLink = getElement("add-label-field-link")
				if (addFieldLink != null) {
					val addFieldForm = getElement("add-label-field-form")
				}
			}
			//fieldTypeSelect.on(EventType.KEYPRESS, (e:BrowserEvent) => if (e.keyCode == KeyCodes.ENTER) addField(e))
			
			// TESTS
			val addTestLink = getElement("add-label-test-link")
			if (addTestLink != null) {
				val addTestForm = getElement("add-label-test-form")
				val addTestButton = getElement("add-label-test-button").asInstanceOf[HTMLButtonElement]
				
				// add test
				val testNameInput = getElement("new-test-name").asInstanceOf[HTMLInputElement]
				val testDescriptionArea = getElement("new-test-description").asInstanceOf[HTMLTextAreaElement]
			}
			
			// TASKS
			val addTaskLink = getElement("add-label-task-link")
			if (addTaskLink != null) {
				val addTaskForm = getElement("add-label-task-form")
				val addTaskButton = getElement("add-label-task-button").asInstanceOf[HTMLButtonElement]
				
				// add test
				val taskContentArea = getElement("new-task-content").asInstanceOf[HTMLTextAreaElement]
				val taskHoursInput = getElement("new-task-hours").asInstanceOf[HTMLInputElement]

			}
		}
		
		def selectLabel (label:String) {
			val labelLinks = getElementsByTagNameAndClass("A", "label-link", labelsDiv)
			
			for (link <- labelLinks) {
				val tr = getAncestorByTagNameAndClass(link, "TR").asInstanceOf[Element]
				if (link.innerHTML == label) {
					goog.dom.classes.add(tr, "label-sel")
				} else {
					goog.dom.classes.remove(tr, "label-sel")
				}
			}
		}
		
		wireLabels()
	}
}
