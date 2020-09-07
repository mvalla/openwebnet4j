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
import org.openwebnet4j.communication.Response;
import org.openwebnet4j.communication.USBConnector;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.GatewayMgmt;
import org.openwebnet4j.message.OpenMessage;
import org.openwebnet4j.message.WhereZigBee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link USBgateway} to connect to USB OpenWebNet gateways using {@link USBConnector}
 *
 * @author M. Valla - Initial contribution
 *
 */
public class USBGateway extends OpenGateway {

    private final Logger logger = LoggerFactory.getLogger(USBGateway.class);

    private String serialPortName;
    private int discoveredProducts = 0; // number of products returned from the last SCAN command
    private int receivedProducts = 0; // number of products returned from PRODUCT INFORMATION requests during a device
                                      // discovery

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
    protected void discoverDevicesInternal() {
        Response res;
        logger.debug("##USB## ----- ### STARTING A NEW DISCOVERY...");
        receivedProducts = 0;
        discoveredProducts = 0;
        try {
            res = send(GatewayMgmt.requestScanNetwork());
            if (!res.isSuccess()) {
                logger.debug("##USB## ----- # Cannot discover devices, requestScanNetwork() returned: " + res);
                isDiscovering = false;
            }
        } catch (OWNException e) {
            logger.error("##USB## ----- # OWNException while discovering devices: {}", e);
            e.printStackTrace();
            isDiscovering = false;
        }
    }

    @Override
    public void onMessage(OpenMessage message) {
        if (isDiscovering && message instanceof GatewayMgmt) {
            handleDiscovery((GatewayMgmt) message);
        }
        super.onMessage(message);
    }

    private void handleDiscovery(GatewayMgmt message) {
        if (message.getDim() == GatewayMgmt.DIM.NB_NETW_PROD) {
            try {
                discoveredProducts = Integer.parseInt(message.getDimValues()[0]);
                logger.debug("##USB## ----- # {} products found!", discoveredProducts);
                // request product infos, starting from last index
                Response res = sendInternal(GatewayMgmt.requestProductInfo(discoveredProducts - 1));
                if (res.getResponseMessages().get(0) instanceof GatewayMgmt) {
                    handleDiscovery((GatewayMgmt) res.getResponseMessages().get(0));
                }
            } catch (Exception e) {
                logger.debug("##USB## ----- # Error while discovering devices: " + e.getMessage());
                isDiscovering = false;
            }
        } else if (message.getDim() == GatewayMgmt.DIM.PRODUCT_INFO) {
            WhereZigBee w = (WhereZigBee) (message.getWhere());
            logger.debug("##USB## ----- # new product found: WHERE={}", w);
            // notify new device found
            notifyListeners((listener) -> listener.onNewDevice(w, getZigBeeDeviceType(message), message));
            // increase receivedProducts only if found product WHERE UNIT is 01 or 00
            if (WhereZigBee.UNIT_ALL.equals(w.getUnit()) || WhereZigBee.UNIT_01.equals(w.getUnit())) {
                receivedProducts++;
            }
            if (receivedProducts < discoveredProducts) {
                // requestProductInfo for next product
                logger.debug("##USB## ----- # DISCOVERED {} / {} products", receivedProducts, discoveredProducts);
                try {
                    Response res = sendInternal(
                            GatewayMgmt.requestProductInfo(discoveredProducts - receivedProducts - 1));
                    if (res.getResponseMessages().get(0) instanceof GatewayMgmt) {
                        handleDiscovery((GatewayMgmt) res.getResponseMessages().get(0));
                    }
                } catch (OWNException e) {
                    logger.debug("##USB## ----- # Error while discovering devices: " + e.getMessage());
                    isDiscovering = false;
                }
            } else {
                isDiscovering = false;
                logger.debug("##USB## ----- ### DISCOVERY COMPLETED - DISCOVERED {} / {} products", receivedProducts,
                        discoveredProducts);
                notifyListeners((listener) -> listener.onDiscoveryCompleted());
            }
        }
    }

    /**
     * Return device type from a product info message received from ZigBee network
     */
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
