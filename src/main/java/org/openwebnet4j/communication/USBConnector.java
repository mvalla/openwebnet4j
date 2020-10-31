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
import java.util.Enumeration;
import java.util.TooManyListenersException;

import org.openwebnet4j.message.AckOpenMessage;
import org.openwebnet4j.message.Automation;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.Dim;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.GatewayMgmt;
import org.openwebnet4j.message.Lighting;
import org.openwebnet4j.message.OpenMessage;
import org.openwebnet4j.message.UnsupportedFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.NRSerialPort;
import gnu.io.NoSuchPortException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * Class for communicating with an USB ZigBee OpenWebNet gateway
 *
 * @author M. Valla - Initial contribution
 *
 */
public class USBConnector extends OpenConnector implements SerialPortEventListener {

    private static final int SERIAL_SPEED = 19200; // UART baud as declared in the OWN specs

    private final Logger logger = LoggerFactory.getLogger(USBConnector.class);
    private final Logger msgLogger = LoggerFactory.getLogger(logger.getName() + ".message");
    private final Logger eventLogger = LoggerFactory.getLogger(logger.getName() + ".message.event");
    private final Logger hsLogger = LoggerFactory.getLogger(logger.getName() + ".handshake");

    private boolean isOldFirmware = false;
    private boolean hasAutomationBug = false;

    private String firmwareVersion = null;
    private static final String AUTOMATION_BUG_FIRMWARE_VERSION = "1.2.0"; // firmware versions <= than this are
                                                                           // affected by inverted Automation bug
    private static final String OLD_FIRMWARE_VERSION = "1.2.3"; // firmware versions <= than this are affected by
                                                                // Dimension response bug

    private final String portName; // the serial port name we are connecting to
    private NRSerialPort serialPort; // the serial port, once connected

    private Response currentResponse;

    /**
     * Synchronisation object for buffer queue manipulation
     */
    private final Object bufferSynchronisationObject = new Object();

    public USBConnector(String portName) {
        super();
        this.portName = portName;
    }

    @Override
    public void openCmdConn() throws OWNException {
        if (isCmdConnected) {
            logger.debug("##USB-conn## CMD is already open");
            return;
        }
        logger.debug("##USB-conn## Opening CMD connection to USB ZigBee Gateway on serial port {}...", portName);
        if (serialPort == null) {
            connectUSBDongle(portName);
            checkFirmwareVersion();
        }
        isCmdConnected = true;
        logger.info("##USB-conn## ============ CMD CONNECTED ============");
    }

    @Override
    public void openMonConn() throws OWNException {
        if (isMonConnected) {
            logger.debug("##USB-conn## MON is already open");
            return;
        }
        logger.debug("##USB-conn## Opening MON connection to USB ZigBee Gateway on serial port {}...", portName);
        if (serialPort == null) {
            connectUSBDongle(portName);
            checkFirmwareVersion();
        }
        try {
            // send supervisor to receive all events from devices
            sendCommandSynchInternal(GatewayMgmt.requestSupervisor().getFrameValue());
        } catch (IOException | FrameException e) {
            throw new OWNException("Failed to set supervisor to USB ZigBee Gateway on serial port: " + portName, e);
        }
        isMonConnected = true;
        logger.info("##USB-conn## ============ MON CONNECTED ============");
    }

