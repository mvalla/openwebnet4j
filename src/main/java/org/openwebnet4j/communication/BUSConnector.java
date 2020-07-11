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
package org.openwebnet4j.communication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Timer;
import java.util.TimerTask;

import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.OpenMessage;
import org.openwebnet4j.message.UnsupportedFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for communicating with a BUS OpenWebNet gateway
 *
 * @author M. Valla - Initial contribution
 *
 */
public class BUSConnector extends OpenConnector {
    public final static String MON_TYPE = "MON"; // monitor socket type
    public final static String CMD_TYPE = "CMD"; // command socket type
    public final static String CMD_SESSION = "*99*0##"; // old CMD session (that always works)
    public final static String CMD_SESSION_ALT = "*99*9##"; // new command session defined in OWN official docs

    public final static String MON_SESSION = "*99*1##";

    /*
     * TIMEOUTS AND TIMERS
     * ===================
     * OWN gateway closes MON connection after 105s if no keepalive message is sent by client
     * --> we send a keepalive message (ACK) every MON_KEEPALIVE_TIMER on MON socket to keep it alive
     * OWN gateway closes CMD connection after 120s if no new command is sent by client
     * --> we check if existing CMD connection can be re-used, otherwise we create a new one
     *
     * Also, we use:
     * - SOCKET_CONNECT_TIMEOUT when opening a new socket connection to the gateway
     * - CMD_SOCKET_READ_TIMEOUT to wait for an answer on a CMD socket
     * - MON_SOCKET_READ_TIMEOUT to wait for new events from MON socket, if it expires something could be wrong with the
     * connection with gateway not receiving events anymore, so we close the connection and create a new one
     * - HANDSHAKE_TIMEOUT to wait for the handshake process to be completed
     */
    final static int SOCKET_CONNECT_TIMEOUT = 5000; // (ms) time to wait while connecting a new socket to the
                                                    // gateway
    final static int MON_SOCKET_READ_TIMEOUT = 120000; // (ms) time to wait while reading from MON socket

    final static int CMD_SOCKET_READ_TIMEOUT = 30000; // (ms) time to wait while reading from CMD socket

    public final static int MON_KEEPALIVE_TIMER = 90000; // (ms) timer to send keepalives on MON channel

    public final static int HANDSHAKE_TIMEOUT = 2000; // (ms) timeout before handshake must be completed

    public final static String HMAC_SHA1 = "*98*1##";
    public final static String HMAC_SHA2 = "*98*2##";

    private Socket cmdSk;
    private Socket monSk;

    private Timer monKeepaliveTimer;

    int port;
    String host;
    String pwd;

    private final Logger logger = LoggerFactory.getLogger(BUSConnector.class);

    public BUSConnector(String host, int port, String pwd) {
        super();
        this.host = host;
        this.port = port;
        this.pwd = pwd;
    }

    @Override
    public void openCmdConn() throws OWNException {
        if (isCmdConnected) {
            logger.debug("##BUS-conn## CMD is already open");
            return;
        }
        openConnection(CMD_TYPE);
        isCmdConnected = true;
        logger.info("##BUS-conn## ============ CMD CONNECTED ============");
    }

    @Override
    public void openMonConn() throws OWNException {
        if (isMonConnected) {
            logger.debug("##BUS-conn## CMD is already open");
            return;
        }
        openConnection(MON_TYPE);
        isMonConnected = true;
        logger.info("##BUS-conn## ============ MON CONNECTED ============");
        monRcvThread = new OWNReceiveThread("BUS-MON-Rcv");
        monRcvThread.start();
        startMonKeepaliveTimer();
    }

    /**
     * establishes a connection based on type
     */
    private void openConnection(String type) throws OWNException {
        logger.debug("##BUS-conn## Establishing {} connection to BUS Gateway on {}:{}...", type, host, port);
        try {
            FrameChannel ch = connectSocket(type);
            logger.debug("##BUS-conn## ...reading first ACK...");
            String fr = ch.readFrames();
            logger.info("(HS) BUS-{} <<<<==HS {}", type, fr);
            if (OpenMessage.FRAME_ACK.equals(fr)) {
                String session = (type == MON_TYPE ? MON_SESSION : CMD_SESSION);
                ch.sendFrame(session);
                logger.info("(HS) BUS-{} HS==>>>> {}", type, session);
                doHandshake(ch);
            } else {
                throw new OWNException("Could not open BUS " + type + " connection to " + host + ":" + port
                        + " (did not receive fist ACK, received: " + fr + ")");
            }
        } catch (IOException e) {
            throw new OWNException("Could not open BUS " + type + " connection to " + host + ":" + port
                    + " (IOException: " + e.getMessage() + ")", e);
        }
    }

    @Override
    protected synchronized Response sendCommandSynchInternal(String frame) throws IOException, FrameException {
        try {
            Response r = sendCmdAndReadResp(frame);
            logger.debug("##BUS-conn## ^^^^^^^^ REUSED    CONNECTION    ^^^^^^^^");
            return r;
        } catch (IOException ie) {
            logger.debug("##BUS-conn## Exception: {}", ie.getMessage());
            // CMD session could have been closed by gateway, let's close this one an try with another CMD connection
            cmdSk.close();
            isCmdConnected = false;
            logger.info("##BUS-conn## trying with NEW CMD connection...");
            try {
                openCmdConn();
            } catch (OWNException oe) {
                logger.error(
                        "##BUS-conn## openCommandConnection() returned exception ({}) while opening NEW CMD connection",
                        oe.getMessage());
                throw new IOException("Cannot create NEW CMD connection to send message " + frame, oe);
            }
            try {
                Response r = sendCmdAndReadResp(frame);
                logger.debug("##BUS-conn## ^^^^^^^^ USED NEW    CONNECTION    ^^^^^^^^");
                return r;
            } catch (IOException | FrameException e) {
                logger.error("##BUS-conn## sendCmdAndReadResp() returned exception ({}) using NEW connection",
                        e.getMessage());
                throw (e);
            }
        }
    }

