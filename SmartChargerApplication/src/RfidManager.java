import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPortException;

public class RfidManager {

	// Consts

	public static final int SERIAL_BAUD_RATE = 115200;
	public static final int TIMEOUT = 2000;
	
	// Variables
	
	public Serial serialDevice = null;
	public boolean initialized = false;
	public RfidListener listener;
	public Timer timer;
	
	// Listeners

    public interface RfidListener {
        public void requestCard(short tagType, char[] serialNumber);
        public void cardRemoved();
    }
    
	// Functions
	
	public RfidManager(String portName) {
		// create an instance of the serial communications class
		serialDevice = SerialFactory.createInstance();
		
		// read timeout
		serialDevice.setMonitorInterval(100);
		
		try {
            // open serial port
            serialDevice.open(portName, SERIAL_BAUD_RATE);
            
            System.out.println("RFID Manager OK");
            
            InitializePort();
            
            SetAntenna(true);
            
            initialized = true;
            
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        } catch (SerialPortException ex) {
            System.out.println("RFID Manager Serial device exception: " + ex.getMessage());
        }
	}

	public void InitializePort() {
		if (serialDevice != null && serialDevice.isOpen()) {
			try {
				char[] buffer = new char[10];

                buffer[0] = 0xAA;
                buffer[1] = 0xBB;
                buffer[2] = (char) (buffer.length - 4);
                buffer[3] = 0x00;
                buffer[4] = 0x00;
                buffer[5] = 0x00;
                buffer[6] = 0x01;
                buffer[7] = 0x01;
                buffer[8] = 0x07;   // 115200 baudrate
                buffer[9] = CalculateCRC(buffer, 6, (buffer.length - 7));

                for (int i = 0; i < buffer.length; i++) {
                	serialDevice.write(buffer[i]);
                }
			} catch (Exception ex) {
			}
		}
	}
	
	public void SetAntenna(boolean state) {
		if (serialDevice != null && serialDevice.isOpen()) {
			try {
				char[] buffer = new char[10];

				buffer[0] = 0xAA;
                buffer[1] = 0xBB;
                buffer[2] = (char)(buffer.length - 4);
                buffer[3] = 0x00;
                buffer[4] = 0x00;
                buffer[5] = 0x00;
                buffer[6] = 0x0C;
                buffer[7] = 0x01;
                buffer[8] = (char) (state ? 0x01 : 0x00);
                buffer[9] = CalculateCRC(buffer, 6, (buffer.length - 7));

                for (int i = 0; i < buffer.length; i++) {
                	serialDevice.write(buffer[i]);
                }
			} catch (Exception ex) {
				
			}
		}
	}

	public void SetBuzzerBeep(int delay) {
		if (serialDevice != null && serialDevice.isOpen()) {
			try {
				char[] buffer = new char[10];

				buffer[0] = 0xAA;
                buffer[1] = 0xBB;
                buffer[2] = (char)(buffer.length - 4);
                buffer[3] = 0x00;
                buffer[4] = 0x00;
                buffer[5] = 0x00;
                buffer[6] = 0x06;
                buffer[7] = 0x01;
                buffer[8] = (char)delay;
                buffer[9] = CalculateCRC(buffer, 6, (buffer.length - 7));

                for (int i = 0; i < buffer.length; i++) {
                	serialDevice.write(buffer[i]);
                }
			} catch (Exception ex) {
				
			}
		}
	}
	
	public short MifareRequest() {
		if (serialDevice != null && serialDevice.isOpen()) {
			try {
				char[] buffer = new char[10];

                buffer[0] = 0xAA;
                buffer[1] = 0xBB;
                buffer[2] = (char) (buffer.length - 4);
                buffer[3] = 0x00;
                buffer[4] = 0x00;
                buffer[5] = 0x00;
                buffer[6] = 0x01;
                buffer[7] = 0x02;
                buffer[8] = 0x52;
                buffer[9] = CalculateCRC(buffer, 6, (buffer.length - 7));

                for (int i = 0; i < buffer.length; i++) {
                	serialDevice.write(buffer[i]);
                }
                
                char[] readBuffer = ReadBuffer();
                
                if (CheckCRC(readBuffer) == true) {
                	return (short)((readBuffer[9] << 8) + readBuffer[10]);
                }
			} catch (Exception ex) {
				
			}
		}
		
		return 0;
	}
	