    private void checkFirmwareVersion() throws OWNException {
        try {
            Response res = sendCommandSynchInternal(GatewayMgmt.requestFirmwareVersion().getFrameValue());
            if (res != null) {
                OpenMessage msg = res.getResponseMessages().get(0);
                if (msg instanceof GatewayMgmt) {
                    GatewayMgmt gmsg = (GatewayMgmt) msg;
                    Dim thisDim = gmsg.getDim();
                    if (thisDim == GatewayMgmt.DIM.FIRMWARE_VERSION) {
                        try {
                            firmwareVersion = GatewayMgmt.parseFirmwareVersion(gmsg);
                            logger.info("##USB-conn## FIRMWARE: {}", firmwareVersion);
                        } catch (FrameException e) {
                            logger.warn("##USB-conn## Cannot parse firmware version from message: {}", gmsg);
                        }
                    }
                    if (versionCompare(firmwareVersion, OLD_FIRMWARE_VERSION) <= 0) {
                        isOldFirmware = true;
                    }
                    if (versionCompare(firmwareVersion, AUTOMATION_BUG_FIRMWARE_VERSION) <= 0) {
                        hasAutomationBug = true;
                    }
                    logger.info("##USB-conn## FIRMWARE: hasAutomationBug={}", hasAutomationBug);
                    logger.info("##USB-conn## FIRMWARE:    isOldFirmware={}", isOldFirmware);
                }

            }
        } catch (IOException | FrameException e) {
            throw new OWNException("Failed to check FirmwareVersion for USB ZigBee Gateway", e);
        }
    }

    /**
     * Returns the firmware version for the connector (e.g. 1.2.3)
     *
     * @return String containing firmware version, null if unknown
     */
    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    // TODO add timeout to connect
    private void connectUSBDongle(String portN) throws OWNException {
        if (serialPort == null) {
            serialPort = connectSerialPort(portN);
            cmdChannel = new FrameChannel(serialPort.getInputStream(), serialPort.getOutputStream(), "USB");
        }
        try {
            // send requestKeepConnect to see if USB stick is ready to receive commands
            GatewayMgmt frame = GatewayMgmt.requestKeepConnect();
            cmdChannel.sendFrame(GatewayMgmt.requestKeepConnect().getFrameValue());
            hsLogger.info("(HS) USB HS==>>>> {}", frame.getFrameValue());
            Thread.sleep(50); // we must wait few ms for the answer to be ready
            String resp = cmdChannel.readFrames();
            hsLogger.info("(HS) USB <<<<==HS {}", resp);
            if (!OpenMessage.FRAME_ACK.equals(resp)) {
                throw new OWNException("Could not communicate with a USB ZigBee Gateway on serial port: " + portN
                        + ". Serial returned: " + resp);
            }
            // set event listener for incoming frames
            serialPort.addEventListener(this);
            logger.debug("##USB-conn## added event listener");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new OWNException("Failed to communicate with USB ZigBee Gateway on serial port: " + portN, e);
        } catch (TooManyListenersException e) {
            throw new OWNException("Failed to communicate with USB ZigBee Gateway on serial port: " + portN, e);
        }
    }

