package org.msr.invokers;

public interface ProgressLogger {

	public void setText(String s);
	
	public void setRunning(boolean b);
	public boolean isRunning();
}
