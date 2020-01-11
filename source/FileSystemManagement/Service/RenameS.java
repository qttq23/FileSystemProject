package FileSystemManagement.Service;



import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import FileSystemManagement.*;




public class RenameS extends FileService{

	public String src;
	public String dest;
	private RenameO _observer;
	public boolean isAbort = false;

	// config
	public boolean isGui = true;
	public boolean isConfirm = true;


	public RenameS(){

		_observer = new RenameO();
		if(isGui){
			_observer = new RenameO_Gui();
		}

		_observer.setFileService(this);
		this.observer = observer;

	}



	@Override
	public void setParams(java.util.List<Object> params){
		src = (String)params.get(0);
		dest = (String)params.get(1);

		_observer.setSource(src);
		_observer.setDest(dest);

	}

	@Override
	public int work(){
		// init
		_observer.init();
		int result = -1;

		try{
			// get absolute path
			File f = new File(src);
			f = f.getAbsoluteFile();

			// check src exists
			if(f.exists()){
				// get absolute dest
				File f2 = new File(dest);
				f2 = f2.getAbsoluteFile();

				

				// check if dest exists
				if(f2.exists()){
					// cancel
					_observer.show("File destination already exists. Please choose another file name.");
					result = 1;
				}
				else{
					_observer.doing();
					
					// create path dest if not exist
					Files.createDirectories(Paths.get(f2.getParent()));

					// move src to dest
					boolean isOk = f.renameTo(f2);
					if(isOk){
						result = 0;
					}
					else{
						_observer.show("File source is being opened. Please close it before renaming.");
						result = 1;
					}
				}
			}
			else{
				// cancel
				_observer.show("File source doesn't exist. Please choose another file.");
				result = 1;
			}

		}
		catch(Exception ex){
			ex.printStackTrace();
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


}


class RenameO extends FileObserver{

	protected boolean isSuccess = false;


	@Override
	protected void init(){
		MyLog.log("Initializing Rename file...");
	}



	@Override
	protected void doing(){
		MyLog.log("renaming file...");

	}

	@Override
	protected void done(){
		MyLog.log("done renaming file");
		super.done();
	}

	protected void show(String message){
		MyLog.log(message);
	}

}

class RenameO_Gui extends RenameO{

	protected JDialog dialog;
	protected JLabel label, subLabel;
	protected JProgressBar progress;
	protected JButton abortBtn;

	public void createDialog(){
		// create a dialog Box 
		dialog = new JDialog();
		dialog.setTitle("Rename file");
		

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
		

	}

	@Override
	protected void doing(){
		
		

	}

	@Override
	protected void done(){

		if(isSuccess){

			try{
				for(int i = 0; i < 100; i++){
					Thread.sleep(3);
					progress.setValue(i);
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
	protected void show(String message){
		JOptionPane.showMessageDialog(null, message,
			String.format("Error rename '%s' to '%s'", src, dest),
			JOptionPane.WARNING_MESSAGE
			); 
	}
}