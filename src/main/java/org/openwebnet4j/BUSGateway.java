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

import org.openwebnet4j.communication.BUSConnector;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.communication.Response;
import org.openwebnet4j.message.Lighting;
import org.openwebnet4j.message.OpenMessage;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereLightAutom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BUSgateway} to connect to BUS OpenWebNet gateways using {@link BUSConnector}
 *
 * @author M. Valla - Initial contribution
 *
 */
public class BUSGateway extends OpenGateway {

    private final Logger logger = LoggerFactory.getLogger(BUSGateway.class);

    private final int DEFAULT_PORT = 20000; // Default OWN gateway port is 20000

    private int port = DEFAULT_PORT;
    private String host;
    private String pwd;

    /**
     * Creates a new BUSGateway instance with host, port and password.
     *
     * @param host the gateway host name or IP
     * @param port the gateway port
     * @param pwd the gateway password
     *
     */
    public BUSGateway(String host, int port, String pwd) {
        this.host = host;
        this.port = port;
        this.pwd = pwd;
    }

    /**
     * Returns the gateway host (IP address or hostname).
     *
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the gateway port.
     *
     * @return port
     */
    public int getPort() {
        return port;
    }

    /**
     * Returns the gateway password.
     *
     * @return password
     */
    public String getPassword() {
        return pwd;
    }

    @Override
    protected void initConnector() {
        connector = new BUSConnector(host, port, pwd);
        logger.info("##BUS## Init BUS ({}:{})...", host, port);

    }

    @Override
    protected void discoverDevicesInternal() throws OWNException {
        Response res;
        logger.debug("##BUS## ----- ### STARTING A NEW DISCOVERY...");
        try {
            // DISCOVER LIGHTS - request status for all lights: *#1*0##
            logger.debug("##BUS## ----- # LIGHTS discovery");
            res = sendInternal(Lighting.requestStatus(WhereLightAutom.GENERAL.value()));
            for (OpenMessage msg : res.getResponseMessages()) {
                if (msg instanceof Lighting) {
                    Lighting lmsg = ((Lighting) msg);
                    OpenDeviceType type = lmsg.detectDeviceType();
                    if (type != null) {
                        Where w = lmsg.getWhere();
                        notifyListeners((listener) -> listener.onNewDevice(w, type, lmsg));
                    }
                }
            }
        } catch (OWNException e) {
            logger.error("##BUS## ----- # OWNException while discovering devices: {}", e.getMessage());
            isDiscovering = false;
            throw e;
        }

        // finished discovery
        isDiscovering = false;
        logger.debug("##BUS## ----- ### DISCOVERY COMPLETED");
        notifyListeners((listener) -> listener.onDiscoveryCompleted());
    }

    @Override
    public String toString() {
        return "BUS_" + host + ":" + port;
    }

    @Override
    public boolean isCmdConnectionReady() {
        long now = System.currentTimeMillis();
        if (isConnected && connector.isCmdConnected() && (now - connector.getLastCmdFrameSentTs() < 120000)) {
            return true;
        } else {
            return false;
        }
    }

} /* class */
