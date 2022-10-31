import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class EnergyMeterManager {

	// Consts
	
	public static final int SERIAL_BAUD_RATE = 9600;
	public static final int DEVICE_ADDRESS = 0;
	public static final int TIMEOUT = 2000;
	
	// Variables
	
	public SerialPort serialPort = null;
	public boolean initialized = false;
	public EnergyMeterListener listener;
	public Timer timer;
	
	// Listeners

    public interface EnergyMeterListener {
    	public void readMeterValues(int voltagePhase1, int voltagePhase2, int voltagePhase3, float currentPhase1, float currentPhase2, float currentPhase3, float totalPower, float t1total, float t1partial, float t2total, float t2partial);
    }
    
	// Functions
	
	public EnergyMeterManager(String portName) {
		try {
			CommPortIdentifier portIdentifier = CommPortIdentifier
			        .getPortIdentifier(portName);
			
			if (portIdentifier.isCurrentlyOwned()) {
	            System.out.println("Energy Meter Manager Error: Port is currently in use");
	        } else {
	            try {
					CommPort commPort = portIdentifier.open(this.getClass().getName(), TIMEOUT);
					
					if (commPort instanceof SerialPort) {
						serialPort = (SerialPort)commPort;
						
				        try {
							serialPort.setSerialPortParams(SERIAL_BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_EVEN);
							
						    if (initialized = InitializeDevice()) {
						    	System.out.println("Energy Meter Manager OK");
						    } else {
						    	System.out.println("Energy Meter Manager Failed");
						    }
				        } catch (UnsupportedCommOperationException ex) {
							System.out.println("Energy Meter Manager Serial device exception: " + ex.getMessage());
						}
					} else {
				        System.out.println("Energy Meter Manager Error: Only serial ports are handled by this example." );
					}
				} catch (PortInUseException ex) {
					System.out.println("Energy Meter Manager Serial device exception: " + ex.getMessage());
				}
	        }
		} catch (NoSuchPortException ex) {
			System.out.println("Energy Meter Manager Serial device exception: " + ex.getMessage());
		}
	}
	
	public boolean InitializeDevice() {
		try {
			if (serialPort != null && serialPort.getOutputStream() != null) {
				char[] buffer = new char[5];

				buffer[0] = 0x10;
				buffer[1] = 0x40;
				buffer[2] = (char)DEVICE_ADDRESS;
				buffer[3] = CalculateCRC(buffer, 1, (buffer.length - 3));
				buffer[4] = 0x16;
				
				for (int i = 0; i < buffer.length; i++) {
					serialPort.getOutputStream().write(buffer[i]);
                }
				
				char[] readBuffer = ReadBuffer();
	                
	            if (readBuffer.length > 0 && readBuffer[0] == 0xE5) {
	            	return true;
	            }
			}
		} catch (IOException e) {
		}
		
		return false;
	}
	
	public char[] ReadMeterValues() {
		try {
			if (serialPort != null && serialPort.getOutputStream() != null) {
				char[] buffer = new char[5];

				buffer[0] = 0x10;
				buffer[1] = 0x7B;
				buffer[2] = (char)DEVICE_ADDRESS;
				buffer[3] = CalculateCRC(buffer, 1, (buffer.length - 3));
				buffer[4] = 0x16;
				
				for (int i = 0; i < buffer.length; i++) {
					serialPort.getOutputStream().write(buffer[i]);
                }
				
				char[] readBuffer = ReadBuffer();
	            
				if (CheckCRC(readBuffer, 4, (readBuffer.length - 6)) == true) {
					return readBuffer;
				}
			}
		} catch (IOException e) {
		}
		
		return null;
	}
	
	public boolean ResetTPartCounter(int counter) {
		try {
			if (serialPort != null && serialPort.getOutputStream() != null) {
				char[] buffer = new char[10];

				buffer[0] = 0x68;
				buffer[1] = 0x04;
				buffer[2] = 0x04;
				buffer[3] = 0x68;
				buffer[4] = 0x53;
				buffer[5] = (char)DEVICE_ADDRESS;
				buffer[6] = 0x50;
				buffer[7] = (char)counter;
				buffer[8] = CalculateCRC(buffer, 4, (buffer.length - 6));
				buffer[9] = 0x16;
				
				for (int i = 0; i < buffer.length; i++) {
					serialPort.getOutputStream().write(buffer[i]);
                }
				
				char[] readBuffer = ReadBuffer();
	            
	            if (readBuffer.length > 0 && readBuffer[0] == 0xE5) {
	            	return true;
	            }
			}
		} catch (IOException e) {
		}
		
		return false;
	}
	
	public char CalculateCRC(char[] buffer, int offset, int length) {
		try {
			char crc = buffer[offset];
	        
	        for (int i = 0; i < (length - 1); i++) {
	            crc += buffer[offset + i + 1];
	        }

	        return (char) (crc & 0xFF);
		} catch (Exception ex) {
		}
		
		return 0x00;
    }
	
	public boolean CheckCRC(char[] buffer, int offset, int length) {
		try {
			char calculatedCRC = CalculateCRC(buffer, offset, length);
	        char crc = buffer[buffer.length - 2];
	        
	        if (calculatedCRC == crc) {
	            return true;
	        }
		} catch (Exception ex) {
		}
		
		return false;
    }
	
	public char[] ReadBuffer() {
		char[] buffer = new char[200];
		char readLength = 0;
        
		try {
			char timeout = TIMEOUT;
	        
	        while (timeout > 0 && serialPort.getInputStream().available() == 0) {
	        	timeout--;
	        	try {
					Thread.sleep(0,01);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        }
	        
	        while (serialPort.getInputStream().available() > 0) {
	        	buffer[readLength++] = (char)serialPort.getInputStream().read();
	        }
			
			return Arrays.copyOfRange(buffer, 0, readLength);
		} catch (IOException e) {
		}
		
		return null;
	}
	
	public static char[] hexStringToByteArray(String s) {
	    int len = s.length();
	    char[] data = new char[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (char) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	public void EnableListenerTimer(int ms) {
		this.timer = new Timer();
		this.timer.schedule(new TimerTask() { 
		    public void run() {
		    	char[] data = ReadMeterValues();
	    		
	    		if (data != null) {
	    			int voltagePhase1 = (data[52] + data[53]);
	    			int voltagePhase2 = (data[79] + data[80]);
	    			int voltagePhase3 = (data[106] + data[107]);
	    			
	    			float currentPhase1 = (float) ((float)(data[59] + data[60]) * 0.1);
	    			float currentPhase2 = (float) ((float)(data[86] + data[87]) * 0.1);
	    			float currentPhase3 = (float) ((float)(data[113] + data[114]) * 0.1);

	    			float totalPower = (float) ((float)(data[137] + data[138]) * 0.01);
	    			
	    			float t1total = (float)((float)((Integer.parseInt(Integer.toHexString(data[25])) * 1000000) + (Integer.parseInt(Integer.toHexString(data[24])) * 10000) + (Integer.parseInt(Integer.toHexString(data[23])) * 100) + Integer.parseInt(Integer.toHexString(data[22]))) * 0.01);
	    			float t1partial = (float)((float)((Integer.parseInt(Integer.toHexString(data[32])) * 1000000) + (Integer.parseInt(Integer.toHexString(data[31])) * 10000) + (Integer.parseInt(Integer.toHexString(data[30])) * 100) + Integer.parseInt(Integer.toHexString(data[29]))) * 0.01);
	    			float t2total = (float)((float)((Integer.parseInt(Integer.toHexString(data[39])) * 1000000) + (Integer.parseInt(Integer.toHexString(data[38])) * 10000) + (Integer.parseInt(Integer.toHexString(data[37])) * 100) + Integer.parseInt(Integer.toHexString(data[36]))) * 0.01);
	    			float t2partial = (float)((float)((Integer.parseInt(Integer.toHexString(data[46])) * 1000000) + (Integer.parseInt(Integer.toHexString(data[45])) * 10000) + (Integer.parseInt(Integer.toHexString(data[44])) * 100) + Integer.parseInt(Integer.toHexString(data[43]))) * 0.01);
	    			
	    			if (listener != null) {
	    				listener.readMeterValues(voltagePhase1, voltagePhase2, voltagePhase3, currentPhase1, currentPhase2, currentPhase3, totalPower, t1total, t1partial, t2total, t2partial);
	    			}
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
