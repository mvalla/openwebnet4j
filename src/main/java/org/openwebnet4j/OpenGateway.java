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
package org.openwebnet4j;

import java.util.ArrayList;
import java.util.function.Consumer;
import org.openwebnet4j.communication.ConnectorListener;
import org.openwebnet4j.communication.OWNAuthException;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.communication.OpenConnector;
import org.openwebnet4j.communication.Response;
import org.openwebnet4j.message.Dim;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.GatewayMgmt;
import org.openwebnet4j.message.OpenMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract OpenGateway to connect, send commands and receive events from a OpenWebNet gateway
 *
 * @author M. Valla - Initial contribution
 */
public abstract class OpenGateway implements ConnectorListener {

    private final Logger logger = LoggerFactory.getLogger(OpenGateway.class);

    protected boolean isConnected = false;
    protected boolean isDiscovering =
            false; // if true: we have already started a device discovery session

    protected final ArrayList<GatewayListener> listeners = new ArrayList<GatewayListener>();
    protected OpenConnector connector;

    private static final int RECONNECT_RETRY_AFTER = 2500; // ms
    private static final int RECONNECT_RETRY_AFTER_MAX = 60000; // ms
    private static final int RECCONECT_RETRY_MULTIPLIER = 2;
    private boolean connectionCloseRequested = false;

    protected byte[] macAddr;
    private String firmwareVersion = null;

    /** Init the connector for this OpenGateway. */
    protected abstract void initConnector();

    /**
     * Connect to the OpenWebNet gateway.
     *
     * @throws OWNException in case of error during connection
     */
    public void connect() throws OWNException {
        if (isConnected) {
            logger.info("OpenGateway is already connected");
            return;
        }
        connectionCloseRequested = false;
        initConnector();
        connector.setListener(this);
        try {
            connector.openMonConn();
            if (connector.isMonConnected()) {
                connector.openCmdConn();
                if (connector.isCmdConnected()) {
                    handleManagementDimensions(sendInternal(GatewayMgmt.requestMACAddress()));
                    handleManagementDimensions(sendInternal(GatewayMgmt.requestFirmwareVersion()));
                    logger.info("##GW## ============ OpenGateway CONNECTED! ============");
                    isConnected = true;
                    notifyListeners((listener) -> listener.onConnected());
                }
            }
        } catch (OWNException e) {
            logger.error("Error while connecting to Gateway: {}", e.getMessage());
            notifyListeners((listener) -> listener.onConnectionError(e));
            throw e;
        }
    }

    /**
     * Returns true if this OpenGateway is connected.
     *
     * @return true if this OpenGateway is connected
     */
    public boolean isConnected() {
        return isConnected;
    }

    /**
     * Tries to reconnect to the OpenWebNet gateway, waiting increasing time intervals. {@link
     * GatewayListener#onConnectionError} is called each time a connection is tried and fails {@link
     * GatewayListener#onReconnected} is called when reconnection is successful. To stop trying,
     * call {@link #closeConnection()}.
     *
     * @throws OWNAuthException in case of auth error (reconnect is stopped)
     */
    public void reconnect() throws OWNAuthException {
        int retry = RECONNECT_RETRY_AFTER;
        while (!isConnected && !connectionCloseRequested) {
            try {
                logger.debug("--Sleeping {}ms before re-connecting...", retry);
                Thread.sleep(retry);
            } catch (InterruptedException e1) {
                logger.warn("--reconnect cycle interrupted. Exception:{}", e1);
            }
            if (!connectionCloseRequested) {
                logger.info("--...slept {}ms, now trying to re-connect...", retry);
                try {
                    connector.openMonConn();
                    if (connector.isMonConnected()) {
                        connector.openCmdConn();
                        if (connector.isCmdConnected()) {
                            handleManagementDimensions(
                                    sendInternal(GatewayMgmt.requestMACAddress()));
                            handleManagementDimensions(
                                    sendInternal(GatewayMgmt.requestFirmwareVersion()));
                            isConnected = true;
                            notifyListeners((listener) -> listener.onReconnected());
                        }
                    }
                } catch (OWNAuthException ae) { // in case of auth exception, we stop re-trying
                    logger.warn("--Re-connect FAILED. OWNAuthException: {}", ae.getMessage());
                    throw ae;
                } catch (OWNException e) {
                    logger.debug("--Error while re-connecting: {}", e.getMessage());
                    retry = retry * RECCONECT_RETRY_MULTIPLIER;
                    if (retry >= RECONNECT_RETRY_AFTER_MAX) {
                        retry = RECONNECT_RETRY_AFTER_MAX;
                    }
                    notifyListeners((listener) -> listener.onConnectionError(e));
                }
            }
        }
    }

    /**
     * Send a command message, and returns the response messages
     *
     * @param msg the {@link OpenMessage} to be sent
     * @return the {@link Response} messages received as response
     * @throws OWNException on send/response reading error
     */
    public Response send(OpenMessage msg) throws OWNException {
        if (isConnected) {
            return sendInternal(msg);
        } else {
            throw new OWNException("Error while sending message: the gateway is not connected");
        }
    }

