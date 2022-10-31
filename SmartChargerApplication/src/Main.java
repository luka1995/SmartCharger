import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
	
	// Consts
	
	public static final String ENERGY_METER_SERIAL_PORT_NAME = "/dev/energymeter";
	public static final String EVCC_SERIAL_PORT_NAME = "/dev/evcc";
	public static final String RFID_SERIAL_PORT_NAME = "/dev/rfid";
	
	// Variables
	
	public static RfidManager rfidManager;
	public static StatusLightManager statusLightManager;
	public static OcppManager ocppManager;
	public static EvccManager evccManager;
	public static EnergyMeterManager energyMeterManager;
	public static TemperatureSensorsManager temperatureSensorsManager;
	
	public static HomeView homeView;
	
	private static Timer timerLedFlashing;
	
	private static boolean requestChargingOk = false;
	
	// Functions
	
	public static void main(String[] args) {
		// Init Home View
		
		homeView = new HomeView();		
		
		// Init Status Light Manager
		
		statusLightManager = new StatusLightManager();
		statusLightManager.setGreen();
			
		// Init Temperature Sensors Manager
		
		temperatureSensorsManager = new TemperatureSensorsManager();
		temperatureSensorsManager.listener = new TemperatureSensorsManager.TemperatureSensorsListener() {
			@Override
			public void readTemperature(float temperature) {
				if (homeView != null) {
					homeView.labelTemperature.setText("Temperature: " + String.format("%.2f", temperature) + "°C");
				}
			}
		};
		temperatureSensorsManager.EnableListenerTimer(500); // 500 ms
		
		// Init RFID Manager
		
		rfidManager = new RfidManager(GetSerialPortName(RFID_SERIAL_PORT_NAME));
		if (rfidManager != null && rfidManager.initialized == true && rfidManager.serialDevice.isOpen() == true) {
			rfidManager.listener = new RfidManager.RfidListener() {
				@Override
				public void requestCard(short tagType, char[] serialNumber) {
					if (requestChargingOk == false) {
						if (evccManager != null && evccManager.initialized == true) {
							if (evccManager.selectedModuleMode == EvccManager.EvccModuleMode.Manual) {
								if (evccManager.currentStateMachineState == EvccManager.EvccStateMachineState.B1) {
									evccManager.StartCharging(false);
									
									if (timerLedFlashing != null) {
										timerLedFlashing.cancel();
										timerLedFlashing = null;
									}
									
									if (statusLightManager != null) {
										statusLightManager.setBlue();
									}
								}
							}
						}
					}
				}
				
				@Override
				public void cardRemoved() {
					
				}
			};
			
			rfidManager.EnableListenerTimer(500); // 500 ms
		}
		
		// Init EVCC Manager
		
		evccManager = new EvccManager(GetSerialPortName(EVCC_SERIAL_PORT_NAME));
		if (evccManager != null && evccManager.initialized == true) {
			evccManager.listener = new EvccManager.EvccListener() {
				@Override
				public void carConnnect() {
					System.out.println("EVCC - CarConnnect");
					
					requestChargingOk = false;
					
					if (timerLedFlashing != null) {
						timerLedFlashing.cancel();
						timerLedFlashing = null;
					}
					
					if (evccManager != null && evccManager.initialized == true) {
						if (evccManager.selectedModuleMode == EvccManager.EvccModuleMode.Auto) {
							if (statusLightManager != null) {
								statusLightManager.setBlue();
							}
						}
					}
					
					if (energyMeterManager != null && energyMeterManager.initialized == true) {
						energyMeterManager.ResetTPartCounter(1);
						energyMeterManager.ResetTPartCounter(2);
					}
					
					updateCableText();
				}

				@Override
				public void carDisconnect() {
					System.out.println("EVCC - CarDisconnect");
					
					requestChargingOk = false;
					
					if (timerLedFlashing != null) {
						timerLedFlashing.cancel();
						timerLedFlashing = null;
					}
					
					if (statusLightManager != null) {
						statusLightManager.setGreen();
					}
					
					updateCableText();
				}

				@Override
				public void startCharging(boolean ventilation) {
					System.out.println("EVCC - StartCharging" + (ventilation == true ? " with ventilation" : ""));
					
					requestChargingOk = true;
					
					if (timerLedFlashing != null) {
						timerLedFlashing.cancel();
						timerLedFlashing = null;
					}
					
					timerLedFlashing = new Timer();
					timerLedFlashing.schedule(new TimerTask() { 
					    public void run() {
					    	if (statusLightManager != null) {
						    	if (ventilation) {
							    	if (statusLightManager.lightState == StatusLightManager.LightState.BLUEPURPLE) {
					    				if (statusLightManager.lightState != StatusLightManager.LightState.OFF) statusLightManager.turnOff();
							    	} else {
							    		if (statusLightManager.lightState != StatusLightManager.LightState.BLUEPURPLE) statusLightManager.setBluePurple();
							    	}
						    	} else {
						    		if (statusLightManager.lightState == StatusLightManager.LightState.BLUE) {
					    				if (statusLightManager.lightState != StatusLightManager.LightState.OFF) statusLightManager.turnOff();
							    	} else {
							    		if (statusLightManager.lightState != StatusLightManager.LightState.BLUE) statusLightManager.setBlue();
							    	}
						    	}
					    	}
					    }
					}, 0, 500);
					
					if (energyMeterManager != null && energyMeterManager.initialized == true) {
						energyMeterManager.ResetTPartCounter(2);
					}
				}

				@Override
				public void stopCharging(boolean ventilation) {
					System.out.println("EVCC - StopCharging" + (ventilation == true ? " with ventilation" : ""));
					
					if (timerLedFlashing != null) {
						timerLedFlashing.cancel();
						timerLedFlashing = null;
					}
					
					if (statusLightManager != null) {
						statusLightManager.setOrange();
					}
					
					if (evccManager.breakStartCharging == false) {
						evccManager.StartCharging(false);
					}
				}

				@Override
				public void requestCharging() {
					System.out.println("EVCC - RequestCharging");
					
					if (timerLedFlashing != null) {
						timerLedFlashing.cancel();
						timerLedFlashing = null;
					}
					
					timerLedFlashing = new Timer();
					timerLedFlashing.schedule(new TimerTask() { 
					    public void run() {
					    	if (statusLightManager != null) {
						    	if (statusLightManager.lightState == StatusLightManager.LightState.GREEN) {
				    				if (statusLightManager.lightState != StatusLightManager.LightState.OFF) statusLightManager.turnOff();
						    	} else {
						    		if (statusLightManager.lightState != StatusLightManager.LightState.GREEN) statusLightManager.setGreen();
						    	}
					    	}
					    }
					}, 0, 500);
				}
				
				@Override
				public void error() {
					System.out.println("EVCC - Error");
					
					if (timerLedFlashing != null) {
						timerLedFlashing.cancel();
						timerLedFlashing = null;
					}
					
					if (statusLightManager != null) {
						statusLightManager.setRed();
					}
				}

				@Override
				public void requestChargingSuccessful() {
					System.out.println("EVCC - RequestChargingSuccessful");
					
				}
			};

	    	if (evccManager.SetMaxCurrent(EvccManager.EvccCurrent.PWM_DUTY_32A) == true) {
	    		System.out.println("EVCC Manager Max Current OK");
	    	}
	    	
	    	evccManager.GetModuleMode();
	    	
	    	if (evccManager.SwitchModeToManual() == true) {
	    		System.out.println("EVCC Manager Switch Mode To Manual OK");
	    	}
	    	
	    	updateCableText();
	    	
	    	evccManager.EnableListenerTimer(500); // 500 ms
		}
	
		homeView.buttonChangeEvccModuleMode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (evccManager != null && evccManager.initialized == true) {
					if (evccManager.currentStateMachineState == EvccManager.EvccStateMachineState.A1 || evccManager.currentStateMachineState == EvccManager.EvccStateMachineState.A2) {					
						if (evccManager.selectedModuleMode == EvccManager.EvccModuleMode.Auto) {
							if (evccManager.SwitchModeToManual() == true) {
					    		System.out.println("EVCC Manager Switch Mode To Manual OK");
					    		
					    		if (homeView != null) {
					    			homeView.buttonChangeEvccModuleMode.setText("Manual Mode");
					    		}
					    	}
						} else {
							if (evccManager.SwitchModeToAuto() == true) {
					    		System.out.println("EVCC Manager Switch Mode To Auto OK");
					    		
					    		if (homeView != null) {
					    			homeView.buttonChangeEvccModuleMode.setText("Auto Mode");
					    		}
					    	}
						}
					}
				}
			}
		});
		
		// Init Energy Meter Manager
		
		energyMeterManager = new EnergyMeterManager(GetSerialPortName(ENERGY_METER_SERIAL_PORT_NAME));
		if (energyMeterManager != null && energyMeterManager.initialized == true) {
			energyMeterManager.listener = new EnergyMeterManager.EnergyMeterListener() {
				@Override
				public void readMeterValues(int voltagePhase1, int voltagePhase2, int voltagePhase3,
						float currentPhase1, float currentPhase2, float currentPhase3, float totalPower, float t1total,
						float t1partial, float t2total, float t2partial) {
					if (homeView != null) {
						homeView.labelEnergyMeterVoltage.setText("Voltage 1: " + Integer.toString(voltagePhase1) + " V" + "   " + "Voltage 2: " + Integer.toString(voltagePhase2) + " V" + "   " + "Voltage 3: " + Integer.toString(voltagePhase3) + " V");
		    			
		    			homeView.labelEnergyMeterCurrent.setText("Current 1: " + String.format("%.2f", currentPhase1) + " A" + "   " + "Current 2: " + String.format("%.2f", ((float)currentPhase2 * 0.1)) + " A" + "   " + "Current 3: " + String.format("%.2f", ((float)currentPhase3 * 0.1)) + " A");
	
		    			homeView.labelEnergyMeterTPartial.setText("Tpartial 1: " + String.format("%.2f", t1partial) + " kW" + "   " + "Tpartial 2: " + String.format("%.2f", t2partial) + " kW");
	
		    			homeView.labelEnergyMeterTotalPower.setText("Total Power: " + String.format("%.2f", totalPower) + " kW");
					}
				}
			};
			
			energyMeterManager.EnableListenerTimer(500); // 500 ms
		}
		
		homeView.buttonStartCharging.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (evccManager != null && evccManager.initialized == true) {
					if (evccManager.selectedModuleMode == EvccManager.EvccModuleMode.Manual) {
						if (evccManager.currentStateMachineState == EvccManager.EvccStateMachineState.B1) {
							evccManager.StartCharging(false);
							
							if (timerLedFlashing != null) {
								timerLedFlashing.cancel();
								timerLedFlashing = null;
							}
							
							if (statusLightManager != null) {
								statusLightManager.setBlue();
							}
						}
					}
				}
			}
		});
	
		homeView.buttonStopCharging.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (evccManager != null && evccManager.initialized == true) {
					if (evccManager.currentStateMachineState == EvccManager.EvccStateMachineState.C1 || evccManager.currentStateMachineState == EvccManager.EvccStateMachineState.C2 || evccManager.currentStateMachineState == EvccManager.EvccStateMachineState.D1 || evccManager.currentStateMachineState == EvccManager.EvccStateMachineState.D2) {
						evccManager.StopCharging(true); // with break start charging
					}
				}
			}
		});
		
		// Timer Camera
		
		if (CameraManager.IsCameraConnected() == true) {
		     (new Thread(new CameraThread())).start();
		}
		
	}

	public static String GetSerialPortName(String name) {
		try {
			Process proc = Runtime.getRuntime().exec("ls -l " + name);
			
			BufferedReader stdInput = new BufferedReader(new 
			     InputStreamReader(proc.getInputStream()));
			
			String s = null;
			String output = null;
			while ((s = stdInput.readLine()) != null) {
				output += s;
			}
			
			if (output != null) {
				int start = output.indexOf("->");
				return "/dev/" + output.substring(start+3, output.length());
			}
		} catch (IOException e) {
		}
		
		return null;
	}
	
	public static void updateCableText() {
		int cable = evccManager.GetCableDetection();
		
		switch (cable) {
			case 1: {
				homeView.labelCable.setText("Cable Detection: 6A");
				break;
			}
			case 2: {
				homeView.labelCable.setText("Cable Detection: 13A");
				break;
			}
			case 3: {
				homeView.labelCable.setText("Cable Detection: 16A");
				break;
			}
			case 4: {
				homeView.labelCable.setText("Cable Detection: 32");
				break;
			}
			case 5: {
				homeView.labelCable.setText("Cable Detection: 60");
				break;
			}
			default: {
				homeView.labelCable.setText("Cable Detection: Not Connected");
				break;
			}
		}
	}
}

class CameraThread implements Runnable {
	public void run() {
		CameraManager.CreateImage();
		
		final Timer timerCamera = new Timer();
		timerCamera.schedule(new TimerTask() { 
		    public void run() {
				CameraManager.CreateImage();
		    }
		}, 0, (60000 * 5)); // 5 min
	}
}