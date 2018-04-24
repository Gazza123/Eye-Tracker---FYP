import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class EyeTester {
	static JFrame frame;
	static JLabel lbl;
	static JLabel rightEyeLabel;
	static JLabel leftEyeLabel;
	
	static JButton calibrateTopLeft = new JButton("Top Left");
	static JButton calibrateBottRight = new JButton("Bottom Right");
	static JButton startButton = new JButton("Start Tracking");
	
	static ImageIcon icon;
	static ImageIcon rightEyeIcon;
	static ImageIcon leftEyeIcon;	
	
	static Point topLeftPoint = null;
	static Point bottRightPoint = null;
	
	static boolean drawAOI = false;
	static int[] lastKnownLocation;
	
	public static VideoCapture startCamera() {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		VideoCapture videoDevice = new VideoCapture();
	
		videoDevice.open(0);
		
		return videoDevice;
	}
	
	public static CascadeClassifier loadClassifier(){
		CascadeClassifier eyeClassifier = new CascadeClassifier(
				"C:/Users/t00181299/Downloads/opencv/build/etc/haarcascades/haarcascade_lefteye_2splits.xml");
		
		return eyeClassifier;
	}
	
	public static int[] startDetection(VideoCapture videoDevice, CascadeClassifier eyeClassifier) {
			
			
		if (videoDevice.isOpened()) {
			try{
				Mat frameCapture = new Mat();
				
				videoDevice.read(frameCapture);
		        		        				
				
				MatOfRect eyes = new MatOfRect();
				eyeClassifier.detectMultiScale(frameCapture, eyes);
				
				Mat currentEye = null;
				
				for (Rect rect : eyes.toArray()) {				
					
					Imgproc.putText(frameCapture, "Eye", new Point(rect.x,rect.y-5), 1, 2, new Scalar(0,0,255));		
					Point centerOfRect = new Point(rect.x + rect.width/2, rect.y + rect.height/2);
										
					Imgproc.rectangle(frameCapture, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
							new Scalar(200, 200, 100),2);		
					
					currentEye = frameCapture.submat(rect);					
				}	
				
				
				Imgproc.cvtColor(currentEye, currentEye, Imgproc.COLOR_BGR2GRAY);
				Core.MinMaxLocResult mmG = Core.minMaxLoc(currentEye);
				
				Point eyeLocation = mmG.minLoc;
				int[] eyeCoordinates = new int[]{(int)eyeLocation.x, (int)eyeLocation.y};
				
				Imgproc.circle(currentEye, eyeLocation,2, new Scalar(255, 255, 255, 255),2);

				lastKnownLocation = new int[]{(int)eyeLocation.x, (int)eyeLocation.y};
				
				
				if(calibrateTopLeft.getModel().isPressed()){
					topLeftPoint = new Point(eyeLocation.x, eyeLocation.y);
					System.out.print("\nTop Left Position: " + topLeftPoint.x + ", " + topLeftPoint.y);					
					calibrateTopLeft.setVisible(false);
				}
				
				if(calibrateBottRight.getModel().isPressed()){
					bottRightPoint = new Point(eyeLocation.x, eyeLocation.y);
					System.out.print("\nBottom Right Position: " + bottRightPoint.x + ", " + bottRightPoint.y);				
					calibrateBottRight.setVisible(false);
				}
				
				if(startButton.getModel().isPressed()){
					drawAOI = true;
					Imgproc.rectangle(currentEye, topLeftPoint, bottRightPoint, new Scalar(200, 200, 100));
					startButton.setVisible(false);
				}
				
				PushImage(ConvertMat2Image(currentEye));
			
				frameCapture.release();
				currentEye.release();

				return eyeCoordinates;
			} catch(Exception e){
				System.out.println("No Eyes Found");
				return lastKnownLocation;
			}
		} else {
			System.out.println("Video device did not open");		
			return lastKnownLocation;
		}
	}
	
	public static boolean startTracking(){
		if(drawAOI){
			return true;
		}
		
		else return false;
	}
	
	private static BufferedImage ConvertMat2Image(Mat cameraMaterial) {
	
		
		MatOfByte byteMaterial = new MatOfByte();

		Imgcodecs.imencode(".jpg", cameraMaterial, byteMaterial);
		byte[] byteArray = byteMaterial.toArray();
		BufferedImage buffImg = null;
		try {
			InputStream in = new ByteArrayInputStream(byteArray);
			buffImg = ImageIO.read(in);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}  
		byteMaterial.release();
		return buffImg;
	}
	
  	
	private static void prepareWindow() {
		frame = new JFrame();
		frame.setLayout(new FlowLayout());
		frame.setSize(700, 600);
		frame.setVisible(true);	
		frame.add(calibrateTopLeft);
		frame.add(calibrateBottRight);
		frame.add(startButton);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		
	}
	
	
	private static void PushImage(Image overview) {
		if (frame == null)
			prepareWindow();
		if (lbl != null)
			frame.remove(lbl);
	
		icon = new ImageIcon(overview);
		
		lbl = new JLabel();	
		lbl.setIcon(icon);	
		
		frame.add(lbl);	
		frame.revalidate();
	}
}
