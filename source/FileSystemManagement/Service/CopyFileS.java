package FileSystemManagement.Service;



import java.util.*;
import java.util.List;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.nio.channels.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import FileSystemManagement.*;




public class CopyFileS extends FileService{

	public String[] src;
	public String dest;
	public boolean isAbort = false;
	private CopyFileO _observer;
	protected List<File> copiedFiles = new ArrayList<File>();
	protected boolean isCreateDir = false;

	// config
	public boolean isGui = true;

	public CopyFileS(){

		_observer = new CopyFileO();
		if(isGui){
			_observer = new CopyFileO_Gui();
		}
		
		this._observer.setFileService(this);

	}



	@Override
	public void setParams(java.util.List<Object> params){

		src = new String[params.size() - 1];
		for(int i = 0; i < params.size() - 1; i++){
			src[i] = (String)params.get(i);
		}
		dest = (String)params.get(params.size() - 1);

		// MyLog.log(params);
		this._observer.setSource(src);
		this._observer.setDest(dest);
	}

	@Override
	public int work() {

		// init
		_observer.init();
		int result = -1;

		// calculate total bytes to be copied
		int total = 0;
		for(String filename: src){
			File ftemp = new File(filename);
			if(ftemp.isFile()){
				total += calFileSize(ftemp);
			}
			else if(ftemp.isDirectory()){
				total += calFolderSize(ftemp);
			}

		}
		_observer.setTotal(total);


		// create absolute path dest
		File destDir = new File(dest);
		destDir = destDir.getAbsoluteFile();
		if(destDir.exists() == false){
			
			try{
				Files.createDirectories(Paths.get(destDir.getAbsolutePath()));
				copiedFiles.add(destDir);
			}
			catch(Exception ex){
				// ex.printStackTrace();
				_observer.show("Can't create destination folder");
			}
		}
		


		_observer.doing();
		result = 0;
		for(String filename: src){
			MyLog.log(filename);


			// get absolute file
			File f = new File(filename);
			f = f.getAbsoluteFile();
			

			if(f.isFile()){
				// copy file
				File output = new File(destDir + "/" + f.getName());
				int res = copyFile(f, output);
				if(res == 1)
					result = 1;
			}
			else if(f.isDirectory()){
				// copy folder
				File output = new File(destDir + "/" + f.getName());
				int res = copyFolder(f, output);
				if(res == 1)
					result = 1;
			}
			else{
				_observer.addFailFile(f.getName());
			}

			

			if(abort()){
				result = 1;
				break;
			}
		}

		// user abort
		if(abort()){
			MyLog.log("deleting...");

			// delete files unzipped
			for(File f: copiedFiles){

				if(f.isFile()){
					if(f.delete()){
						MyLog.log("Deleted " + f.getName() + " done.");
					}
					else{
						MyLog.log("Deleted " + f.getName() + " failed.");

					}
				}
			}

			// delete folders unzipped
			for(int i = copiedFiles.size() - 1; i >= 0; i--){
				File f = copiedFiles.get(i);
				if(f.isDirectory()){
					if(f.delete()){
						MyLog.log("Deleted " + f.getName() + " done.");
					}
					else{
						MyLog.log("Deleted " + f.getName() + " failed.");

					}
				}
			}

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


	public void abort(boolean iabort){
		this.isAbort = iabort;
	}

	public boolean abort(){
		return this.isAbort;
	}

	public int copyFile(File f, File output)
	{
		int result = -1;
		_observer.doing(f.getName());

		// check if source file exists
		if(f.exists()){

			// check if dest file exists
			String destFile = output.getAbsolutePath();
			File f2 = new File(destFile);



			// if dest exists, create 'dest-copy(1).txt'
			String temp = destFile;
			int count = 0;
			while(f2.exists()){
				int indexStartExt = destFile.lastIndexOf(".");
				if(indexStartExt == -1){
					// case file doesnot have extension
					// just ignore it
					indexStartExt = destFile.length();
				}

				temp = destFile.substring(0, indexStartExt) 
				+ String.format(" - copy(%d)", count++) 
				+ destFile.substring(indexStartExt);

				f2 = new File(temp);
				MyLog.log(f2.getAbsolutePath());
			}
			destFile = temp;
			f2 = f2.getAbsoluteFile();


			// create new dest file with fix size
			long length = 0;
			try(RandomAccessFile fout = new RandomAccessFile(destFile, "rw");
				){

				length = Files.size(Paths.get(f.getAbsolutePath()));
				fout.setLength(length);
				result = 0;
			}
			catch(Exception ex){
				ex.printStackTrace();
				result = 1;

			}

			if(result == 0){

				// create file success. add to copied list, later delete if abort
				copiedFiles.add(f2);

				// read/write to file
				long counter = 0;
				int numBytes = 0;
				byte[] block = new byte[super.rate];

				try(RandomAccessFile fin = new RandomAccessFile(f.getAbsolutePath(), "r");
					RandomAccessFile fout = new RandomAccessFile(f2.getAbsolutePath(), "rw");
					)
				{

					fout.seek(0);
					while( !abort() && ( (numBytes = fin.read(block)) != -1)) {
						counter += numBytes;
						fout.write(block, 0, numBytes);
						_observer.addCurrent(numBytes);			// _observer
					}

					if(!abort()){
						//copy attribute
						DosFileAttributes attributes = Files.readAttributes(
							Paths.get(f.getAbsolutePath()),
							DosFileAttributes.class
							);
						Files.setAttribute(Paths.get(f2.getAbsolutePath()),
							"dos:readonly",
							attributes.isReadOnly()
							);
						Files.setAttribute(Paths.get(f2.getAbsolutePath()),
							"dos:hidden",
							attributes.isHidden()
							);

						// add to success list
						_observer.done(f.getName());
						result = 0;
					}
					else{
						result = 1;
					}

				}
				catch(Exception e){
					// e.printStackTrace();
					// show error io
					_observer.addFailFile(f.getName());
					_observer.show(String.format("Can't read/write from '%s' to '%s'", 
						f.getName(), 
						f2.getName()
						)
					);
					result = 1;
				}

				
			}	
			else{
				// error create file failed
				_observer.show(String.format("Can't create file '%s'", f2.getAbsolutePath()));
				_observer.addFailFile(f.getName());
			}
		}
		else{
			// cancel, add to error list: source file not exists
			_observer.show(String.format("File '%s' doesn't exist", f.getName()));
			_observer.addFailFile(f.getName());
		}

		return result;
	}

	public int copyFolder(File folder, File output)
	{
		int result = 0;
		_observer.doing(folder.getName());

		// create folder dest
		// if folder alread exists, create 'folder-copy(1)'
		String temp = output.getAbsolutePath();
		while(output.exists()){

			temp += String.format(" - copy");

			output = new File(temp);
			MyLog.log(output.getAbsolutePath());
		}
		output = new File(temp);
		output = output.getAbsoluteFile();
		output.mkdir();
		copiedFiles.add(output);
		

		// copy contents in it
		String[] listFiles = folder.list();
		for(String filename : listFiles){
			File f = new File(folder.getAbsolutePath() + "/" + filename);
			f = f.getAbsoluteFile();

			if(f.isFile()){
				// file
				File output2 = new File(output.getAbsolutePath() + "/" + f.getName());
				int res = copyFile(f, output2);
				if(res == 1){
					result = 1;
				}
			}
			else if (f.isDirectory()){
				// folder
				File output2 = new File(output.getAbsolutePath() + "/" + f.getName());
				int res = copyFolder(f, output2);
				if(res == 1){
					result = 1;
				}
			}

			if(abort()){
				result = 1;
				break;
			}
		}

		return result;
	}

	public long calFileSize(File f){
		return f.length();
	}

	public long calFolderSize(File folder){

		long total = 0;

		String[] listFiles = folder.list();
		for(String filename : listFiles){

			File f = new File(folder.getAbsolutePath() + "/" + filename);
			f = f.getAbsoluteFile();

			if(f.isFile()){
				// file
				total += calFileSize(f);
			}
			else if (f.isDirectory()){
				// folder
				total += calFolderSize(f);
			}

			
		}

		return total;
	}
}




class CopyFileO extends FileObserver{

	protected boolean isSuccess = false;
	protected CopyFileS fservice;
	protected List<String> sources;
	protected List<String> failList;
	protected List<String> successList;
	protected List<String> skipList;

	protected long total, current;
	protected int oldPercent = 0;


	protected void setSource(String[] sources){
		this.sources = Arrays.asList(sources);
		this.failList = new ArrayList<String>();
		this.successList = new ArrayList<String>();
		this.skipList = new ArrayList<String>();
	}

	public void addFailFile(String filename){

		failList.add(filename);
		MyLog.log(String.format("Copy file %s failed.", filename));
	}

	public void addSkipFile(String filename){

		skipList.add(filename);
		MyLog.log(String.format("Skip file %s", filename));
	}

	protected void doing(String filename){
		MyLog.log("copying file " + filename);
	}

	protected void done(String filename){
		successList.add(filename);
		MyLog.log("done copying file " + filename);
	}

	@Override
	protected void init(){
		MyLog.log("Initializing copy...");

	}

	@Override
	protected void doing(){
		MyLog.log(String.format("Copying %d file(s)", sources.size()));

	}

	@Override
	protected void done(){
		MyLog.log("Done copying file");
		MyLog.log(String.format("Total: %d, copied: %d, failed/skip: %d, aborted: %b", 
			sources.size(),
			successList.size(),
			failList.size() + skipList.size(),
			fservice.abort()
			)
		);
		super.done();
	}


	protected void show(String message){
		MyLog.log(message);
	}



	public void setTotal(long total){
		this.total = total;

	}

	public void addCurrent(long cur){
		this.current += cur;
		int percent = (int)Math.round((double)current/total * 100
			);

		if(percent - oldPercent >= 1){
			oldPercent = percent;
			update();

		}


	}

	protected void update(){
		
	}

	@Override
	public void setFileService(FileService fs){
		this.fservice = (CopyFileS)fs;
	}

}

class CopyFileO_Gui extends CopyFileO{

	
	protected JDialog dialog;
	protected JLabel label, subLabel;
	protected JProgressBar progress;
	protected JButton abortBtn;

	public void createDialog(){
		// create a dialog Box 
		dialog = new JDialog();//(Main.frame, "dialog Box"); 
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setTitle("Copy file");
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {

				try{
					dialog.setTitle(dialog.getTitle() + " - Closing...");
					fservice.abort(true);

				}
				catch(Exception ex){
					ex.printStackTrace();
				}


			}
		});

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
		abortBtn.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e){
				try{
					dialog.setTitle(dialog.getTitle() + " - Aborting...");
					fservice.abort(true);

				}
				catch(Exception ex){
					ex.printStackTrace();
				}
			}
		});

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
	protected void update(){

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progress.setValue(oldPercent);
				subLabel.setText(currentPerTotalString());
			}
		});
	}

	@Override
	protected void init(){

		createDialog();
		abortBtn.setEnabled(false);
		label.setText("initializing...");
		progress.setValue(0);
	}

	@Override
	protected void doing(){
		super.doing();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				abortBtn.setEnabled(true);
				String text = String.format("Copying %d file(s)", sources.size());
				dialog.setTitle(text);
				// subLabel.setText(currentPerTotalString());
			}
		});
	}



	@Override
	protected void done(){
		super.done();

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				abortBtn.setEnabled(false);
				label.setText("Done");
				closeDialog();
			}
		});


	}

	@Override
	protected void doing(String filename){

		super.doing(filename);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				label.setText("copying file " + filename);
			}
		});
	}

	@Override
	protected void done(String filename){
		super.done(filename);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				label.setText("done copying file " + filename);

				// int percent = successList.size() / sources.size() * 100;
				// progress.setValue(percent);
			}
		});
	}



	private String currentPerTotalString(){
		String text2 = (new Long(this.current)).toString() + 
		" / " + 
		(new Long(this.total)).toString() +
		" Bytes";
		return text2;
	}

	


	@Override
	protected void show(String message){
		Thread t = new Thread(new Runnable(){
			public void run(){
				JOptionPane.showMessageDialog(null, message,
					String.format("Error copy file"),
					JOptionPane.WARNING_MESSAGE
					); 

			}
		});
		t.start();
	}

	@Override
	public void addCurrent(long cur){
		super.addCurrent(cur);


		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				subLabel.setText(currentPerTotalString());
			}
		});

	}


}
