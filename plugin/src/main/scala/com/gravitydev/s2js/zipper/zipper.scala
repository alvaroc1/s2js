package com.gravitydev.s2js.zipper

sealed trait Node[A] {
  def item: A
}

case class Leaf [A](item: A) extends Node[A]
case class Branch [A](item: A, children: List[Node[A]]) extends Node[A]
object Branch {
  def apply [A](item: A)(children: Node[A]*): Branch[A] = new Branch(item, children.toList)
}

sealed case class Location[A](node: Node[A], path: Path[A]) {
  def down = node match { // to to the first child
    case Leaf(_) => None
    case Branch(node, children) => Some(Location(children.head, Hole(Nil, this, children.tail)))
  }
  def up = path match {
    case Top() => None
    case Hole(left, parent, right) => Some(parent)
  }
  def right = path match {
    case Top() => None
    case Hole(_,_,Nil) => None
    case Hole(leftsibs,parent,rightsibs) => Some(Location(rightsibs.head, Hole(node :: leftsibs, parent, rightsibs.tail)))
  }
  def left = path match {
    case Top() => None
    case Hole(Nil,_,_) => None
    case Hole(leftsibs,parent,rightsibs) => Some(Location(leftsibs.head, Hole(leftsibs.tail, parent, node :: rightsibs)))
  }
}

sealed trait Path[A]
final case class Top [A]() extends Path[A]
final case class Hole [A](left: List[Node[A]], parent: Location[A], right: List[Node[A]]) extends Path[A]

object A extends App {

  val tree = Branch("A")(
    Branch("B")(
      Leaf("E")
    ),
    Branch("C")(
      Leaf("F"),
      Leaf("G"),
      Leaf("H")
    ),
    Leaf("D"),
    Leaf("X")
  )
  
  val location = Location (tree, Top())
  
  println(location.node.item)
  println(location.down.flatMap(_.right.flatMap(_.left)))
  
  val item1 = for {
    d <- location.down
    r <- d.right
    l <- r.left
    l2 <- l.left
  } yield println(l2.node)
  
  println(item1)    

}
