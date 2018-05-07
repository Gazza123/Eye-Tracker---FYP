import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

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
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class PupilLocator {
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
	
	static Point eyePoint = new Point(0, 0);
	
	static boolean drawAOI = false;
	static int[] lastKnownLocation;
	
	static ArrayList<Integer> xList = new ArrayList<Integer>();
	static ArrayList<Integer> yList = new ArrayList<Integer>();
	
	
	public static VideoCapture startCamera() {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		VideoCapture videoDevice = new VideoCapture();
	
		videoDevice.open(0);
		
		return videoDevice;
	}
	
	
	public static CascadeClassifier loadClassifier(){
		CascadeClassifier eyeClassifier = new CascadeClassifier(
				"resources/haarcascade_lefteye_2splits.xml");
		
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
					Imgproc.rectangle(frameCapture, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
							new Scalar(200, 200, 100),2);		
					
					currentEye = frameCapture.submat(rect);		
					Size size = new Size(320, 180);
					Imgproc.resize(currentEye, currentEye, size);
					Core.flip(currentEye, currentEye, 1);
					
					//Imgproc.rectangle(currentEye, new Point(120, 75), new Point(220, 145),
						//	new Scalar(200, 200, 100),2);
				}					
				
				
				Imgproc.cvtColor(currentEye, currentEye, Imgproc.COLOR_BGR2GRAY);
				Core.MinMaxLocResult mmG = Core.minMaxLoc(currentEye);				
				Point eyeLocation = mmG.minLoc;
				
				
				lastKnownLocation = new int[]{(int)eyeLocation.x, (int)eyeLocation.y};
				
				
				smoothEyePoint( eyeLocation);
				
				int[] eyeCoordinates = new int[]{(int)eyePoint.x, (int)eyePoint.y};
				
				
				Imgproc.circle(currentEye, eyePoint, 2, new Scalar(255, 255, 255, 255),2);
				
				System.out.println(eyePoint.x + ", " + eyePoint.y);
				
				if(calibrateTopLeft.getModel().isPressed()){
					topLeftPoint = new Point(eyePoint.x, eyePoint.y);
					calibrateTopLeft.setVisible(false);
				}
				
				if(calibrateBottRight.getModel().isPressed()){
					bottRightPoint = new Point(eyePoint.x, eyePoint.y);								
					calibrateBottRight.setVisible(false);
				}
				
				if(startButton.getModel().isPressed()){
					drawAOI = true;					
					startButton.setVisible(false);
				}
				
				if(drawAOI){
					Imgproc.rectangle(currentEye, topLeftPoint, bottRightPoint, new Scalar(200, 200, 100));
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
	
	
	public static void smoothEyePoint( Point newPoint){
		
		
		xList.add((int)newPoint.x);
		yList.add((int)newPoint.y);
		
		int smoothingRate = 10;
		if (xList.size() > smoothingRate)
		{   if (distanceNotTooBig(newPoint,eyePoint) )
			{
				eyePoint.x +=  ( newPoint.x - xList.get(0)  ) / smoothingRate;
				eyePoint.y += ( newPoint.y - yList.get(0)  ) / smoothingRate;
				xList.remove(0);
				yList.remove(0);
			}
		else {
			
			xList.remove(smoothingRate);
			yList.remove(smoothingRate);
			
		}
			
		}
		
		else
			if (xList.size() == 1)
			{
				eyePoint = newPoint;
				
			}
			else 
			{   float n = xList.size() - 1;
				eyePoint.x = (n * eyePoint.x + newPoint.x)/ (n+1);
				eyePoint.y = (n * eyePoint.y + newPoint.y)/ (n+1);
			
			}		
		
	}
	
	
	private static boolean distanceNotTooBig(Point newPoint, Point eyePoint2) {
		//issues found with this method, returns true until fixed
		double dist = Math.sqrt((newPoint.x - eyePoint2.x) *(newPoint.x - eyePoint2.x)  +  (newPoint.y - eyePoint2.y) *(newPoint.y - eyePoint2.y) );
		return true;
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
		frame.setSize(700, 300);
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
