import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
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

public class EyeDetector {
	
	static JFrame frame;
	static JLabel lbl;
	static JLabel rightEyeLabel;
	static JLabel leftEyeLabel;
	
	static ImageIcon icon;
	static ImageIcon rightEyeIcon;
	static ImageIcon leftEyeIcon;	
	
	
	
	public static void main(String[] args) {
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		CascadeClassifier eyeClassifier = new CascadeClassifier(
				"C:/opencv/build/etc/haarcascades/haarcascade_lefteye_2splits.xml");
		
		VideoCapture videoDevice = new VideoCapture();
		videoDevice.open(0);
		
		while(true) {
			startDetection(videoDevice, eyeClassifier);
		}
	}
	
	
	
	public static int[] startDetection(VideoCapture videoDevice, CascadeClassifier eyeClassifier) {
		int[] eyeCoordinates = null;
		
		
		
		videoDevice.open(0);
		if (videoDevice.isOpened()) {
			//while (true) {		 
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
					
					
					//currentEye = frameCapture.adjustROI(rect.width/2, (rect.width/2)+rect.height, rect.height/2, (rect.height/2)+rect.width);
					currentEye = frameCapture.submat(rect);
					
				}			
				

				Imgproc.cvtColor(currentEye, currentEye, Imgproc.COLOR_BGR2GRAY);
				Core.MinMaxLocResult mmG = Core.minMaxLoc(currentEye);
				Imgproc.circle(currentEye, mmG.minLoc,2, new Scalar(255, 255, 255, 255),2);
				

		        
		        
				
				PushImage(ConvertMat2Image(currentEye));
				
				//videoDevice.release();
				frameCapture.release();
				currentEye.release();
				
				//eyes.release();
				
				return eyeCoordinates;
				//System.out.println(String.format("%s face(FACES) %s eye(EYE) detected.", faces.toArray().length,eyes.toArray().length));
			//}
		} else {
			System.out.println("Video device did not open");
			return new int[]{0,0};
		}
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
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		
	}
	private static void PushImage(Image overview) {
		if (frame == null)
			prepareWindow();
		if (lbl != null)
			frame.remove(lbl);
			//frame.remove(rightEyeLabel);
			//frame.remove(leftEyeLabel);
			
		icon = new ImageIcon(overview);
		//rightEyeIcon = new ImageIcon(rightEye);
		//leftEyeIcon = new ImageIcon(leftEye);
		
		lbl = new JLabel();
		//rightEyeLabel = new JLabel();
		//leftEyeLabel = new JLabel();
		
		lbl.setIcon(icon);
		//rightEyeLabel.setIcon(rightEyeIcon);
		//leftEyeLabel.setIcon(leftEyeIcon);
		
		
		frame.add(lbl);
		//frame.add(rightEyeLabel);
		//frame.add(leftEyeLabel);
		
		frame.revalidate();
	}

	
	private static Mat detectEyes(Mat imageToDetect , CascadeClassifier classifier){
		MatOfRect eyes = new MatOfRect();		
		
		classifier.detectMultiScale(imageToDetect, eyes);
		
		Mat currentEye = imageToDetect;
		for (Rect rect : eyes.toArray()) {				
			
			Imgproc.putText(imageToDetect, "Eye", new Point(rect.x,rect.y-5), 1, 2, new Scalar(0,0,255));		
			Point centerOfRect = new Point(rect.x + rect.width/2, rect.y + rect.height/2);
			
			Imgproc.line(imageToDetect, new Point(rect.x + rect.width, rect.y + rect.height/2), 
					new Point(rect.x, rect.y + rect.height/2), new Scalar(200, 200, 100));
			
			Imgproc.line(imageToDetect, new Point(rect.x + rect.width/2, rect.y), 
					new Point(rect.x + rect.width/2, rect.y + rect.height), new Scalar(200, 200, 100));
			
			Imgproc.circle(imageToDetect, centerOfRect, 6, new Scalar(200, 200, 100));
			Imgproc.rectangle(imageToDetect, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
					new Scalar(200, 200, 100),2);	
			
			currentEye = imageToDetect.submat(rect);
		}
		
		return currentEye;
	}
}
