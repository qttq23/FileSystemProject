package FileSystemManagement.Gui;


import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.text.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileSystemView;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import FileSystemManagement.*;
import FileSystemManagement.Service.*;



public class SimpleFrame extends JFrame{
	public JButton startBtn;
	public Thread thread;

	public SimpleFrame fr;
	public boolean stop = false;
	public JPanel statusBar;
	public JLabel startLabel = new JLabel("");
	public JLabel centerLabel = new JLabel("");
	public JLabel endLabel = new JLabel("");
	public JPopupMenu pm;
	public JMenuItem m1,m2,m3,m4,m5,m6,m7,m8,m9;

	public String currentPath, selectedPath;
	public java.util.List selectedPaths = new ArrayList<String>();
	public java.util.List tempList = new ArrayList<String>();
	public void out(Object o){
		System.out.println(o);

	}

	public SimpleFrame(String title){

		

		// set up frame
		super(title);
		fr = this;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setIconImage(createIcon("images/icon/filesystem.png", 50, 50).getImage());

        // table for showing file/folder
		String[] colNames = {"Name", "Size", "Last modified", "Type"};
		DefaultTableModel dtm = new DefaultTableModel(colNames, 0);
		JTable table = new JTable(dtm);
		table.setEnabled(true);
		table.setShowGrid(false);

        // load data to table
		this.currentPath = new File(".").getAbsolutePath();
		this.currentPath = currentPath.substring(0, currentPath.lastIndexOf("."));
		loadDataToTable(table, currentPath);
		// JFrame fr = this;
		table.addMouseListener(new MouseAdapter() {

			public void mousePressed(MouseEvent mouseEvent) {

				if(mouseEvent.getClickCount() == 2){
					fr.setTitle("MyCommander - loading...");
					int selectedRow = table.getSelectedRow();
					int selectedCol = table.getSelectedColumn();

					if (selectedRow != -1) {
                        // your valueChanged overridden method 
						String type = (String)table.getModel().getValueAt(selectedRow, 3);
						String name = (String)table.getModel().getValueAt(selectedRow, 0);


						if(type.toLowerCase().equals("folder")) {
                            // go to folder
							loadDataToTable(table, name);
						}
						else if(type.toLowerCase().equals("unknown")) {
							JOptionPane.showMessageDialog(null, "This type is not supported by this program");
						}
						else {
							try{
                                // cmd start file
								String cmd = String.format("cmd.exe /c \"%s\"", name);
								Process process = Runtime.getRuntime().exec(cmd);
							}
							catch(Exception ex){
								ex.printStackTrace();
							}

						}

					}

					fr.setTitle("MyCommander - " + fr.currentPath);
				}

			}

			public void mouseReleased(MouseEvent e){

				

				if(SwingUtilities.isRightMouseButton(e)){

                    // process
					int r = table.rowAtPoint(e.getPoint());
					if (r >= 0 && r < table.getRowCount()) {

						if(fr.selectedPaths.size() < 1){

							table.setRowSelectionInterval(r, r);
						}

                        // show
						int selectedRow = table.getSelectedRow();
						if(selectedRow >= 0){

							String type = (String)table.getModel().getValueAt(selectedRow, 3);
							String name = (String)table.getModel().getValueAt(selectedRow, 0);

							// set default menu
							m3.setEnabled(true);	// enabled view content for file
							m9.setEnabled(false);	// disabled unzip

							// set specified
							if(type.toLowerCase().equals("folder")){
								m3.setEnabled(false);	// disabled view content for folder
							}
							if(type.toLowerCase().equals(".zip")){
								m9.setEnabled(true);	// disabled unzip
							}

							pm.show(e.getComponent(), e.getX(), e.getY());

						}
						
					} else {
						table.clearSelection();
					}


				}
				else{

					endLabel.setText(String.format("%d selected", table.getSelectedRows().length));

				}

				if(table.getSelectedRows().length > 0){
					// set selected item
					fr.selectedPath = (String)table.getModel().getValueAt(table.getSelectedRow(), 0);

					// set multi selected items
					fr.selectedPaths = new ArrayList<String>();
					for(int rowIndex : table.getSelectedRows()){
						fr.selectedPaths.add((String)table.getModel().getValueAt(rowIndex, 0));
					}
					out("frame set selectedPath");
				}
			}


		});

		table.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) {
				MyLog.log("keyPressed");

				int key = e.getKeyCode();
				if(key ==  KeyEvent.VK_F5){
					// refresh table
					loadDataToTable(table, fr.currentPath);
				}
				else if(key ==  KeyEvent.VK_F1){
					// create file
					m1.doClick();
	
				}
				else if(key ==  KeyEvent.VK_F2){
					

					// create folder
					m2.doClick();
		
				}
				else if(key ==  KeyEvent.VK_F3){
					// open
					m3.doClick();
			
				}
				else if(key ==  KeyEvent.VK_F4){
					// copy file
					m4.doClick();
			
				}
				else if(key ==  KeyEvent.VK_F6){
					// paste file
					m5.doClick();
			
				}
				else if(key ==  KeyEvent.VK_F7){
					// rename
					m6.doClick();
			
				}
				else if(key ==  KeyEvent.VK_DELETE){
					// delete
					m7.doClick();
			
				}
				else if(key ==  KeyEvent.VK_F8){
					// zip
					m8.doClick();
			
				}
				else if(key ==  KeyEvent.VK_F9){
					// unzip
					m9.doClick();
			
				}
				
			}

