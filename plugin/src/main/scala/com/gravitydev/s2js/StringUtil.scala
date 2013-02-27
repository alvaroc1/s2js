package com.gravitydev.s2js

object StringUtil {
	def indent (text:String) = if (text == "") "" else text.split("\n").map("  "+_).mkString("\n") + "\n"
	def stripQuotes (text:String) = text.stripSuffix("\"").stripPrefix("\"")
}
