package com.gravitydev.s2js

trait Extractors {self: Processor2 =>
  import global._
  
  def processAST (tree: ast.Tree): ast.Tree = tree match {
    case Array(elements) => processAST( ast.Array(elements) )
    //case x: Branch = x.visit(processAST)
    case x => x
  }
  
  object Array {
    def unapply (tree: ast.Tree): Option[Seq[ast.Tree]] = tree match {
      // empty array
      case tree => Some(Seq(tree))
    }
      /*
      case ast.Apply ( 
          JsTypeApply( 
            JsApply( 
              JsSelect( 
                JsSelect( 
                  JsSelect(
                    JsSelect(JsIdent("scala",_), "collection", JsSelectType.Module, _), 
                    "immutable", 
                    JsSelectType.Module,
                    _
                  ), 
                  "List", 
                  JsSelectType.Module,
                  _
                ), 
                "apply", 
                JsSelectType.Method,
                _
              ), 
              _
            ), 
            _
          ), 
          params
        ) => ast.Arr
      case x => None
    }
    * 
    */
  }
}
