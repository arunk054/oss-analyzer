package org.msr.gui;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.msr.invokers.ProgressLogger;

public class ProgressLoggerJTextArea implements ProgressLogger{

	private JTextArea outputTextArea;
	private boolean isRunning;
	private JButton interruptButton;
	public ProgressLoggerJTextArea(JTextArea outputTextArea, JButton interruptButton) {
		this.outputTextArea = outputTextArea;
		isRunning = false;
		this.interruptButton = interruptButton;
	}
	@Override
	public void setText(final String s) {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				outputTextArea.append(s+"\n");
				
			}
		});
		
	}
	@Override
	public void setRunning(boolean b) {
		if (b)
		{
			
			SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					interruptButton.setText("Pause Download");
					interruptButton.setEnabled(true);
					
				}
			});
			
			

		} else {
				SwingUtilities.invokeLater(new Runnable() {
				
				@Override
				public void run() {
					interruptButton.setText("Restart Download");
					interruptButton.setEnabled(true);
					
				}
			});
			
		}
		this.isRunning = b;
		
	}
	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return isRunning;
	}

}
