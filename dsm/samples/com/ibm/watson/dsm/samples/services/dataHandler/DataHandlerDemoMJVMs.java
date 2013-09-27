package com.ibm.watson.dsm.samples.services.dataHandler;


//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.ibm.watson.dsm.DSMException;
import com.ibm.watson.dsm.platform.ApplicationDescriptor;
import com.ibm.watson.dsm.platform.IApplicationDescriptor;
import com.ibm.watson.dsm.services.dataHandler.DataHandler;
import com.ibm.watson.dsm.services.dataHandler.IDataHandler;
/**
 * Demonstrating DataHandler usage
 * 
 * @author rbdilmag
 *
 */

public class DataHandlerDemoMJVMs implements MouseListener{

	final public static int NUM_NODES= 3;
	IDataHandler dataHandler[] = new IDataHandler[NUM_NODES];	
	
	private JFrame window = new JFrame("Data Handler Service"); 	
	private JTextArea displayTextArea = new JTextArea();
	private JScrollPane displayScrollPane = new JScrollPane(displayTextArea);
	
	private JButton clearButton = new JButton("Clear");
	private JPanel topPanel = new JPanel();
	private JPanel bottomPanel = new JPanel();	
	
	
	
	JButton retrieveButton[]= new JButton[3];
	

	
	
	JButton saveButton[]=new JButton[3];
	
	

	private JTextField fileSaveTextField = new JTextField(12);
	private JTextField fileRetrieveTextField = new JTextField(12);

	private JLabel fileNameSave = new JLabel("File Name S",	SwingConstants.RIGHT);
	private JLabel fileNameRetrieve = new JLabel("File Name R",SwingConstants.RIGHT);	
	
	/*Constructors*/
	public DataHandlerDemoMJVMs(){
		super();
	}
	
	public DataHandlerDemoMJVMs(String instanceID) throws DSMException {
		super();		

		int storageLimit = 3;
		int storageCheckInterval = 5000;			

		for (int i=0 ; i<dataHandler.length ; i++){
//			String id=  (instanceID == null ? "" : i) + i;
			String id="instance"+i;
			System.out.println("Creating instance " + i);
			IApplicationDescriptor appDesc = new ApplicationDescriptor(this.getClass().getSimpleName(), id);
			dataHandler[i] = new DataHandler(appDesc, storageLimit, storageCheckInterval);	
		}

		for(int k=0;k<retrieveButton.length;k++){
			retrieveButton[k] = new JButton();
			retrieveButton[k].setText("retrieve");
			
		}
		for(int i=0;i<saveButton.length;i++){
			saveButton[i] = new JButton();
			saveButton[i].setText("save");
			
		}
					
		topPanel.add(fileNameSave);
		topPanel.add(fileSaveTextField);
		
		window.getContentPane().add(topPanel, "North");

		bottomPanel.add(fileNameRetrieve);
		bottomPanel.add(fileRetrieveTextField);
		
		bottomPanel.add(clearButton);
		window.getContentPane().add(bottomPanel, "South");
		window.getContentPane().add(displayScrollPane, "Center"); 
		window.setSize(1000, 100); // width,height
		window.setLocation(200, 200);
		window.setVisible(true); // retrieve!
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		displayTextArea.setEditable(false); // keep cursor out of this area!
		displayTextArea.setFont(new Font("default", Font.PLAIN, 12));
		
		for (int i=0 ; i<3 ; i++) {
		dataHandler[i].getApplicationDescriptor().getInstanceID();
		topPanel.add(saveButton[i]);
		saveButton[i].setBackground(Color.magenta);
		saveButton[i].addMouseListener(new SaveMouseListener(dataHandler[i]));
		retrieveButton[i].addMouseListener(new RetrieveMouseListener(dataHandler[i],i));	
		bottomPanel.add(retrieveButton[i]);
		retrieveButton[i].setBackground(Color.cyan);
		}		

		clearButton.addMouseListener(new ClearMouseListener(dataHandler[0],dataHandler[1],dataHandler[2]));	

		
		
		
		window.getContentPane().add(topPanel, "North");

		bottomPanel.add(fileNameRetrieve);
		bottomPanel.add(fileRetrieveTextField);
		
		bottomPanel.add(clearButton);
		
		window.getContentPane().add(bottomPanel, "South");

		window.getContentPane().add(displayScrollPane, "Center"); 
		
		window.setSize(1000, 400); // width,height
		window.setLocation(200, 200);
		window.setVisible(true); // retrieve!
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 		

		clearButton.setBackground(Color.blue);		

	}
	final static String path="samples/com/ibm/watson/dsm/samples/services/dataHandler/";
	
	public static void main(String[] args) throws DSMException {
		
		new DataHandlerDemoMJVMs(null);				
		System.out.println("Waiting for platforms to register...");						
	} //end of main
	
	//******************
	private static byte[] fileToByte(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		long length = file.length();                

		byte[] bytes = new byte[(int)length];

		int offset = 0;        
		int numRead = 0;
		while (offset < bytes.length
				&& (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
		}

		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "+file.getName());
		}

		is.close();
		return bytes;
	}	

