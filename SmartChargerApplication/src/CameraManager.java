import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CameraManager {

	// Consts
	
	public static int CAMERA_ROTATION = 70;
	public static int CAMERA_ISO = 0;
	public static int CAMERA_BRIGHTNESS = 50;
	public static int CAMERA_CONTRAST = 0;
	public static String CAMERA_IMAGE_NAME = "image.jpg";
	
	// Functions
	
	public static boolean IsCameraConnected() {
		try {
			Process proc = Runtime.getRuntime().exec("vcgencmd get_camera");
			
			BufferedReader stdInput = new BufferedReader(new 
			     InputStreamReader(proc.getInputStream()));
			
			String s = "";
			String output = "";
			while ((s = stdInput.readLine()) != null) {
				output += s;
			}
			
			if (output != null && output.length() > 0) {
				if (output.contains("supported=1") && output.contains("detected=1")) {
					return true;
				}
			}
		} catch (IOException e) {
		}
		
		
		return false;
	}
	
	public static void CreateImage() {
		try {
			Runtime.getRuntime().exec("sudo raspistill --nopreview --rotation " + Integer.toString(CAMERA_ROTATION) + " --ISO " + Integer.toString(CAMERA_ISO) + " --brightness " + Integer.toString(CAMERA_BRIGHTNESS) + " --contrast " + Integer.toString(CAMERA_CONTRAST) + " -o " + "../" + CAMERA_IMAGE_NAME);
		} catch (IOException e) {
		}
	}
}
