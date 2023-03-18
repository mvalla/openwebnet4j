/**
 * Copyright (c) 2020-2023 Contributors to the openwebnet4j project
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 */
public class BUSConnector extends OpenConnector {
    public static final String MON_TYPE = "MON"; // monitor socket type
    public static final String CMD_TYPE = "CMD"; // command socket type
    public static final String CMD_SESSION = "*99*0##"; // old CMD session (that always works)
    public static final String CMD_SESSION_ALT = "*99*9##"; // new command session defined in OWN official docs

    public static final String MON_SESSION = "*99*1##";

    /*
     * TIMEOUTS AND TIMERS
     * ===================
     * OWN gateway closes a MON connection after 105s if no keepalive message is sent by client
     * --> we send a keepalive message (ACK) every MON_KEEPALIVE_TIMER on MON socket to keep it alive
     * OWN gateway closes a CMD connection after 120s if no new command is sent by client
     * --> we check if existing CMD connection can be re-used, otherwise we create a new one
     *
     * Also, we use:
     * - SOCKET_CONNECT_TIMEOUT when opening a new socket connection to the gateway
     * - CMD_SOCKET_READ_TIMEOUT to wait for an answer on a CMD socket
     * - MON_SOCKET_READ_TIMEOUT to wait for new events from MON socket, if it expires something could be wrong with the
     * MON connection with gateway not receiving events anymore, so we send a CMD to check if gw is still reachable
     * - HANDSHAKE_TIMEOUT to wait for the handshake process to be completed
     */
    static final int SOCKET_CONNECT_TIMEOUT = 5000; // (ms) time to wait while connecting a new socket to the
    // gateway
    static final int MON_SOCKET_READ_TIMEOUT = 120000; // (ms) time to wait while reading from MON socket

    static final int CMD_SOCKET_READ_TIMEOUT = 30000; // (ms) time to wait while reading from CMD socket

    public static final int MON_KEEPALIVE_TIMER = 90000; // (ms) timer to send a keepalive on MON channel

    public static final int HANDSHAKE_TIMEOUT = 2000; // (ms) timeout before handshake must be completed

    public static final String HMAC_SHA1 = "*98*1##";
    public static final String HMAC_SHA2 = "*98*2##";

    private Socket cmdSk;
    private Socket monSk;

    private Timer monKeepaliveTimer;

    int port;
    String host;
    String pwd;

    private final Logger logger = LoggerFactory.getLogger(BUSConnector.class);
    private final Logger msgLogger = LoggerFactory.getLogger(logger.getName() + ".message");
    private final Logger eventLogger = LoggerFactory.getLogger(logger.getName() + ".message.event");
    private final Logger kaLogger = LoggerFactory.getLogger(logger.getName() + ".keepalive");
    private final Logger hsLogger = LoggerFactory.getLogger(logger.getName() + ".handshake");

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

    /** establishes a connection based on type */
    private void openConnection(String type) throws OWNException {
        logger.debug("##BUS-conn## Establishing {} connection to BUS Gateway on {}:{}...", type, host, port);
        try {
            FrameChannel ch = connectSocket(type);
            doHandshake(ch, type);
        } catch (IOException e) {
            throw new OWNException("Could not open BUS-" + type + " connection to " + host + ":" + port
                    + " (IOException: " + e.getMessage() + ")", e);
        }
    }

    @Override
    protected synchronized Response sendCommandSynchInternal(String frame) throws IOException, FrameException {
        try {
            Response r = sendCmdAndReadResp(frame, false);
            logger.debug("##BUS-conn## ^^^^^^^^ REUSED    CONNECTION    ^^^^^^^^");
            return r;
        } catch (IOException ie) {
            logger.debug("##BUS-conn## Exception: {}", ie.getMessage());
            // CMD session could have been closed by gateway, let's close this one an try with
            // another CMD connection
            cmdSk.close();
            isCmdConnected = false;
            logger.info("##BUS-conn## trying NEW CMD connection...");
            try {
                openCmdConn();
            } catch (OWNException oe) {
                logger.warn(
                        "##BUS-conn## openCommandConnection() returned exception ({}) while opening NEW CMD connection",
                        oe.getMessage());
                throw new IOException("Cannot create NEW CMD connection to send message " + frame, oe);
            }

            try {
                Response r = sendCmdAndReadResp(frame, true);
                logger.debug("##BUS-conn## ^^^^^^^^ USED NEW    CONNECTION    ^^^^^^^^");
                return r;
            } catch (IOException | FrameException e) {
                logger.warn("##BUS-conn## sendCmdAndReadResp() returned exception ({}) using NEW connection",
                        e.getMessage());
                throw (e);
            }
        }
    }

