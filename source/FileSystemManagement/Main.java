package FileSystemManagement;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import FileSystemManagement.Service.*;
import FileSystemManagement.Gui.*;
import java.util.*;
import java.util.List;

public class Main{
	public static JFrame frame;

	public static String[] listArgs;

	public static void main(String args[]){

		// log option
		MyLog.isLog = true;
		listArgs = args;
		try{

			SwingUtilities.invokeAndWait(new Runnable(){
				public void run(){
					frame = new SimpleFrame("MyCommander");

				}
			});
		}
		catch(Exception ex){
			ex.printStackTrace();
		}

		// testCopy(args);
		// testDelete(args);
		// testCreate(args);
		// testRename(args);
	}

	public static void test(){
		// testView(listArgs);
		// testZip(listArgs);
		testCopy(listArgs);
	}

	public static void testCopy(String args[]){
		// create service
		IService service = FileService.service("copyfile");


		// set params
		List<Object> list = new ArrayList<Object>();
		for(String arg: args){
			list.add((Object)arg);	// filename
		}	
		

		// int rate = 1024 * 1024 * 10;
		// list.add((Object)rate);
		service.setParams(list);


		// service work
		int result = service.work();

		
		
		// display result
		MyLog.log((result == 0) ? "OK" : "FAIL");
	}

	public static void testDelete(String args[]){
		// create service
		IService service = FileService.service("delete");


		// set params
		List<Object> list = new ArrayList<Object>();
		for(String arg: args){
			list.add((Object)arg);
		}
		service.setParams(list);


		// service work
		int result = service.work();

		
		
		// display result
		MyLog.log((result == 0) ? "OK" : "FAIL");
	}

	public static void testCreate(String args[]){
		// create service
		IService service = FileService.service("createfile");


		// set params
		List<Object> list = new ArrayList<Object>();
		list.add((Object)args[0]);
		service.setParams(list);


		// service work
		int result = service.work();

		
		
		// display result
		MyLog.log((result == 0) ? "OK" : "FAIL");
	}

	public static void testCreateFolder(String args[]){
		// create service
		IService service = FileService.service("createfolder");


		// set params
		List<Object> list = new ArrayList<Object>();
		list.add((Object)args[0]);
		service.setParams(list);


		// service work
		int result = service.work();

		
		
		// display result
		MyLog.log((result == 0) ? "OK" : "FAIL");
	}

	public static void testRename(String args[]){
		// create service
		IService service = FileService.service("rename");


		// set params
		List<Object> list = new ArrayList<Object>();
		list.add((Object)args[0]);
		list.add((Object)args[1]);
		service.setParams(list);


		// service work
		int result = service.work();

		
		
		// display result
		MyLog.log((result == 0) ? "OK" : "FAIL");
	}

	public static void testZip(String args[]){
		// create service
		IService service = FileService.service("zipfile");


		// set params
		List<Object> list = new ArrayList<Object>();
		for(String arg: args){
			list.add((Object)arg);	// filename
		}	
		service.setParams(list);


		// service work
		int result = service.work();

		
		
		// display result
		MyLog.log((result == 0) ? "OK" : "FAIL");
	}

	public static void testUnzip(String args[]){
		// create service
		IService service = FileService.service("unzipfile");


		// set params
		List<Object> list = new ArrayList<Object>();
		for(String arg: args){
			list.add((Object)arg);	// filename
		}	
		service.setParams(list);


		// service work
		int result = service.work();

		
		
		// display result
		MyLog.log((result == 0) ? "OK" : "FAIL");
	}

	public static void testView(String args[]){
		// create service
		IService service = FileService.service("viewfile");


		// set params
		List<Object> list = new ArrayList<Object>();
		for(String arg: args){
			list.add((Object)arg);	// filename
		}	
		service.setParams(list);


		// service work
		int result = service.work();

		
		
		// display result
		MyLog.log((result == 0) ? "OK" : "FAIL");
	}



}