package FileSystemManagement.Service;

import java.util.*;
import java.util.concurrent.*;

import FileSystemManagement.*;



public abstract class FileObserver{


	protected String src, dest;
	


	public FileObserver(){

	}
	
	

	public void setSource(String src){
		this.src = src;
	}

	public void setDest(String dest){
		this.dest = dest;
	}

	public void setFileService(FileService fs){
	
	}
	

	// service calls init
	protected void init(){
	}

	// service calls doing
	protected void doing(){
	}

	// service calls done
	protected void done(){

	}


	

}
