package org.msr.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.msr.invokers.AggregateStats;
import org.msr.invokers.DownloadInvokeThread;
import org.msr.invokers.RepoElement;

import com.mongodb.client.model.Aggregates;

public class AnalyzeDialog extends JDialog{

	private List<RepoElement> reposToMine;
	private JPanel panel;
	private JLabel labelRepos;
	private JComboBox<RepoElement> repoComboBox;
	private JButton analyzeAggregate;
	private JButton analyzeTrend;
	private JLabel aggregateLabel;
	private JTextArea aggregateText;
	private JLabel trendLabel;
	private JTextArea trendText;
	private JButton closeDialog;

	public AnalyzeDialog(List<RepoElement> reposToMine, MainPanel parent) {

		this.reposToMine = reposToMine;
		setTitle("Analyze Github Data ");
		//create the panel
		panel = new JPanel(new GridBagLayout());
		this.panel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
		createComponents();
		addComponents();
		this.setContentPane(panel);
		this.setModal(true);
		this.pack();

		setLocationRelativeTo(parent);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		//the dialog should know the thread object
		setVisible(true);
	}

	private void createComponents() {
		//The label and dropdown list
		labelRepos = new JLabel("Select a Repo:  ");

		repoComboBox=  new JComboBox<RepoElement>(reposToMine.toArray(new RepoElement[1]));
		repoComboBox.setSelectedIndex(0);

		//Two buttons to analyze, one Static andother dynamic.

		analyzeAggregate = new JButton("Show Aggregates");
		addAnalyzeAggregateActions();

		analyzeTrend = new JButton("Show Trend");
		addAnalyzeTrendActions();
		//Create the Label and Text area for trend but keep it hidden first
		aggregateLabel = new JLabel("Aggregate Stats: ",  SwingConstants.LEFT);
		aggregateText = new JTextArea(25,30);

		trendLabel = new JLabel("Trend Stats: ", SwingConstants.LEFT);
		trendText = new JTextArea(25,30);

		closeDialog = new JButton("Close");
		closeDialog.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				performExit();

			}
		});


	}

	private void performExit() {
		this.setVisible(false);
		this.dispose();

	}

	private void addAnalyzeTrendActions() {
		analyzeTrend.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//Show a popup first to get the window of trend
				int months = 12;
				Integer[] choices = new Integer[months];
				for (int i = 0; i < months; ++i) {
					choices[i]=i+1;
				}
				final Integer choice = (Integer) JOptionPane.showInputDialog(null, "Select the interval (in Months): ",
						"Choose interval for Showing Trend", JOptionPane.QUESTION_MESSAGE, null, choices, choices[5]); // Initial choice
				System.out.println("Interval: "+choice);
				if (choice==null)
					return;
				printDataForAnalysisContribs();
				final RepoElement repoElem = (RepoElement) repoComboBox.getSelectedItem();

				//Set this button disabled
				analyzeTrend.setEnabled(false);
				analyzeTrend.setText("Please Wait while we compute... ");
				trendText.setText("");

				Runnable newThread = new Runnable() {

					private void cleanUp(TrendBuilder trendBuilder ) {
						if (trendBuilder!=null) {
							trendText.setText(trendBuilder.getFormattedOutput().toString());
						} else {
							trendText.setText("=== ERROR Computing Trend Stats. Try after Downloading the data again.");
						}
						//Enable this button and change action
						trendLabel.setText("Showing "+choice+" Months trend Stats for: "+repoElem.getOwner()+"/"+repoElem.getRepo());
						analyzeTrend.setEnabled(true);
						analyzeTrend.setText("Show Trend");
					}

					@Override
					public void run() {


						try {
							final TrendBuilder trendBuilder = new TrendBuilder(choice, repoElem.getOwner(), repoElem.getRepo());
							trendBuilder.computeAll();
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									cleanUp(trendBuilder);
								}
							});
						} catch (Exception e) {
							cleanUp(null);
						}
					}
				};
				new Thread(newThread).start();

			}
		});
	}
	private void printDataForAnalysisContribs() {
		int len = repoComboBox.getModel().getSize();
		final int choice = 3;
		for (int i = 0; i < len; ++i) {
			final RepoElement repoElem = repoComboBox.getModel().getElementAt(i);
			Runnable newThread = new Runnable() {

				@Override
				public void run() {

					try {
						final TrendBuilder trendBuilder = new TrendBuilder(choice, repoElem.getOwner(), repoElem.getRepo());
						trendBuilder.computeAll();
						System.out.println(trendBuilder.getOutputForAnalysis());
					} catch (Exception e) {
					}
				}
			};
			new Thread(newThread).start();

		}
	}

	private void printDataForAnalysisDistributions() {

		int len = repoComboBox.getModel().getSize();
		final List<AggregateStats> aggList = new ArrayList<AggregateStats>();
		
		String[] validOnes = {};
		final CountDownLatch latch = new CountDownLatch(len - validOnes.length);
		for (int i = 0; i < len; ++i) {
			final RepoElement repoElem = repoComboBox.getModel().getElementAt(i);
			boolean found = false;
			for (String validStr : validOnes) {
				if (repoElem.getRepo().equalsIgnoreCase(validStr)) {
					found = true;
				}
			}
			if (found)
				continue;
			Runnable newThread = new Runnable() {

				@Override
				public void run() {

					try {
						final AggregateStats aggregatesData = new AggregateStats(repoElem.getOwner(), repoElem.getRepo());
						aggregatesData.computeAll();
						aggList.add(aggregatesData);
						latch.countDown();
					} catch (Exception e) {
					}
				}
			};
			Thread t1 = new Thread(newThread);
			t1.start();
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		printAllAggregateStats(aggList);

	}
	private void printAllAggregateStats(List<AggregateStats> aggList) {
		HashMap<String, List<Object>> consolidated = new HashMap<String, List<Object>>();
		consolidated.put("Downloads", new ArrayList<Object>());
		consolidated.put("popularity", new ArrayList<Object>());
		consolidated.put("users", new ArrayList<Object>());
		consolidated.put("usersMS", new ArrayList<Object>());
		consolidated.put("usersNonMS", new ArrayList<Object>());
		consolidated.put("contributors", new ArrayList<Object>());
		consolidated.put("contributorsMS", new ArrayList<Object>());
		consolidated.put("contributorsNonMS", new ArrayList<Object>());
		consolidated.put("contributions", new ArrayList<Object>());
		consolidated.put("contributionsMS", new ArrayList<Object>());
		consolidated.put("contributionsNonMS", new ArrayList<Object>());
		consolidated.put("linesAdded", new ArrayList<Object>());
		consolidated.put("linesAddedMS", new ArrayList<Object>());
		consolidated.put("linesAddedNonMS", new ArrayList<Object>());

		
		consolidated.put("issues", new ArrayList<Object>());
		consolidated.put("issuesMS", new ArrayList<Object>());
		consolidated.put("issuesNonMS", new ArrayList<Object>());
		consolidated.put("pr", new ArrayList<Object>());
		consolidated.put("prMS", new ArrayList<Object>());
		consolidated.put("prNonMS", new ArrayList<Object>());
		
		consolidated.put("closeIssue", new ArrayList<Object>());
		consolidated.put("closePR", new ArrayList<Object>());
		
		for (AggregateStats ag: aggList) {
			
			consolidated.get("Downloads").add(ag.getDownloadCountInt()/ag.getAgeInYearsTweaked());
			consolidated.get("popularity").add((ag.getStars()+ag.getForks()+ag.getWatchers())/ag.getAgeInYearsTweaked());
			consolidated.get("users").add(ag.getUsers()/ag.getAgeInYearsTweaked());
			consolidated.get("usersMS").add(ag.getUsersMS()/ag.getAgeInYearsTweaked());
			consolidated.get("usersNonMS").add(ag.getUsersNonMS()/ag.getAgeInYearsTweaked());
			consolidated.get("contributors").add(ag.getNumContributors()/ag.getAgeInYearsTweaked());
			consolidated.get("contributorsMS").add(ag.getNumContributorsMS()/ag.getAgeInYearsTweaked());
			consolidated.get("contributorsNonMS").add(ag.getNumContributorsNonMS()/ag.getAgeInYearsTweaked());
			consolidated.get("contributions").add(ag.getTotalContributions()/ag.getAgeInYearsTweaked());
			consolidated.get("contributionsMS").add(ag.getMsContributions()/ag.getAgeInYearsTweaked());
			consolidated.get("contributionsNonMS").add(ag.getNonMSContributions()/ag.getAgeInYearsTweaked());

			consolidated.get("linesAdded").add((ag.getMsLinesAdded()+ag.getNonMSLinesAdded())/ag.getAgeInYearsTweaked());
			consolidated.get("linesAddedMS").add(ag.getMsLinesAdded()/ag.getAgeInYearsTweaked()) ;
			consolidated.get("linesAddedNonMS").add(ag.getNonMSLinesAdded()/ag.getAgeInYearsTweaked());

			
			consolidated.get("issues").add(ag.getBugs()+ag.getPullReqs()/ag.getAgeInYearsTweaked()	);
			consolidated.get("issuesMS").add(ag.getIssuesReportedByMS()/ag.getAgeInYearsTweaked());
			consolidated.get("issuesNonMS").add(ag.getIssuesOutsideMS()/ag.getAgeInYearsTweaked());
			consolidated.get("pr").add(ag.getPullReqs()/ag.getAgeInYearsTweaked());
			consolidated.get("prMS").add(ag.getPullReqsReportedByMS()/ag.getAgeInYearsTweaked());
			consolidated.get("prNonMS").add(ag.getPullReqsOutsideMS()/ag.getAgeInYearsTweaked());

			consolidated.get("closeIssue").add(ag.getAverageTimeToClose(false));
			consolidated.get("closePR").add(ag.getAverageTimeToClose(true));
			
			
		}
		for (Map.Entry<String, List<Object>> entry: consolidated.entrySet()){
			StringBuilder sb = new StringBuilder();
			sb.append(entry.getKey()+" ");
			for (Object ob : entry.getValue()) {
				sb.append(ob.toString()+" ");
			}
			System.out.println(sb);
		}
	}



	private void addAnalyzeAggregateActions() {
		analyzeAggregate.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				//Set this button disabled
				analyzeAggregate.setEnabled(false);
				analyzeAggregate.setText("Please Wait while we compute... ");
				aggregateText.setText("");
				//printDataForAnalysisDistributions();
				//Get repo Elem from Combobox
				final RepoElement repoElem = (RepoElement) repoComboBox.getSelectedItem();

				Runnable newThread = new Runnable() {

					private void cleanUp(AggregateStats aggregatesData ) {
						if (aggregatesData!=null) {
							aggregateText.setText(aggregatesData.getFormattedOutput().toString());
						} else {
							aggregateText.setText("=== ERROR Computing Stats. Try after Downloading the data again.");
						}
						//Enable this button and change action
						aggregateLabel.setText("Showing Aggregate Stats for: "+repoElem.getOwner()+"/"+repoElem.getRepo());
						analyzeAggregate.setEnabled(true);
						analyzeAggregate.setText("Show Aggregates");

					}
					@Override
					public void run() {


						try {
							final AggregateStats aggregatesData = new AggregateStats(repoElem.getOwner(), repoElem.getRepo());
							aggregatesData.computeAll();

							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									cleanUp(aggregatesData);
								}
							});
						} catch (Exception e) {
							cleanUp(null);
						}
					}
				};
				new Thread(newThread).start();

			}
		});


	}

	private void addComponents() {
		GridBagConstraints c;

		c = new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.LINE_START,GridBagConstraints.NONE,new Insets(5, 5, 5, 0),0,0);
		panel.add(labelRepos,c);

		c = new GridBagConstraints(1,0,1,1,0.5,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(5, 0, 5, 5),0,0);
		panel.add(repoComboBox,c);

		c = new GridBagConstraints(0,1,2,1,0.5,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(10, 0, 5, 0),0,0);
		panel.add(new JSeparator(),c);

		c = new GridBagConstraints(0,2,2,1,0.5,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(10, 5, 5, 5),0,0);
		panel.add(analyzeAggregate,c);

		c = new GridBagConstraints(0,3,2,1,0.5,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(5, 5, 5, 5),0,0);
		panel.add(aggregateLabel,c);

		c = new GridBagConstraints(0,4,2,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0, 5, 5, 5),0,0);
		JScrollPane textScroller = new JScrollPane(aggregateText);
		textScroller.setPreferredSize(new Dimension(500, 150));
		panel.add(textScroller,c);

		c = new GridBagConstraints(0,5,2,1,0.5,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(10, 0, 5, 0),0,0);
		panel.add(new JSeparator(),c);

		c = new GridBagConstraints(0,6,2,1,0.5,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(10, 5, 5, 5),0,0);
		panel.add(analyzeTrend,c);		

		c = new GridBagConstraints(0,7,2,1,0.5,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(5, 5, 5, 5),0,0);
		panel.add(trendLabel,c);

		c = new GridBagConstraints(0,8,2,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(0, 5, 5, 5),0,0);
		textScroller = new JScrollPane(trendText);
		textScroller.setPreferredSize(new Dimension(500, 150));
		panel.add(textScroller,c);

		c = new GridBagConstraints(0,9,2,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(5, 5, 5, 5),80,10);
		panel.add(closeDialog, c);
	}
}
