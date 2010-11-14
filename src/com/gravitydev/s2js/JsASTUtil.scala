package com.gravitydev.s2js

object JsASTUtil {
		
	// method that can apply a function to all children of a Node
	def visitAST (tree:JsTree, fn:(JsTree)=>JsTree):JsTree = {
		tree match {
			case JsSourceFile(path,name,classes) => JsSourceFile(
				path, 
				name, 
				classes map fn 
			)
			case JsClass(name,pkg,parents,constructor,properties,methods) => {
				
				JsClass(
					name,
					pkg,
					parents map (fn(_).asInstanceOf[JsSelect]),
					fn(constructor).asInstanceOf[JsConstructor],
					properties map (fn(_).asInstanceOf[JsProperty]),
					methods map (fn(_).asInstanceOf[JsMethod])
				)
			}
			case JsModule (owner,name,body) => JsModule(
				fn(owner),
				name,
				body map fn
			)
			case JsConstructor(name,params,constructorBody,classBody) => JsConstructor(
				name,
				params map (fn(_).asInstanceOf[JsParam]),
				constructorBody map fn,
				classBody map fn
			)
			case JsApply(fun,params) => JsApply(
				fn(fun),
				params map fn
			)
			case JsTypeApply(fun,params) => JsTypeApply(
				fn(fun),
				params map fn
			)
			case JsSelect(qualifier,name,t) => JsSelect(
				fn(qualifier),
				name,
				t
			)
			case JsMethod(owner, name,params,children,ret) => JsMethod(
				fn(owner),
				name,
				params map (fn(_).asInstanceOf[JsParam]),
				children map fn,
				fn(ret)
			)
			case JsBlock(children) => JsBlock(
				children map fn	
			)
			case JsVar (id, tpe, rhs) => JsVar(id, tpe, fn(rhs))
			
			case JsIf (cond, thenp, elsep) => JsIf(fn(cond), fn(thenp), fn(elsep))
			
			case JsAssign (lhs, rhs) => JsAssign (fn(lhs), fn(rhs))
			
			case JsUnaryOp (select, op) => JsUnaryOp(fn(select), op)
			
			case JsComparison (lhs, operator, rhs) => JsComparison (fn(lhs), operator, fn(rhs))
			
			case JsParam (name, tpe) => JsParam(name, fn(tpe))
			
			case JsProperty (mods, name, tpt, rhs) => JsProperty (mods, name, fn(tpt), fn(rhs))
			
			case JsNew (tpt) => JsNew( fn(tpt).asInstanceOf[JsSelect] )
			
			case JsMap (elements) => JsMap ( elements map (fn(_).asInstanceOf[JsMapElement]))
			
			case JsMapElement (key, value) => JsMapElement(key, fn(value))
			
			case x:JsSuper => x
			case x:JsVoid => x
			case x:JsIdent => x
			case x:JsLiteral => x
			case x:JsThis => x
			case x:JsBuiltInType => x
			case x => {
				println(x)
				
				x
			}
		}
	}
}
