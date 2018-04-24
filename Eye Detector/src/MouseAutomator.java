import java.awt.AWTException;
import java.awt.Robot;

import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class MouseAutomator {
			
	public static void main(String[] args) {
		try{	
			
			VideoCapture videoDevice = EyeTester.startCamera();
			CascadeClassifier eyeClassifier = EyeTester.loadClassifier();
			while(true){
				if(EyeTester.startTracking()){
					int[] eyeLocations = EyeTester.startDetection(videoDevice, eyeClassifier);
					
					Robot wallE = new Robot();
				
					//System.out.println(eyeLocations[0] + ", " + eyeLocations[1]);
					wallE.mouseMove(eyeLocations[0], eyeLocations[1]);
				} else {
					EyeTester.startDetection(videoDevice, eyeClassifier);
				}
				
			}
		} catch (AWTException e) {
			e.printStackTrace();
		}

	}

}
