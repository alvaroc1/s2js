package com.gravitydev.s2js

import org.kiama.rewriting.Rewriter
import com.gravitydev.s2js.ast._
import scala.collection.mutable.ListBuffer

object Inspector extends Rewriter {
  import Printer.print
  
  def findProvides (node :Tree) :Set[String] = {
    def findProvidesInChildren (b:Product) = {
      ((b.productIterator filter (_.isInstanceOf[Tree])) ++
      (b.productIterator filter (_.isInstanceOf[Seq[_]]) flatMap (_.asInstanceOf[Seq[_]] filter (_.isInstanceOf[Tree]) map (_.asInstanceOf[Tree]))) map 
          (_.asInstanceOf[Tree]) map {findProvides _}).foldLeft(Set[String]()){_++_}
    }
    
    node match {
      case m @ Module(name, _, _, _, _) => findProvidesInChildren(m) + name
      case c @ Class(name, _, _, _, _) => findProvidesInChildren(c) + name
      case p @ Package("_default_", _, _) => findProvidesInChildren(p)
      case p:Package => findProvidesInChildren(p) map {p.name + "." + _}
      case b:Product => findProvidesInChildren(b)
    }
  }
  
  def findRequires (tree: Tree): List[String] = {     
    val l = new ListBuffer[String]
    
    val find = query {
      // class supertype
      case Class(_,Some(supType),_,_,_) => l += Printer.printType(supType)
      
      // ignore right side of JsProperty and JsVar if it's a select, but add the tpe
      /*
      case Var(id, tpe @ Select(_,_,_), Select(_,_,_)) => {
        l.append(print(tpe))
      }*/
      
      //case Property(owner, name, tpt @ Select(_,_,_,_), Select(_,_,_,_)) => {
      //  l.append(print(tpt))
      //}
      // TODO: these should be collapsed into one case with guards
      // select class
      case s @ Select(_,_,SelectType.Class) => {
        l.append(print(s))
      }
      // select module
      case Select(s @ Select(_,_,SelectType.Module), _, SelectType.Method) => {
        l.append(print(s))
      }
      // select prop
      case Select (s @ Select(_,_,SelectType.Module),_,SelectType.Prop) => {
        l.append(print(s))
      }
      
      // TODO: had to move this one down to not conflict with the package one. Gotta figure out what the deal is
      // ignore applications on local variables
      //case Select(JsIdent(_,_),_,_,_) => ()
      
      //case ClassRef(name) => l.append(name)
      
      case _ => ()
    }
    
    topdown(find)(tree)
    
    // remove anything that ends in underscore or starts with browser
    l.filterNot(_ endsWith "_").filterNot(_ startsWith "browser.").toList.distinct
  }
  
}
