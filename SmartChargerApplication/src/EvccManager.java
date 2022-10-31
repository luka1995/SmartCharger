import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class EvccManager {

	// Consts

	public static final int SERIAL_BAUD_RATE = 38400;
	public static final int DEVICE_ADDRESS = 0;
	public static final int TIMEOUT = 1000;
	
	public enum EvccStateMachineState {
		A1,
		A2,
		B1,
		B2,
		C1,
		C2,
		D1,
		D2,
		E,
		F
	};
	
	public class EvccCurrent {
		public static final int PWM_DUTY_6A = 100;
		public static final int PWM_DUTY_10A = 166;
		public static final int PWM_DUTY_13A = 216;
		public static final int PWM_DUTY_16A = 266;
		public static final int PWM_DUTY_20A = 333;
		public static final int PWM_DUTY_32A = 533;
		public static final int PWM_DUTY_40A = 660;
		public static final int PWM_DUTY_50A = 840;
		public static final int PWM_DUTY_60A = 880;
		public static final int PWM_DUTY_70A = 920;
		public static final int PWM_DUTY_80A = 960;
	}

	public enum EvccModuleMode {
		Auto,
		Manual
	};
	
	// Variables
	
	public SerialPort serialPort = null;
	public boolean initialized = false;
	public EvccListener listener;
	public Timer timer;
	public EvccModuleMode selectedModuleMode = EvccModuleMode.Auto;
	public EvccStateMachineState currentStateMachineState = EvccStateMachineState.A1;
	public boolean breakStartCharging = false;
	public boolean waitingResponse = false;
	
	// Listeners

    public interface EvccListener {
    	public void carConnnect();
    	public void carDisconnect();
    	public void startCharging(boolean ventilation);
    	public void stopCharging(boolean ventilation);
    	public void requestCharging();
    	public void error();
		public void requestChargingSuccessful();
    }
    
	// Functions
	
	public EvccManager(String portName) {
		try {
			CommPortIdentifier portIdentifier = CommPortIdentifier
			        .getPortIdentifier(portName);
			
			if (portIdentifier.isCurrentlyOwned()) {
	            System.out.println("EVCC Manager Error: Port is currently in use");
	        } else {
	            try {
					CommPort commPort = portIdentifier.open(this.getClass().getName(), TIMEOUT);
					
					if (commPort instanceof SerialPort) {
						serialPort = (SerialPort)commPort;
						
				        try {
							serialPort.setSerialPortParams(SERIAL_BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
							
							String firmwareVersion = ReadFirmwareVersion();
							
						    if (firmwareVersion != null) {
						    	initialized = true;
						    	
						    	System.out.println("EVCC Manager OK");
						    	
						    	System.out.println("EVCC ABL Firmware Version: " + firmwareVersion);
						    } else {
						    	System.out.println("EVCC Manager Failed");
						    }
				        } catch (UnsupportedCommOperationException ex) {
							System.out.println("EVCC Manager Serial device exception: " + ex.getMessage());
						}
					} else {
				        System.out.println("EVCC Manager Error: Only serial ports are handled by this example." );
					}
				} catch (PortInUseException ex) {
					System.out.println("EVCC Manager Serial device exception: " + ex.getMessage());
				}
	        }
		} catch (NoSuchPortException ex) {
			System.out.println("EVCC Manager Serial device exception: " + ex.getMessage());
		}
	}
	
	public String ReadFirmwareVersion() {
		try {
			if (serialPort != null && serialPort.getOutputStream() != null) {
				byte[] buffer = String.format("!%d 02\r\n", DEVICE_ADDRESS).getBytes();

				for (int i = 0; i < buffer.length; i++) {
					serialPort.getOutputStream().write(buffer[i]);
                }

				waitingResponse = true;
                
				String responseString = ReadString();
                
				waitingResponse = false;
				
				String responseValue = ParseResponseValue(responseString);
                
                return responseValue;
			}
		} catch (IOException e) {
		}
		
		return null;
	}
	
	public void ResetDevice() {
		try {
			if (serialPort != null && serialPort.getOutputStream() != null) {
				byte[] buffer = String.format("!%d 00\r\n", DEVICE_ADDRESS).getBytes();

				for (int i = 0; i < buffer.length; i++) {
					serialPort.getOutputStream().write(buffer[i]);
                }
			}
		} catch (IOException e) {
		}
	}

	public EvccModuleMode GetModuleMode() {
		try {
			if (serialPort != null && serialPort.getOutputStream() != null) {
				byte[] buffer = String.format("!%d 06\r\n", DEVICE_ADDRESS).getBytes();

				for (int i = 0; i < buffer.length; i++) {
					serialPort.getOutputStream().write(buffer[i]);
                }
				
				waitingResponse = true;
				
				String responseString = ReadString();
                
				waitingResponse = false;
				
				String responseValue = ParseResponseValue(responseString);
				
				EvccModuleMode moduleMode = EvccModuleMode.values()[Integer.parseInt(responseValue)];
				
				this.selectedModuleMode = moduleMode;
				
                return this.selectedModuleMode;
			}
		} catch (IOException e) {
		}
		
		return this.selectedModuleMode;
	}
	
	public EvccStateMachineState ReadStateMachineState() {
		try {
			if (serialPort != null && serialPort.getOutputStream() != null) {
				byte[] buffer = String.format("!%d 05\r\n", DEVICE_ADDRESS).getBytes();

				for (int i = 0; i < buffer.length; i++) {
					serialPort.getOutputStream().write(buffer[i]);
                }
				
				waitingResponse = true;
				
				String responseString = ReadString();
                
				waitingResponse = false;
				
				String responseValue = ParseResponseValue(responseString);
				
                return EvccStateMachineState.values()[Integer.parseInt(responseValue)];
			}
		} catch (IOException e) {
		}
		
		return EvccStateMachineState.A1;
	}
	
	public boolean SetMaxCurrent(int value) {
		try {
			if (serialPort != null && serialPort.getOutputStream() != null) {
				byte[] buffer = String.format("!%d 18 %d\r\n", DEVICE_ADDRESS, value).getBytes();
				
                for (int i = 0; i < buffer.length; i++) {
                	serialPort.getOutputStream().write(buffer[i]);
                }
				
				waitingResponse = true;
				                
				byte[] readBuffer = ReadBuffer();

				waitingResponse = false;
				
                return Arrays.equals(readBuffer, String.format(">%d 18\r\n", DEVICE_ADDRESS, value).getBytes());
			}
		} catch (IOException e) {
		}
		
		return false;
	}
	
	public int GetCurrentDutyCycle() {
		try {
			if (serialPort != null && serialPort.getOutputStream() != null) {
				byte[] buffer = String.format("!%d 17\r\n", DEVICE_ADDRESS).getBytes();
				
                for (int i = 0; i < buffer.length; i++) {
                	serialPort.getOutputStream().write(buffer[i]);
                }
                
                waitingResponse = true;
                
                String responseString = ReadString();

				waitingResponse = false;
				
				String responseValue = ParseResponseValue(responseString);
				
                return Integer.parseInt(responseValue);
			}
		} catch (IOException e) {
		}
		
		return 8;
	}
	
	public boolean StartCharging(boolean breakStartCharging) {
		try {
			if (serialPort != null && serialPort.getOutputStream() != null) {
				byte[] buffer = String.format("!%d 24\r\n", DEVICE_ADDRESS).getBytes();
				
                for (int i = 0; i < buffer.length; i++) {
                	serialPort.getOutputStream().write(buffer[i]);
                }

                waitingResponse = true;
                
				byte[] readBuffer = ReadBuffer();
                
				waitingResponse = false;
				
                if (Arrays.equals(readBuffer, String.format(">%d 24\r\n", DEVICE_ADDRESS).getBytes()) == true) {
                	this.breakStartCharging = breakStartCharging;
                	
                	return true;
                }
			}
		} catch (IOException e) {
		}
		
		return false;
	}
	
	public boolean SwitchModeToManual() {
		try {
			if (serialPort != null && serialPort.getOutputStream() != null) {
				byte[] buffer = String.format("!%d 07\r\n", DEVICE_ADDRESS).getBytes();
				
                for (int i = 0; i < buffer.length; i++) {
                	serialPort.getOutputStream().write(buffer[i]);
                }

                waitingResponse = true;
                
				byte[] readBuffer = ReadBuffer();
                
				waitingResponse = false;
				
                if (Arrays.equals(readBuffer, String.format(">%d 07\r\n", DEVICE_ADDRESS).getBytes()) == true) {
                	this.selectedModuleMode = EvccModuleMode.Manual;
                	
                	return true;
                }
			}
		} catch (IOException e) {
		}
		
		return false;
	}
	
	public boolean SwitchModeToAuto() {
		try {
			if (serialPort != null && serialPort.getOutputStream() != null) {
				byte[] buffer = String.format("!%d 08\r\n", DEVICE_ADDRESS).getBytes();
				
                for (int i = 0; i < buffer.length; i++) {
                	serialPort.getOutputStream().write(buffer[i]);
                }

                waitingResponse = true;
                
				byte[] readBuffer = ReadBuffer();
                
				waitingResponse = false;
				
                if (Arrays.equals(readBuffer, String.format(">%d 08\r\n", DEVICE_ADDRESS).getBytes()) == true) {
                	this.selectedModuleMode = EvccModuleMode.Auto;
                	
                	return true;
                }
			}
		} catch (IOException e) {
		}
		
		return false;
	}
	
	public boolean StopCharging(boolean breakStartCharging) {
		try {
			if (serialPort != null && serialPort.getOutputStream() != null) {
				byte[] buffer = String.format("!%d 25\r\n", DEVICE_ADDRESS).getBytes();
				
                for (int i = 0; i < buffer.length; i++) {
                	serialPort.getOutputStream().write(buffer[i]);
                }

                waitingResponse = true;
                
				byte[] readBuffer = ReadBuffer();
                
				waitingResponse = false;
				
                if (Arrays.equals(readBuffer, String.format(">%d 25\r\n", DEVICE_ADDRESS).getBytes()) == true) {
                	this.breakStartCharging = breakStartCharging;
                	
                	return true;
                }
			}
		} catch (IOException e) {
		}
		
		return false;
	}
	
	public float GetProximityVoltage() {
		try {
			if (serialPort != null && serialPort.getOutputStream() != null) {
				byte[] buffer = String.format("!%d 16\r\n", DEVICE_ADDRESS).getBytes();
				
                for (int i = 0; i < buffer.length; i++) {
                	serialPort.getOutputStream().write(buffer[i]);
                }
                
                waitingResponse = true;
                
                String responseString = ReadString();

				waitingResponse = false;
				
				String responseValue = ParseResponseValue(responseString);
				
                return Float.parseFloat(responseValue);
			}
		} catch (IOException e) {
		}
		
		return 3.3f;
	}
	
	public int GetCableDetection() {
		float proximityVoltage = GetProximityVoltage();
		float tolerance = 0.2f;

		if (proximityVoltage >= (2.57 - tolerance)) {
			return 1;
		} else if (proximityVoltage <= (2.57 + tolerance) && proximityVoltage >= (2.57 - tolerance)) {
			return 2;
		} else if (proximityVoltage <= (2.22 + tolerance) && proximityVoltage >= (2.22 - tolerance)) {
			return 3;
		} else if (proximityVoltage <= (1.82 + tolerance) && proximityVoltage >= (1.82 - tolerance)) {
			return 4;
		} else if (proximityVoltage <= (1.66 + tolerance) && proximityVoltage >= (1.66 - tolerance)) {
			return 5;
		} else {
			return 0;
		}
	}
	
	public String ReadString() {
		byte[] buffer = ReadBuffer();

        String value = new String(buffer);
        
        return value;
	}

	public byte[] ReadBuffer() {
		byte[] buffer = new byte[200];
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
	        	buffer[readLength++] = (byte)serialPort.getInputStream().read();
	        }
	        
	        return Arrays.copyOfRange(buffer, 0, readLength);
		} catch (IOException e) {
		}
		
        return null;
	}
	
	public String ParseResponseValue(String text) {
		try {
			String value = text.substring(text.lastIndexOf(" ") + 1, text.lastIndexOf("\r\n"));
            
            return value;
		} catch (Exception ex) {
			
		}
		
		return null;
	}
	
	public void EnableListenerTimer(int ms) {
		this.timer = new Timer();
		this.timer.schedule(new TimerTask() { 
		    public void run() {
		    	if (waitingResponse == false) {
			    	EvccStateMachineState state = ReadStateMachineState();
			    	
			    	if (state != currentStateMachineState) {
			    		switch (state) {
							case A1: {
								if (currentStateMachineState == EvccStateMachineState.C2) {
									if (listener != null) {
										listener.stopCharging(false);
									}	
								} else if (currentStateMachineState == EvccStateMachineState.D2) {
									if (listener != null) {
										listener.stopCharging(true);
									}	
								}
								
								if (listener != null) {
									listener.carDisconnect();
								}
								break;
							}
							case A2: {
								if (currentStateMachineState == EvccStateMachineState.C2) {
									if (listener != null) {
										listener.stopCharging(false);
									}	
								} else if (currentStateMachineState == EvccStateMachineState.D2) {
									if (listener != null) {
										listener.stopCharging(true);
									}	
								}
								
								if (listener != null) {
									listener.carDisconnect();
								}
								break;
							}
							case B1: {
								if (currentStateMachineState == EvccStateMachineState.C2) {
									if (listener != null) {
										listener.stopCharging(false);
									}
								} else if (currentStateMachineState == EvccStateMachineState.D2) {
									if (listener != null) {
										listener.stopCharging(true);
									}
								} else {
									if (listener != null) {
										listener.carConnnect();
									}
									
									if (selectedModuleMode == EvccModuleMode.Manual) {
										if (listener != null) {
											listener.requestCharging();
										}
									}
								}
								break;
							}
							case B2: {
								if (selectedModuleMode == EvccModuleMode.Manual) {
									if (listener != null) {
										listener.requestChargingSuccessful();
									}
								}
								break;
							}
							case C1: {
								if (currentStateMachineState == EvccStateMachineState.C2 || currentStateMachineState == EvccStateMachineState.D2) {
									if (listener != null) {
										listener.stopCharging(false);
									}
								}
								break;
							}
							case C2: {
								if (selectedModuleMode == EvccModuleMode.Auto && (currentStateMachineState == EvccStateMachineState.A1 || currentStateMachineState == EvccStateMachineState.A2)) {
									if (listener != null) {
										listener.carConnnect();
									}
									
									if (listener != null) {
										listener.startCharging(false);
									}
								} else if (currentStateMachineState == EvccStateMachineState.B1 || currentStateMachineState == EvccStateMachineState.B2) {
									if (selectedModuleMode == EvccModuleMode.Manual) {
										if (breakStartCharging == false) {
											if (listener != null) {
												listener.startCharging(false);
											}
										}
									} else {
										if (listener != null) {
											listener.startCharging(false);
										}
									}
								} else if (currentStateMachineState == EvccStateMachineState.D2) {
									if (listener != null) {
										listener.stopCharging(true);
									}
									
									if (listener != null) {
										listener.startCharging(false);
									}
								}
								break;
							}
							case D1: {
								if (currentStateMachineState == EvccStateMachineState.C2 || currentStateMachineState == EvccStateMachineState.D2) {
									if (listener != null) {
										listener.stopCharging(true);
									}
								}
								break;
							}
							case D2: {
								if (selectedModuleMode == EvccModuleMode.Auto && (currentStateMachineState == EvccStateMachineState.A1 || currentStateMachineState == EvccStateMachineState.A2)) {
									if (listener != null) {
										listener.carConnnect();
									}
									
									if (listener != null) {
										listener.startCharging(true);
									}
								} else if (currentStateMachineState == EvccStateMachineState.B1 || currentStateMachineState == EvccStateMachineState.B2) {
									if (selectedModuleMode == EvccModuleMode.Manual) {
										if (breakStartCharging == false) {
											if (listener != null) {
												listener.startCharging(true);
											}
										}
									} else {
										if (listener != null) {
											listener.startCharging(true);
										}
									}
								} else if (currentStateMachineState == EvccStateMachineState.C1 || currentStateMachineState == EvccStateMachineState.C2) {
									if (listener != null) {
										listener.startCharging(true);
									}
								}
								break;
							}
							case E: {
								if (listener != null) {
									listener.error();
								}
								break;
							}
							case F: {
								if (listener != null) {
									listener.error();
								}
								break;
							}
				    	}
			    		
			    		currentStateMachineState = state;
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
