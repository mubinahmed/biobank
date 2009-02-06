package edu.ualberta.med.biobank.model;

import gov.nih.nci.system.applicationservice.WritableApplicationService;

import java.util.ArrayList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;

public class SessionNode extends WsObject {
	private ArrayList<SiteNode> siteNodes = null;
	
	private ListenerList listeners;
	
	private WritableApplicationService appService;
	
	public SessionNode(WritableApplicationService appService, String name) {
		super(null);
		this.appService = appService;
		setName(name);
	}
	
	public void addSite(Site site) {
		if (siteNodes == null) {
			siteNodes = new ArrayList<SiteNode>();
		}
		
		// is site has already been added, get rid of old one
		if (!siteNodes.isEmpty())
			removeSite(site);
		
		SiteNode siteNode = new SiteNode(this, site);
		siteNode.setParent(this);
		siteNodes.add(siteNode);
		fireChildrenChanged(null);
	}

	public void removeSite(Site site) {
		if (siteNodes == null) return;
		
		SiteNode nodeToRemove = null;

		for (SiteNode node : siteNodes) {
			if (node.getSite().getId().equals(site.getId())) 
				nodeToRemove = node;
		}
		
		if (nodeToRemove != null)
			siteNodes.remove(nodeToRemove);
		
		if (siteNodes.isEmpty())
			siteNodes = null;
	}
	
	public boolean containsSite(Site site) {
		if (siteNodes == null) return false;
		
		for (SiteNode node : siteNodes) {
			if (node.getSite().getId().equals(site.getId())) return true;
		}
		return false;
	}
	
	public SiteNode[] getSites() {
		if (siteNodes == null) {
			return new SiteNode[0];
		}
		return (SiteNode[]) siteNodes.toArray(new SiteNode[siteNodes.size()]);
	}
	
	public SiteNode getSite(int id) {
		if (siteNodes == null) return null;
		
		for (SiteNode node : siteNodes) {
			if (node.getSite().getId().equals(id)) return node;
		}
		Assert.isTrue(false, "node with id " + id + " not found");
		return null;
	}
	
	public WritableApplicationService getAppService() {
		return appService;
	}


	public void addListener(ISessionNodeListener listener) {
		if (listeners == null)
			listeners = new ListenerList();
		listeners.add(listener);
	}

	public void removeListener(ISessionNodeListener listener) {
		if (listeners != null) {
			listeners.remove(listener);
			if (listeners.isEmpty())
				listeners = null;
		}
	}

	protected void fireChildrenChanged(SiteNode siteNode) {
		if (listeners == null) return;
		
		for (Object l : listeners.getListeners()) {
			((ISessionNodeListener) l).sessionChanged(this, siteNode);
		}
	}

	@Override
	public int getId() {
		return 0;
	}
}