	public char[] MifareAnticollision() {
		if (serialDevice != null && serialDevice.isOpen()) {
			try {
				char[] buffer = new char[9];

                buffer[0] = 0xAA;
                buffer[1] = 0xBB;
                buffer[2] = (char) (buffer.length - 4);
                buffer[3] = 0x00;
                buffer[4] = 0x00;
                buffer[5] = 0x00;
                buffer[6] = 0x02;
                buffer[7] = 0x02;
                buffer[8] = CalculateCRC(buffer, 6, (buffer.length - 7));

                for (int i = 0; i < buffer.length; i++) {
                	serialDevice.write(buffer[i]);
                }
                
                char[] readBuffer = ReadBuffer();
                
                if (CheckCRC(readBuffer) == true) {
                	if (readBuffer.length > 10) {
                		char[] serialNumber = new char[(readBuffer.length - 10)];

                        for (int i = 0; i < (readBuffer.length - 10); i++)
                        {
                            serialNumber[i] = readBuffer[9 + i];
                        }

                        return serialNumber;
                	}
                }
			} catch (Exception ex) {
				
			}
		}
		
		return null;
	}
	
	public char[] ReadBuffer() {
		char[] buffer = new char[50];
		char readLength = 0;
		
		serialDevice.flush();
        
        char timeout = 200;
        
        while (timeout > 0 && serialDevice.availableBytes() == 0) {
        	timeout--;
        	try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        
        while (serialDevice.availableBytes() > 0) {
        	buffer[readLength++] = serialDevice.read();
        }
		
		return Arrays.copyOfRange(buffer, 0, readLength);
	}
	
	public char CalculateCRC(char[] buffer, int offset, int length) {
        char crc = buffer[offset];

        for (int i = 0; i < (length - 1); i++) {
            crc ^= buffer[offset + i + 1];
        }

        return crc;
    }

	public boolean CheckCRC(char[] buffer) {
        char calculatedCRC = CalculateCRC(buffer, 6, (buffer.length - 7));
        char crc = buffer[buffer.length - 1];

        if (calculatedCRC == crc) {
            return true;
        } else {
            return false;
        }
    }
	
	public static String ByteArrayToString(char[] buffer) {
        String string = "";
        
        try {
        	for (int i = 0; i < buffer.length; i++) {
        		string += Integer.toHexString(buffer[i]);
        	}
        } catch (Exception ex) {
        }
        
		return string;
    }

	public static String TagTypeString(short tagType) {
		switch (tagType) {
			case 0x4400: return "Ultra Light";
			case 0x0400: return "Mifare One (S50)";
			case 0x0200: return "Mifare One (S70)";
			case 0x4403: return "Mifare DESFire";
			case 0x0800: return "Mifare Pro";
			case 0x0403: return "Mifare Pro X";
		}
		
		return null;
	}
	
	private short savedTagType;
	private char[] savedSerialNumber;
	
	public void EnableListenerTimer(int ms) {
		this.timer = new Timer();
		this.timer.schedule(new TimerTask() { 
		    public void run() {
		    	short tagType = MifareRequest();
	    		
	    		if (tagType != 0) {
	    			char[] serialNumber = MifareAnticollision();
	    			
                    if (serialNumber.length > 0) {
                    	if (savedTagType != tagType && Arrays.equals(savedSerialNumber, serialNumber) == false) {
                    		savedTagType = tagType;
                        	savedSerialNumber = serialNumber;
                        	
                        	if (listener != null) {
                        		listener.requestCard(tagType, serialNumber);
                        	}
                    	}
                    }
	    		} else {
	    			if (savedTagType != 0 && savedSerialNumber != null) {
	    				savedTagType = 0;
	    				savedSerialNumber = null;
	    				
	    				if (listener != null) {
                    		listener.cardRemoved();
                    	}
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
