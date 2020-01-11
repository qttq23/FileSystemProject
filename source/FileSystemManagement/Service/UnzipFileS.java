package FileSystemManagement.Service;



import java.util.*;
import java.util.List;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.nio.channels.*;
import java.util.zip.*;
import java.util.zip.ZipFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import FileSystemManagement.*;




public class UnzipFileS extends FileService{

	public String src;
	public String dest;
	public ZipInputStream zi;
	public final int BUFFER_SIZE = 1024*1024*10;
	public long totalSizeForUnzip = 0;
	public long currentSizeForUnzip = 0;
	public List<String> unzippedList = new ArrayList<String>();

	public boolean isAbort = false;
	private UnzipFileO _observer;


	// config
	public boolean isGui = true;

	public UnzipFileS(){

		_observer = new UnzipFileO();
		if(isGui){
			_observer = new UnzipFileO_Gui();
		}
		
		this._observer.setFileService(this);

	}

	@Override
	public void setParams(java.util.List<Object> params){

		src = (String)params.get(0);
		dest = (String)params.get(1);

		// MyLog.log(params);
		this._observer.setSource(new String[]{src});
		this._observer.setDest(dest);
	}

	@Override
	public int work() {

		int result = 0;
		try {
			// init
			_observer.init();
			String target = src;
			String output = dest;
			
			// make output directory
			File file = new File(output);
			if (!file.exists()) {
				file.mkdirs();
				unzippedList.add(output);
			}

			// calculate total unzip size
			totalSizeForUnzip = calcSizeUnzip(target);
			_observer.setTotal(totalSizeForUnzip);


        	// iterates over entries in the zip file
			_observer.doing();
			zi = new ZipInputStream(new FileInputStream(target));
			ZipEntry entry = zi.getNextEntry();

			while (!abort() && entry != null) {
				String filePath = output + File.separator + entry.getName();
				_observer.doing(filePath);

				if (!entry.isDirectory()) {
                	// if the entry is a file, extracts it
					int res = unzipFile(filePath);
					if(res == 1){
						// fail
						result = 1;
						_observer.addFailFile(entry.getName());
					}
					else{
						// success
						_observer.done(entry.getName());
					}
					unzippedList.add(filePath);
				} else {
                	// if the entry is a directory, make the directory
					File dir = new File(filePath);
					if(!dir.exists()){
						dir.mkdir();

						// success
						unzippedList.add(filePath);
						_observer.done(entry.getName());
					}

				}
				zi.closeEntry();
				_observer.done(filePath);

				entry = zi.getNextEntry();
			}


		} catch (FileNotFoundException ex) {

			_observer.show("a file does not exist");
			result = 1;
		} catch (IOException ex) {
			_observer.show("I/O error: " + ex);
			result = 1;
		}
		finally{
			try{
				zi.closeEntry();
				MyLog.log("close entries");
			}
			catch(Exception ex){
				ex.printStackTrace();
			}

			try{
				zi.close();
				MyLog.log("close zip file");
			}
			catch(Exception ex){
				ex.printStackTrace();
			}

		}

		// user abort...
		if(abort()){

			// delete files unzipped
			for(String filename: unzippedList){

				File f = new File(filename);
				if(f.isFile()){
					if(f.delete()){
						MyLog.log("Deleted " + filename + " done.");
					}
					else{
						MyLog.log("Deleted " + filename + " failed.");

					}
				}
			}

			// delete folders unzipped
			for(int i = unzippedList.size() - 1; i >= 0; i--){
				File f = new File((String)unzippedList.get(i));
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

	public int unzipFile(String filePath) {

		int result = -1;
		
		
		RandomAccessFile fout = null;
		try
		{
			fout = new RandomAccessFile(filePath, "rw");
			byte[] buffer = new byte[BUFFER_SIZE];
			int numBytes = 0;
			while(!abort() && (numBytes = zi.read(buffer)) != -1
				)
			{
				fout.write(buffer, 0, numBytes);

				// show percent
				currentSizeForUnzip += numBytes;
				_observer.addCurrent(numBytes);

			}

			result = 0;
			if(abort()){
				result = 1;
			}
		}
		catch(Exception ex){
			result = 1;
		}
		finally{
			try{
				fout.close();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}

		}


		return result;
	}

	// calculate size unzip
	public long calcSizeUnzip(String target) throws IOException{

		long total = 0;

		// iterates over entries in the zip file
		ZipFile zipfile = new ZipFile(new File(target)); 
		java.util.Enumeration zipEnum = zipfile.entries();
		while (zipEnum.hasMoreElements ()){

			ZipEntry entry = (ZipEntry) zipEnum.nextElement(); 

			if(!entry.isDirectory()){
				total += entry.getSize();
			}

		}

		return total;
	}
}




class UnzipFileO extends FileObserver{

	protected boolean isSuccess = false;
	protected UnzipFileS fservice;
	protected List<String> sources;
	protected List<String> failList;
	protected List<String> successList;
	protected List<String> skipList;
	protected String dest;

	protected long total, current;
	protected int oldPercent = 0;


	public UnzipFileO(){
		this.failList = new ArrayList<String>();
		this.successList = new ArrayList<String>();
		this.skipList = new ArrayList<String>();
	}

	protected void setSource(String[] sources){
		this.sources = Arrays.asList(sources);

	}

	@Override
	public void setDest(String dest){
		this.dest = dest;
	}

	public void addFailFile(String filename){

		failList.add(filename);
		MyLog.log(String.format("UnZip file %s failed.", filename));
	}


	protected void doing(String filename){
		MyLog.log("unzipping file " + filename);
	}

	protected void done(String filename){
		successList.add(filename);
		MyLog.log("done unzipping file " + filename);
	}

	@Override
	protected void init(){
		MyLog.log("Initializing unzip...");

	}

	@Override
	protected void doing(){
		MyLog.log(String.format("unzipping %d file(s)", sources.size()));

	}

	@Override
	protected void done(){
		MyLog.log("Done unzipping file");
		MyLog.log(String.format("Total: %d, unzipped: %d, failed/skip: %d, aborted: %b", 
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
		this.fservice = (UnzipFileS)fs;
	}

}

class UnzipFileO_Gui extends UnzipFileO{


	protected JDialog dialog;
	protected JLabel label, subLabel;
	protected JProgressBar progress;
	protected JButton abortBtn;

	public void createDialog(){
		// create a dialog Box 
		dialog = new JDialog();
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setTitle("Unzip file");
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
				String text = String.format("Unzipping %d file(s)", sources.size());
				dialog.setTitle(text);
				dialog.setTitle("Unzip file - output: " + dest);

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

				label.setText("Unzipping file " + filename);
			}
		});
	}

	@Override
	protected void done(String filename){
		super.done(filename);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				label.setText("done unzipping file " + filename);

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
					String.format("Error unzip file"),
					JOptionPane.WARNING_MESSAGE
					); 

			}
		});
		t.start();
	}




}
