import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

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

public class ImageFunctions {
	
	public static Image readImage(String filename) {
	
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		return ConvertMat2Image(Imgcodecs.imread(filename));
		
	}
	
	
	public static Image detectFeatures(Image displayedImage) {
		//TO DO 
		
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

}
