package goog.events


class Event (tpe:String, opt_target:AnyRef = null) {
  val target = opt_target
  val currentTarget:AnyRef = null
  def preventDefault() {}
      
  
  /**
   * Stops event propagation.
   */
  def stopPropagation () {}
}
