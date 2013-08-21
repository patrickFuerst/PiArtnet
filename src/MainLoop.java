
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import Helper.Config;

public class MainLoop extends Thread implements GpioPinListenerDigital {


	// define the log level for this Class
	private static final Level LOGLEVEL = Level.INFO;
	private static Logger logger =  Logger.getLogger( MainLoop.class.getName() );

	//Integer is the timecode xxx
	//The arraylist contains all timeevents  at the same time 
	private  HashMap<Integer, ArrayList<TimeEvent> > timeEvents = new HashMap<Integer, ArrayList<TimeEvent>>(); 

	private  HashMap<Integer, Integer > channelValues = new HashMap<Integer, Integer>(); 
	private  HashMap<Integer, Integer > oldChannelValues = new HashMap<Integer, Integer>(); 

	private  Config conf;

	private ArtnetDevice artnetDevice = new ArtnetDevice(); 


	//map the Time events value to this values.
	private int startValue, endValue;
	
	// checks if the sensor are on or off
	private boolean sensor1On, sensor2On;
	
	// how long the thread sleeps after each run
	private int sleepTime; 

	// the min and max changes if the sensor in on 
	private int minPercentageChange, maxPercentageChange;
	
	// defines which sensor changes which channel
	private int sensor1Channel,sensor2Channel;
	
	// create gpio controller instance
	final GpioController gpio = GpioFactory.getInstance();

    // provision gpio pin #02 as an input pin with its internal pull down resistor enabled
    // (configure pin edge to both rising and falling to get notified for HIGH and LOW state
    // changes)
    GpioPinDigitalInput sensor1 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_07,             // PIN NUMBER
                                                                 "Sensor1",                   // PIN FRIENDLY NAME (optional)
                                                                 PinPullResistance.PULL_DOWN); // PIN RESISTANCE (optional)

    GpioPinDigitalInput sensor2 = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00,             // PIN NUMBER
    															"Sensor2",                   // PIN FRIENDLY NAME (optional)
    															PinPullResistance.PULL_DOWN); // PIN RESISTANCE (optional)

	public MainLoop(Config conf) {
		super();
		this.conf = conf;
		logger.setLevel(LOGLEVEL);
		sensor1.addListener(this);
		sensor2.addListener(this);
		sensor1On = false;
		sensor2On = false;
		sleepTime = 1000;
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();


		loadPropertieFile();
		artnetDevice.start();


		while(true){

			checkTimeEvents();
			checkSensors();
			sendData();
						
			// wait some time to check next
			try {
				MainLoop.sleep(sleepTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}



	}
	

	private void checkSensors() {

		if (sensor1On) {
			
			Integer currentValue = channelValues.get(sensor1Channel);
			//only changes value if there isn't an old value
			// otherwise do nothin, because we don't want to change the values will the sensor is constantly on.
			if (currentValue != null && !oldChannelValues.containsKey(sensor1Channel)) {
				int newValue = getRandomValue(currentValue);
				channelValues.put(sensor1Channel, newValue);
				oldChannelValues.put(sensor1Channel, currentValue);
				
			}
			
		}else{
			Integer oldValue = oldChannelValues.get(sensor1Channel);
			if (oldValue != null) {
				channelValues.put(sensor1Channel, oldValue);	
				oldChannelValues.remove(sensor1Channel);
			}
		}
		
		
		
		if (sensor2On) {
			//only changes value if there isn't an old value
			// otherwise do nothin, because we don't want to change the values will the sensor is constantly on.
			Integer currentValue = channelValues.get(sensor2Channel);
			if (currentValue != null && !oldChannelValues.containsKey(sensor2Channel)) {
				int newValue = getRandomValue(currentValue);
				channelValues.put(sensor2Channel, newValue);
				oldChannelValues.put(sensor2Channel, currentValue);

			}
			
		}else{
			Integer oldValue = oldChannelValues.get(sensor2Channel);
			if (oldValue != null) {
				channelValues.put(sensor2Channel, oldValue);
				oldChannelValues.remove(sensor2Channel);
			}
		}
		
		
	}


	private int getRandomValue(Integer currentValue) {
		
		int percentage = minPercentageChange + (int)(Math.random() * ((maxPercentageChange - minPercentageChange) + 1));		
		
		if (Math.random() < 0.5) {
			return currentValue - currentValue * percentage/100;
		}else
			return currentValue + currentValue * percentage/100;

		
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
				int value = event.getValue();
				
				//map to dmx 0-255
				value = startValue + (endValue - startValue) * value/100;
				
				// old values get replaced 
				channelValues.put(channel, value);
			}



		}


	}

	private void loadPropertieFile(){


		loadTimeEvents();
		loadMappingValues();
		loadSleepTime();
		loadSensorProperties();
		loadInitValues();
		logger.info("Properties loaded");

		
	}

	private void loadInitValues() {

		int initValue  = conf.getInt("initValue");
		channelValues.put(0, initValue);
		channelValues.put(1, initValue);
		
	}


	private void loadSensorProperties() {
		int min  = conf.getInt("minPercentageChange");
		int max = conf.getInt("maxPercentageChange");	
		
		if( minPercentageChange < 0 || minPercentageChange > 100 
				|| maxPercentageChange < 0 || maxPercentageChange > 100){
			logger.warning(" Percentage values have to be between 0 and 100");
		}else{
		
			minPercentageChange = min;
			maxPercentageChange = max;
		}
		int s1c  = conf.getInt("sensor1Channel");
		int s2c = conf.getInt("sensor2Channel");	
		
		if( s1c < 0 || s1c > 512 
				|| s2c < 0 || s2c > 512){
			logger.warning(" Sensor to channel values have to be between 0 and 512");
		}else{
		
			sensor1Channel = s1c;
			sensor2Channel = s2c;
			
		}
		
		
	}


	private void loadSleepTime() {

		sleepTime = conf.getInt("sleepTime");
	}


	private void loadMappingValues() {
		startValue = conf.getInt("startValue");
		endValue = conf.getInt("endValue");

	}


	private  void loadTimeEvents() {

		String[] events = conf.getString("timeEvents").split(",");

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


	}


	@Override
	public void handleGpioPinDigitalStateChangeEvent( GpioPinDigitalStateChangeEvent event) {

		logger.info("Input Event from " + event.getPin().toString() + ". State = " +  event.getState());
		if (event.getPin().getName() == "Sensor1") {
			sensor1On = event.getState().isHigh();
		}else if (event.getPin().getName() == "Sensor2") {
			sensor2On = event.getState().isHigh();

		}
		
	    
		
	}

}
