package FileSystemManagement.Service;



import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import FileSystemManagement.*;




public class CreateFileS extends FileService{

	public String src;
	private CreateFileO _observer;

	// config
	public boolean isGui = true;
	public boolean isConfirm = true;


	public CreateFileS(){

		_observer = new CreateFileO();
		if(isGui){
			_observer = new CreateFileO_Gui();
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
			MyLog.log(f.getAbsolutePath());

			// check if file exists
			if(f.exists()){
				// cancel, show error
				_observer.show("File name already exists. Please choose another file name.");
				result = 1;
			}
			else{
				_observer.doing();
				
				// create path if path not exists
				Files.createDirectories(Paths.get(f.getParent()));
				
				// create files
				f.createNewFile();
				result = 0;
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


class CreateFileO extends FileObserver{

	protected boolean isSuccess = false;

	@Override
	protected void init(){
		MyLog.log("Initializing Create file...");
	}

	protected int userConfirm(){
		int result = -1;
		try{
			MyLog.log(String.format(
				"The file %s is alread exists, Do you want to delete and create new file? (y/n)",
				this.src
				));


			Scanner scan = new Scanner(System.in);
			String choose = scan.nextLine();
			if(choose.startsWith("y")){
				result = 0;
			}
			else{
				result = 1;
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			result = 1;
		}

		

		return result;
	}

	@Override
	protected void doing(){
		MyLog.log("creating file...");

	}

	@Override
	protected void done(){
		MyLog.log("done creating file");
		super.done();
	}

	protected void show(String message){
		MyLog.log(message);
	}

}

class CreateFileO_Gui extends CreateFileO{

	protected JDialog dialog;
	protected JLabel label, subLabel;
	protected JProgressBar progress;
	protected JButton abortBtn;

	public void createDialog(){
		// create a dialog Box 
		dialog = new JDialog();
		dialog.setTitle("Create file");
		

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


	protected int userConfirm(){
		int result = -1;
		try{

			int a = JOptionPane.showConfirmDialog(null, String.format(
				"The file %s is alread exists, Do you want to delete and create new file?",
				this.src
				));  
			if(a == JOptionPane.YES_OPTION){  
				result = 0;
			}
			else{
				result = 1;
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			result = 1;
		}

		return result;

	}

	protected void show(String message){
		JOptionPane.showMessageDialog(null, message);
	}
}