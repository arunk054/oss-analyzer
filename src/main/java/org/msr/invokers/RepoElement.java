package org.msr.invokers;

public class RepoElement {

	private static final String GITHUB_DOMAIN = "https://github.com/";
	private String repo;
	@Override
	public String toString() {
		return this.url;
	}
	public String getRepo() {
		return repo;
	}

	@Override
	public int hashCode() {	
		return this.url.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		RepoElement other = (RepoElement) obj;
		return this.url.equalsIgnoreCase(other.url);
	}
	public void setRepo(String repo) {
		this.repo = repo;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	private String owner;
	private String url;

	public RepoElement(String repoURL) throws InvalidRepoURLException {
		//check if valid URL
		setOwnerRepo(repoURL);
		this.url = GITHUB_DOMAIN+owner+"/"+repo;
	
	}
	
	private void setOwnerRepo(String repoURL) throws InvalidRepoURLException {
		String githubDomain ="github.com/";
		int domainStrLen = githubDomain.length();
		int i = repoURL.indexOf(githubDomain);
		if (i==-1)
			throw new InvalidRepoURLException();
		i+=domainStrLen;
		while(i<repoURL.length() && repoURL.charAt(i)=='/') {
			i++;
		}
		owner = repoURL.substring(i, repoURL.indexOf("/",i+1));
		i += owner.length();
		while(i<repoURL.length() && repoURL.charAt(i)=='/') {
			i++;
		}
		int indexOfSlash = repoURL.indexOf("/",i);
		if (indexOfSlash == -1)
			indexOfSlash = repoURL.length();
		repo = repoURL.substring(i,indexOfSlash);
		
		if (owner.isEmpty() || repo.isEmpty())
			throw new InvalidRepoURLException();
		
	}
	

}

class InvalidRepoURLException extends Exception {

	public InvalidRepoURLException(Exception e) {
		super(e.getMessage());
	}

	public InvalidRepoURLException() {
		// TODO Auto-generated constructor stub
	}
	
}
