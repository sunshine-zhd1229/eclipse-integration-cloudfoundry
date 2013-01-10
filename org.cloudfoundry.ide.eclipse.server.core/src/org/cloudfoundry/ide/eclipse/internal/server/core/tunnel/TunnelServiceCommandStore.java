/*******************************************************************************
 * Copyright (c) 2012 - 2013 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.cloudfoundry.ide.eclipse.internal.server.core.tunnel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.ide.eclipse.internal.server.core.CloudFoundryPlugin;
import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Typically there should only be one store instance available per session.
 * 
 */
public class TunnelServiceCommandStore {

	public static final String SERVICE_COMMANDS_PREF = CloudFoundryPlugin.PLUGIN_ID + ".service.tunnel.commands";

	private final static ObjectMapper mapper = new ObjectMapper();

	private TunnelServiceCommands cachedCommands;

	public TunnelServiceCommandStore() {
		//
	}

	public synchronized TunnelServiceCommands getTunnelServiceCommands() throws CoreException {
		loadCommandsFromStore();
		return cachedCommands;
	}

	protected void loadCommandsFromStore() throws CoreException {
		String storedValue = CloudFoundryPlugin.getDefault().getPreferences().get(SERVICE_COMMANDS_PREF, null);
		cachedCommands = parseAndUpdateTunnelServiceCommands(storedValue);
	}

	protected TunnelServiceCommands parseAndUpdateTunnelServiceCommands(String json) throws CoreException {
		TunnelServiceCommands commands = null;
		if (json != null) {
			try {
				commands = mapper.readValue(json, TunnelServiceCommands.class);
			}
			catch (IOException e) {

				CloudFoundryPlugin.logError("Error while reading Java Map from JSON response: ", e);
			}
		}

		if (commands == null) {
			// initialise commands for the first time
			commands = new TunnelServiceCommands();

			List<ServerService> services = new ArrayList<ServerService>();
			// Fill in all the default services if creating a clean list of
			// services
			for (ServiceInfo service : ServiceInfo.values()) {
				ServerService serverService = new ServerService();
				serverService.setServiceInfo(service);
				services.add(serverService);
			}
			commands.setServices(services);

		}

		return commands;
	}

	public synchronized String storeServerServiceCommands(TunnelServiceCommands services) throws CoreException {
		String serialisedCommands = null;
		cachedCommands = services;
		if (services != null) {
			if (mapper.canSerialize(services.getClass())) {
				try {
					serialisedCommands = mapper.writeValueAsString(services);
				}
				catch (IOException e) {
					throw new CoreException(CloudFoundryPlugin.getErrorStatus(
							"Error while serialising Java Map from JSON response: ", e));
				}
			}
			else {
				throw new CoreException(CloudFoundryPlugin.getErrorStatus("Value of type "
						+ services.getClass().getName() + " can not be serialized to JSON."));
			}
		}

		if (serialisedCommands != null) {
			IEclipsePreferences prefs = CloudFoundryPlugin.getDefault().getPreferences();
			prefs.put(SERVICE_COMMANDS_PREF, serialisedCommands);
			try {
				prefs.flush();
			}
			catch (BackingStoreException e) {
				CloudFoundryPlugin.logError(e);
			}

		}

		return serialisedCommands;

	}

	public synchronized List<ServiceCommand> getCommandsForService(String vendor, boolean forceLoad)
			throws CoreException {
		List<ServiceCommand> commands = new ArrayList<ServiceCommand>();
		if (forceLoad) {
			loadCommandsFromStore();
		}
		if (cachedCommands != null && cachedCommands.getServices() != null) {
			for (ServerService service : cachedCommands.getServices()) {
				if (service.getServiceInfo().getVendor().equals(vendor)) {
					commands = service.getCommands();
					break;
				}
			}
		}
		return commands;
	}

	public static TunnelServiceCommandStore getCurrentStore() {
		return CloudFoundryPlugin.getDefault().getTunnelCommandsStore();
	}

}
