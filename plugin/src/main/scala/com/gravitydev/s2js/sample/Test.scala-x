package gravity.ui

import browser._

class AddStoryDialog (url:String, labels:List[String], opt_params:Object) extends goog.ui.Dialog {
	setTitle("Add Story")
	setContent(
		<div id='add-story-dialog'>
			<form>
			
			<div id='add-story-errors' class='error' style='display: none'></div>
				
				<table class='form' width='100%'>
					<tr>
						<td colspan='3'><label>Title: </label><br />
						<input id='add-story-title' name='Name' style='width: 100%' class='text' /></td>
					</tr>
						
					<tr valign='top'>
						<td colspan='3'>
							<label>Description:</label><br />
							<div class='editor'>
								<div id='add-story-description-toolbar'></div>
								<div id='add-story-description-editor'><font size='2'><span style='font-family: arial, sans-serif'> </span></font></div>
							</div>
						</td>
					</tr>
					<tr>
						<td>
							<label>Labels: </label><br />
							<input id='labels-field' name='Labels' class='text' style='width: 100%;' />
						</td>
						
						<td width='20'></td>
					
						<td width='50%'>
							<label>Cost: </label><br />
							<input name='Estimate' class='text' size='2' value='3' /> &nbsp;
							<acronym title="Amount of effort required to complete (1 ideal day = 1 point)">points</acronym>
						</td>
					</tr>
				
				</table>
			
			</form>
		</div>
		.toString
	)
	
	alert("Test")
}
