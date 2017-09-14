package org.msr.gui;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.msr.invokers.Configurations;
import org.msr.mongodblayer.MongoDBLayer;

public class SettingsDialog extends JDialog {

	private static SettingsDialog currentInstance = null;
	protected JPanel panel;
	protected JLabel[] labels;
	protected JTextField[] txtFields;
	protected JButton saveButton;

	private static int ROWS = 4;

	public SettingsDialog(Component parent) {

		setTitle("Edit Settings");
		//create the panel
		panel = new JPanel(new GridBagLayout());
		this.panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));
		createComponents();
		addComponents();
		this.setContentPane(panel);
		this.setModal(true);
		this.pack();
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(parent);
		//setVisible(true);
	}

	private void addComponents() {

		GridBagConstraints c;
		
		for (int i = 0; i < ROWS; ++i) {
			c = new GridBagConstraints(0,(i*2),1,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(10, 5, 1, 5),0,0);
			this.panel.add(labels[i],c);
			c = new GridBagConstraints(0,((i*2)+1),1,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(1, 5, 5, 5),0,0);
			this.panel.add(txtFields[i],c);
		}
		c = new GridBagConstraints(0,ROWS*2,2,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(15, 25, 5, 25),0,0);
		this.panel.add(saveButton,c);


	}

	private void createComponents() {
		//Label and text field pair
		labels = new JLabel[ROWS];
		txtFields = new JTextField[ROWS];

		labels[0] = new JLabel("MongoDB Database Name: ");
		txtFields[0] = new JTextField(20);
		txtFields[0].setText(Configurations.CURRENT_DB_NAME);
		//Cannot Edit this
		txtFields[0].setEditable(false);

		labels[1] = new JLabel("Github Client ID: ");
		txtFields[1] = new JTextField(40);
		txtFields[1].setText(Configurations.CLIENT_IDS[Configurations.CURRENT_CLIENT_INDEX]);
		System.out.println(Configurations.CLIENT_IDS[Configurations.CURRENT_CLIENT_INDEX]);
		System.out.println(Configurations.CURRENT_CLIENT_INDEX);
		
		labels[2] = new JLabel("Github Client Secret: ");
		txtFields[2] = new JTextField(40);
		txtFields[2].setText(Configurations.CLIENT_SECRETS[Configurations.CURRENT_CLIENT_INDEX]);
		
		labels[3] = new JLabel("Number of Years of data (can also be a decimal value e.g: 0.5): ");
		txtFields[3]= new JTextField(10);
		txtFields[3].setText(String.valueOf(Configurations.NUM_YEARS_OF_DATA));
		
		saveButton = new JButton("Save Settings");
		saveButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				//Access all fields and then save it to Mongo DB
				//case insensitive
				//Intentionally canno tedit this
				//String prevDBName = txtFields[0].getText();
				//Configurations.CURRENT_DB_NAME = txtFields[0].getText();
				
				Configurations.CLIENT_IDS[Configurations.CURRENT_CLIENT_INDEX] = txtFields[1].getText();
				Configurations.CLIENT_SECRETS[Configurations.CURRENT_CLIENT_INDEX] = txtFields[2].getText();
				

				try {
					Configurations.NUM_YEARS_OF_DATA  = Double.parseDouble(txtFields[3].getText());
				} catch (NumberFormatException e1) {
					
				}
				
				//Write to MongoDB using DBOperationsUI
				Runnable newThread = new Runnable() {
					
					@Override
					public void run() {
						
						MongoDBLayer.getInstance().updateSettings();
					}
				};
				(new Thread(newThread)).start();
			
				
			}
		});
				


	}


}
