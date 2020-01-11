package FileSystemManagement.Service;



import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import FileSystemManagement.*;




public class CreateFolderS extends FileService{

	public String src;
	private CreateFolderO _observer;

	// config
	public boolean isGui = true;
	public boolean isConfirm = true;


	public CreateFolderS(){

		_observer = new CreateFolderO();
		if(isGui){
			_observer = new CreateFolderO_Gui();
		}

		_observer.setFileService(this);
		this.observer = observer;

	}



	@Override
	public void setParams(java.util.List<Object> params){
		src = (String)params.get(0);

		_observer.setSource(src);

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

			// check if file exists
			if(f.exists()){
				// cancel, show error
				_observer.show("File name already exists. Please choose another file name.");
				result = 1;
			}
			else
			{
				_observer.doing();
				

				// create folders
				boolean isSuc = f.mkdirs();
				if(isSuc){
					result = 0;
				}
				else{
					result = 1;
				}
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
		_observer.done();				// observer
		
		return result;
	}


}


class CreateFolderO extends FileObserver{

	protected boolean isSuccess = false;

	@Override
	protected void init(){
		MyLog.log("Initializing Create folder...");
	}


	@Override
	protected void doing(){
		MyLog.log("creating folder...");

	}

	@Override
	protected void done(){
		MyLog.log("done creating folder");
		super.done();
	}

	protected void show(String message){
		MyLog.log(message);
	}

}

class CreateFolderO_Gui extends CreateFolderO{

	protected JDialog dialog;
	protected JLabel label, subLabel;
	protected JProgressBar progress;
	protected JButton abortBtn;

	public void createDialog(){
		// create a dialog Box 
		dialog = new JDialog();
		dialog.setTitle("Create folder");
		

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
		label.setText("initializing...");
		

	}

	@Override
	protected void doing(){
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				String text = "Creating '" + src + "' ...";

				label.setText(text);
			}
		});
		

	}

	@Override
	protected void done(){

		if(isSuccess){

			// try{
			// 	for(int i = 0; i < 100; i++){
			// 		Thread.sleep(3);
			// 		progress.setValue(i);
			// 	}

			// }
			// catch(Exception ex){
			// 	ex.printStackTrace();
			// }	
			
		}

		label.setText("Done");
		closeDialog();
	}



	protected void show(String message){
		JOptionPane.showMessageDialog(null, message);
	}
}



