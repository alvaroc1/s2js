package com.gravitydev.s2js.ast

sealed case class Location(node: Tree, path: Path) {
  def down = node match { // to to the first child
    case l:Leaf => None
    case b:Branch => Some(Location(b.children.head, Hole(Nil, this, b.children.tail.toList)))
  }
  def up = path match {
    case Top => None
    case Hole(left, parent, right) => Some(parent)
  }
  def right = path match {
    case Top => None
    case Hole(_,_,Nil) => None
    case Hole(leftsibs,parent,rightsibs) => Some(Location(rightsibs.head, Hole(node :: leftsibs, parent, rightsibs.tail)))
  }
  def left = path match {
    case Top => None
    case Hole(Nil,_,_) => None
    case Hole(leftsibs,parent,rightsibs) => Some(Location(leftsibs.head, Hole(leftsibs.tail, parent, node :: rightsibs)))
  }
}

sealed trait Path
case object Top extends Path
final case class Hole (left: List[Tree], parent: Location, right: List[Tree]) extends Path

trait Transformer [A <: Tree] {
  def apply (a: A): A
}
 