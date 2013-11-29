
public class TimeEvent {

	/*
	 * The time when the event .
	 * Format xxxx 
	 */
	private int time; 
	
	/*
	 * The channel to which this event belongs
	 */
	private int channel;
	
	/*
	 * The value send to the channel
	 */
	
	private double timeFound;
	
	private int value;

	public TimeEvent(int time, int channel, int value) {
		this.time = time;
		this.channel = channel;
		this.value = value;
		this.timeFound = 0;
		
		
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getChannel() {
		return channel;
	}

	public void setChannel(int channel) {
		this.channel = channel;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public double getTimeFound() {
		return timeFound;
	}

	public void setTimeFound(double timeFound) {
		this.timeFound = timeFound;
	} 
	
	
}
