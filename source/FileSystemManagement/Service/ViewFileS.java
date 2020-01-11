package FileSystemManagement.Service;



import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import FileSystemManagement.*;




public class ViewFileS extends FileService{

	public String src;
	public String encoding;
	private ViewFileO _observer;

	// config
	public boolean isGui = true;

	public ViewFileS(){

		_observer = new ViewFileO();
		if(isGui){
			_observer = new ViewFileO_Gui();
		}

		_observer.setFileService(this);
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
			_observer.doing();

			// file
			File f = new File(src);

			// check encoding
			boolean isUseEncoding = false;
			encoding = "";
			try{
				byte[] bytes = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
				if(bytes.length == 0){
					encoding = "UTF-8";
					isUseEncoding = false;
				}
				else if (bytes.length < 4){
					encoding = "UTF-8";
					isUseEncoding = false;
				}
				else if((bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB 
					&& (bytes[2] & 0xFF) == 0xBF)
				{

					encoding = "UTF-8";
				}
				else if((bytes[0] & 0xFE) == 0xFE && (bytes[1] & 0xFF) == 0xFF )
				{
					encoding = "UTF-16BE";
				}
				else if((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xFE )
				{

					encoding = "UTF-16LE";
				}
				else
				{
					encoding = "UTF-8";
					isUseEncoding = false;
				}

			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			MyLog.log(encoding);

			// show
			BufferedReader br = null;
			try
			{
				br = new BufferedReader(
					new InputStreamReader(
						new FileInputStream(f.getAbsolutePath()), encoding));


				if(isUseEncoding){
					br.skip(1);
				}

				String content = "";
				String line;
				while ((line = br.readLine()) != null) {
					content += line + "\n";
				}

				// show(content);
				_observer.display(content, encoding);
				result = 0;

			}
			catch (Exception ex)
			{
				ex.printStackTrace();
				result = 1;
			}
			finally{
				try{
					br.close();
				}
				catch(Exception ex){
					ex.printStackTrace();
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


class ViewFileO extends FileObserver{

	protected boolean isSuccess = false;

	@Override
	protected void init(){
		MyLog.log("Initializing view content file...");
	}


	@Override
	protected void doing(){
		MyLog.log("Reading file...");

	}

	@Override
	protected void done(){
		MyLog.log("done view content file");
		super.done();
	}

	protected void show(String message){
		MyLog.log(message);
	}

	protected void display(String content, String encoding){

		MyLog.log(encoding);
		MyLog.log(content);
	}

}

class ViewFileO_Gui extends ViewFileO{

	protected JDialog dialog;
	protected JTextArea area;
	protected JScrollPane scrollPane;

	public void createDialog(){
		// create a dialog Box 
		dialog = new JDialog();
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setTitle("View file");


		// creat area
		area = new JTextArea("");  
		area.setEditable(false);
		scrollPane = new JScrollPane(area);  


        // add area
		dialog.add(scrollPane); 

		// size and visible
		dialog.setMinimumSize(new Dimension(500, 500));
		dialog.pack();
		dialog.setVisible(true);
	}

	public void closeDialog(){
		if(dialog != null){
			dialog.dispose();
		}

	}




	@Override
	protected void init(){
		super.init();
		createDialog();
		// SwingUtilities.invokeLater(new Runnable() {
		// 	public void run() {
				
		// 	}
		// });
		
	}

	@Override
	protected void doing(){
		super.doing();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				dialog.setTitle("Reading " + src + "...");
			}
		});
		

	}

	@Override
	protected void done(){
		super.done();
		// closeDialog();
	}

	@Override
	protected void display(String content, String encoding){
		// super.display(content, encoding);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				dialog.setTitle(String.format(src + " - " + encoding));
				area.setText(content);
				area.setLineWrap(true);
			}
		});


	}


	protected void show(String message){
		JOptionPane.showMessageDialog(null, message);
	}
}