    /**
     * Send a command message with high priority, and returns the response messages
     *
     * @param msg the {@link OpenMessage} to be sent
     * @return the {@link Response} messages received as response
     * @throws OWNException on send/response reading error
     */
    public Response sendHighPriority(OpenMessage msg) throws OWNException {
        // TODO sendHighPriority method
        logger.debug("------< sendHighPriority NOT YET IMPLEMENTED, using normal SEND >------");
        return send(msg);
    }

    protected Response sendInternal(OpenMessage msg) throws OWNException {
        return connector.sendCommandSynch(msg.getFrameValue());
    }

    /**
     * Returns true if CMD connection is ready to send messages (connector must be connected and in
     * case of BUS connection checks if a CMD was sent recently &lt; 120sec)
     *
     * @return boolean
     */
    public abstract boolean isCmdConnectionReady();

    @Override
    public void onMessage(OpenMessage message) {
        notifyListeners((listener) -> listener.onEventMessage(message));
    }

    @Override
    public void onMonDisconnected(OWNException e) {
        logger.debug("##GW## onMonDisconnected() OWNException={}", e.getMessage());
        notifyListeners((listener) -> listener.onDisconnected(e));
        isConnected = false;
    }

    private void handleManagementDimensions(Response res) {
        if (res != null) {
            for (OpenMessage msg : res.getResponseMessages()) {
                if (msg instanceof GatewayMgmt) {
                    GatewayMgmt gmsg = (GatewayMgmt) msg;
                    logger.debug("##GW## handleManagementDimensions() for frame: {}", gmsg);
                    Dim thisDim = gmsg.getDim();
                    if (thisDim == GatewayMgmt.DimGatewayMgmt.MAC_ADDRESS) {
                        try {
                            macAddr = GatewayMgmt.parseMACAddress(gmsg);
                            logger.info("##GW## MAC ADDRESS: {}", getMACAddr());
                        } catch (FrameException e) {
                            logger.warn("##GW## Cannot parse MAC address from message: {}", gmsg);
                        }
                    } else if (thisDim == GatewayMgmt.DimGatewayMgmt.FIRMWARE_VERSION) {
                        try {
                            firmwareVersion = GatewayMgmt.parseFirmwareVersion(gmsg);
                            logger.info("##GW## FIRMWARE: {}", getFirmwareVersion());
                        } catch (FrameException e) {
                            logger.warn(
                                    "##GW## Cannot parse firmware version from message: {}", gmsg);
                        }
                    } else {
                        logger.debug(
                                "##GW## handleManagementDimensions DIM {} not supported", thisDim);
                    }
                }
            }
        }
    }

    /**
     * Add a listener for events from this OpenGateway.
     *
     * @param listener the {@link GatewayListener} to add
     */
    public void subscribe(GatewayListener listener) {
        synchronized (listeners) {
            // check if this listener is already registered
            if (listeners.contains(listener)) {
                logger.debug("Event Listener {} already registered", listener);
                return;
            }
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener for events from this OpenGateway.
     *
     * @param listener the {@link GatewayListener} to remove.
     */
    public void unsubscribe(GatewayListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Generic method to notify registered OpenListener about 'method' event. Thread safe. A
     * (single) notification thread is used.
     *
     * @param method the method to be notified
     */
    protected void notifyListeners(Consumer<? super GatewayListener> method) {
        ArrayList<GatewayListener> listenersCopy;
        synchronized (listeners) {
            listenersCopy = new ArrayList<GatewayListener>(listeners);
        }
        // TODO use notifierExecutor instead of Thread, like in OpenConnector
        // Execute 'method' on each of the listeners, using a new notifier Thread
        Thread notifier =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                listenersCopy.forEach(method);
                            }
                        });
        notifier.start();
    }

    /**
     * Start a device discovery session and notify {@link GatewayListener}s for each new device
     * discovered calling method {@link GatewayListener#onNewDevice}
     *
     * @throws OWNException in case of error while discovering
     */
    public void discoverDevices() throws OWNException {
        // TODO add timeout for discoverDevices()
        logger.debug("##GW## ----- discoverDevices()");
        if (isDiscovering) {
            logger.warn("##GW## ----- discovery already in progress -> SKIPPING...");
        } else if (!isConnected) {
            logger.warn("##GW## ----- cannot perform discovery: gateway is not connected.");
        } else {
            isDiscovering = true;
            discoverDevicesInternal();
        }
    }

    /**
     * Check if the gateway has started a device discovery session
     *
     * @return true if the gateway has started a device discovery session
     */
    public boolean isDiscovering() {
        return isDiscovering;
    }

    protected abstract void discoverDevicesInternal() throws OWNException;

    /**
     * Returns the firmware version of the gateway (e.g. 1.2.3)
     *
     * @return String containing gateway firmware version, null if unknown
     */
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    /**
     * Returns the MAC address of the gateway as human readable String
     *
     * @return gateway MAC address as String (XX:YY:ZZ:...), or null if unknown
     */
    public String getMACAddr() {
        if (macAddr != null) {
            StringBuilder sb = new StringBuilder(18);
            for (byte b : macAddr) {
                if (sb.length() > 0) {
                    sb.append(':');
                }
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } else {
            return null;
        }
    }

    /** Closes connection to the gateway and releases resources */
    public void closeConnection() {
        connectionCloseRequested = true;
        connector.disconnect();
        isConnected = false;
    }
}
