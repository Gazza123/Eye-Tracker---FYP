import java.awt.AWTException;
import java.awt.Robot;

public class MouseAutomator {
			
	public static void main(String[] args) {
		try{		
			while(true){
				int[] eyeLocations = DetectFace.startDetection();
			
				Robot wallE = new Robot();
			
				//System.out.println(eyeLocations[0] + ", " + eyeLocations[1]);
				wallE.mouseMove(eyeLocations[0], eyeLocations[1]);
				
			}
		} catch (AWTException e) {
			e.printStackTrace();
		}

	}

}
