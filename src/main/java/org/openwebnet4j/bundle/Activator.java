/**
 * Copyright (c) 2020-2022 Contributors to the openwebnet4j project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.openwebnet4j.bundle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lifecycle class of the bundle
 *
 * @author M. Valla - Initial contribution
 */
public class Activator implements BundleActivator {

    private final Logger logger = LoggerFactory.getLogger(Activator.class);

    @Override
    public void start(BundleContext context) {
        logger.debug("@@@@@@@@@@@@ ===== Starting openwebnet4j bundle");
    }

    @Override
    public void stop(BundleContext context) {
        logger.debug("@@@@@@@@@@@@ ===== Stopping openwebnet4j bundle");
    }
}
