package org.msr.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.msr.invokers.DownloadInvokeThread;
import org.msr.invokers.RepoElement;
import org.msr.mongodblayer.MongoDBLayer;

public class MainPanel extends JPanel{

	protected JLabel repoListLabel;

	protected JScrollPane repoListScroller;

	protected JLabel urlLabel;

	protected JTextField urlText;

	protected JButton addRepo;

	protected JButton removeRepos;

	protected JButton settings;

	protected JButton downloadData;

	protected JButton analyzeData;

	protected JLabel totalRepos;

	protected JList repoList;
	protected DefaultListModel<RepoElement> repoListModel;

	public void clearAndAddRepos(List<RepoElement> listOfRepos) {
		repoListModel.removeAllElements();
		for (RepoElement repo: listOfRepos) {
			repoListModel.addElement(repo);	
		}
		setTotalRepos();
	}
	public MainPanel() {
		super(new GridBagLayout());
		createComponents();
		addComponents();
		this.setBorder(BorderFactory.createEmptyBorder(10, 20, 30, 20));

	}

	private void addComponents() {
		//Set layout

		GridBagConstraints c ;

		c = new GridBagConstraints(0,0,2,1,0.5,0.5,GridBagConstraints.LINE_START,GridBagConstraints.HORIZONTAL,new Insets(5, -10, 0, 0),0,0);
		this.add(totalRepos, c);

		c = new GridBagConstraints(3,0,1,1,0.5,0.5,GridBagConstraints.LINE_START,GridBagConstraints.HORIZONTAL,new Insets(5, 0, 0, -10),0,0);
		this.add(settings, c);

		c = new GridBagConstraints(0,1,4,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(5, 5, 0, 5),0,0);
		this.add(repoListLabel,c);
		c = new GridBagConstraints(0,2,4,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(5, 5, 0, 5),0,0);
		this.add(repoListScroller,c);

		c = new GridBagConstraints(0,3,4,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(0,5, 5, 5),0,0);
		this.add(removeRepos,c);

		c = new GridBagConstraints(0,4,1,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(5, 5, 5, 0),0,0);
		this.add(urlLabel,c);
		c = new GridBagConstraints(1,4,2,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(5, 0, 5, 0),0,0);
		this.add(urlText,c);
		c = new GridBagConstraints(3,4,1,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(5,0, 5, 5),0,0);
		this.add(addRepo,c);

		c = new GridBagConstraints(0,5,4,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(5,0, 5, 0),0,0);
		this.add(new JSeparator(), c);

		c = new GridBagConstraints(1,6,1,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(5,5, 5, 5),0,0);
		this.add(downloadData, c);

		c = new GridBagConstraints(2,6,1,1,0.5,0.5,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(5,5, 5, 5),0,0);
		this.add(analyzeData, c);
	}

	private void createComponents() {

		//Add the label for repository list
		this.repoListLabel = new JLabel("List of Github Repositories",SwingConstants.CENTER);

		repoListModel = new DefaultListModel<RepoElement>();
		repoList = new JList(repoListModel);
		repoList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		repoList.setVisibleRowCount(15);
		repoList.setLayoutOrientation(JList.VERTICAL);
		this.repoListScroller = new JScrollPane(repoList);
		repoListScroller.setPreferredSize(new Dimension(200, 110));

		urlLabel = new JLabel("Github URL:  ", SwingConstants.RIGHT);
		urlText = new JTextField(28);

		//Buttons to add and remove URL
		addRepo = new JButton("Add");
		addAddRepoActions();

		removeRepos = new JButton("Remove Selected Repositories");
		removeReposActions();
		//Settings button
		settings = new JButton("Settings");
		addSettingsListener(this);

		totalRepos = new JLabel();
		setTotalRepos();
		//The action buttons
		downloadData = new JButton("Download Github Data");
		addDownloadDataListener(this);

		analyzeData = new JButton("Analyze Github Data");
		addAnalyzeDataListener(this);

	}

