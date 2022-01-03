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

import java.nio.ByteBuffer;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.communication.Response;
import org.openwebnet4j.communication.USBConnector;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.GatewayMgmt;
import org.openwebnet4j.message.OpenMessage;
import org.openwebnet4j.message.WhereZigBee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class {@link USBGateway} to connect to ZigBee USB Gateways using {@link USBConnector}
 *
 * @author M. Valla - Initial contribution
 */
public class USBGateway extends OpenGateway {

    private final Logger logger = LoggerFactory.getLogger(USBGateway.class);

    private String serialPortName;
    private int discoveredProducts = 0; // number of products returned from the last SCAN command
    private int receivedProducts = 0; // number of products returned from PRODUCT INFORMATION
    // requests during a device discovery

    public USBGateway(String serialPortName) {
        this.serialPortName = serialPortName;
    }

    /**
     * Returns the gateway serial port name
     *
     * @return serial port
     */
    public String getSerialPortName() {
        return serialPortName;
    }

    @Override
    protected void initConnector() {
        connector = new USBConnector(serialPortName);
        logger.info("##USB## Init USB ({})...", serialPortName);
    }

    @Override
    protected void discoverDevicesInternal() throws OWNException {
        Response res;
        logger.debug("##USB## ----- ### STARTING A NEW DISCOVERY...");
        receivedProducts = 0;
        discoveredProducts = 0;
        try {
            res = send(GatewayMgmt.requestScanNetwork());
            if (!res.isSuccess()) {
                throw new OWNException(
                        "Error while discovering devices on USB gateway. RequestScanNetwork returned: "
                                + res.getFinalResponse());
            }
        } catch (OWNException e) {
            logger.error(
                    "##USB## ----- # OWNException while discovering devices: {}", e.getMessage());
            isDiscovering = false;
            throw e;
        }
    }

    @Override
    public void onMessage(OpenMessage message) {
        if (isDiscovering && message instanceof GatewayMgmt) {
            handleDiscoveryEvent((GatewayMgmt) message);
        }
        super.onMessage(message);
    }

    /*
     * handle a discovery event msg (number of products in the network)
     */
    private void handleDiscoveryEvent(GatewayMgmt message) {
        if (message.getDim() == GatewayMgmt.DimGatewayMgmt.NB_NETW_PROD) {
            try {
                discoveredProducts = Integer.parseInt(message.getDimValues()[0]);
                logger.debug("##USB## ----- # {} products found!", discoveredProducts);
                // request product infos, starting from index 0
                for (int p = 0; p < discoveredProducts; p++) {
                    handleDiscoveryResponse(sendInternal(GatewayMgmt.requestProductInfo(p)));
                    receivedProducts++;
                    logger.debug(
                            "##USB## ----- # DISCOVERED {} / {} products",
                            receivedProducts,
                            discoveredProducts);
                }
                logger.debug(
                        "##USB## ----- ### DISCOVERY COMPLETED - DISCOVERED {} / {} products",
                        receivedProducts,
                        discoveredProducts);
                notifyListeners((listener) -> listener.onDiscoveryCompleted());
            } catch (Exception e) {
                logger.debug("##USB## ----- # Error while discovering devices: " + e.getMessage());
            }
            isDiscovering = false;
        }
    }

    /*
     * handle a response to a product info request
     */
    private void handleDiscoveryResponse(Response r) {
        GatewayMgmt gMsg = null;
        int i = 0;
        // get messages in the response and notify all endpoints in the response, before last
        // ACK/NACK
        while (r.getResponseMessages().get(i) instanceof GatewayMgmt) {
            gMsg = (GatewayMgmt) r.getResponseMessages().get(i);
            if (gMsg != null && gMsg.getDim() == GatewayMgmt.DimGatewayMgmt.PRODUCT_INFO) {
                WhereZigBee w = (WhereZigBee) (gMsg.getWhere());
                logger.debug("##USB## ----- # new product found: WHERE={}", w);
                // notify new endpoint found
                final GatewayMgmt m = gMsg;
                notifyListeners((listener) -> listener.onNewDevice(w, getZigBeeDeviceType(m), m));
            }
            i++;
        }
    }

    /**
     * Returns the ZigBee ID of the gateway in decimal format (=four last bytes of the ZigBee MAC
     * address of the product converted in decimal format).
     *
     * @return the gateway ZigBeeId, or 0 if it is unknown
     */
    public int getZigBeeIdAsDecimal() {
        if (macAddr != null) {
            ByteBuffer bb = ByteBuffer.wrap(macAddr, 4, 4); // get last 4 bytes
            return bb.getInt();
        } else {
            return 0;
        }
    }

    /** Return device type from a product info message received from ZigBee network */
    private OpenDeviceType getZigBeeDeviceType(BaseOpenMessage openmsg) {
        String frame = openmsg.getFrameValue();
        frame = frame.substring(0, frame.length() - 2);
        int commandIndex = frame.lastIndexOf("*66*");
        if (-1 != commandIndex) {
            frame = frame.substring(commandIndex + 4, frame.length());
            if (frame.contains("*")) {
                String devTypeStr = frame.substring(frame.lastIndexOf("*") + 1, frame.length());
                if (devTypeStr != null) {
                    int dev;
                    try {
                        dev = Integer.parseInt(devTypeStr);
                    } catch (NumberFormatException e) {
                        logger.warn("##USB## cannot recognize device type! frame={}", openmsg);
                        return OpenDeviceType.UNKNOWN;
                    }
                    OpenDeviceType devType = OpenDeviceType.fromValue(dev);
                    if (devType != null) {
                        logger.debug("##USB## deviceType = {}", devType);
                        return devType;
                    }
                }
            }
        }
        logger.warn("##USB## cannot recognize device type! frame={}", openmsg);
        return OpenDeviceType.UNKNOWN;
    }

    @Override
    public String toString() {
        return "USB_" + serialPortName;
    }

    @Override
    public boolean isCmdConnectionReady() {
        return (isConnected && connector.isCmdConnected());
    }
}
