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
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

public class DetectFace extends Thread{
 
	static JFrame frame;
	static JLabel lbl;
	static JLabel rightEyeLabel;
	static JLabel leftEyeLabel;
	
	static ImageIcon icon;
	static ImageIcon rightEyeIcon;
	static ImageIcon leftEyeIcon;	
 
	
	
	public static int[] startDetection() {
		int[] eyeCoordinates = null;
		
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		CascadeClassifier cascadeFaceClassifier = new CascadeClassifier(
				"C:/Users/t00181299/Downloads/opencv/sources/data/haarcascades/haarcascade_frontalface_default.xml");

	    CascadeClassifier eyeClassifier = new CascadeClassifier(
				"C:/Users/t00181299/Downloads/opencv/sources/data/haarcascades/haarcascade_eye_tree_eyeglasses.xml");
		
		Thread cameraThread = new Thread();
		
		VideoCapture videoDevice = new VideoCapture();
		videoDevice.open(0);
		if (videoDevice.isOpened()) {
			while (true) {		
				Mat frameCapture = new Mat();
				Mat leftEyeCapure = new Mat();
				Mat rightEyeCapture = new Mat();
				
				Mat zoomedMat = frameCapture;
				
				Mat edges = new Mat();
				videoDevice.read(frameCapture);
				
				
				
		        //Imgproc.Canny(zoomedMat, zoomedMat, 15, 15*3);
				
				//MatOfRect faces = new MatOfRect();
				//cascadeFaceClassifier.detectMultiScale(frameCapture, faces);					
				/*for (Rect rect : faces.toArray()) {					
					Imgproc.putText(frameCapture, "Face", new Point(rect.x,rect.y-5), 1, 2, new Scalar(0,0,255));								
					Imgproc.rectangle(frameCapture, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
							new Scalar(0, 100, 0),3);
					
					zoomedMat = new Mat(frameCapture, rect);
				}*/
														
				
				detectEyes(frameCapture, eyeClassifier);				
				

				//Mat gray = new Mat();
		        //Imgproc.cvtColor(frameCapture, gray, Imgproc.COLOR_BGR2GRAY);
		        //Imgproc.medianBlur(gray, gray, 5);
		        Mat circles = new Mat();
		        Imgproc.cvtColor(frameCapture, frameCapture, Imgproc.COLOR_BGR2GRAY);
		        Imgproc.medianBlur(frameCapture, frameCapture, 5);
		        //Imgproc.Canny(frameCapture, frameCapture, 30.0f, 40.0f);
		        Imgproc.HoughCircles(frameCapture, circles, Imgproc.HOUGH_GRADIENT, 1.0,
		                (double)frameCapture.rows()/16, 
		                100.0, 30.0, 1, 70 );

		        eyeCoordinates = detectPupils(circles, frameCapture);
				//Imgproc.cvtColor(frameCapture, edges, Imgproc.COLOR_BGR2GRAY);
				//Size zoomedSize = new Size(500, 500);		        
		        //Imgproc.resize(zoomedMat, zoomedMat, zoomedSize);
		        
		        //Imgproc.cvtColor(zoomedMat, zoomedMat,Imgproc.COLOR_RGB2GRAY );
		        //Imgproc.Canny(zoomedMat, zoomedMat, 15, 15*3);
		        
		        
				
				PushImage(ConvertMat2Image(frameCapture));
				return eyeCoordinates;
				//System.out.println(String.format("%s face(FACES) %s eye(EYE) detected.", faces.toArray().length,eyes.toArray().length));
			}
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
	
	private static int[] detectPupils(Mat circles, Mat frameCapture){
		
		int[] coordinates = new int[]{0,0};
		
		for (int x = 0; x < circles.cols(); x++) {
            double[] c = circles.get(0, x);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));

            Imgproc.circle(frameCapture, center, 1, new Scalar(0,100,100), 3, 8, 0 );
            System.out.println(circles.cols());
            
            
            coordinates = new int[]{(int)center.x, (int)center.y};
            System.out.println((int)center.x + " " + (int)center.y);
            //int radius = (int) Math.round(c[2]);
            //Imgproc.circle(frameCapture, center, radius, new Scalar(255,0,255), 3, 8, 0 );
        }
        
        return coordinates;	
	}
}