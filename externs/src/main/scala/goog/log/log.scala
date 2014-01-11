package goog.log

object `package` {
  
  /**
   * Finds or creates a logger for a named subsystem. If a logger has already been
   * created with the given name it is returned. Otherwise a new logger is
   * created. If a new logger is created its log level will be configured based
   * on the goog.debug.LogManager configuration and it will configured to also
   * send logging output to its parent's handlers.
   * @see goog.debug.LogManager
   *
   * @param name A name for the logger. This should be a dot-separated
   *     name and should normally be based on the package name or class name of
   *     the subsystem, such as goog.net.BrowserChannel.
   * @param opt_level If provided, override the
   *     default logging level with the provided level.
   * @return The named logger or null if logging is disabled.
   */
  def getLogger (name: String, opt_level: Level = null): Logger = null
  
  /**
   * Logs a message at the Level.INFO level.
   * If the logger is currently enabled for the given message level then the
   * given message is forwarded to all the registered output Handler objects.
   * @param logger
   * @param msg The message to log.
   * @param An exception associated with the message.
   */
  def info (logger: Logger, msg: String, opt_exception: Exception = null) {}
  
}