    private NRSerialPort connectSerialPort(String portN) throws OWNException {
        try {
            // FIXME If I call RXTXVersion.getVersion(), it throws NoClassDefError each time gnu.io is called again
            // logger.debug("RXTXVersion: {}", RXTXVersion.getVersion());
        } catch (NoClassDefFoundError e) {
            logger.error(
                    "##USB-conn## Serial connection requires RXTX libraries to be available, but they could not be found!");
            throw new OWNException(
                    "Serial connection requires RXTX libraries to be available, but they could not be found!", e);
        }
        CommPortIdentifier ident = null;
        try { // see if serial port exists
            ident = CommPortIdentifier.getPortIdentifier(portN);
        } catch (NoSuchPortException e) {
            logger.error("##USB-conn## Failed to connect to serial port {} - NoSuchPortException", portN);
            logger.error("##USB-conn## Available ports are: {}", listSerialPorts());
            throw new OWNException("Failed to connect to serial port " + portN, e);
        }
        logger.debug("##USB-conn## CommPortIndetifier: name={} type={} owner={}", ident.getName(), ident.getPortType(),
                ident.getCurrentOwner());
        NRSerialPort connectPort = new NRSerialPort(portN, SERIAL_SPEED);
        logger.debug("##USB-conn## NRSerialPort created");
        if (connectPort.connect()) {
            logger.debug("##USB-conn## Sucessfully connected to port {}",
                    connectPort.getSerialPortInstance().getName());
            try {
                connectPort.getSerialPortInstance().setSerialPortParams(SERIAL_SPEED, SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                throw new OWNException("Failed to connect to serial port: " + portN, e);
            }
            SerialPort sp = connectPort.getSerialPortInstance();
            // try {
            // sp.enableReceiveThreshold(1); // makes read() blocking until at least 1 char is received
            // sp.disableReceiveTimeout(); // disable any read() timeout
            // sp.enableReceiveTimeout(SERIAL_RECEIVE_TIMEOUT);
            logger.debug("##USB-conn## isReceiveThresholdEnabled={} v={}", sp.isReceiveThresholdEnabled(),
                    sp.getReceiveThreshold());
            logger.debug("##USB-conn## isReceiveTimeoutEnabled={} v={}", sp.isReceiveTimeoutEnabled(),
                    sp.getReceiveTimeout());
            return connectPort;
        } else {
            logger.warn("Failed to connect to serial port {}", ident.getName());
            throw new OWNException(
                    "Failed to connect to serial port " + ident.getName() + " (NRSerialPort connect() returned false)");
        }
    }

    private String listSerialPorts() {
        StringBuilder sb = new StringBuilder();
        Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
            if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                sb.append(id.getName());
                sb.append(", ");
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        } else {
            sb.append("-NO SERIAL PORTS FOUND-");
        }
        return sb.toString();
    }

    @Override
    protected synchronized Response sendCommandSynchInternal(String frame) throws IOException, FrameException {
        // TODO add timeout?
        OpenMessage msg = BaseOpenMessage.parse(frame);
        currentResponse = new Response(msg); // FIXME check if we have to store original or modified message
        msg = fixInvertedUpDownBug(msg);
        String frameSend = msg.getFrameValue();
        cmdChannel.sendFrame(frameSend);
        lastCmdFrameSentTs = System.currentTimeMillis();
        msgLogger.info("USB-CMD ====>>>> {}", frameSend);
        try {
            logger.debug("##USB-conn## [{}] waiting for response to complete...", Thread.currentThread().getName());
            currentResponse.waitResponse();
            logger.debug("##USB-conn## [{}] response COMPLETE!", Thread.currentThread().getName());
        } catch (IllegalMonitorStateException e) {
            e.printStackTrace();
        }
        final Response res = currentResponse;
        currentResponse = null;
        msgLogger.info("USB-CMD <<<<==== {}", res.getResponseMessages());
        return res;
    }

    @Override
    protected void processFrame(String newFrame) {
        logger.debug("##USB-conn## processing frame: {}", newFrame);
        OpenMessage msg;
        try {
            msg = BaseOpenMessage.parse(newFrame);
        } catch (UnsupportedFrameException e) {
            logger.warn("##USB-conn## UNSUPPORTED FRAME: {}, skipping it", newFrame);
            return;
        } catch (FrameException e) {
            logger.warn("##USB-conn## INVALID FRAME: {}, skipping it", newFrame);
            return;
        }
        if (currentResponse == null) { // no command is currently waiting
            if (msg.isACK() || msg.isNACK()) {
                logger.warn("##USB-conn## Recevied ACK/NACK without a command waiting, skipping it");
            } else {
                eventLogger.info("USB-MON <<<<<<<< {}", newFrame);
                notifyListener(msg);
            }
        } else {
            if (!currentResponse.hasFinalResponse()) {
                // a command is waiting for response messages, let's add them to the response object
                // TODO handle the BUSY_NACK case
                msgLogger.debug("USB-CMD   <<==   {}", newFrame);
                currentResponse.addResponse(msg);
                if (currentResponse.hasFinalResponse()) {
                    // we received an ACK/NACK, so let's signal response is ready to the waiting thread
                    logger.trace("##USB-conn## USB final response: {}", currentResponse);
                    currentResponse.responseReady();
                } else {
                    // perform fixes to compensate bugs of older gateways
                    fixDimensionResponseBug();
                    msg = fixInvertedUpDownBug(msg);
                }
            } else {
                logger.warn(
                        "##USB-conn## a command is waiting but has already a final response -> processing frame as event");
                eventLogger.info("USB-MON <<<<<<<< {}", newFrame);
                notifyListener(msg);
            }
        }
    }

