import java.io.IOException;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;

public class StatusLightManager {
	
	// Consts
	
	public static final SpiChannel SPI_CHANNEL = SpiChannel.CS0;
	public static final SpiMode SPI_MODE = SpiDevice.DEFAULT_SPI_MODE;
	public static final int SPI_SPEED = 10000000;
	
	public enum LightState {
	    GREEN,
	    RED,
	    BLUE,
	    BLUEPURPLE,
	    ORANGE,
	    OFF,
	}
	
	// Variables
	
	public SpiDevice spiDevice;
	public LightState lightState = LightState.OFF;
	
	// Functions
	
	public StatusLightManager() {
        try {
        	// create SPI object instance for SPI for communication
			spiDevice = SpiFactory.getInstance(SPI_CHANNEL, SPI_SPEED, SPI_MODE);

			 // init ok
            System.out.println("Status Light Manager OK");
		} catch (IOException ex) {
			System.out.println("Status Light Manager exception: " + ex.getMessage());
		}
	}
	
	public void SPI_SendColor(boolean swi, int bri, int red1, int green1, int blue1, int red2, int green2, int blue2, int red3, int green3, int blue3, int red4, int green4, int blue4) {
        if (spiDevice != null) {
            byte[] buffer = new byte[28];

            buffer[0] = (byte)((0x25 << 2) | (0x00 << 1) | 0x00);
            buffer[1] = (byte)((0x00 << 7) | (0x01 << 6) | (~(swi ? 0x01 : 0x00) << 5) | (bri & 0x7C) >> 2);
            buffer[2] = (byte)(((bri & 0x03) << 6) | (bri & 0x7E) >> 1);
            buffer[3] = (byte)(((bri & 0x01) << 7) | (bri & 0x7E));

            buffer[4] = (byte)blue1;
            buffer[5] = (byte)blue1;
            buffer[6] = (byte)red1;
            buffer[7] = (byte)red1;
            buffer[8] = (byte)green1;
            buffer[9] = (byte)green1;

            buffer[10] = (byte)blue2;
            buffer[11] = (byte)blue2;
            buffer[12] = (byte)red2;
            buffer[13] = (byte)red2;
            buffer[14] = (byte)green2;
            buffer[15] = (byte)green2;

            buffer[16] = (byte)blue3;
            buffer[17] = (byte)blue3;
            buffer[18] = (byte)red3;
            buffer[19] = (byte)red3;
            buffer[20] = (byte)green3;
            buffer[21] = (byte)green3;

            buffer[22] = (byte)blue4;
            buffer[23] = (byte)blue4;
            buffer[24] = (byte)red4;
            buffer[25] = (byte)red4;
            buffer[26] = (byte)green4;
            buffer[27] = (byte)green4;
            
            try {
				spiDevice.write(buffer);
			} catch (IOException e) {
				
			}
        }
    }

	public void setOrange() {
		SPI_SendColor(true, 127, 0xFF, 0x55, 0x00, 0xFF, 0x55, 0x00, 0xFF, 0x55, 0x00, 0xFF, 0x55, 0x00);
        
        lightState = LightState.ORANGE;
	}
	
    public void setGreen() {
        SPI_SendColor(true, 127, 0x00, 0xFF, 0x00, 0x00, 0xFF, 0x00, 0x00, 0xFF, 0x00, 0x00, 0xFF, 0x00);
        
        lightState = LightState.GREEN;
    }

    public void setRed() {
        SPI_SendColor(true, 127, 0xFF, 0x00, 0x00, 0xFF, 0x00, 0x00, 0xFF, 0x00, 0x00, 0xFF, 0x00, 0x00);
        
        lightState = LightState.RED;
    }
    
    public void setBlue() {
    	SPI_SendColor(true, 127, 0x00, 0x30, 0xFF, 0x00, 0x30, 0xFF, 0x00, 0x30, 0xFF, 0x00, 0x30, 0xFF);
         
        lightState = LightState.BLUE;
    }

    public void setBluePurple() {
    	SPI_SendColor(true, 127, 0x00, 0x30, 0xFF, 0x00, 0x30, 0xFF, 0xFF, 0x00, 0xFF, 0xFF, 0x00, 0xFF);
         
        lightState = LightState.BLUEPURPLE;
    }

    public void turnOff() {
        SPI_SendColor(false, 0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00);
        
        lightState = LightState.OFF;
    }
}
