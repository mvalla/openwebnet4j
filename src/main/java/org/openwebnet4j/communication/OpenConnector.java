/**
 * Copyright (c) 2020-2021 Contributors to the openwebnet4j project
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

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openwebnet4j.message.AckOpenMessage;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.GatewayMgmt;
import org.openwebnet4j.message.OpenMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract connector for communicating with an OpenWebNet gateway using command command (CMD) and
 * monitoring (MON) connections
 *
 * @author M. Valla - Initial contribution
 */
public abstract class OpenConnector {
    protected static final int OWN_MAX_DATA = 1024;

    private final Logger logger = LoggerFactory.getLogger(OpenConnector.class);

    protected FrameChannel cmdChannel;
    protected FrameChannel monChannel;

    protected boolean isCmdConnected = false;
    protected boolean isMonConnected = false;

    protected long lastCmdFrameSentTs = -1;

    protected OWNReceiveThread monRcvThread;

    protected ConnectorListener listener;
    protected ExecutorService notifierExecutor = Executors.newSingleThreadExecutor(); // single thread executor to
    // notify listener

    /**
     * Opens command (CMD) connection
     *
     * @throws OWNException in case of error
     */
    public abstract void openCmdConn() throws OWNException;

    /**
     * Opens monitor (MON) connection
     *
     * @throws OWNException in case of error
     */
    public abstract void openMonConn() throws OWNException;

    /**
     * Check if CMD is connected
     *
     * @return boolean true if connected
     */
    public boolean isCmdConnected() {
        return isCmdConnected;
    }

    /**
     * Get timestamp of last CMD frame sent successfully
     *
     * @return long timestamp
     */
    public long getLastCmdFrameSentTs() {
        return lastCmdFrameSentTs;
    }

    /**
     * Check if MON is connected
     *
     * @return boolean true if connected
     */
    public boolean isMonConnected() {
        return isMonConnected;
    }

    /**
     * Sets the {@link ConnectorListener} for MONITOR events for this OpenConnector
     *
     * @param listener the ConnectorListener to set
     */
    public void setListener(ConnectorListener listener) {
        this.listener = listener;
    }

    /**
     * Send a command frame String on the connection, waits for a {@link AckOpenMessage} (ACN/NACK)
     * or timeout and returns the received messages in a {@link Response} object
     *
     * @param frame the frame String to send
     * @return {@link Response} object with messages received as response
     * @throws OWNException in case of error while sending command frame or reading response
     */
    // TODO add timeout??
    public synchronized Response sendCommandSynch(String frame) throws OWNException {
        if (!isCmdConnected()) {
            throw new OWNException("CMD is not connected");
        }
        try {
            return sendCommandSynchInternal(frame);
        } catch (IOException e) {
            logger.debug("##OPEN-conn## IOException while sending frame {} or reading response: {}", frame,
                    e.getMessage());
            throw new OWNException(
                    "IOException while sending frame " + frame + " or reading response: " + e.getMessage(), e);
        } catch (FrameException e) {
            logger.warn("##OPEN-conn## FrameException while sending frame {} or reading response: {}", frame,
                    e.getMessage());
            throw new OWNException(
                    "FrameException while sending frame " + frame + " or reading response: " + e.getMessage(), e);
        }
    }

    protected abstract Response sendCommandSynchInternal(String frame) throws IOException, FrameException;

    /**
     * Process a frame string received
     *
     * @param newFrame the new frame received
     */
    protected abstract void processFrame(String newFrame);

    /**
     * Notify new message on MON connection to OpenListener using notifierExecutor
     *
     * @param msg the {@link OpenMessage} to be notified
     */
    protected void notifyListener(OpenMessage msg) {
        logger.trace("notifyListener for message: {}", msg);
        notifierExecutor.submit(() -> {
            try {
                logger.trace("##OPEN-conn## notifyListener:Executing EXECUTOR : {}, message={}",
                        Thread.currentThread().getName(), msg);
                listener.onMessage(msg);
            } catch (Exception e) {
                logger.warn("##OPEN-conn## Error while notifying message {} to listener: {}", msg, e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /** OWNReceiveThread is a thread to read frames from MON InputStream */
    protected class OWNReceiveThread extends Thread {

        public OWNReceiveThread(String name) {
            super(name);
        }

        private boolean stopRequested = false;
        private final Logger logger = LoggerFactory.getLogger(OWNReceiveThread.class);

        /** Run method to read frames from MON */
        @Override
        public void run() {
            String fr;
            logger.debug("{} - STARTED", getName());
            while (!stopRequested) {
                try {
                    fr = monChannel.readFrames();
                    if (fr == null) {
                        logger.debug("{} readFrame() returned null", getName());
                        if (!stopRequested) {
                            handleMonDisconnect(new OWNException(getName() + " readFrame() returned null"));
                            break;
                        }
                    } else {
                        processFrame(fr);
                    }
                } catch (SocketTimeoutException st) {
                    logger.debug("{} - got SocketTimeoutException", getName());
                    if (stopRequested) {
                        logger.debug("{} - stopRequested, do nothing.", getName());
                    } else {
                        if (!isMonConnected) {
                            logger.debug("{} - MON is not connected, do nothing.", getName());
                        } else {
                            logger.info("{} - sending CMD message to see if gw is still reachable...", getName());
                            try {
                                Response res = sendCommandSynchInternal(GatewayMgmt.requestModel().getFrameValue());
                                if (res.isSuccess()) {
                                    logger.debug("{} - gw is still reachable!", getName());
                                } else {
                                    handleMonDisconnect(new OWNException(
                                            getName() + " - gw response while checking if still reachable: " + res,
                                            st));
                                    break;
                                }
                            } catch (IOException | FrameException e) {
                                logger.debug("{} - Exception while checking if gw is still reachable: {}", getName(),
                                        e.getMessage());
                                handleMonDisconnect(new OWNException(getName()
                                        + " - exception while checking if gw is still reachable: " + e.getMessage(),
                                        e));
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    logger.debug("{} - got IOException: {}", getName(), e.getMessage());
                    if (!stopRequested) {
                        handleMonDisconnect(new OWNException(getName() + " got IOException: " + e.getMessage(), e));
                        break;
                    }
                }
            }
            logger.debug("{} - thread STOPPED", getName());
        }

        protected synchronized void stopReceiving() {
            stopRequested = true;
        }
    } /* END class OWNReceiveThread */

    /**
     * Called when MON connection is disconnected
     *
     * @param e the {link OWNException} received when disconnected
     */
    protected void handleMonDisconnect(OWNException e) {
        logger.debug("##OPEN-conn## handleMonDisconnect() OWNException={}", e.getMessage());
        disconnectMonChannel();
        listener.onMonDisconnected(e);
    }

    /** Disconnects both MON and CMD connections and stops MON receive thread */
    public void disconnect() {
        logger.debug("##OPEN-conn## OpenConnector.disconnect() ...");
        if (monRcvThread != null) {
            logger.debug("##OPEN-conn## ... sending STOP to MON RcvThread ...");
            monRcvThread.stopReceiving();
            monRcvThread = null;
        }
        logger.debug("##OPEN-conn## ... closing all streams ...");
        disconnectCmdChannel();
        disconnectMonChannel();
        logger.debug("##OPEN-conn## ... all streams closed!");
    }

    protected void disconnectMonChannel() {
        isMonConnected = false;
        if (monChannel != null) {
            monChannel.disconnect();
        }
    }

    protected void disconnectCmdChannel() {
        isCmdConnected = false;
        if (cmdChannel != null) {
            cmdChannel.disconnect();
        }
    }
}
