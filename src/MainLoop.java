
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import Helper.Config;

public class MainLoop extends Thread {


	// define the log level for this Class
	private static final Level LOGLEVEL = Level.INFO;
	private static Logger logger =  Logger.getLogger( MainLoop.class.getName() );

	//Integer is the timecode xxx
	//The arraylist contains all timeevents  at the same time 
	private  HashMap<Integer, ArrayList<TimeEvent> > timeEvents = new HashMap<Integer, ArrayList<TimeEvent>>(); 

	private  HashMap<Integer, Integer > channelValues = new HashMap<Integer, Integer>(); 

	private  Config conf;

	private ArtnetDevice artnetDevice = new ArtnetDevice(); 


	//map the Time events value to this values.
	private int startValue, endValue;




	public MainLoop(Config conf) {
		super();
		this.conf = conf;
		logger.setLevel(LOGLEVEL);
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();



		loadPropertieFile();
		setUpArtNet();


		while(true){

			//logger.info("Next loop");


			checkTimeEvents();
			//checkSensors();
			sendData();
			artnetDevice.allChannelsOn();

			// wait some time to check next
			try {
				MainLoop.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}



	}


	private void sendData() {
		artnetDevice.sendValuesForChannels( channelValues);
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


			ArrayList<TimeEvent> eventsOnTime = timeEvents.get(currentFormatedTime);

			for (TimeEvent event : eventsOnTime) {

				int channel = event.getChannel();
				int value = event.getTime();	
				
				//map to dmx 0-255

				value = Math.round( 255 * (value/100) );
				// old values get replaced 
				channelValues.put(channel, value);
			}



		}


	}


	private void setUpValueMapping() {
		// TODO Auto-generated method stub

	}


	private void setUpArtNet() {
		artnetDevice.start();
	}

	private void loadPropertieFile(){


		loadTimeEvents();
		loadMappingValues();

	}

	private void loadMappingValues() {
		startValue = conf.getInt("startValue");
		endValue = conf.getInt("endValue");

	}


	private  void loadTimeEvents() {

		String[] events = conf.getString("timeevents").split(",");

		// parse all time events
		for (String event : events) {
			String[] eventData = event.split("-");

			//check if right format
			//skip invalid ones
			if (eventData.length != 3) {
				logger.warning( "Event Data not correct. timecode - channel - value");
				continue;
			}
			if ( eventData[0].trim().length() !=  4) {
				logger.warning( "Event Time not correct. format = xxxx");
				continue;
			}




			int channel = Integer.parseInt( eventData[1].trim());
			int value = Integer.parseInt( eventData[2].trim());
			int time = Integer.parseInt(eventData[0].trim());

			if (time < 0) {
				logger.warning( "Time has to be greater 0");
				continue;
			}
			if (channel < 0 || channel > 512) {
				logger.warning( "Channel has to be between 0 and 512");
				continue;
			}
			if (value < 0 || value > 100) {
				logger.warning( "Value has to be between 0 and 100");
				continue;
			}

			TimeEvent timeEvent = new TimeEvent(time,channel,value);


			// if our hashmap already contains an event on that time, put it in the array list
			//else create a new one 
			if (timeEvents.containsKey(time)) {

				timeEvents.get(time).add(timeEvent);

			}else{
				ArrayList<TimeEvent> e = new ArrayList<TimeEvent>();
				e.add(timeEvent);
				timeEvents.put(time, e );
			}
		}

		logger.info("Time events loaded");

	}

}
