package com.ibm.watson.dsm.samples.services.dataHandler;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ibm.watson.dsm.DSMException;
import com.ibm.watson.dsm.platform.ApplicationDescriptor;
import com.ibm.watson.dsm.platform.IApplicationDescriptor;
import com.ibm.watson.dsm.services.dataHandler.DataHandler;
import com.ibm.watson.dsm.services.dataHandler.DataHandlerException;
import com.ibm.watson.dsm.services.dataHandler.IDataHandler;
import com.ibm.watson.pml.util.CommandArgs;

/**
 * Demonstrating DataHandler usage
 * 
 * @author rbdilmag
 *
 */


public class DataHandlerDemoMultipleJVMs{

	// One instance per application to provide the data sharing services.
	IDataHandler dataHandler=null;	

	/**
	 * Create an instance containing a shared bean platform that has the given instance name.  
	 * The platform is started and  a subscription to all data is created.
	 * @param instanceID
	 * @throws DSMException
	 */
	public DataHandlerDemoMultipleJVMs(String instanceID) throws DSMException {
		System.out.println("Creating instance " + instanceID);

		int storageLimit = 3;
		int storageCheckInterval = 5000;
		
			
		IApplicationDescriptor appDesc = new ApplicationDescriptor("DataHandlerTest", instanceID);
				
		dataHandler = new DataHandler(appDesc, storageLimit, storageCheckInterval);				
	}
	final static String path="samples/com/ibm/watson/dsm/samples/services/dataHandler/";
	
	public static void main(String[] args) throws DSMException {

		CommandArgs cmdargs = new CommandArgs(args);
		String instanceid = cmdargs.getOption("instance");
		DataHandlerDemoMultipleJVMs dataHandleri= new DataHandlerDemoMultipleJVMs(instanceid);
		// Create/initialize 3 platforms all with different instance ids.
		// Normally there is only one platform/JVM, but here we create them all in the
		// same JVM to simplify this sample code.				
		
		
		
		// Give the registry some time to share its information platforms
		System.out.println("Waiting for platform to register...");		
		try {Thread.sleep(5000);} catch (InterruptedException e) {	}
		
		//Files that are the objects we share			
		File a = new File(path +"a.txt");
		File b = new File(path +"b.txt");
		File c = new File(path +"c.txt");
		File d = new File(path +"d.txt");
		File e = new File(path +"e.txt");
		File a2 = new File(path +"a2.txt");//a duplicate file, same as "a"

		for (int i=0 ; i<2 ; i++) {
			
			// 
			//dataHandler.dataHandler.getApplicationDescriptor().getInstanceID();
			try {
				dataHandleri.dataHandler.putData("a", fileToByte(a));
			} catch (DataHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// Share the msg into the shared bean platform, which will cause it to be sent to those listening for it.
			byte[] bo;
			try {
				bo = dataHandleri.dataHandler.getData("a");
				System.out.println("The object content is " + new String(bo));
				byteToFile(bo, "dataHandler1-a.txt");
				System.out.println("");
//				bo = dataHandleri.dataHandler.getData("b");
//				System.out.println("The object content is " + new String(bo));
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (DataHandlerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
	}
	

	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
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

	/**
	 * convert byte[] to file
	 * @param data
	 * @param fileName
	 * @throws IOException
	 */
	private static void byteToFile(byte[] data, String fileName) throws IOException{
		OutputStream out = new FileOutputStream(fileName);
		out.write(data);
		out.close();
	}

}
