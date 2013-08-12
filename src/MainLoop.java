
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import Helper.Config;

public class MainLoop extends Thread {

	
	// define the log level for this Class
		private static final Level LOGLEVEL = Level.WARNING;
		private static Logger logger =  Logger.getLogger( MainLoop.class.getName() );

		
		private  HashMap<Integer, int[] > timeEvents = new HashMap<Integer, int[]>(); 
		private  HashMap<Integer, Integer > channelValues = new HashMap<Integer, Integer>(); 
	private  Config conf;
	
	
	
	
	public MainLoop(Config conf) {
		super();
		this.conf = conf;
		logger.setLevel(LOGLEVEL);
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		
		
		
		loadTimeEvents();
		setUpArtNet();
		setUpValueMapping();
		
		
		while(true){
			
			//logger.info("Next loop");
			
			
			checkTimeEvents();
			//checkSensors();
			//changeArtnetValues();
			//sendArtnet();
			
		
			// wait some time to check next
			try {
				MainLoop.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		
	}

	
	private void checkTimeEvents() {
		
		Date date = new Date(System.currentTimeMillis());
		//logger.info("Current Date = " + date.toString());
	
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		
		int formatedHours;
		if (hours < 10) {
			formatedHours = 1000 + hours * 100;
		}else
			formatedHours = hours * 100;
		
		
		//time is written in xxxx 
		int currentFormatedTime = formatedHours + minutes;
		
		if (timeEvents.containsKey(currentFormatedTime)) {
			// TODO if it matches timeevent it jumps in here for the whole minute
			logger.info("Found time event");
			
			int[] eventData = timeEvents.get(currentFormatedTime);
			
			int channel = eventData[0];
			int value = eventData[1];
			
			logger.info("Channel = " + channel);
			logger.info("Value = " + value);
			
		}
		
		
	}


	private void setUpValueMapping() {
		// TODO Auto-generated method stub
		
	}


	private void setUpArtNet() {
		// TODO Auto-generated method stub
		
	}


	public  void loadTimeEvents() {
		
		String[] events = conf.getString("timeevents").split(",");
		
		for (String event : events) {
			String[] eventData = event.split("-");
			
			if (eventData.length != 3) {
				logger.warning( "Event Data not correct. timecode - channel - value");
				continue;
			}
			 if ( eventData[0].trim().length() !=  4) {
				 logger.warning( "Event Time not correct. format = xxxx");
				continue;
			 }
		
			 
			int[] data = new int[2];
					
			data[0] = Integer.parseInt( eventData[1].trim());
			data[1] = Integer.parseInt( eventData[2].trim());
			int time = Integer.parseInt(eventData[0].trim());
			
			if (time < 0) {
				 logger.warning( "Time has to be greater 0");
					continue;
			}
			if (data[0] < 0 || data[0] > 512) {
				 logger.warning( "Channel has to be between 0 and 512");
					continue;
			}
			if (data[1] < 0 || data[1] > 100) {
				 logger.warning( "Value has to be between 0 and 100");
					continue;
			}
			
			timeEvents.put(time, data );
		}
		
		logger.info("Time events loaded");
		
	}
	
}