	//******************	
	private static void byteToFile(byte[] data, String fileName) throws IOException{
		OutputStream out = new FileOutputStream(fileName);
		out.write(data);
		out.close();
	}	
	//******************
	
	//******************
	private abstract class AbstractMouseListener implements MouseListener {
		protected IDataHandler dataHandler;

		public AbstractMouseListener(IDataHandler dataHandler) {
			this.dataHandler = dataHandler;			
		}
		
		@Override
		public void mouseEntered(MouseEvent arg0) {
		
		}
	
		@Override
		public void mouseExited(MouseEvent arg0) {
		
		}
	
		@Override
		public void mousePressed(MouseEvent arg0) {

		}
	
		@Override
		public void mouseReleased(MouseEvent arg0) {
	
		}
	}

	private class SaveMouseListener extends AbstractMouseListener implements MouseListener {

		public SaveMouseListener(IDataHandler dataHandler) {
			super(dataHandler);
		}
		
		
		private void save(String saveType, MouseEvent ae) {
				
				try {					
						String fileName = fileSaveTextField.getText().trim();
						
						if (fileName.length() == 0)
						{
							displayTextArea.append("File Name is Required! \n");
							return;
						}
						if (fileName.contains(" "))
						{
							displayTextArea.append("File Name does not contain space \n ");				
							return ;
						}

						
						try
						{
							File a = new File(path +fileName+".txt");
							
							//File file = new File(filePath, "Test_1.exe");

						     if (a.exists())
						     {
						    	 dataHandler.putData(fileName, fileToByte(a));				    	 
						    							    	 					    	
						    	
						     }
						     else
						     {
						            displayTextArea.append("File not found" + "\t");
						            
						            
						      }
						}
						catch(SecurityException se)
						{
						     se.printStackTrace();
						}
						String strB = fileName + "\t" + dataHandler.getApplicationDescriptor().toString() +  "\n";
						
						displayTextArea.append(strB);
						System.out.println(strB);
						
						String listing = dataHandler.getRepositoryListing();
						displayTextArea.append(listing);
						displayTextArea.append("------------------- \n");
						

				} catch (Exception e) {
					
					e.printStackTrace();
				}
						
			}
	
			@Override
			public void mouseClicked(MouseEvent arg0) {
					this.save("",arg0);
					
			}
	}	// end of SaveMouseListener	
	
	//******************
	private class RetrieveMouseListener extends AbstractMouseListener implements MouseListener {

		private int buttonIndex;

		public RetrieveMouseListener(IDataHandler dataHandler, int buttonIndex) {
			super(dataHandler);
			this.buttonIndex = buttonIndex;
		}
		
		private void retrieve(String retrieveType, MouseEvent ae){
			try {
				
			String fileName = fileRetrieveTextField.getText().trim();

				if (fileName.length() == 0)
				{
					displayTextArea.append("File Name is Required! \n");
					return;
				}

				if (fileName.contains(" "))
				{
					displayTextArea.append("Invalid File Name; file name does not contain space  \n");
					return;
				}
				
				try
				{
					File a = new File(path +fileName+".txt");
					
					
				     if (a.exists())
				     {
				    	 System.out.println("retrieving file name " + fileName);	
							byte[] bo;
							bo = dataHandler.getData(fileName);	
							System.out.println("The object content is " + new String(bo));
							byteToFile(bo, path +"dataHandler"+buttonIndex+"-"+fileName+".txt");
							
							
							String strB = dataHandler.getApplicationDescriptor().toString() + "  File name:" + fileName + "\t" +"file content is:"+ ""+new String(bo) + "\n" ;
							
							displayTextArea.append(strB);
							System.out.println(strB);
				    	
				     }
				     else
				     {
				            displayTextArea.append("File does not exist!" + "\t");
				            
				            
				      }
				}
				catch(SecurityException se)
				{
				     se.printStackTrace();
				}

				
				String listing = dataHandler.getRepositoryListing();
				displayTextArea.append(listing);
				displayTextArea.append("------------------- \n"); 
						
				

			} catch (Exception e) {
				
				//e.printStackTrace();
				displayTextArea.append("File is not in the system!" + "\n");
				displayTextArea.append("------------------- \n"); 
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			this.retrieve("", e);
		}	
	} // end of RetrieveMouseListener	
	
	//******************
	private class ClearMouseListener extends AbstractMouseListener implements MouseListener {

		
		public ClearMouseListener(IDataHandler dataHandler, IDataHandler dataHandlern, IDataHandler dataHandlerm) {
			super(dataHandler);
			
		}		
			
	
	private void clear(String retrieveType, MouseEvent ae){
		displayTextArea.setText("");
	}
		@Override
		public void mouseClicked(MouseEvent arg0) {
			
			this.clear("", arg0);
		}
	} //end of class ClearMouseListener



	

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}