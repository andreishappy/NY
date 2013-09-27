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

/**
 * Demonstrating DataHandler usage
 * 
 * @author Yu-Ting Yu, rbdilmag
 *
 */


public class DataHandlerDemoorig{

	// One instance per application to provide the data sharing services.
	IDataHandler dataHandler;	

	/**
	 * Create an instance containing a shared bean platform that has the given instance name.  
	 * The platform is started and  a subscription to all data is created.
	 * @param instanceID
	 * @throws DSMException
	 */
	public DataHandlerDemoorig(String instanceID) throws DSMException {
		System.out.println("Creating instance " + instanceID);

		int storageLimit = 3;
		int storageCheckInterval = 5000;
		
			
		IApplicationDescriptor appDesc = new ApplicationDescriptor("DataHandlerTest", instanceID);
				
		dataHandler = new DataHandler(appDesc, storageLimit, storageCheckInterval);				
	}
	final static String path="samples/com/ibm/watson/dsm/samples/services/dataHandler/";
	
	public static void main(String[] args) throws DSMException {

		// Create/initialize 3 platforms all with different instance ids.
		// Normally there is only one platform/JVM, but here we create them all in the
		// same JVM to simplify this sample code.				
		DataHandlerDemoorig dataHandler1 = new DataHandlerDemoorig("dataHandler1");
		DataHandlerDemoorig dataHandler2 = new DataHandlerDemoorig("dataHandler2");
		DataHandlerDemoorig dataHandler3 = new DataHandlerDemoorig("dataHandler3");
		
		// Give the registry some time to share its information platforms
		System.out.println("Waiting for platforms to register...");		
		try {Thread.sleep(5000);} catch (InterruptedException e) {	}
		
		//Files that are the objects we share			
		File a = new File(path +"a.txt");
		File b = new File(path +"b.txt");
		File c = new File(path +"c.txt");
		File d = new File(path +"d.txt");
		File e = new File(path +"e.txt");
		File a2 = new File(path +"a2.txt");//a duplicate file, same as "a"

		
		try {
			//dataHandler2 shares object "a"
			dataHandler2.dataHandler.putData("a", fileToByte(a));
			System.out.println("");
			dataHandler2.dataHandler.putData("b", fileToByte(b));
			System.out.println("");
			dataHandler3.dataHandler.putData("c", fileToByte(c));
			System.out.println("");
			//dataHandler 3 shares object "a2"
			//since this object is the same as "a", dataHandler
			//detects the duplicate
			dataHandler3.dataHandler.putData("a2", fileToByte(a2));
			System.out.println("");
			dataHandler1.dataHandler.putData("d", fileToByte(d));
			System.out.println("");
			dataHandler1.dataHandler.putData("e", fileToByte(e));
			System.out.println("");
			
											
			byte[] bo;
			
			//dataHandler1 retrieves remote data
			bo = dataHandler1.dataHandler.getData("a");			
			System.out.println("The object content is " + new String(bo));
			byteToFile(bo, "dataHandler1-a.txt");
			System.out.println("");
			//the second time dataHandler1 retrieves "a", a is cached locally
			bo = dataHandler1.dataHandler.getData("a");
			System.out.println("The object content is " + new String(bo));
			byteToFile(bo, "dataHandler1-a.txt");
			System.out.println("");
			//dataHandler3 retrieves "a", 
			//the object will be from "dataHandler1"
			bo = dataHandler3.dataHandler.getData("a");
			System.out.println("The object content is " + new String(bo));
			byteToFile(bo, "dataHandler3-a.txt");
			System.out.println("");
			bo = dataHandler1.dataHandler.getData("a");
			System.out.println("The object content is " + new String(bo));
			byteToFile(bo, "dataHandler1-a.txt");
			System.out.println("");
			bo = dataHandler1.dataHandler.getData("a");
			byteToFile(bo, "dataHandler1-a.txt");
			System.out.println("The object content is " + new String(bo));
			System.out.println("");
			bo = dataHandler1.dataHandler.getData("b");
			byteToFile(bo, "dataHandler1-b.txt");
			System.out.println("The object content is " + new String(bo));
			System.out.println("");
			bo = dataHandler1.dataHandler.getData("b");
			byteToFile(bo, "dataHandler1-b.txt");
			System.out.println("The object content is " + new String(bo));
			System.out.println("");
			bo = dataHandler1.dataHandler.getData("c");
			byteToFile(bo, "dataHandler1-c.txt");
			System.out.println("The object content is " + new String(bo));
			System.out.println("");
			
			//wait 10 seconds, "b" and "c" should be removed at dataHandler1 
			//since the cleanup interval is 5s
			System.out.println("Sleep 10 seconds...\n");
			Thread.sleep(10000);
			
			//dataHandler1 retrieves "b" again, 
			//this time it should retrieve it from remote peers 
			bo = dataHandler1.dataHandler.getData("b");
			System.out.println("The object content is " + new String(bo));
			byteToFile(bo, "dataHandler1-b.txt");
			System.out.println("");
									
		} catch (DataHandlerException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Throwable e1) {
			e1.printStackTrace();
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
