/*******************************************************************************
 * Copyright (c) 2012, 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.cloudfoundry.ide.eclipse.internal.server.core;

import java.net.URI;
import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.ide.eclipse.internal.server.core.client.CloudFoundryApplicationModule;
import org.cloudfoundry.ide.eclipse.internal.server.core.client.CloudFoundryServerBehaviour;
import org.cloudfoundry.ide.eclipse.server.tests.util.CloudFoundryTestFixture;
import org.cloudfoundry.ide.eclipse.server.tests.util.CloudFoundryTestFixture.Harness;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IModule;
import org.junit.Assert;

public class CloudFoundryWebApplicationContentTest extends AbstractCloudFoundryTest {

	@Override
	protected Harness createHarness() {
		return CloudFoundryTestFixture.current().harness();
	}

	public void testStartModuleCheckWebContent() throws Exception {
		assertCreateApplication(DYNAMIC_WEBAPP_NAME);
		IModule[] modules = server.getModules();

		serverBehavior.startModuleWaitForDeployment(modules, new NullProgressMonitor());
		assertWebApplicationURL(modules, DYNAMIC_WEBAPP_NAME);
		assertApplicationIsRunning(modules, DYNAMIC_WEBAPP_NAME);
		URI uri = new URI("http://" + harness.getUrl(AbstractCloudFoundryTest.DYNAMIC_WEBAPP_NAME) + "/index.html");
		assertEquals("Hello World.", getContent(uri, modules[0], serverBehavior));
	}

	public void testStartModuleWithUsedUrl() throws Exception {
		getClient().deleteAllApplications();

		assertCreateApplication(AbstractCloudFoundryTest.DYNAMIC_WEBAPP_NAME);
		IModule[] modules = server.getModules();

		serverBehavior.startModuleWaitForDeployment(modules, new NullProgressMonitor());

		assertWebApplicationURL(modules, AbstractCloudFoundryTest.DYNAMIC_WEBAPP_NAME);
		assertApplicationIsRunning(modules, AbstractCloudFoundryTest.DYNAMIC_WEBAPP_NAME);

		CloudFoundryApplicationModule module = cloudServer.getExistingCloudModule(modules[0]);

		Assert.assertNull(module.getErrorMessage());

		harness = CloudFoundryTestFixture.current("dynamic-webapp-with-appclient-module",
				harness.getUrl(AbstractCloudFoundryTest.DYNAMIC_WEBAPP_NAME)).harness();
		server = harness.createServer();
		cloudServer = (CloudFoundryServer) server.loadAdapter(CloudFoundryServer.class, null);
		serverBehavior = (CloudFoundryServerBehaviour) server.loadAdapter(CloudFoundryServerBehaviour.class, null);

		harness.createProjectAndAddModule("dynamic-webapp-with-appclient-module");
		modules = server.getModules();

		// FIXME: once we verify what the proper behavior is, we should fail
		// appropriately
		// try {
		// serverBehavior.deployOrStartModule(modules, true, null);
		// Assert.fail("Expects CoreException due to duplicate URL");
		// }
		// catch (CoreException e) {
		// }
		//
		// module = cloudServer.getApplication(modules[0]);
		// Assert.assertNotNull(module.getErrorMessage());
		// try {
		// serverBehavior.getClient().deleteApplication("dynamic-webapp-with-appclient-module");
		// }
		// catch (Exception e) {
		//
		// }
	}

	// This case should never pass since the wizard should guard against
	// duplicate ID
	public void testStartModuleWithDuplicatedId() throws Exception {
		harness = CloudFoundryTestFixture.current("dynamic-webapp-test").harness();
		server = harness.createServer();
		cloudServer = (CloudFoundryServer) server.loadAdapter(CloudFoundryServer.class, null);
		serverBehavior = (CloudFoundryServerBehaviour) server.loadAdapter(CloudFoundryServerBehaviour.class, null);

		assertCreateApplication(DYNAMIC_WEBAPP_NAME);

		// There should only be one module
		IModule[] modules = server.getModules();

		serverBehavior.startModuleWaitForDeployment(modules, new NullProgressMonitor());

		assertWebApplicationURL(modules, DYNAMIC_WEBAPP_NAME);
		assertApplicationIsRunning(modules, DYNAMIC_WEBAPP_NAME);

		serverBehavior.refreshModules(new NullProgressMonitor());
		List<CloudApplication> applications = serverBehavior.getApplications(new NullProgressMonitor());
		boolean found = false;

		for (CloudApplication application : applications) {
			if (application.getName().equals("dynamic-webapp-test")) {
				found = true;
			}
		}

		Assert.assertTrue(found);

		harness.createProjectAndAddModule("dynamic-webapp-with-appclient-module");

		modules = server.getModules();
		serverBehavior.startModuleWaitForDeployment(modules, new NullProgressMonitor());

		// wait 1s until app is actually started
		URI uri = new URI("http://" + harness.getUrl("dynamic-webapp-with-appclient-module") + "/index.html");
		assertEquals("Hello World.", getContent(uri, modules[0], serverBehavior));

		serverBehavior.refreshModules(new NullProgressMonitor());
		applications = serverBehavior.getApplications(new NullProgressMonitor());
		found = false;

		for (CloudApplication application : applications) {
			if (application.getName().equals("dynamic-webapp-test")) {
				found = true;
			}
		}

		Assert.assertTrue(found);
	}

}