    /**
     * helper method for sendCommandSynchInternal()
     */
    private Response sendCmdAndReadResp(String frame) throws IOException, FrameException {
        // TODO add timeout? or CMD_SOCKET_READ_TIMEOUT is enough?
        Response res = new Response(BaseOpenMessage.parse(frame));
        logger.info("BUS-CMD ====>>>> {}", frame);
        cmdChannel.sendFrame(frame);
        String fr;
        while (!res.hasFinalResponse()) {
            logger.trace("now reading new frame...");
            fr = cmdChannel.readFrames();
            if (fr != null) {
                res.addResponse(BaseOpenMessage.parse(fr));
                logger.debug("BUS-CMD   <<==   {}", fr);
            } else {
                throw new IOException("Received null frame while reading responses to command");
            }
        }
        logger.info("BUS-CMD <<<<==== {}", res.getResponseMessages());
        return res;
    }

    @Override
    protected void processFrame(String newFrame) {
        logger.info("BUS-MON <<<<<<<< {}", newFrame);
        OpenMessage msg;
        try {
            msg = BaseOpenMessage.parse(newFrame);
            notifyListener(msg);
        } catch (UnsupportedFrameException e) {
            logger.debug("UNSUPPORTED FRAME: {}, skipping it", newFrame);
        } catch (FrameException e) {
            logger.warn("INVALID FRAME: {}, skipping it", newFrame);
        }
    }

    private void startMonKeepaliveTimer() {
        logger.debug("##BUS-conn## starting MON keepalive timer");
        // cancel previous keepalive timer
        if (monKeepaliveTimer != null) {
            logger.debug("##BUS-conn## cancelling previuos keepalive timer");
            monKeepaliveTimer.cancel();
        }
        monKeepaliveTimer = new Timer();
        monKeepaliveTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (monSk.isClosed()) {
                    logger.debug("##BUS-conn## socket is closed, cancelling keepalive timer: {}",
                            java.lang.Thread.currentThread().getName());
                    this.cancel();
                    return;
                }
                logger.trace("##BUS-conn## sending MON keepalive ACK");
                try {
                    monChannel.sendFrame(OpenMessage.FRAME_ACK);
                    logger.info("BUS-MON =KA=>>>> {}", OpenMessage.FRAME_ACK);
                } catch (IOException e) {
                    logger.debug("##BUS-conn## could not send MON keepalive ACK: exception={}", e.getMessage());
                }
            }
        }, MON_KEEPALIVE_TIMER, MON_KEEPALIVE_TIMER);
    }

    private void stopMonKeepaliveTimer() {
        if (monKeepaliveTimer != null) {
            logger.debug("##BUS-conn## stop keepalive timer");
            monKeepaliveTimer.cancel();
        }
    }

    private FrameChannel connectSocket(String type) throws IOException {
        Socket sk = new Socket();
        SocketAddress endpoint = new InetSocketAddress(host, port);
        if (type.equals(MON_TYPE)) {
            sk.setSoTimeout(MON_SOCKET_READ_TIMEOUT);
        } else {
            sk.setSoTimeout(CMD_SOCKET_READ_TIMEOUT);
        }
        sk.connect(endpoint, SOCKET_CONNECT_TIMEOUT);
        logger.debug("##BUS-conn## {} Socket connected", type);
        if (type.equals(MON_TYPE)) {
            monChannel = new FrameChannel(sk.getInputStream(), sk.getOutputStream(), "BUS-" + MON_TYPE);
            monSk = sk;
            return monChannel;
        } else {
            cmdChannel = new FrameChannel(sk.getInputStream(), sk.getOutputStream(), "BUS-" + CMD_TYPE);
            cmdSk = sk;
            return cmdChannel;
        }
    }

    private void doHandshake(FrameChannel frCh) throws IOException, OWNAuthException {
        logger.debug("(HS) starting HANDSHAKE on channel {}... ", frCh.getName());
        String fr;
        fr = frCh.readFrames();
        logger.info("(HS) {} <<<<==HS {}", frCh.getName(), fr);
        if (OpenMessage.FRAME_ACK.equals(fr)) {
            logger.debug("(HS) ... NO_AUTH: HANDSHAKE COMPLETED !");
        } else {
            logger.debug("(HS) ... NO_AUTH not supported by gateway -> HANDSHAKE FAILED");
            throw new OWNAuthException("Handshake failed: NO_AUTH not supported by gateway");
        }
    }

    @Override
    protected void handleMonDisconnect(OWNException e) {
        super.handleMonDisconnect(e);
        stopMonKeepaliveTimer();
    }

    @Override
    public void disconnect() {
        stopMonKeepaliveTimer();
        super.disconnect();
        try {
            if (cmdSk != null) {
                cmdSk.close();
                cmdSk = null;
            }
            if (monSk != null) {
                monSk.close();
                monSk = null;
            }
            logger.debug("CMD+MON sockets CLOSED");
        } catch (IOException e) {
            logger.debug("IOException during disconnect(): {}", e.getMessage());
        }
    }

}
