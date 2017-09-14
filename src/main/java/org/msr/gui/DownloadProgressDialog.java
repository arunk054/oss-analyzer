package org.msr.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import org.msr.invokers.DownloadInvokeThread;
import org.msr.invokers.RepoElement;
import org.msr.miners.WrapperRepoMiner;

public class DownloadProgressDialog extends JDialog{

	private JPanel panel;
	private JTextArea progressText;
	private JButton interruptButton;
	private JButton closeButton;
	private ProgressLoggerJTextArea progressLogger;
	private DownloadInvokeThread downloadThread;
	private RepoElement[] reposToMine;

	public DownloadProgressDialog(RepoElement[] reposToMine, Component parent) {

		this.reposToMine = reposToMine;
		setTitle("Download Github Data - Progress...");
		//create the panel
		panel = new JPanel(new GridBagLayout());
		this.panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
		createComponents();
		addComponents();
		this.setContentPane(panel);
		this.setModal(true);
		this.pack();
		setLocationRelativeTo(parent);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
				performExit();
			}
		});
		

		//the dialog should know the thread object
		this.downloadThread = new DownloadInvokeThread(reposToMine,progressLogger);
		downloadThread.start();
		setVisible(true);
	}

	private void addComponents() {
		GridBagConstraints c;
		
		c = new GridBagConstraints(0,0,2,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(5, 5, 5, 5),0,0);
		JScrollPane textScroller = new JScrollPane(progressText);
		textScroller.setPreferredSize(new Dimension(500, 330));
		this.panel.add(textScroller,c);
		
		c = new GridBagConstraints(0,1,1,1,0.5,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(5, 5, 5, 10),0,0);
		this.panel.add(interruptButton,c);
		
		
		c = new GridBagConstraints(1,1,1,1,0.5,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(5, 10, 5, 5),0,0);
		this.panel.add(closeButton,c);
		
		
	}

	private void createComponents() {

		progressText = new JTextArea(15,35);

		interruptButton = new JButton("Start Download");
		interruptButton.setEnabled(false);
		interruptButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (getProgressLogger().isRunning()) {
					interruptButton.setEnabled(false);
					interruptButton.setText("Aborting.. Please Wait...");;
					//Send the interrupt signal
					getDownloadThread().stopThread();				
				} else {
					interruptButton.setEnabled(false);
					downloadThread = new DownloadInvokeThread(reposToMine,progressLogger);
					downloadThread.start();
				}
				

				
			}
		});

		
		this.progressLogger = new ProgressLoggerJTextArea(progressText,interruptButton);
		progressLogger.setText("Initializing... ");
		WrapperRepoMiner.setProgressLogger(progressLogger);
		
		closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				performExit();
				
			}
		});
	}
	public ProgressLoggerJTextArea getProgressLogger() {
		return progressLogger;
	}

	public void performExit() {

		if (getProgressLogger().isRunning()) {
			//Give a prompt
			int confirmed = JOptionPane.showConfirmDialog(null, 
					"Are you sure you want exit this dialog?\n"
					+ "Note: This will also stop the download process if it is running.","Close Download Dialog",
					JOptionPane.YES_NO_OPTION);
	
			if (confirmed != JOptionPane.YES_OPTION) {
				return;
			}
			closeButton.setEnabled(false);
			closeButton.setText("Aborting.. Please Wait...");
			getProgressLogger().setText("\n\n ***** Aborting Download before closing.... \n\n");
			do {
				getDownloadThread().stopThread();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					System.out.println("Swing thread interrupted..");
				}
			} while (getProgressLogger().isRunning());
		}
		this.dispose();
		this.setVisible(false);
	}

	public void setDownloadThread(DownloadInvokeThread downloadThread) {
		this.downloadThread = downloadThread;
		
	}

	public DownloadInvokeThread getDownloadThread() {
		return downloadThread;
	}


}

