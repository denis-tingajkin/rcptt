/*******************************************************************************
 * Copyright (c) 2009, 2015 Xored Software Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xored Software Inc - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.rcptt.runtime.internal.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.rcptt.core.launching.events.AutEventLocation;
import org.eclipse.rcptt.core.launching.events.EventsFactory;
import org.eclipse.rcptt.reporting.core.ReportManager;
import org.eclipse.rcptt.runtime.AutEventManager;
import org.eclipse.rcptt.runtime.Q7Monitor;
import org.eclipse.rcptt.tesla.ui.ide.events.UIIDEManager;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
// TODO (e4 support): implement 'get-views' and 'get-perspectives' for e4
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.rcptt.runtime.ui";

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		// TODO (e4 support): review
		if (PlatformUI.isWorkbenchRunning()) {
			if (AutEventManager.getQ7EclPort() != -1) {
				new Q7Monitor().start();
				sendInitialState();
				UIIDEManager.addListener(new UIIDEManager.IUIIDEListener() {
					public void handleNewWorkspaceLocation(String path) {
						AutEventLocation location = EventsFactory.eINSTANCE
								.createAutEventLocation();
						location.setLocation(path);
						try {
							AutEventManager.getInstance().sendEvent(location);
						} catch (CoreException e) {
							log(e.getMessage(), e);
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					}
				});
			}
			ReportManager.reload();
		}
	}

	private void sendInitialState() {
		AutEventManager.getInstance().sendInit();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static void log(Throwable t) {
		log(createStatus(t));
	}

	public static void log(String message, Throwable t) {
		log(createStatus(message, t));
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void info(String message, Object... args) {
		info(null, message, args);
	}

	public static void info(Throwable e, String message, Object... args) {
		log(new Status(Status.INFO, PLUGIN_ID, String.format(message, args), e));
	}

	public static void err(Throwable cause, String message, Object... args) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, String.format(message, args), cause));
	}

	public static IStatus createStatus(Throwable t) {
		return new Status(Status.ERROR, PLUGIN_ID, t.getMessage(), t);
	}

	public static IStatus createStatus(String message, Throwable t) {
		return new Status(Status.ERROR, PLUGIN_ID, message, t);
	}

}
