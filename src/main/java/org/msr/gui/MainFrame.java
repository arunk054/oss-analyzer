package org.msr.gui;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.msr.controller.Constants;
import org.msr.invokers.RepoElement;
import org.msr.mongodblayer.MongoDBLayer;

public class MainFrame extends JFrame{

	private MainPanel mainPanel;

	public MainPanel getMainPanel() {
		return mainPanel;
	}


	public MainFrame() {
		setTitle("Open Source Projects @Microsoft : Analyze Github Data");
		setLocation(300,200);
		this.pack();//or do set size
		this.setSize(300, 200);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener( new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				int confirmed = JOptionPane.showConfirmDialog(null, 
						"Are you sure you want to exit the program?", "Exit Program Message Box",
						JOptionPane.YES_NO_OPTION);

				if (confirmed == JOptionPane.YES_OPTION) {
					dispose();
					MongoDBLayer.getInstance().closeConnection();
					System.exit(0);
				}
			}
		});
		
		addMainPanel();
		this.pack();
		mainPanel.urlText.requestFocusInWindow();
	}

	private void addMainPanel() {
		// TODO Auto-generated method stub
		mainPanel = new MainPanel();
		this.setContentPane(mainPanel);
		
	}

	public static void loadRepositories(final MainFrame mainframe) {
		
		Runnable loadRepoThread = new Runnable() {
			
			@Override
			public void run() {
				final List<RepoElement> repos = MongoDBLayer.getInstance().getRepositoriesFromDB();
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						mainframe.getMainPanel().clearAndAddRepos(repos);
					}
				});
				
			}
		};
		(new Thread(loadRepoThread)).start();
	}
	
	public static void loadSettings() {
		
		Runnable loadSettingsThread = new Runnable() {
			
			@Override
			public void run() {
				MongoDBLayer.getInstance().loadSettings();			
			}
		};
		(new Thread(loadSettingsThread)).start();
	}
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
		    @Override
		    public void run() {
		    	
		    	//Create directory - data/db first just in case
		    	File dataDB = new File(Constants.MONGODB_DATA_DIR);
		    	if (!dataDB.exists()) {
		    		dataDB.mkdirs();
		    	}
		    	//Make sure Mongo Db is running if not prompt and exit
		    	if (!MongoDBLayer.getInstance().isRunning()) {
		    		JOptionPane.showMessageDialog(null, "ERROR! MONGO DB is Not Running!\n Type mongod from command prompt to start it.");
		    		System.exit(0);
		    		return;
		    	}
		    	
		    	JFrame jf = new MainFrame();
				jf.setVisible(true);
				//Load the repositories in another thread
				loadRepositories((MainFrame) jf);
				loadSettings();
		    }
		});
	}

}