    /*
     * Add final ACK to older USB gateways that do not return an ACK after dimension response
     */
    private void fixDimensionResponseBug() {
        if (isOldFirmware && !currentResponse.getRequest().isCommand()
                && currentResponse.getRequest() instanceof Lighting) {
            logger.debug("##USB-conn## BUGFIX for older USB gateways: adding final ACK");
            currentResponse.addResponse(AckOpenMessage.ACK);
            msgLogger.debug("USB-CMD   <<==   {}", AckOpenMessage.ACK);
            currentResponse.responseReady();
        }
    }

    /*
     * Bugfix to invert UP/DOWN for older USB gateways
     */
    private OpenMessage fixInvertedUpDownBug(OpenMessage msg) {
        if (hasAutomationBug && msg instanceof Automation) {
            try {
                logger.debug("##USB-conn## older firmware: converting Automation UP / DOWN on message: {}", msg);
                return Automation.convertUpDown((Automation) msg);
            } catch (FrameException fe) {
                logger.warn(
                        "##USB-conn## older firmware: FrameException while converting Automation UP/DOWN message: {}.",
                        msg);
            }
        }
        return msg;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            String frame = null;
            logger.trace("##USB-conn## START Processing DATA_AVAILABLE event...");
            do {
                // we enter synchronisation area to avoid race with sendCommandSynchInternal.readFrame.read() from
                // command
                // FIXME is "synchronized" still needed here? since we do not use readFrame anymore in
                // USBConnector.sendCommandSynchInternal
                synchronized (bufferSynchronisationObject) {
                    logger.trace("##USB-conn## serialEvent [{}] acquired Mutex", Thread.currentThread().getName());
                    try {
                        frame = cmdChannel.readFrames();
                    } catch (IOException e) {
                        logger.error("##USB-conn## IOException while reading frames from DATA_AVAILABLE event: {}",
                                e.getMessage());
                    }
                    logger.trace("##USB-conn## serialEvent [{}] release Mutex", Thread.currentThread().getName());
                }
                if (frame == null) {
                    logger.debug(
                            "##USB-conn## no more frames to read from DATA_AVAILABLE event (readFrame() returned {})",
                            frame);
                } else {
                    processFrame(frame);
                }
            } while (frame != null);
            logger.trace("##USB-conn## END processing DATA_AVAILABLE event");
        } else {
            logger.debug("##USB-conn## serialEvent() - unhandled event type: {}", event.getEventType());
        }
    }

    @Override
    public void disconnect() {
        super.disconnect();
        if (serialPort != null) {
            serialPort.disconnect();
            serialPort = null;
            logger.debug("Serial port CLOSED");
        }
    }

    /**
     * Compares two version strings.
     *
     * Use this instead of String.compareTo() for a non-lexicographical
     * comparison that works for version strings. e.g. "1.10".compareTo("1.6").
     *
     * @note It does not work if "1.10" is supposed to be equal to "1.10.0".
     *
     * @param str1 a string of ordinal numbers separated by decimal points.
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2.
     *         The result is a positive integer if str1 is _numerically_ greater than str2.
     *         The result is zero if the strings are _numerically_ equal.
     */
    private static int versionCompare(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        // set index to first non-equal ordinal or length of shortest version string
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }
        // compare first non-equal ordinal number
        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }
        // the strings are equal or one string is a substring of the other
        // e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
        return Integer.signum(vals1.length - vals2.length);
    }
}
