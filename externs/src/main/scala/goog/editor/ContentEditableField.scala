package goog.editor

import browser._

/**
 * @fileoverview Class to encapsulate an editable field that blends into the
 * style of the page and never uses an iframe.  The field's height can be
 * controlled by CSS styles like min-height, max-height, and overflow.  This is
 * a goog.editor.Field, but overrides everything iframe related to use
 * contentEditable divs.  This is essentially a much lighter alternative to
 * goog.editor.SeamlessField, but only works in Firefox 3+, and only works
 * *well* in Firefox 12+ due to
 * https://bugzilla.mozilla.org/show_bug.cgi?id=669026.
 *
 * @author gboyer@google.com (Garrett Boyer)
 * @author jparent@google.com (Julie Parent)
 * @author nicksantos@google.com (Nick Santos)
 * @author ojan@google.com (Ojan Vafai)
 */


/**
 * This class encapsulates an editable field that is just a contentEditable
 * div.
 *
 * To see events fired by this object, please see the base class.
 *
 * @param {string} id An identifer for the field. This is used to find the
 *     field and the element associated with this field.
 * @param {Document=} opt_doc The document that the element with the given
 *     id can be found it.
 */
class ContentEditableField (id: String, opt_doc: Document = null) extends Field (id, opt_doc) {
  
}

/*

/**
 * @override
 */
goog.editor.ContentEditableField.prototype.logger =
    goog.debug.Logger.getLogger('goog.editor.ContentEditableField');


/** @override */
goog.editor.ContentEditableField.prototype.usesIframe = function() {
  // Never uses an iframe in any browser.
  return false;
};


// Overridden to improve dead code elimination only.
/** @override */
goog.editor.ContentEditableField.prototype.turnOnDesignModeGecko =
    goog.nullFunction;


/** @override */
goog.editor.ContentEditableField.prototype.installStyles = function() {
  goog.asserts.assert(!this.cssStyles, 'ContentEditableField does not support' +
      ' CSS styles; instead just write plain old CSS on the main page.');
};


/** @override */
goog.editor.ContentEditableField.prototype.makeEditableInternal = function(
    opt_iframeSrc) {
  var field = this.getOriginalElement();
  if (field) {
    this.setupFieldObject(field);
    // TODO(gboyer): Allow clients/plugins to override with 'plaintext-only'
    // for WebKit.
    field.contentEditable = true;

    this.injectContents(field.innerHTML, field);

    this.handleFieldLoad();
  }
};


/**
 * @override
 *
 * ContentEditableField does not make any changes to the DOM when it is made
 * editable other than setting contentEditable to true.
 */
goog.editor.ContentEditableField.prototype.restoreDom =
    goog.nullFunction;

*/