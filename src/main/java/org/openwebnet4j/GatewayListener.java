/**
 * Copyright (c) 2020 Contributors to the openwebnet4j project
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
package org.openwebnet4j;

import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.OpenMessage;
import org.openwebnet4j.message.Where;

/**
 * Interface {@link GatewayListener} to listen to event and changes in a {@link OpenGateway}
 *
 * @author M. Valla - Initial contribution
 */
public interface GatewayListener {

    /**
     * This method is called after the connection to the gateway has been established correctly
     */
    public void onConnected();

    /**
     * This method is called when connecting to gateway has returned and error
     *
     * @param error the {@link OWNException} returned
     */
    public void onConnectionError(OWNException error);

    /**
     * This method is called after the gateway connection has been closed calling {@link OpenGateway#closeConnection()}
     */
    public void onConnectionClosed();

    /**
     * This method is called after the connection with gateway has been lost/disconnected
     *
     * @param error the {@link OWNException} returned
     */
    public void onDisconnected(OWNException error);

    /**
     * This method is called after the connection with gateway has been re-connected
     */
    public void onReconnected();

    /**
     * This method is called when a new OpenWebNet message is received on the gateway MON session
     *
     * @param msg the OpenMessage received
     */
    public void onEventMessage(OpenMessage msg);

    /**
     * After {@link OpenGateway#discoverDevices} is called, each time a new device is discovered, this method will be
     * called
     *
     * @param where the discovered device's address (WHERE)
     * @param deviceType device type of the discovered device
     * @param message the OWN message received that identified the device
     */
    public void onNewDevice(Where where, OpenDeviceType deviceType, BaseOpenMessage message);

    /**
     * This method is called after {@link OpenGateway#discoverDevices} is called when device discovery has been
     * completed successfully
     *
     */
    public void onDiscoveryCompleted();
}
