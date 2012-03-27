/*
 File: AbstractLoadNetworkTask.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.task.internal.loadnetwork;

import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Properties;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.loadnetwork.LoadNetworkURLTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;

/**
 * Task to load a new network.
 */
abstract public class AbstractLoadNetworkTask extends AbstractTask {
	
	@ProvidesTitle
	public String getTitle() {
		return "Import Network";
	}
	
	private final String VIEW_THRESHOLD = "viewThreshold";
	private static final int DEF_VIEW_THRESHOLD = 3000;
	
	protected int viewThreshold;
	
	protected CyNetworkReader reader;
	protected URI uri;
	protected TaskMonitor taskMonitor;
	protected String name;
	protected boolean interrupted = false;
	protected CyNetworkReaderManager mgr;
	protected CyNetworkManager networkManager;
	protected CyNetworkViewManager networkViewManager;
	protected Properties props;
	protected CyNetworkNaming namingUtil;

	public AbstractLoadNetworkTask(final CyNetworkReaderManager mgr, final CyNetworkManager networkManager,
			final CyNetworkViewManager networkViewManager, final Properties props, final CyNetworkNaming namingUtil) {
		this.mgr = mgr;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.props = props;
		this.namingUtil = namingUtil;
		
		this.viewThreshold = getViewThreshold();
	}

	protected void loadNetwork(final CyNetworkReader viewReader) throws Exception {
		if (viewReader == null)
			throw new IllegalArgumentException("Could not read file: Network View Reader is null.");

		if (taskMonitor != null) {
			taskMonitor.setStatusMessage("Reading in Network Data...");
			taskMonitor.setProgress(0.0);
			taskMonitor.setStatusMessage("Creating Cytoscape Network...");
		}
		insertTasksAfterCurrentTask(viewReader, new GenerateNetworkViewsTask(name, viewReader, networkManager,
				networkViewManager, namingUtil, viewThreshold));
		
		if (taskMonitor != null)
			taskMonitor.setProgress(1.0);
	}

	private int getViewThreshold() {
		final String vts = props.getProperty(VIEW_THRESHOLD);
		int threshold;
		try {
			threshold = Integer.parseInt(vts);
		} catch (Exception e) {
			threshold = DEF_VIEW_THRESHOLD;
		}

		return threshold;
	}
}