			public void keyReleased(KeyEvent e) {
				MyLog.log("keyReleased");

				
			}

			public void keyTyped(KeyEvent e) {
				MyLog.log("Key Typed");
			}
		});




		JPanel tablePanel = new JPanel(new BorderLayout());
		tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);


        // status bar
		statusBar = new JPanel();
		statusBar.setLayout(new BorderLayout());
		statusBar.setBorder(new CompoundBorder(new LineBorder(Color.WHITE),
			new EmptyBorder(0, 0, 0, 0)));
		statusBar.setBackground(Color.WHITE);
		statusBar.add(startLabel, BorderLayout.LINE_START);
		statusBar.add(centerLabel, BorderLayout.CENTER);
		statusBar.add(endLabel, BorderLayout.LINE_END);



        // create a popup menu 
		pm = new JPopupMenu("Message"); 

        // create menuItems 
		m1 = new JMenuItem("New file (F1)"); 
		m1.setIcon(createIcon("images/icon/addfile.png",20,20)
			);
		m1.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) 
			{
				Thread t = new Thread(new Runnable(){
					public void run(){
						// show dialog to get name of file/folder
						String name = JOptionPane.showInputDialog(fr, "Enter file name");
						out(name);
						if(name == null || name.equals("") || name.matches("\\s*") /*not all spaces*/){
							return;
						}


            			// create service
						IService service = FileService.service("createfile");


						// set params
						java.util.List<Object> list = new ArrayList<Object>();
						list.add(fr.currentPath + "/" + name);
						service.setParams(list);


						// service work
						int result = service.work();



						// display result
						if(result == 0){

							fr.loadDataToTable(table, fr.currentPath);
							fr.centerLabel.setText("\tCreate file done");

						}
						else{
							fr.centerLabel.setText("\tCreate file fail");

						}
					}
				});

				t.start();


			} 
		}); 

		m2 = new JMenuItem("New folder (F2)");
		m2.setIcon(createIcon("images/icon/addfolder.png",20,20)
			); 
		m2.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) 
			{
				Thread t = new Thread(new Runnable(){
					public void run(){
						// show dialog to get name of file/folder
						String name = JOptionPane.showInputDialog(fr, "Enter folder name");
						out(name);
						if(name == null || name.equals("") || name.matches("\\s*") /*not all spaces*/){
							return;
						}


            			// create service
						IService service = FileService.service("createfolder");


						// set params
						java.util.List<Object> list = new ArrayList<Object>();
						list.add(fr.currentPath + "/" + name);
						service.setParams(list);


						// service work
						int result = service.work();



						// display result
						if(result == 0){

							fr.loadDataToTable(table, fr.currentPath);
							fr.centerLabel.setText("\tCreate folder done");

						}
						else{
							fr.centerLabel.setText("\tCreate folder fail");

						}
					}
				});

				t.start();


			} 
		}); 

		m3 = new JMenuItem("Open with MyCommander (F3)");
		m3.setIcon(createIcon("images/icon/filesystem.png",20,20)
			); 
		m3.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) 
			{
				Thread t = new Thread(new Runnable(){
					public void run(){

						// check if any file selected
						if(fr.selectedPaths == null || fr.selectedPaths.size() < 1){
							JOptionPane.showMessageDialog(fr, "Please select file or folder.");
							return;
						}

            			// create service
						IService service = FileService.service("viewfile");


						// set params
						java.util.List<Object> list = new ArrayList<Object>();
						list.add(fr.selectedPath);
						service.setParams(list);


						// service work
						int result = service.work();



						// display result
						if(result == 0){

							fr.loadDataToTable(table, fr.currentPath);
							fr.centerLabel.setText("\tView file done");

						}
						else{
							fr.centerLabel.setText("\tView file fail");

						}
					}
				});

				t.start();


			} 
		}); 

		m4 = new JMenuItem("Copy (F4)");
		m4.setIcon(createIcon("images/icon/copy.png",20,20)
			); 
		m4.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) 
			{
				Thread t = new Thread(new Runnable(){
					public void run(){
						
						// check if any file selected
						if(fr.selectedPaths == null || fr.selectedPaths.size() < 1){
							JOptionPane.showMessageDialog(fr, "Please select file or folder.");
							return;
						}

						// add selected items to temp list
						fr.tempList = new ArrayList<String>();
						for(Object item: fr.selectedPaths){
							fr.tempList.add((String)item);
						}

						// set 'paste' option enabled
						m5.setEnabled(true);

						// set result
						fr.centerLabel.setText(
							String.format("Copied %d file(s) to memory",
								fr.tempList.size())
							);
					}
				});

				t.start();


			} 
		});
		m5 = new JMenuItem("Paste (F6)"); m5.setEnabled(false);
		m5.setIcon(createIcon("images/icon/arrowdown.png",20,20)
			);
		m5.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) 
			{
				Thread t = new Thread(new Runnable(){
					public void run(){

            			// create service
						IService service = FileService.service("copyfile");


						// set params
						java.util.List<Object> list = new ArrayList<Object>(fr.tempList);
						list.add(fr.currentPath);	// destination where copy to
						service.setParams(list);


						// service work
						int result = service.work();



						// display result
						if(result == 0){

							fr.loadDataToTable(table, fr.currentPath);
							fr.centerLabel.setText("\t Copy done");

						}
						else{
							fr.centerLabel.setText("\t Copy fail");

						}
					}
				});

				t.start();


			} 
		}); 

		m6 = new JMenuItem("Rename (F7)");
		m6.setIcon(createIcon("images/icon/edit.png",20,20)
			); 
		m6.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) 
			{
				Thread t = new Thread(new Runnable(){
					public void run(){

						// check if any file selected
						if(fr.selectedPaths == null || fr.selectedPaths.size() < 1){
							JOptionPane.showMessageDialog(fr, "Please select file or folder.");
							return;
						}

						// show dialog to get name of file/folder
						String originalName = (new File(fr.selectedPath)).getName();
						String name = JOptionPane.showInputDialog(fr, 
							String.format("Rename '%s' to:", originalName),
							originalName);
						out(name);
						if(name == null || name.equals("") || name.matches("\\s*") /*not all spaces*/){
							return;
						}


            			// create service
						IService service = FileService.service("rename");


						// set params
						java.util.List<Object> list = new ArrayList<Object>();
						list.add(fr.selectedPath);
						list.add(fr.currentPath + "/" + name);
						service.setParams(list);


						// service work
						int result = service.work();



						// display result
						if(result == 0){

							fr.loadDataToTable(table, fr.currentPath);
							fr.centerLabel.setText("\t Rename done");

						}
						else{
							fr.centerLabel.setText("\tCreate fail");

						}
					}
				});

				t.start();


			} 
		}); 

		m7 = new JMenuItem("Delete");
		m7.setIcon(createIcon("images/icon/delete.png",20,20)
			); 
		m7.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) 
			{
				Thread t = new Thread(new Runnable(){
					public void run(){

						// check if any file selected
						if(fr.selectedPaths == null || fr.selectedPaths.size() < 1){
							JOptionPane.showMessageDialog(fr, "Please select file or folder.");
							return;
						}
						
						out(fr.selectedPaths);

            			// create service
						IService service = FileService.service("delete");


						// set params
						java.util.List<Object> list = new ArrayList<Object>(fr.selectedPaths);
						// list.add(fr.selectedPath);
						service.setParams(list);


						// service work
						int result = service.work();



						// display result
						if(result == 0){

							fr.loadDataToTable(table, fr.currentPath);
							fr.centerLabel.setText("\t Delete done");

						}
						else{
							fr.centerLabel.setText("\t Delete fail");

						}
					}
				});

				t.start();


			} 
		}); 

		m8 = new JMenuItem("Zip (F8)");
		m8.setIcon(createIcon("images/icon/package.png",20,20)
			); 
		m8.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) 
			{
				Thread t = new Thread(new Runnable(){
					public void run(){
						
						// check if any file selected
						if(fr.selectedPaths == null || fr.selectedPaths.size() < 1){
							JOptionPane.showMessageDialog(fr, "Please select file or folder.");
							return;
						}

						// show dialog to get name of file/folder
						String name = new File((String)fr.selectedPaths.get(0)).getName();
						name = name.substring(0, (name.lastIndexOf(".") != -1) ? name.lastIndexOf("."): name.length())
						+ ".zip";
						name = JOptionPane.showInputDialog(fr, 
							"Enter name for output file", 
							name);
						out(name);
						if(name == null || name.equals("") || name.matches("\\s*") /*not all spaces*/){
							return;
						}


            			// create service
						IService service = FileService.service("zipfile");


						// set params
						java.util.List<Object> list = new ArrayList<Object>(fr.selectedPaths);
						list.add(fr.currentPath + "/" + name);	// output.zip
						service.setParams(list);


						// service work
						int result = service.work();



						// display result
						if(result == 0){

							fr.loadDataToTable(table, fr.currentPath);
							fr.centerLabel.setText("\tZip file(s) done");

						}
						else{
							fr.centerLabel.setText("\tZip file(s) fail");

						}
					}
				});

				t.start();


			} 
		}); 
		m9 = new JMenuItem("Unzip (F9)");
		m9.setIcon(createIcon("images/icon/openpackage.png",20,20)
			); 
		m9.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) 
			{
				Thread t = new Thread(new Runnable(){
					public void run(){
						
						// check if any file selected
						if(fr.selectedPaths == null || fr.selectedPaths.size() < 1){
							JOptionPane.showMessageDialog(fr, "Please select file or folder.");
							return;
						}

						// show dialog to get name of file/folder
						String name = new File(fr.selectedPath).getName();
						name = name.substring(0, name.lastIndexOf("."));

						name = JOptionPane.showInputDialog(fr, 
							"Enter name for output folder", 
							name);
						out(name);
						if(name == null || name.equals("") || name.matches("\\s*") /*not all spaces*/){
							return;
						}


            			// create service
						IService service = FileService.service("unzipfile");


						// set params
						java.util.List<Object> list = new ArrayList<Object>();
						list.add(fr.selectedPath);
						list.add(fr.currentPath + "/" + name);	// output.zip
						service.setParams(list);


						// service work
						int result = service.work();



						// display result
						if(result == 0){

							fr.loadDataToTable(table, fr.currentPath);
							fr.centerLabel.setText("\tUnzip done");

						}
						else{
							fr.centerLabel.setText("\tUnzip fail");

						}
					}
				});

				t.start();


			} 
		}); 

        // add menuitems to popup menu 
		pm.add(m1); 
		pm.add(m2); 
		pm.add(m3); 
		pm.add(m4); 
		pm.add(m5); 
		pm.add(m6); 
		pm.add(m7); 
		pm.add(m8); 
		pm.add(m9); 



        // create a menubar 
		JMenuBar menubar = new JMenuBar(); 

        // create a menu 
		JMenu x1 = new JMenu("Settings"); 
		JMenu x2 = new JMenu("About"); 

        // create menuitems 
		JMenuItem configMenuItem = new JMenuItem("Go to settings"); 
		configMenuItem.setIcon(createIcon("images/icon/settings.png",20,20)
			);
		JMenuItem aboutMenuItem = new JMenuItem("About MyCommander"); 
		aboutMenuItem.setIcon(createIcon("images/icon/information.png",20,20)
			);
		aboutMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try{

					ImageIcon icon = createIcon("images/icon/filesystem.png", 100, 100);
					byte[] bytes = Files.readAllBytes(Paths.get("about.txt"));
					String aboutString = new String(bytes);
					JOptionPane.showMessageDialog(null, aboutString, "About MyCommander", JOptionPane.INFORMATION_MESSAGE, icon);
				}
				catch(Exception ex){
					MyLog.log("Can't read info");
					ex.printStackTrace();

				}

			}
		});

       	// add menu items to menu 
		x1.add(configMenuItem); 
		x2.add(aboutMenuItem); 

		menubar.add(x1);
		menubar.add(x2);

        // add to frame
		this.setJMenuBar(menubar);
		this.setLayout(new BorderLayout());
		this.add(tablePanel, BorderLayout.CENTER);
		this.add(statusBar, BorderLayout.PAGE_END);


        // show frame
		this.pack();
		this.setVisible(true);

	}

	public boolean match(String line, String pattern){
		// String to be scanned to find the pattern.
		// String line = "This order was placed for QT3000! OK?";
		// String pattern = "(.*)(\\d+)(.*)";

      	// Create a Pattern object
		Pattern r = Pattern.compile(pattern);

      	// Now create matcher object.
		Matcher m = r.matcher(line);

		if(m.find())
			return true;
		else
			return false;
	}

	public ImageIcon createIcon(String source, int width, int height){
		ImageIcon addIcon = new ImageIcon(source);
		Image img = addIcon.getImage();  
		Image resizedImage = img.getScaledInstance(width, height,  java.awt.Image.SCALE_SMOOTH);  
		return new ImageIcon(resizedImage);
	}

	public void loadDataToTable(JTable table, String folder){
		this.currentPath = folder;

        // load data to table
		String[] colNames = {"Name", "Size", "Last modified", "Type"};
		String[][] data = getListFiles(folder);
		DefaultTableModel dtm2 = new DefaultTableModel(colNames, 0);
		DefaultTableModel tableModel = new DefaultTableModel(colNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {

				return false;
			}
		};
		if(data != null){
			for(int i = 0; i < data.length; i++){
				tableModel.addRow(data[i]);
			}
		}

    	// add model to table
		table.setModel(tableModel);
    	// add data listener
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			public void valueChanged(ListSelectionEvent lse) {
				if (!lse.getValueIsAdjusting()) {
					int[] rowIndices = table.getSelectedRows();
					endLabel.setText(String.format("%d selected(s)", rowIndices.length));
				}
			}
		});

        // modify table
		TableColumn col = table.getColumnModel().getColumn(0);
		col.setCellRenderer(new MyTableCellRenderer());

		this.setTitle(String.format("MyCommander - %s", folder));
		startLabel.setText(String.format("%d object(s)", data.length));
		endLabel.setText(String.format("%d selected(s)", 0));
        // endLabel.setText("%d selected(s)", 0);


        // reset selected items
		fr.selectedPath = "";
		fr.selectedPaths = new ArrayList<String>();
	}

	public String[][] getListFiles(String folder){
		out("folder: " + folder);

		File f = new File(folder);
		File[] listFiles = f.listFiles();
		if(listFiles == null){
			listFiles = new File[0];
		}

		out(Arrays.asList(listFiles)
			);

		String[][] records = new String[listFiles.length + 1][4];
		int count = 0;
		File parent = f.getAbsoluteFile().getParentFile();
		if(parent == null){
			parent = f.getAbsoluteFile();

		}
		out("parent: " + parent);
		records[count][0] = parent.getAbsolutePath();
		records[count][1] = getSize(parent);
		records[count][2] = convertTime(parent.lastModified());
		records[count][3] = getExt(parent);
		count++;

		out("name\tsize\tlast modified\ttype");
		for(File file : listFiles){

            // out(count);
			records[count][0] = file.getAbsolutePath();
			records[count][1] = getSize(file);
			records[count][2] = convertTime(file.lastModified());
			records[count][3] = getExt(file);
			count++;


		}

		return records;
	}
	public String convertTime(long time){
		Date date = new Date(time);
		Format format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		return format.format(date);
	}

	public String getExt(File f){
		if(f.isDirectory())
			return "folder";
		try{

			String filename = f.getName();
			return filename.substring(
				filename.lastIndexOf(".")
				);
		}
		catch(Exception ex){
            // ex.printStackTrace();
			return "unknown";
		}

	}

	public String getSize(File f){

		if(f.isDirectory()){
			return "";
		}


		String result = "";
        // get bytes
		long length = f.length();
		long oneMB = 1024*1024;
		long oneKB = 1024;

		if(length >= oneMB){
            // convert to MB
			double d = (double)length/oneMB;
			result = String.format("%.2f MB", d);
		}
		else if(length >= oneKB){
            // convert to KB
			double d = (double)length/oneKB;
			result = String.format("%.2f KB", d);
		}
		else{
			result = String.format("%d B", length);
		}

		return result;
	}

}



class MyTableCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer 
{
	public Component getTableCellRendererComponent(
		JTable table, 
		Object value, 
		boolean isSelected, 
		boolean hasFocus, 
		int row, 
		int col)
	{
		super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,col);

		File f = new File((String)value);
		Icon icon = FileSystemView.getFileSystemView().getSystemIcon(f);
		setIcon(icon);

		if(row == 0){
			setText(".."); 
            // parent folder
		}
		else{

			setText(f.getName());
		}

		setToolTipText((String) value);



		return this;
	}



}
