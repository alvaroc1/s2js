package goog
  
/**
 * Class that provides the basic implementation for disposable objects. If your
 * class holds one or more references to COM objects, DOM nodes, or other
 * disposable objects, it should extend this class or implement the disposable
 * interface (defined in goog.disposable.IDisposable).
 */
class Disposable {
  
  /**
   * Disposes of the object. If the object hasn't already been disposed of, calls
   * {@link #disposeInternal}. Classes that extend {@code goog.Disposable} should
   * override {@link #disposeInternal} in order to delete references to COM
   * objects, DOM nodes, and other disposable objects. Reentrant.
   */
  def dispose () {}
}
