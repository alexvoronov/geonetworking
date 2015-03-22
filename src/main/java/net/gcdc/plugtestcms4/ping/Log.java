package net.gcdc.plugtestcms4.ping;

import java.util.logging.Logger;

/** A tiny utility class to share a {@link java.util.logging.Logger} in a package.
 * 
 * A static logger is available through <code>Log.LOGGER</code> without the need
 * of a method call to get the logger (i.e., keeps logging code concise).
 * 
 * <p>
 * The logger has package access but can be set from external source (outside this package)
 * through {@link #setLogger} method.
 * 
 * <p>
 * Unfortunately, because we do not want to grant public access to {@link #LOGGER},
 * this class must be present as a copy in every package you want to use it in.
 * 
 * @author Jan de Jongh
 * 
 */
public class Log
{

  static Logger LOGGER = Logger.getLogger (Logger.GLOBAL_LOGGER_NAME);
  
  /** Set the {@link Logger} for errors and warnings from this package.
   *
   * @param logger The new logger; may be null in which the logger is set to
   *               {@link Logger#GLOBAL_LOGGER_NAME}.
   *
   * @see Logger#getLogger(java.lang.String)
   * 
   */
  public static void setLogger (Logger logger)
  {
    if (LOGGER != logger)
    {
      LOGGER.warning ("Switching logger!");
      if (logger == null)
        LOGGER = Logger.getLogger (Logger.GLOBAL_LOGGER_NAME);
      else
        LOGGER = logger;
    }
  }

}