    /** helper method for sendCommandSynchInternal() */
    private Response sendCmdAndReadResp(String frame, boolean reopen) throws IOException, FrameException {
        // TODO add timeout? or CMD_SOCKET_READ_TIMEOUT is enough?
        Response res = new Response(BaseOpenMessage.parse(frame));
        cmdChannel.sendFrame(frame);
        lastCmdFrameSentTs = System.currentTimeMillis();
        msgLogger.info("BUS-CMD ====>>>> `{}`" + (reopen ? " [ REOPEN ]" : ""), frame);
        String fr;
        while (!res.hasFinalResponse()) {
            logger.trace("now reading new frame...");
            fr = cmdChannel.readFrames();
            if (fr != null) {
                try {
                    res.addResponse(BaseOpenMessage.parse(fr));
                    msgLogger.debug("BUS-CMD   <<==   `{}`", fr);
                } catch (UnsupportedFrameException ufe) {
                    msgLogger.debug("BUS-CMD   <<=X   `{}` ignoring unsupported response frame ({})", fr,
                            ufe.getMessage());
                }
            } else {
                msgLogger.info("BUS-CMD <<<<==== X [no frames]");
                throw new IOException("Received null frame while reading responses to command");
            }
        }
        msgLogger.info("BUS-CMD <<<<==== `{}`", res.getResponseMessages());

        return res;
    }

    @Override
    protected void processFrame(String newFrame) {
        eventLogger.info("BUS-MON <<<<<<<< `{}`", newFrame);
        OpenMessage msg;
        try {
            msg = BaseOpenMessage.parse(newFrame);
            notifyListener(msg);
        } catch (UnsupportedFrameException e) {
            logger.debug("##BUS-conn## UNSUPPORTED FRAME ON MON: `{}`, skipping it", newFrame);
        } catch (FrameException e) {
            logger.warn("##BUS-conn## INVALID FRAME RECEIVED ON MON: `{}`, skipping it", newFrame);
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
        monKeepaliveTimer.schedule(new TimerTask() {
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
                    kaLogger.info("BUS-MON =KA=>>>> `{}`", OpenMessage.FRAME_ACK);
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
        logger.debug("##BUS-conn## {} socket connected", type);
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

    private void startHandshakeTimeout(FrameChannel frCh) throws OWNAuthException {
        Timer handshakeTimeout = new Timer();
        handshakeTimeout.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!frCh.handshakeCompleted) {
                    logger.warn("(HS) ... handshake not completed but timeout expired, closing {} channel",
                            frCh.getName());
                    frCh.disconnect();
                    // TODO close also MON/CMD sockets ?
                }
            }
        }, HANDSHAKE_TIMEOUT);
    }

