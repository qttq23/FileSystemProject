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




public class ZipFileS extends FileService{

	public String[] src;
	public String dest;
	public ZipOutputStream zos;
	public final int BUFFER_SIZE = 1024*1024*10;
	public long totalSizeForZip = 0;
	public long currentSizeForZip = 0;

	public boolean isAbort = false;
	private ZipFileO _observer;


	// config
	public boolean isGui = true;

	public ZipFileS(){

		_observer = new ZipFileO();
		if(isGui){
			_observer = new ZipFileO_Gui();
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

		int result = 0;
		try {
			// init
			_observer.init();
			
			// form dest string, ex: 'something.zip'
			File file = new File(dest);

			// check if dest file already exist, if form new filename
			int count = 1;
			String temp = dest;
			while(file.exists()){
				temp = dest.substring(0, dest.lastIndexOf(".")) + String.format("(%d)", count++) + ".zip";
				file = new File(temp);
				MyLog.log(file.getAbsolutePath());
			}
			dest = temp;
			String dirFromZip = file.getName().substring(0, file.getName().lastIndexOf("."));	// ==> something
			file = file.getAbsoluteFile();


			// create dest file and neccessary folders
			(new File(file.getParent())).mkdirs();
			zos = new ZipOutputStream(new FileOutputStream(dest));

			// create default folder entry
			zos.putNextEntry(
				new ZipEntry(
					dirFromZip + "/"
					)
				);

			// calculate total size
			totalSizeForZip = calcSizeZip(src);
			_observer.setTotal(totalSizeForZip);
			// MyLog.log("Total size to zip: " + totalSizeForZip + " Byte(s)");

			// zip each file/folder
			_observer.doing();
			String[] listFiles = src;
			for(String filename : listFiles){

				if(abort()){
					break;
				}

				_observer.doing(filename);
				File f = new File(filename);
				if(f.isFile()){
					// file
					// MyLog.log(f.getName() + " is file");
					int res = zipFile( f.getAbsolutePath(), dirFromZip + "/" + f.getName());
					if(res == 1){
						result = 1;
						_observer.addFailFile(filename);
					}
					else{
						_observer.done(filename);
					}
				}
				else if (f.isDirectory()){
					// folder
					// MyLog.log(f.getName() + " is folder");
					int res = zipFolder( f.getAbsolutePath(), dirFromZip + "/" + f.getName());
					if(res == 1)
						result = 1;
					else{
						_observer.done(filename);
					}
				}
			}


			


		} catch (FileNotFoundException ex) {
			// System.err.format("a file does not exist");
			_observer.show("a file does not exist");
			result = 1;
		} catch (IOException ex) {
			_observer.show("I/O error: " + ex);
			result = 1;
		}
		finally{
			try{
				zos.closeEntry();
				zos.close();
				MyLog.log("close entries and zip file.");
			}
			catch(Exception ex){
				ex.printStackTrace();
			}

		}

		if(abort()){
			// delete zipped files
			MyLog.log("Deleting zipped file...");
			File delTarget = new File(dest);
			if(delTarget.delete()){
				MyLog.log("Delete done");
			}
			else{
				MyLog.log("Delete failed");
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

	public int zipFile(String target, String output) {

		MyLog.log("zipFile, target: " + target + ", output: " + output);
		int result = -1;
		_observer.doing(new File(target).getName());
		RandomAccessFile fin = null;
		try {

			zos.putNextEntry(new ZipEntry(output));

			fin = new RandomAccessFile(target, "r");
			byte[] buffer = new byte[BUFFER_SIZE];
			int numBytes = 0;
			while(!abort() && (numBytes = fin.read(buffer)) != -1
				)
			{
				zos.write(buffer, 0, numBytes);

				// show percent
				_observer.addCurrent(numBytes);

			}

			result = 0;

		} catch (FileNotFoundException ex) {
			System.err.format("The file %s does not exist", target);
			_observer.addFailFile(target);
			result = 1;
		} catch (IOException ex) {
			System.err.println("I/O error: " + ex);
			_observer.addFailFile(target);
			result = 1;
		}
		finally{
			try{
				fin.close();
				zos.closeEntry();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}

		}

		return result;
	}

	public int zipFolder(String target, String output){
		MyLog.log("zipFolder, target: " + target + ", output: " + output);
		try {
			// create default folder entry
			zos.putNextEntry(
				new ZipEntry(
					output + "/"
					)
				);


			File file = new File(target);
			// String dirFromZip = filePath;

			// zip each file/folder
			File[] listFiles = file.listFiles();
			for(File f : listFiles){
				if(abort()){
					break;
				}

				if(f.isFile()){
					// file
					zipFile(target + "/" + f.getName(), output + "/" + f.getName());

				}
				else if (f.isDirectory()){
					// folder
					zipFolder(target + "/" + f.getName(), output + "/" + f.getName());
				}
			}

		} catch (FileNotFoundException ex) {
			System.err.format("The file %s does not exist", target);
			return 1;
		} catch (IOException ex) {
			System.err.println("I/O error: " + ex);
			return 1;
		}
		finally{
			try{

				zos.closeEntry();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}

		}

		return 0;
	}

	// calculate size
	public long calcSizeZip(String[] listFiles){

		long total = 0;

		// zip each file/folder
		for(String filename : listFiles){
			File f = new File(filename);
			f = f.getAbsoluteFile();


			if(f.isFile()){
				// file
				// get size
				total += f.length();

			}
			else if (f.isDirectory()){
				// folder

				String[] list = f.list();
				for(int i = 0; i < list.length; i++){
					list[i] = f.getAbsolutePath() + "\\" + list[i];
				}
				total += calcSizeZip(list);

			}
		}

		return total;
	}
}




class ZipFileO extends FileObserver{

	protected boolean isSuccess = false;
	protected ZipFileS fservice;
	protected List<String> sources;
	protected List<String> failList;
	protected List<String> successList;
	protected List<String> skipList;
	protected String dest;

	protected long total, current;
	protected int oldPercent = 0;


	protected void setSource(String[] sources){
		this.sources = Arrays.asList(sources);
		this.failList = new ArrayList<String>();
		this.successList = new ArrayList<String>();
		this.skipList = new ArrayList<String>();
	}

	@Override
	public void setDest(String dest){
		this.dest = dest;
	}

	public void addFailFile(String filename){

		failList.add(filename);
		MyLog.log(String.format("Zip file %s failed.", filename));
	}


	protected void doing(String filename){
		MyLog.log("zipping file " + filename);
	}

	protected void done(String filename){
		successList.add(filename);
		MyLog.log("done zipping file " + filename);
	}

	@Override
	protected void init(){
		MyLog.log("Initializing zip...");

	}

	@Override
	protected void doing(){
		MyLog.log(String.format("zipping %d file(s)", sources.size()));

	}

	@Override
	protected void done(){
		MyLog.log("Done zipping file");
		MyLog.log(String.format("Total: %d, zipped: %d, failed/skip: %d, aborted: %b", 
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
		this.fservice = (ZipFileS)fs;
	}

}

class ZipFileO_Gui extends ZipFileO{


	protected JDialog dialog;
	protected JLabel label, subLabel;
	protected JProgressBar progress;
	protected JButton abortBtn;

	public void createDialog(){
		// create a dialog Box 
		dialog = new JDialog();
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		dialog.setTitle("Zip file");
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
				String text = String.format("Zipping %d file(s)", sources.size());
				dialog.setTitle(text);
				dialog.setTitle("Zip file - output: " + dest);

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

				label.setText("Zipping file " + filename);
			}
		});
	}

	@Override
	protected void done(String filename){
		super.done(filename);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				label.setText("done zipping file " + filename);

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
					String.format("Error zip file"),
					JOptionPane.WARNING_MESSAGE
					); 

			}
		});
		t.start();
	}




}
