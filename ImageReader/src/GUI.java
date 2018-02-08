import java.awt.EventQueue;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileSystemView;

public class GUI {

	private JFrame frame;
	private Image displayedImage = null;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 650, 550);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JPanel pnl_Image = new JPanel();
		pnl_Image.setBounds(10, 11, 614, 439);
		frame.getContentPane().add(pnl_Image);
		pnl_Image.setLayout(null);
		
		JLabel lblImage = new JLabel("Image");
		lblImage.setBounds(10, 11, 594, 417);
		pnl_Image.add(lblImage);
		
		JPanel pnl_Functions = new JPanel();
		pnl_Functions.setBounds(10, 455, 614, 45);
		frame.getContentPane().add(pnl_Functions);
		pnl_Functions.setLayout(null);
		
		JButton btnImage = new JButton("Image...");
		btnImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String filename = "";
				
				JFileChooser uploadChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
				int returnValue = uploadChooser.showOpenDialog(null);
				if(returnValue == JFileChooser.APPROVE_OPTION){
					File selectedFile = uploadChooser.getSelectedFile();
					filename = selectedFile.getAbsolutePath();
				}

				lblImage.setIcon(new ImageIcon(ImageFunctions.readImage(filename)));
			}
		});
		btnImage.setBounds(10, 11, 89, 23);
		pnl_Functions.add(btnImage);
		
		JButton btnDetectFaces = new JButton("Detect Faces");
		btnDetectFaces.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ImageFunctions.detectFeatures(displayedImage);
			}
		});
		btnDetectFaces.setBounds(109, 11, 89, 23);
		pnl_Functions.add(btnDetectFaces);
		
		JButton btnCanny = new JButton("Canny");
		btnCanny.setBounds(208, 11, 89, 23);
		pnl_Functions.add(btnCanny);
	}
}
