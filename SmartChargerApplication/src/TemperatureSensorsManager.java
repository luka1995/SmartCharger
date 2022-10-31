import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class TemperatureSensorsManager {
	
	// Variables
	
	public TemperatureSensorsListener listener;
	public Timer timer;
	
	// Listeners

    public interface TemperatureSensorsListener {
        public void readTemperature(float temperature);
    }
    
	// Functions
	
	public float GetTemperature() {
		try {
			Process proc = Runtime.getRuntime().exec("cat /sys/bus/w1/devices/" + GetSensorId() + "/w1_slave");
			
			BufferedReader stdInput = new BufferedReader(new 
			     InputStreamReader(proc.getInputStream()));
			
			String s = "";
			String output = "";
			while ((s = stdInput.readLine()) != null) {
				output += s;
			}
			
			if (output != null && output.length() > 0) {
				int start = output.indexOf("t=");
				String temperature = output.substring(start+2, output.length());
			
				return (Float.parseFloat(temperature) / 1000);
			}
		} catch (IOException e) {
		}
		
		return 0;
	}
	
	public String GetSensorId() {
		Process proc;
		try {
			proc = Runtime.getRuntime().exec("ls /sys/bus/w1/devices/");
			
			BufferedReader stdInput = new BufferedReader(new 
				     InputStreamReader(proc.getInputStream()));
			
			return stdInput.readLine();
		} catch (IOException e) {
		}

		return null;
	}

	public void EnableListenerTimer(int ms) {
		this.timer = new Timer();
		this.timer.schedule(new TimerTask() { 
		    public void run() {
		    	if (listener != null) {
		    		listener.readTemperature(GetTemperature());
		    	}
		    }
		}, 0, ms);
	}
	
	public void DisableListenerTimer() {
		if (this.timer != null) {
			this.timer.cancel();
			this.timer = null;
		}
	}
}
