/*******************************************************************************
 * Copyright (c) 2012 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.cloudfoundry.ide.eclipse.internal.server.ui.editor;

import org.cloudfoundry.client.lib.domain.CloudService;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;

public class ServiceViewerSorter extends CloudFoundryViewerSorter {
	private final TableViewer tableViewer;

	private final boolean supportsSpace;

	public ServiceViewerSorter(TableViewer tableViewer, boolean supportsSpaces) {
		this.tableViewer = tableViewer;
		this.supportsSpace = supportsSpaces;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		TableColumn sortColumn = tableViewer.getTable().getSortColumn();
		if (sortColumn != null) {
			ServiceViewColumn serviceColumn = (ServiceViewColumn) sortColumn.getData();
			int result = 0;
			int sortDirection = tableViewer.getTable().getSortDirection();
			if (serviceColumn != null) {
				if (e1 instanceof CloudService && e2 instanceof CloudService) {
					CloudService service1 = (CloudService) e1;
					CloudService service2 = (CloudService) e2;

					switch (serviceColumn) {
					case Name:
						result = super.compare(tableViewer, e1, e2);
						break;
					default:
						result = compare(service1, service2, serviceColumn);
						break;
					}

				}
			}
			return sortDirection == SWT.UP ? result : -result;
		}

		return super.compare(viewer, e1, e2);
	}

	protected int compare(CloudService service1, CloudService service2, ServiceViewColumn sortColumn) {
		int result = 0;
		switch (sortColumn) {
		case Type:
			result = service1.getType() != null ? service1.getType().compareTo(service2.getType()) : 0;
			break;
		case Vendor:
			result = supportsSpace ? (service1.getLabel() != null ? service1.getLabel().compareTo(service2.getLabel())
					: 0) : (service1.getVendor() != null ? service1.getVendor().compareTo(service2.getVendor()) : 0);
			break;
		case Plan:
			result = service1.getPlan() != null ? service1.getPlan().compareTo(service2.getPlan()) : 0;
			break;
		case Provider:
			result = service1.getProvider() != null ? service1.getProvider().compareTo(service2.getProvider()) : 0;
			break;
		}
		return result;
	}
}