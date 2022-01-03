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
package org.openwebnet4j.communication;

import org.openwebnet4j.message.OpenMessage;

/**
 * The {@link ConnectorListener} interface defines methods to receive MONITOR messages from
 * OpenConnector
 *
 * @author M. Valla - Initial contribution
 */
public interface ConnectorListener {

    /**
     * This method is called when a new {@link OpenMessage} is received by the OpenConnector MONITOR
     * connection
     *
     * @param message the {@link OpenMessage} received
     */
    public void onMessage(OpenMessage message);

    /**
     * This method is called when the MONITOR connection gets disconnected for some error
     *
     * @param e the Exception that caused disconnection
     */
    public void onMonDisconnected(OWNException e);
}
