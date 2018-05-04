import java.awt.AWTException;
import java.awt.Robot;

import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class MouseAutomator {
			
	public static void main(String[] args) {
		try{	
			
			VideoCapture videoDevice = PupilLocator.startCamera();
			CascadeClassifier eyeClassifier = PupilLocator.loadClassifier();
			while(true){
				if(PupilLocator.startTracking()){
					int[] eyeLocations = PupilLocator.startDetection(videoDevice, eyeClassifier);
					
					eyeLocations = translatePointsToScreen(eyeLocations);
					
					Robot wallE = new Robot();
				
					//System.out.println(eyeLocations[0] + ", " + eyeLocations[1]);
					wallE.mouseMove(eyeLocations[0], eyeLocations[1]);
				} else {
					PupilLocator.startDetection(videoDevice, eyeClassifier);
				}
				
			}
		} catch (AWTException e) {
			e.printStackTrace();
		}

	}
	
	
	public static int[] translatePointsToScreen(int[] eyeLocations) {
		//1920 x 1080 == 320 x 180
		int[] translatedEyeLocations = {0, 0};
		
		translatedEyeLocations[0] = eyeLocations[0]*6;
		translatedEyeLocations[1] = eyeLocations[1]*6;
		
		System.out.println(translatedEyeLocations[0] + ", " + translatedEyeLocations[1]);
		
		return translatedEyeLocations;
	}

}
