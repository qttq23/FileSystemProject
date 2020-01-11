package FileSystemManagement.Service;



import java.util.*;


public abstract class FileService implements IService{
	
	public int rate = DEFAULT_READWRITERATE;
	public static int DEFAULT_READWRITERATE = 1024*1024*10;

	
	public FileObserver observer;
	// public Thread thread;


	public void setParams(List<Object> params){}

	public int work(){return 0;}

	public static IService service(String serviceName){
		IService result = null;
		if(serviceName.toLowerCase().equals("copyfile")){
			result = new CopyFileS();
		}
		else if(serviceName.toLowerCase().equals("delete")){
			result = new DeleteS();
		}
		else if(serviceName.toLowerCase().equals("createfile")){
			result = new CreateFileS();
		}
		else if(serviceName.toLowerCase().equals("createfolder")){
			result = new CreateFolderS();
		}
		else if(serviceName.toLowerCase().equals("rename")){
			result = new RenameS();
		}
		else if(serviceName.toLowerCase().equals("zipfile")){
			result = new ZipFileS();
		}
		else if(serviceName.toLowerCase().equals("unzipfile")){
			result = new UnzipFileS();
		}
		else if(serviceName.toLowerCase().equals("viewfile")){
			result = new ViewFileS();
		}

		return result;
	}


	

}