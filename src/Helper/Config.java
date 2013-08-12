 package Helper;


import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * The helper class for reading configuration from .properties file.
 */
public class Config {

	// define the log level for this Class
		private static final Level LOGLEVEL = Level.WARNING;
		private static Logger logger =  Logger.getLogger( Config.class.getName() );

	
	private final Properties props;

	/**
	 * Creates instance of Config which reads configuration data form
	 * .properties file with given name found in classpath.
	 * 
	 * @param name the name of the .properties file
	 */
	public Config(final String name) throws IOException  {

		logger.setLevel(LOGLEVEL);
		
		java.io.InputStream is = ClassLoader.getSystemResourceAsStream(name);
		if (is != null) {
			props = new Properties();
			try {
				props.load(is);			
			} catch (IOException e) {
			
				logger.severe("IOException - Could not load Property List");
				throw e;
			
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					logger.severe("IOException - Could not close Stream");
					throw e;
				}
			}
		} else {
			logger.severe("Properties file not found!");
			throw new IOException();
		}
	}

	/**
	 * Returns the value as String for the given key.
	 * 
	 * @param key the property's key
	 * @return String value of the property, null if not found
	 */
	public String getString(final String key) {
		return props.getProperty(key);
	}

	/**
	 * Returns the value as int for the given key.
	 * 
	 * @param key the property's key
	 * @return int value of the property
	 */
	public int getInt(final String key) throws NumberFormatException{
		return Integer.parseInt(getString(key));
	}
	
	/**
	 * Returns the value as long for the given key.
	 * 
	 * @param key the property's key
	 * @return long value of the property
	 */
	public long getLong(final String key) throws NumberFormatException{
		return Long.parseLong(getString(key));
	}
}
