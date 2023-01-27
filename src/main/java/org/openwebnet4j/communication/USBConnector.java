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
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

/**
 * Class for communicating with a ZigBee USB Gateway using the OpenWebNet protocol
 *
 * @author M. Valla - Initial contribution
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
    private final Object requestSentSynchObj = new Object(); // Synch object to synchronise sending a request frame and
    // processing its answer

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
        logger.debug("##USB-conn## Opening CMD connection to ZigBee USB Gateway on serial port {}...", portName);
        if (serialPort == null) {
            connectUSBDongle(portName);
            checkFirmwareVersion();
        }
        isCmdConnected = true;
        logger.info("##USB-conn## ============ CMD CONNECTED - {} ==========", portName);
    }

    @Override
    public void openMonConn() throws OWNException {
        if (isMonConnected) {
            logger.debug("##USB-conn## MON is already open");
            return;
        }
        if (serialPort == null) {
            connectUSBDongle(portName);
            checkFirmwareVersion();
        }
        try {
            // send supervisor to receive all events from devices
            sendCommandSynchInternal(GatewayMgmt.requestSupervisor().getFrameValue());
        } catch (IOException | FrameException e) {
            throw new OWNException("Failed to set supervisor to ZigBee USB Gateway on serial port: " + portName, e);
        }
        isMonConnected = true;
        logger.info("##USB-conn## ============ MON CONNECTED - {} ==========", portName);
    }

    private void checkFirmwareVersion() throws OWNException {
        try {
            Response res = sendCommandSynchInternal(GatewayMgmt.requestFirmwareVersion().getFrameValue());
            if (res != null) {
                OpenMessage msg = res.getResponseMessages().get(0);
                if (msg instanceof GatewayMgmt) {
                    GatewayMgmt gmsg = (GatewayMgmt) msg;
                    Dim thisDim = gmsg.getDim();
                    if (thisDim == GatewayMgmt.DimGatewayMgmt.FIRMWARE_VERSION) {
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
            throw new OWNException("Failed to check FirmwareVersion for ZigBee USB Gateway", e);
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
                disconnectSerialPort();
                throw new OWNException("Could not communicate with a Zigbee USB Gateway on serial port: " + portN
                        + ". Serial returned: " + resp);
            }
            // set event listener for incoming frames
            serialPort.addEventListener(this);
            logger.debug("##USB-conn## added event listener");
            logger.info("##USB-conn## === CONNECTED TO USB GATEWAY on serial port: {} ===", portN);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            disconnectSerialPort();
            throw new OWNException("Failed to communicate with Zigbee USB Gateway on serial port: " + portN, e);
        } catch (TooManyListenersException e) {
            disconnectSerialPort();
            throw new OWNException("Failed to communicate with Zigbee USB Gateway on serial port: " + portN, e);
        }
    }

    private NRSerialPort connectSerialPort(String portN) throws OWNException {
        try {
            // FIXME If I call RXTXVersion.getVersion(), it throws NoClassDefError each time gnu.io
            // is called again
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
            logger.info("##USB-conn## Failed to connect to serial port {}: NoSuchPortException", portN);
            String availPorts = listSerialPorts();
            logger.info("##USB-conn## Available serial ports are: {}", availPorts);
            throw new OWNException(
                    "Failed to connect to serial port " + portN + ". Available serial ports are: " + availPorts, e);
        }
        logger.debug("##USB-conn## CommPortIndetifier: name={} type={} owner={}", ident.getName(), ident.getPortType(),
                ident.getCurrentOwner());

        if (ident.getPortType() != CommPortIdentifier.PORT_SERIAL) {
            logger.error("##USB-conn## Port {} is not a serial port", ident.getName());
            throw new OWNException("Failed to connect to port " + portN + " (not a serial port).");
        }
        if (ident.isCurrentlyOwned()) {
            logger.debug("##USB-conn## Serial port {} is already in use", ident.getName());
            throw new OWNException("Failed to connect to serial port " + portN + ". Port is already in use.");
        }
        NRSerialPort connectPort = new NRSerialPort(portN, SERIAL_SPEED);
        logger.debug("##USB-conn## NRSerialPort created");

        try {
            if (!connectPort.connect()) {
                logger.info("Failed to connect to serial port {}", ident.getName());
                throw new OWNException("Failed to connect to serial port " + ident.getName()
                        + " (NRSerialPort.connect() returned false)");
            }
            connectPort.getSerialPortInstance().setSerialPortParams(SERIAL_SPEED, SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException ue) {
            logger.error("##USB-conn## Failed setting params for port {}", ident.getName());
            throw new OWNException("Failed setting params for port: " + portN, ue);
        } catch (OWNException oe) {
            throw oe;
        } catch (Exception e) {
            // PortInUseException could be thrown but is not declared, so we check here
            if (e instanceof PortInUseException) {
                logger.debug("##USB-conn## Serial port {} is already in use (PortInUseException)", ident.getName());
                throw new OWNException(
                        "Failed to connect to serial port " + portN + ". Port is already in use (PortInUseException).",
                        e);
            } else {
                logger.debug("##USB-conn## Exception while connecting serial port: {}", e.getMessage());
                throw new OWNException("Failed to connect to serial port " + portN, e);
            }
        }
        SerialPort sp = connectPort.getSerialPortInstance();

        // try {
        // sp.enableReceiveThreshold(1); // makes read() blocking until at least 1 char is
        // received
        // sp.disableReceiveTimeout(); // disable any read() timeout
        // sp.enableReceiveTimeout(SERIAL_RECEIVE_TIMEOUT);
        logger.debug("##USB-conn## isReceiveThresholdEnabled={} v={}", sp.isReceiveThresholdEnabled(),
                sp.getReceiveThreshold());
        logger.debug("##USB-conn## isReceiveTimeoutEnabled={} v={}", sp.isReceiveTimeoutEnabled(),
                sp.getReceiveTimeout());
        logger.debug("##USB-conn## FlowControl: {}", sp.getFlowControlMode());

        logger.info("##USB-conn## === CONNECTED TO SERIAL PORT {} ===", connectPort.getSerialPortInstance().getName());
        return connectPort;
    }

    private void disconnectSerialPort() {
        if (serialPort != null) {
            serialPort.disconnect();
            logger.debug("##USB-conn## Serial port {} DISCONNECTED", portName);
            serialPort = null;
        }
    }

    private String listSerialPorts() {
        StringBuilder sb = new StringBuilder();
        Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
        boolean first = true;
        while (portList.hasMoreElements()) {
            CommPortIdentifier id = (CommPortIdentifier) portList.nextElement();
            if (id.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(id.getName());
                first = false;
            }
        }
        if (sb.length() == 0) {
            sb.append("-NO SERIAL PORTS FOUND-");
        }
        return sb.toString();
    }

    @Override
    protected synchronized Response sendCommandSynchInternal(String frame) throws IOException, FrameException {
        // TODO add timeout?
        OpenMessage msg = BaseOpenMessage.parse(frame);
        OpenMessage fixedMsg = fixInvertedUpDownBug(msg);
        synchronized (requestSentSynchObj) {
            currentResponse = new Response(fixedMsg); // FIXME check if we have to store original or modified
            // message
            String frameSend = fixedMsg.getFrameValue();
            cmdChannel.sendFrame(frameSend);
            lastCmdFrameSentTs = System.currentTimeMillis();
            msgLogger.info("USB-CMD ====>>>> {}", frameSend);
        }
        try {
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
            logger.info("##USB-conn## UNSUPPORTED FRAME: {}, skipping it", newFrame);
            return;
        } catch (FrameException e) {
            logger.warn("##USB-conn## INVALID FRAME: {}, skipping it", newFrame);
            return;
        }
        synchronized (requestSentSynchObj) {
            // fix up/down bug for older gateways
            msg = fixInvertedUpDownBug(msg);
            if (currentResponse == null) { // no request is currently waiting
                if (msg.isACK() || msg.isNACK()) {
                    logger.warn("##USB-conn## Recevied ACK/NACK without a request waiting, skipping it");
                } else {
                    eventLogger.info("USB-MON <<<<<<<< {}", msg.getFrameValue());
                    notifyListener(msg);
                }
            } else { // some request is currently waiting
                logger.debug("##USB-conn## a request is waiting");
                if (msg.isCommand()) {
                    // perform fixes to compensate bugs of older gateways
                    fixDimensionResponseBug();
                    eventLogger.info("USB-MON <<<<<<<< {}", msg.getFrameValue());
                    notifyListener(msg);
                } else { // add them to the response object
                    // TODO handle the BUSY_NACK case
                    msgLogger.debug("USB-CMD   <<==   {}", newFrame);
                    currentResponse.addResponse(msg);
                }
                if (currentResponse.hasFinalResponse()) {
                    // we received an ACK/NACK, so let's signal response is ready to the waiting
                    // thread
                    logger.debug("##USB-conn## USB final response: {}", currentResponse);
                    currentResponse.responseReady();
                }
            }
        }
    }

    /*
     * Add final ACK to response for older USB gateways that do not return an ACK after dimension response.
     * See OpenWebNet Zigbee docs page 35 / page 17 of older version
     */
    private void fixDimensionResponseBug() {
        if (isOldFirmware && !currentResponse.getRequest().isCommand()
                && (currentResponse.getRequest() instanceof Lighting
                        || currentResponse.getRequest() instanceof Automation)) {
            logger.debug("##USB-conn## BUGFIX for older USB gateways: adding final ACK");
            currentResponse.addResponse(AckOpenMessage.ACK);
            msgLogger.debug("USB-CMD   <<==   {}   (added)", AckOpenMessage.ACK);
        }
    }

    /*
     * Bugfix to invert UP/DOWN for older USB gateways
     */
    private OpenMessage fixInvertedUpDownBug(OpenMessage msg) {
        if (hasAutomationBug && msg instanceof Automation) {
            try {
                Automation msgConverted = Automation.convertUpDown((Automation) msg);
                logger.debug("##USB-conn## older firmware: converting Automation UP / DOWN on message {} --> {}", msg,
                        msgConverted);
                return msgConverted;
            } catch (FrameException fe) {
                logger.warn(
                        "##USB-conn## older firmware: FrameException while converting Automation UP/DOWN on message {}: {}",
                        msg, fe.getMessage());
            }
        }
        return msg;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            case SerialPortEvent.DATA_AVAILABLE:
                String frame = null;
                logger.debug("##USB-conn## START Processing DATA_AVAILABLE event...");
                do {
                    try {
                        frame = cmdChannel.readFrames();
                    } catch (IOException e) {
                        logger.error("##USB-conn## IOException while reading frames from DATA_AVAILABLE event: {}",
                                e.getMessage());
                    }
                    if (frame == null) {
                        logger.trace(
                                "##USB-conn## no more frames to read from DATA_AVAILABLE event (readFrames() returned {})",
                                frame);
                    } else {
                        processFrame(frame);
                    }
                } while (frame != null);
                logger.trace("##USB-conn## END processing DATA_AVAILABLE event");
                break;
            case 11: // Event is defined from nrjavaserial > 5.1.0 - 11 =
                // SerialPortEvent.HARDWARE_ERROR
                logger.warn("##USB-conn## serialEvent received HARDWARE_ERROR event: disconnecting serial port {}...",
                        portName);
                disconnectCmdChannel();
                disconnectSerialPort();
                handleMonDisconnect(new OWNException("Serial port " + portName + " received HARDWARE_ERROR event"));
                break;
            default:
                logger.debug("##USB-conn## serialEvent() received unhandled event type: {}", event.getEventType());
                break;
        }
    }

    @Override
    public void disconnect() {
        super.disconnect();
        disconnectSerialPort();
    }

    /**
     * Compares two version strings.
     *
     * <p>
     * Use this instead of String.compareTo() for a non-lexicographical comparison that works for
     * version strings. e.g. "1.10".compareTo("1.6").
     *
     * @apiNote It does not work if "1.10" is supposed to be equal to "1.10.0".
     * @param str1 a string of ordinal numbers separated by decimal points.
     * @param str2 a string of ordinal numbers separated by decimal points.
     * @return The result is a negative integer if str1 is _numerically_ less than str2. The result
     *         is a positive integer if str1 is _numerically_ greater than str2. The result is zero if
     *         the strings are _numerically_ equal.
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
