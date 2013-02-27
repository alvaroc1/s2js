package com.gravitydev.s2js

object Cleaner {
  def clean (tree: ast.Tree) = tree match {
    case x: ast.Leaf => x
    case x: ast.Branch => 
  }
}