    private void doHandshake(FrameChannel frCh, String type) throws IOException, OWNAuthException {
        logger.debug("(HS) starting HANDSHAKE on channel {}... ", frCh.getName());
        startHandshakeTimeout(frCh);
        String fr;
        // STEP-1: wait for ACK from GW
        hsLogger.debug("(HS) ... STEP-1: receive ACK from GW");
        fr = frCh.readFrames();
        hsLogger.info("(HS) {} <<<<==HS `{}`", frCh.getName(), fr);
        if (!(OpenMessage.FRAME_ACK.equals(fr))) {
            hsLogger.warn("(HS) ... STEP-1: HANDSHAKE FAILED, no ACK recevied, received: {}", fr);
            throw new OWNAuthException("Could not open BUS-" + type + " connection to " + host + ":" + port
                    + " (no ACK received at STEP-1, received: " + fr + ")");
        }
        hsLogger.debug("(HS) ... STEP-1: first ACK received");
        // STEP-2: send session request and check for ACK/NACK/NONCE/HMAC from GW
        String session = (type == MON_TYPE ? MON_SESSION : CMD_SESSION);
        hsLogger.debug("(HS) ... STEP-2: send session request {} ... ", session);
        frCh.sendFrame(session);
        hsLogger.info("(HS) BUS-{} HS==>>>> `{}`", type, session);
        fr = frCh.readFrames();
        hsLogger.info("(HS) {} <<<<==HS `{}`", frCh.getName(), fr);
        if (OpenMessage.FRAME_NACK.equals(fr) && type == CMD_TYPE) {
            // try alt CMD session
            hsLogger.debug("(HS) ... STEP-2: received NACK, trying CMD_SESSION_ALT ...");
            frCh.sendFrame(CMD_SESSION_ALT);
            hsLogger.info("(HS) {} HS==>>>> `{}`", frCh.getName(), CMD_SESSION_ALT);
            fr = frCh.readFrames();
            hsLogger.info("(HS) {} <<<<==HS `{}`", frCh.getName(), fr);
        }
        if (OpenMessage.FRAME_ACK.equals(fr)) {
            // STEP-2: NO_AUTH - Free beer and party, the connection is unauthenticated!
            frCh.handshakeCompleted = true;
            hsLogger.debug("(HS) ... STEP-2: NO_AUTH: second ACK received, GW has no pwd ==HANDSHAKE COMPLETED==");
        } else if (fr.matches("\\*#\\d+##")) {
            // STEP-2: OPEN_AUTH passwd nonce received
            doOPENHandshake(fr, frCh);
            frCh.handshakeCompleted = true;
        } else if (fr.equals(HMAC_SHA1) || fr.equals(HMAC_SHA2)) {
            // STEP-2: HMAC_AUTH type received
            doHMACHandshake(fr, frCh);
            frCh.handshakeCompleted = true;
        } else {
            hsLogger.warn("(HS) ... STEP-2: cannot authenticate with gateway (unexpected answer: `{}`)", fr);
            throw new OWNAuthException(
                    "Cannot authenticate with gateway: handshake failed at STEP-2 (unexpected answer: " + fr + ")");
        }
    }

    private void doOPENHandshake(String nonceFrame, FrameChannel frCh) throws IOException, OWNAuthException {
        String nonce = nonceFrame.substring(2, nonceFrame.length() - 2);
        hsLogger.debug("(HS) ... STEP-2: OPEN_AUTH: received nonce=`{}` ... ", nonce);
        // STEP-3: send pwd and check ACK
        String pwdMessage;
        try {
            pwdMessage = OpenMessage.FRAME_START_DIM + Auth.calcOpenPass(pwd, nonce) + OpenMessage.FRAME_END;
        } catch (NumberFormatException e) {
            hsLogger.warn("(HS) ... STEP-3: OPEN_AUTH: invalid gateway password. Password must contain only digits");
            throw new OWNAuthException("Invalid gateway password. Password must contain only digits (OPEN_AUTH)");
        }
        hsLogger.debug("(HS) ... STEP-3: OPEN_AUTH: sending encoded pwd ... ");
        frCh.sendFrame(pwdMessage);
        hsLogger.info("(HS) {} HS==>>>> `{}`", frCh.getName(), pwdMessage);
        String fr = frCh.readFrames();
        hsLogger.info("(HS) {} <<<<==HS `{}`", frCh.getName(), fr);
        if (OpenMessage.FRAME_ACK.equals(fr)) {
            hsLogger.debug("(HS) ... STEP-3: OPEN_AUTH: pwd accepted ==HANDSHAKE COMPLETED==");
            return;
        } else {
            hsLogger.warn("(HS) ... STEP-3: OPEN_AUTH: pwd NOT ACCEPTED");
            throw new OWNAuthException("Password not accepted by gateway, check password configuration (OPEN_AUTH)");
        }
    }

