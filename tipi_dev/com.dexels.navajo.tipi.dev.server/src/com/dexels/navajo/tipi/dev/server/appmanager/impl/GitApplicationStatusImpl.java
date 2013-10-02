package com.dexels.navajo.tipi.dev.server.appmanager.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.tipi.dev.server.appmanager.AppStoreOperation;
import com.dexels.navajo.tipi.dev.server.appmanager.ApplicationManager;
import com.dexels.navajo.tipi.dev.server.appmanager.ApplicationStatus;
import com.dexels.navajo.tipi.dev.server.appmanager.GitApplicationStatus;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;

public class GitApplicationStatusImpl extends ApplicationStatusImpl implements
		ApplicationStatus, GitApplicationStatus {

	
	private final static Logger logger = LoggerFactory.getLogger(GitApplicationStatusImpl.class);

	private ApplicationManager applicationManager;
	private File privateKey;
	private File publicKey;
	private String branch;
	private String gitUrl;
	private AppStoreOperation xsdBuild;
	private AppStoreOperation jnlpBuild;
	private AppStoreOperation cacheBuild;

	private Repository repository;

	private Git git;

	private RevCommit lastCommit;

	public ApplicationManager getApplicationManager() {
		return applicationManager;
	}

	public void setXsdBuild(AppStoreOperation xsdBuild) {
		this.xsdBuild = xsdBuild;
	}

	public void clearXsdBuild(AppStoreOperation xsdBuild) {
		this.xsdBuild = null;
	}
	public void setJnlpBuild(AppStoreOperation jnlpBuild) {
		this.jnlpBuild = jnlpBuild;
	}

	public void clearJnlpBuild(AppStoreOperation jnlpBuild) {
		this.jnlpBuild = null;
	}
	public void setCacheBuild(AppStoreOperation cacheBuild) {
		this.cacheBuild = cacheBuild;
	}
	public void clearCacheBuild(AppStoreOperation cacheBuild) {
		this.cacheBuild = null;
	}
	
	public void setApplicationManager(ApplicationManager applicationManager) {
		this.applicationManager = applicationManager;
	}

	public void clearApplicationManager(ApplicationManager applicationManager) {
		this.applicationManager = null;
	}

	public String getLastCommitVersion() {
		if(lastCommit!=null) {
			return lastCommit.getId().toObjectId().toString();
		}
		return null;
	}

	public String getLastCommitMessage() {
		if(lastCommit!=null) {
			return lastCommit.getFullMessage();
		}
		return null;
	}

	public Date getLastCommitDate() {
		if(lastCommit!=null) {
			PersonIdent authorIdent = lastCommit.getAuthorIdent();
			if(authorIdent!=null) {
				return authorIdent.getWhen();
			}
		}
		return null;
	}
	public String getLastCommitAuthor() throws NoHeadException, GitAPIException {
		if(lastCommit!=null) {
			PersonIdent authorIdent = lastCommit.getAuthorIdent();
			if(authorIdent!=null) {
				return authorIdent.toString();
			}
		}
		return null;
	}

	
	public void deactivate() {
		if(repository!=null) {
			repository.close();
		}
	}
	
	@Override
	public void activate(Map<String,Object> settings) throws IOException {
		File gitRepoFolder = new File(applicationManager.getStoreFolder(),"applications");
		gitUrl = (String) settings.get("url");
		String reponame = (String) settings.get("repositoryname");
		String key = (String) settings.get("key");
		branch = (String) settings.get("branch");
		String combinedname = reponame + "-"+branch;
		applicationFolder = new File(gitRepoFolder,combinedname);
		File keyFolder = new File(applicationManager.getStoreFolder(),"gitssh");
		
		super.load(applicationFolder);
		privateKey = null;
		publicKey = null;
		privateKey = new File(keyFolder,key);
		publicKey = new File(keyFolder,key+".pub");
		try {
			
			if(applicationFolder.exists()) {
				callPull();
			} else {
				callClone();
			}
			
		} catch (InvalidRemoteException e) {
			logger.error("Error: ", e);
		} catch (TransportException e) {
			logger.error("Error: ", e);
		} catch (GitAPIException e) {
			logger.error("Error: ", e);
		} catch(Throwable t) {
			logger.error("Error: ", t);
			
		}

	}

    
	@Override
	public void callPull() throws GitAPIException,
			IOException {
		File gitSubfolder = new File(applicationFolder,".git");
		if(!gitSubfolder.exists()) {
			logger.info("Folder: "+applicationFolder.getAbsolutePath()+" is not a git repo. Not pulling.");
		}
		repository = getRepository(applicationFolder);
		git = new Git(repository);
		git.pull().setProgressMonitor(new NavajoProgress()).call();
		logger.info("Current branch: "+repository.getBranch());
//		if(!repository.getBranch().equals(branch)) {
//			git.checkout().setName(branch).call();
//		}
		git.clean().call();
		git.reset().setMode(ResetType.HARD).call();
		xsdBuild.build(this);
		cacheBuild.build(this);
		Iterable<RevCommit> log = git.log().call();
		lastCommit = log.iterator().next();
		
	}

	@Override
	public void callClone() throws GitAPIException,
			InvalidRemoteException, TransportException, IOException {


//		JschConfigSessionFactory jc = new JschConfigSessionFactory() {
			
//		    @Override
//		    protected void configure(OpenSshConfig.Host host, Session session) {
//		        session.setConfig("StrictHostKeyChecking", "yes");
//		    }
//		};
	    JSch jsch = new JSch();
	    JSch.setLogger(new com.jcraft.jsch.Logger() {
			
			@Override
			public void log(int arg0, String txt) {
//				System.err.println(">>>"+txt);
			}
			
			@Override
			public boolean isEnabled(int arg0) {
				return true;
			}
		});
	    try {
	        jsch.addIdentity(privateKey.getAbsolutePath(),publicKey.getAbsolutePath());
//	        jsch.setKnownHosts("/Users/frank/.ssh/known_hosts");
	    } catch (JSchException e) {
	        e.printStackTrace();  
	    }
//	    SshSessionFactory.setInstance(jc);
		
	    CredentialsProvider user = CredentialsProvider.getDefault(); // new
	    
	    git = Git.cloneRepository().setProgressMonitor(new NavajoProgress()).
				setBare(false).setCloneAllBranches(true).setDirectory(applicationFolder).
				setURI(gitUrl).setCredentialsProvider(user).call();
	    repository = git.getRepository();
//		if(branch!=null) {
//			clone.setBranch(branch);
//		}
	    
	    git.branchCreate() 
	       .setName(branch)
	       .setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM)
	       .setStartPoint("origin/"+branch)
	       .setForce(true)
	       .call(); 	    
//		git.branchCreate().setName(branch).call();
//		git.checkout().setName("Acceptance").call();

		git.checkout().setName(branch).call();
		StoredConfig config = git.getRepository().getConfig();
		config.setString("remote", "origin", "fetch", "+refs/*:refs/*");
//		[branch "Production"]
//				remote = origin
//				merge = refs/heads/Production
		config.setString("branch",branch,"merge","refs/heads/"+branch);
		config.setString("branch",branch,"remote","origin");
		config.save();
		callPull();
		jnlpBuild.build(this);

	}

	private Repository getRepository(File basePath) throws IOException {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Repository repository = builder.setGitDir(new File(basePath,".git"))
				.readEnvironment().findGitDir().build();
		return repository;
	}

	public static void main(String[] args) throws IOException, GitAPIException {
//		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Git git = Git.open(new File("/Users/frank/git/navajo/tipi_dev/com.dexels.navajo.tipi.dev.store/applications/com.sportlink.club-Acceptance"));
		List<Ref> aa = git.branchList().call();
		System.err.println("aa: "+aa+" size: "+aa.size());
//		git.branchCreate().setName("Acceptance").call();
		git.checkout().setName("Acceptance").call();
	}
}