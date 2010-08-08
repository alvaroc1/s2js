goog.provide('gravity.Test');

/**
 * @constructor
 */
gravity.Test = function () {
};
/**
 * @param {Array[String]} args
 */
gravity.Test.prototype.run = function (args) {
	/** @type {boolean} */
	var x = true;
	scala.this.Predef.println("testing")
	scala.this.Predef.println("now i'm done")
};
/**
 * @param {boolean} j
 */
gravity.Test.prototype.a = function (j) {
	/** @type {boolean} */
	var y = false;
};
