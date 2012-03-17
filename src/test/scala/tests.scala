import org.specs2.mutable._
import S2JSParser.parse

class BasicCompilerSpec extends Specification {
  "Compiler" should {
    def matchCompiled (code:String, expected:String) = {
      cleanWhitespace(parse(code.stripMargin('|'))) mustEqual cleanWhitespace(expected)
    }
    def cleanWhitespace (s:String) = s.stripMargin('|').trim.replace("\n\n", "\n").replace("\n", " ").replace("  ", " ").replace("  ", " ")
    
    "parse correctly" in {
      matchCompiled (
        "object Hello",
        
        "goog.provide('Hello');"
      )
      
      matchCompiled (
        "package hello; object World",
          
        "goog.provide('hello.World');"
      )
      
      matchCompiled (
        "class Test",
        
        "goog.provide('Test'); Test = function () {};"
      )
      
      matchCompiled (
        """|package com.utils
           |
           |object array {
           |  def print () {} 
           |
           |}""",
           
        """|goog.provide('com.utils.array');
           |
           |com.utils.array.print = function () {};"""          
      )
      
      matchCompiled (
        """|package com.utils
           |
           |object array {
           |  def print (s:String) = s
           |  def test (b:Boolean) = if (b) true else false
           |}"""

        ,
        """|goog.provide('com.utils.array');
           |
           |/**
           | * @param {string} s
           | * @return {string}
           | */
           |com.utils.array.print = function (s) {
           |  return s;
           |};
           |
           |com.utils.array.test = function (b) {
           |  
           |};"""
      )
    }
  }
}
