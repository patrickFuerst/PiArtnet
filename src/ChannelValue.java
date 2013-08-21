
public class ChannelValue {

	private int currentValue;
	
	private int oldValue; 
	
	private double timeValueChangedBySensor;
	
	private boolean sensorMode;
	
	public ChannelValue(int currentValue){
		this.currentValue = currentValue;
		oldValue = 0;
		timeValueChangedBySensor = System.currentTimeMillis();
		sensorMode = false;
	}
	
	public boolean isSensorMode() {
		return sensorMode;
	}

	public void setSensorMode(boolean sensorMode) {
		this.sensorMode = sensorMode;
	}

	public int getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(int currentValue) {
		this.currentValue = currentValue;
	}

	public int getOldValue() {
		return oldValue;
	}

	public void setOldValue(int oldValue) {
		this.oldValue = oldValue;
	}

	public double getTimeValueChangedBySensor() {
		return timeValueChangedBySensor;
	}

	public void setTimeValueChangedBySensor(double timeValueChanged) {
		this.timeValueChangedBySensor = timeValueChanged;
	}
	
	public double getValueDuration(){
		return System.currentTimeMillis() - timeValueChangedBySensor;
	}
}
