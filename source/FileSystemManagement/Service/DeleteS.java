package FileSystemManagement.Service;



import java.util.*;
import java.util.List;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import FileSystemManagement.*;




public class DeleteS extends FileService{

	public String[] src;
	private DeleteFileO _observer;

	// config
	public boolean isConfirm = true;
	public boolean isGui = true;



	public DeleteS(){

		_observer = new DeleteFileO();
		if(isGui){
			_observer = new DeleteFileO_Gui();
		}

		_observer.setFileService(this);
		this.observer = observer;

	}



	@Override
	public void setParams(java.util.List<Object> params){
		src = new String[params.size()];
		for(int i = 0; i < params.size(); i++){
			src[i] = (String)params.get(i);
		}

		_observer.setSource(src);

	}

	@Override
	public int work(){

		// init
		_observer.init();
		int result = -1;

		// confirm
		boolean isDelete = _observer.userConfirm();
		if(isDelete){


			for(String filename : src){
				// get absolute path
				File f = new File(filename);
				f = f.getAbsoluteFile();

				// check if exists and not open
				if(f.exists() && f.renameTo(f)==true){

					

					// delete
					if(f.isFile()){
						// file
						int res = deleteFile(f);

					}
					else if(f.isDirectory()){
						// folder
						int res = deleteFolder(f);
	
					}
					
				}	
				else{
					// ignore and add to error message
					_observer.show(String.format("File '%s' doesn't exist or being opened. Delete failed.",
						f.getName())
					);
					_observer.addFailFile(f.getName());
					result = 1;
				}

			}

			result = 0;
			if(_observer.numFailFile() > 0){
				result = 1;
			}

		}
		else{
			// cancel
			result = 1;
		}

		

		// done
		_observer.isSuccess = false;
		if(result == 0){
			_observer.isSuccess = true;
		}
		_observer.done();
		
		return result;
	}

	public int deleteFile(File f){

		_observer.doing(f.getName());

		boolean isDeleted = f.delete();
		if(!isDeleted){
			// ignore and add to error message
			_observer.addFailFile(f.getName());
			return 1;
		}
		else{
			_observer.done(f.getName());
			return 0;
		}
	}

	public int deleteFolder(File folder){
		_observer.doing(folder.getName());
		
		String[] listFiles = folder.list();
		for(String filename : listFiles){
			File f = new File(folder.getAbsolutePath() + "/" + filename);
			f = f.getAbsoluteFile();

			if(f.isFile()){
				// file
				deleteFile(f);

			}
			else if (f.isDirectory()){
				// folder
				deleteFolder(f);
			}
		}

		// finally delete folder itself
		if(folder.delete()){
			_observer.done(folder.getName());
			return 0;
		}
		else{
			_observer.addFailFile(folder.getName());
			return 1;
		}
	}


}


class DeleteFileO extends FileObserver{

	protected boolean isSuccess = false;
	protected List<String> sources;
	protected List<String> failList;
	protected List<String> successList;

	protected void setSource(String[] sources){
		this.sources = new ArrayList<String>();
		this.failList = new ArrayList<String>();
		this.successList = new ArrayList<String>();


		// reduce sources, absolute path -> relative apth
		for(int i = 0; i < sources.length; i++){
			this.sources.add(new File(sources[i]).getName()
				);
		}
	}

	public void addFailFile(String filename){

		failList.add(filename);
		MyLog.log(String.format("Delete file %s failed.", filename));
	}

	public int numFailFile(){
		return failList.size();
	}

	@Override
	protected void init(){
		MyLog.log("Initializing delete...");
	}

	protected boolean userConfirm(){
		boolean result = false;
		try{
			MyLog.log(String.format(
				"Are you sure to delete '%s'? (y/n)",
				this.sources.toString()
				));


			Scanner scan = new Scanner(System.in);
			String choose = scan.nextLine();
			if(choose.toLowerCase().startsWith("y")){
				result = true;
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			result = false;
		}

		

		return result;
	}

	protected void doing(String filename){
		MyLog.log("deleting file " + filename);
	}

	protected void done(String filename){
		successList.add(filename);
		MyLog.log("done deleting file " + filename);
	}

	@Override
	protected void doing(){
		MyLog.log("deleting file...");

	}

	@Override
	protected void done(){
		MyLog.log("done deleting file");
		MyLog.log(String.format("Total: %d, deleted: %d, failed: %d", 
			sources.size(),
			successList.size(),
			failList.size()
			)
		);
		super.done();
	}

	protected void show(String message){
		MyLog.log(message);
	}

}

class DeleteFileO_Gui extends DeleteFileO{
	
	protected JDialog dialog;
	protected JLabel label, subLabel;
	protected JProgressBar progress;
	protected JButton abortBtn;

	public void createDialog(){
		// create a dialog Box 
		dialog = new JDialog();
		dialog.setTitle("Delete file");
		

		// create a panel 
		JPanel p = new JPanel(); 
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		// create a label 
		label = new JLabel(""); 
		subLabel = new JLabel(""); 

        // create a progressbar 
		progress = new JProgressBar(); 
		progress.setMinimum(0);
		progress.setMaximum(100);

        // set initial value 
		progress.setValue(0); 
		
		progress.setStringPainted(true); 
		

		// buttons
		abortBtn = new JButton("Abort");
		abortBtn.setEnabled(false);

        // add progressbar 
		p.add(label);
		p.add(subLabel);
		p.add(progress); 
		p.add(abortBtn);


		dialog.add(p); 

        // setsize of dialog 
		dialog.setSize(new Dimension(400,200));

        // set visibility of dialog 
		dialog.setVisible(true); 
	}

	public void closeDialog(){
		if(dialog != null){
			dialog.dispose();
		}

	}


	@Override
	protected void init(){
		createDialog();
		label.setText("Deleting...");
	}

	@Override
	protected void doing(){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				String text = String.format("Deleting %d file(s)", sources.size());
				dialog.setTitle(text);
			}
		});
	}

	

	@Override
	protected void done(){

		super.done();

		if(isSuccess){

			try{
				for(int i = 0; i < 2; i++){

					Thread.sleep(100);
					progress.setValue((i + 1) * 50);
				}

			}
			catch(Exception ex){
				ex.printStackTrace();
			}

		}

		label.setText("Done");
		closeDialog();


	}

	@Override
	protected boolean userConfirm(){
		boolean result = false;
		try{

			int a = JOptionPane.showConfirmDialog(dialog, String.format(
				"Are you sure to delete '%s'?",
				this.sources.toString()
				));  
			if(a == JOptionPane.YES_OPTION){  
				result = true;
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			result = false;
		}

		return result;

	}

	@Override
	protected void show(String message){
		Thread t = new Thread(new Runnable(){
			public void run(){
				JOptionPane.showMessageDialog(null, message,
					String.format("Error delete file"),
					JOptionPane.WARNING_MESSAGE
					); 

			}
		});
		t.start();
	}


	@Override
	protected void doing(String filename){

		super.doing(filename);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				label.setText("deleting file " + filename);
			}
		});
	}

	@Override
	protected void done(String filename){
		super.done(filename);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				label.setText("done deleting file " + filename);

				int percent = successList.size() / sources.size() * 100;
				progress.setValue(percent);
			}
		});
	}


}