	public RepoElement[] getReposList( ){
		int len = repoListModel.getSize();
		RepoElement[] reposToMine = new RepoElement[len];
		for(int i = 0; i < len; ++i) {
			reposToMine[i] = repoListModel.getElementAt(i);
		}
		return reposToMine;
	}
	private void addAnalyzeDataListener(final MainPanel mainPanel) {
		this.analyzeData.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int len = repoListModel.getSize();

				if (len == 0) {
					JOptionPane.showMessageDialog(null, "No Repositories to Analyze!\nAdd one or more repos");
					return;
				}

				//Get the list of Repos to mine
				final RepoElement[] reposToMine = getReposList();

				//Check which of these repos we are yet to download data.
				Runnable separateThread = new Runnable() {
					
					@Override
					public void run() {
						final List<RepoElement> completeRepos = DBOperationsFromUI.getCompleteRepos(reposToMine);

	
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									if (completeRepos.size() < reposToMine.length) {
										//Show the Joption pane
										int confirmed = JOptionPane.showConfirmDialog(mainPanel, 
												getPanel(completeRepos,reposToMine), "Incomplete Data for Analyze!",
												JOptionPane.YES_NO_OPTION);
										if (confirmed != JOptionPane.YES_NO_OPTION) {
											return;
										}
									}
									if (completeRepos.size() == 0) {
										JOptionPane.showMessageDialog(null, "No Complete Repositories Available to Analyze!\nClick Download Github Data");
										return;
									}
									AnalyzeDialog analyzeDialog = new AnalyzeDialog(completeRepos,mainPanel);								
								}
							});
					}
				};
				(new Thread(separateThread)).start();
			}

			private Component getPanel(List<RepoElement> completeRepos, RepoElement[] reposToMine) {
				JLabel label = new JLabel();
				StringBuilder sb = new StringBuilder();
				sb.append("<html>Download Incomplete for following repos:<br><br><b>");
				for (RepoElement rep: reposToMine) {
					if (completeRepos.contains(rep))
						continue;
					sb.append(rep.getOwner()+"/"+rep.getRepo()+"<br>");
				}
				sb.append("</b><br>Select Yes if you want to continue analyzing remaining Repos?<br>If you choose No, you can either remove incomplete repos or click Download");
				sb.append("</html>");
				label.setText(sb.toString());
				return label;
			}
		});

	}

	private void addDownloadDataListener(final MainPanel mainPanel) {
		this.downloadData.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int len = repoListModel.getSize();

				if (len == 0) {
					JOptionPane.showMessageDialog(null, "No Repositories to Download");
					return;
				}

				//Get the list of Repos to mine
				RepoElement[] reposToMine = getReposList();

				//Open the dialog and start printing the progress
				DownloadProgressDialog downloadDialog = new DownloadProgressDialog(reposToMine, mainPanel);
			}
		});

	}

	private void addSettingsListener(final MainPanel mp) {
		this.settings.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new SettingsDialog(mp).setVisible(true);
			}
		});

	}

	private void removeReposActions() {
		this.removeRepos.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int[] indices = repoList.getSelectedIndices();
				if (indices.length > 0) {
					int confirmed = JOptionPane.showConfirmDialog(null, 
							"This will also delete any downloaded data for the selected repositories.\n Are you sure you want to delete?", "Delete Repositories Message Box",
							JOptionPane.YES_NO_OPTION);

					if (confirmed != JOptionPane.YES_OPTION) {
						return;
					}
				}
				List<RepoElement> removedRepos = new ArrayList<RepoElement>();
				for (int i = indices.length-1; i>=0 ; --i) {
					System.out.println(indices[i]);
					removedRepos.add(repoListModel.getElementAt(indices[i]));
					repoListModel.removeElementAt(indices[i]);
				}
				setTotalRepos();
				RepoElement[] reposToMine = getReposList();
				clearAndAddReposToDB(reposToMine);
				permanentlyDeleteRepos(removedRepos);

			}

		});
	}
	private void permanentlyDeleteRepos(final List<RepoElement> removedRepos) {
		Runnable newThread = new Runnable() {

			@Override
			public void run() {
				MongoDBLayer.getInstance().permanentlyRemoveRepos(removedRepos);

			}
		};
		(new Thread(newThread)).start();	
	}

	public void clearAndAddReposToDB(final RepoElement[] reposToMine) {
		Runnable newThread = new Runnable() {

			@Override
			public void run() {
				MongoDBLayer.getInstance().clearAndAddRepos(reposToMine);

			}
		};
		(new Thread(newThread)).start();
	}
	private void addAddRepoActions() {
		this.addRepo.addActionListener(new AddRepoActionListener(this));
		this.urlText.addActionListener(new AddRepoActionListener(this));

	}

	public RepoElement getRepoElement(String repoURL) {
		try {
			RepoElement returnVal = new RepoElement(repoURL);
			return returnVal;
		} catch (Exception e) {
			return null;
		}

	}

	public void setTotalRepos() {
		totalRepos.setText("Total Repos: "+repoListModel.size());
	}
}
class AddRepoActionListener implements ActionListener {

	private MainPanel mp;
	public AddRepoActionListener(MainPanel mp) {
		this.mp = mp;
	}
	@Override
	public void actionPerformed(ActionEvent e) {

		String repoURL = mp.urlText.getText().trim();
		RepoElement repoElement = null;

		if (repoURL.isEmpty() || (repoElement = mp.getRepoElement(repoURL)) == null) {
			JOptionPane.showMessageDialog(null, "INVALID GITHUB URL!");
		} else if (mp.repoListModel.contains(repoElement)) {
			JOptionPane.showMessageDialog(null, "URL Already Exists!");

		} else  {
			mp.repoListModel.addElement(repoElement);
			mp.setTotalRepos();
			mp.urlText.setText("");
			//Add to DB
			RepoElement[] reposToMine = mp.getReposList();
			mp.clearAndAddReposToDB(reposToMine);
		}
		mp.urlText.selectAll();
		mp.urlText.requestFocusInWindow();

	}
	

}