    private void doHMACHandshake(String hmacType, FrameChannel frCh) throws IOException, OWNAuthException {
        hsLogger.debug("(HS) ... STEP-2: HMAC_AUTH: HMAC type received: {}, sending ACK ... ", hmacType);

        // STEP-3: send ACK, wait for HMAC Ra and -based on that- calculate HMAC-encoded pwd
        frCh.sendFrame(OpenMessage.FRAME_ACK);
        hsLogger.info("(HS) {} HS==>>>> `{}`", frCh.getName(), OpenMessage.FRAME_ACK);
        String fr = frCh.readFrames();
        hsLogger.info("(HS) {} <<<<==HS `{}`", frCh.getName(), fr);
        Pattern pattern = Pattern.compile("\\*#(\\d{80,128})##");
        Matcher matcher = pattern.matcher(fr);
        if (matcher.find()) {
            // STEP-3: HMAC Ra received, calculate HMAC-encoded pwd
            String raDigits = matcher.group(1);
            hsLogger.debug("(HS) ... STEP-3: HMAC_AUTH: Ra digits received: {} ...", raDigits);
            String ra = Auth.digitToHex(raDigits);
            hsLogger.trace("(HS) ...       Ra  = {}", ra);
            String rb = Auth.calcHmacRb();
            hsLogger.trace("(HS) ...       Rb  = {}", rb);
            String a = "736F70653E";
            logger.trace("(HS) ...       A   = {}", a);
            String b = "636F70653E";
            hsLogger.trace("(HS) ...       B   = {}", b);
            hsLogger.trace("(HS) ...       pwd = {}", pwd);
            String kab = Auth.calcSHA256(pwd);
            hsLogger.trace("(HS) ...       Kab = {}", kab);
            String hmacRaRbABKab = Auth.calcSHA256(ra + rb + a + b + kab);
            hsLogger.trace("(HS) ... STEP-3: HMAC_AUTH: HMAC(Ra,Rb,A,B,Kab) = {}", hmacRaRbABKab);

            // STEP-4: send calculated HMAC-encoded pwd and check final hash
            String hmacMessage = OpenMessage.FRAME_START_DIM + Auth.hexToDigit(rb) + "*"
                    + Auth.hexToDigit(hmacRaRbABKab) + OpenMessage.FRAME_END;
            hsLogger.debug("(HS) ... STEP-4: HMAC_AUTH: sending <Rb, HMAC(Ra,Rb,A,B,Kab)> ... ");
            frCh.sendFrame(hmacMessage);
            hsLogger.info("(HS) {} HS==>>>> `{}`", frCh.getName(), hmacMessage);
            fr = frCh.readFrames();
            hsLogger.info("(HS) {} <<<<==HS `{}`", frCh.getName(), fr);

            if (OpenMessage.FRAME_NACK.equals(fr)) {
                hsLogger.warn("(HS) ... STEP-4: HMAC_AUTH: pwd NOT ACCEPTED");
                throw new OWNAuthException("Password not accepted by gateway, check password configuration (HMAC)");
            } else {
                matcher = pattern.matcher(fr);
                if (matcher.find()) {
                    // STEP-4: verify final hash
                    String hmacRaRbKab = Auth.digitToHex(matcher.group(1));
                    hsLogger.trace("(HS) ... STEP-4: HMAC_AUTH: final hash HMAC(Ra, Rb, Kab) received: {} ...",
                            hmacRaRbKab);
                    if (Auth.calcSHA256(ra + rb + kab).equals(hmacRaRbKab)) {
                        hsLogger.trace("(HS) ... STEP-4: HMAC_AUTH:  HMAC(Ra, Rb, Kab) --MATCH--, sending ACK ...");
                        frCh.sendFrame(OpenMessage.FRAME_ACK);
                        hsLogger.info("(HS) {} HS==>>>> `{}`", frCh.getName(), OpenMessage.FRAME_ACK);
                        hsLogger.debug("(HS) ... STEP-4: HMAC_AUTH: final ACK sent ==HANDSHAKE COMPLETED==");
                        return;
                    } else {
                        hsLogger.warn(
                                "(HS) ... STEP-4: HMAC_AUTH: HANDSHAKE FAILED, final HMAC(Ra, Rb, Kab) does not match. Received HMAC(Ra, Rb, Kab)={}",
                                hmacRaRbKab);
                        throw new OWNAuthException(
                                "Handshake failed, final HMAC(Ra, Rb, Kab) does not match (HMAC_AUTH STEP-4)");
                    }
                } else {
                    hsLogger.warn(
                            "(HS) ... STEP-4: HMAC_AUTH: HANDSHAKE FAILED, invalid HMAC(Ra, Rb, Kab) received. Response={}",
                            fr);
                    throw new OWNAuthException(
                            "Handshake failed, invalid HMAC(Ra, Rb, Kab) received from GW at HMAC STEP-4: " + fr);
                }
            }
        } else {
            hsLogger.warn("(HS) ... STEP-3: HMAC_AUTH: HANDSHAKE FAILED, invalid Ra received. Response={}", fr);
            throw new OWNAuthException("Handshake failed, no Ra received from GW at HMAC STEP-3: " + fr);
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
