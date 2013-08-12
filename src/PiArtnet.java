


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import Helper.Config;
public class PiArtnet {

	// define the log level for this Class
	private static final Level LOGLEVEL = Level.INFO;
	private static Logger logger =  Logger.getLogger( PiArtnet.class.getName() );

	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		logger.setLevel(LOGLEVEL);
		logger.info("Hello Pi");
		
		Config conf = null;
		try {
			conf = new Config("config.properties");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		MainLoop main = new MainLoop(conf);
		main.start();
		
	}
	
	
	
	

	
	
	
}
