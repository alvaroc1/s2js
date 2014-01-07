package goog.math

class Box (val top:Int, val right:Int, val bottom:Int, val left:Int)

class Size (val width:Int, val height:Int)

class Coordinate (opt_x:Int = 0, opt_y:Int = 0) {
	val x = opt_x
	val y = opt_y
}
