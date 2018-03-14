import java.awt.AWTException;
import java.awt.Robot;

public class MouseAutomator {

	public static void main(String[] args) {
		try {
			Robot wallE = new Robot();
			
			wallE.mouseMove(0, 0);
		} catch (AWTException e) {
			e.printStackTrace();
		}

	}

}
