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
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.FrameException;
import org.openwebnet4j.message.GatewayMgmt;
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

    // TODO add timeout to connect
    private void connectUSBDongle(String portN) throws OWNException {
        if (serialPort == null) {
            serialPort = connectSerialPort(portN);
            cmdChannel = new FrameChannel(serialPort.getInputStream(), serialPort.getOutputStream(), "USB");
        }
        try {
            // send requestKeepConnect to see if USB stick is ready to receive commands
            GatewayMgmt frame = GatewayMgmt.requestKeepConnect();
            cmdChannel.sendFrame(GatewayMgmt.requestKeepConnect().getFrameValue()); //
            logger.info("(HS) USB HS==>>>> {}", frame.getFrameValue());
            Thread.sleep(50); // !!! we must wait few ms for the answer to be ready
            String resp = cmdChannel.readFrames();
            logger.info("(HS) USB <<<<==HS {}", resp);
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
            // activation of serialEvent callback (it's the default!)
            // sp.notifyOnDataAvailable(true);
            // } catch (UnsupportedCommOperationException e) {
            // throw new OWNException("Failed to connect to serial port: " + portN, e);
            // }
            // logger.debug("Sucessfully set comms parameters");
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
        currentResponse = new Response(BaseOpenMessage.parse(frame));
        cmdChannel.sendFrame(frame);
        logger.info("USB-CMD ====>>>> {}", frame);
        try {
            logger.debug("##USB-conn## [{}] waiting for response to complete...", Thread.currentThread().getName());
            currentResponse.waitResponse();
            logger.debug("##USB-conn## [{}] response COMPLETE!", Thread.currentThread().getName());
        } catch (IllegalMonitorStateException e) {
            e.printStackTrace();
        }
        final Response res = currentResponse;
        currentResponse = null;
        logger.info("USB-CMD <<<<==== {}", res.getResponseMessages());
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
                logger.info("USB-MON <<<<<<<< {}", newFrame);
                notifyListener(msg);
            }
        } else { // a command is waiting for response, let's add them to the response object
            // TODO handle the BUSY_NACK case
            logger.debug("USB-CMD   <<==   {}", newFrame);
            currentResponse.addResponse(msg);
            if (currentResponse.hasFinalResponse()) {
                // we received an ACK/NACK, so let's signal response is ready to the waiting thread
                logger.trace("##USB-conn## USB final response: {}", currentResponse);
                currentResponse.responseReady();
            } else {
                fixDimensionResponseBug();
            }
        }
    }

    private void fixDimensionResponseBug() {
        if (!currentResponse.getRequest().isCommand()) {
            // FIXME check if this is required for old/new USB sticks : isOldFirmware
            // add virtual ACK to old USB sticks that do not return an ACK after dimension response
            logger.debug("##USB-conn## BUGFIX for older USB sticks: adding final ACK");
            currentResponse.addResponse(AckOpenMessage.ACK);
            logger.debug("USB-CMD   <<==   {}", AckOpenMessage.ACK);
            currentResponse.responseReady();
        }
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

}
