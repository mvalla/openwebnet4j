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
package org.openwebnet4j;

import java.util.ArrayList;
import java.util.List;

import org.openwebnet4j.communication.BUSConnector;
import org.openwebnet4j.communication.OWNException;
import org.openwebnet4j.communication.Response;
import org.openwebnet4j.message.Alarm;
import org.openwebnet4j.message.Automation;
import org.openwebnet4j.message.Auxiliary;
import org.openwebnet4j.message.BaseOpenMessage;
import org.openwebnet4j.message.CENPlusScenario;
import org.openwebnet4j.message.EnergyManagementDiagnostic;
import org.openwebnet4j.message.Lighting;
import org.openwebnet4j.message.OpenMessage;
import org.openwebnet4j.message.ThermoregulationDiagnostic;
import org.openwebnet4j.message.Where;
import org.openwebnet4j.message.WhereAuxiliary;
import org.openwebnet4j.message.WhereEnergyManagement;
import org.openwebnet4j.message.WhereLightAutom;
import org.openwebnet4j.message.WhereThermo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BUSGateway} to connect to BUS OpenWebNet gateways using {@link BUSConnector}
 *
 * @author M. Valla - Initial contribution
 * @author Andrea Conte - Energy manager contribution
 * @author G.Fabiani - Auxiliary support
 */
public class BUSGateway extends OpenGateway {

    private class DiscoveryResult {
        public Where where;
        public OpenDeviceType type;
        public BaseOpenMessage message;

        public DiscoveryResult(Where w, OpenDeviceType t, BaseOpenMessage m) {
            this.where = w;
            this.type = t;
            this.message = m;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(BUSGateway.class);

    private final int DEFAULT_PORT = 20000; // Default OWN gateway port is 20000
    private final int CONNECTION_TIMEOUT_MS = 120000;

    private int port = DEFAULT_PORT;
    private String host;
    private String pwd;

    /**
     * Creates a new BUSGateway instance with host, port and password.
     *
     * @param host the gateway host name or IP
     * @param port the gateway port
     * @param pwd the gateway password
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
            logger.debug("##BUS## ----- LIGHTS discovery -----");
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
            // DISCOVER AUTOMATION - request status for all automations: *#2*0##
            logger.debug("##BUS## ----- AUTOMATION discovery -----");
            res = sendInternal(Automation.requestStatus(WhereLightAutom.GENERAL.value()));
            for (OpenMessage msg : res.getResponseMessages()) {
                if (msg instanceof Automation) {
                    Automation amsg = ((Automation) msg);
                    OpenDeviceType type = amsg.detectDeviceType();
                    if (type != null) {
                        Where w = amsg.getWhere();
                        notifyListeners((listener) -> listener.onNewDevice(w, type, amsg));
                    }
                }
            }
            // DISCOVER ENERGY MANAGEMENT - request diagnostic for all energy devices: *#1018*0*7##
            // response <<<< *#1018*WHERE*7*BITS##
            logger.debug("##BUS## ----- ENERGY MANAGEMENT discovery -----");
            res = sendInternal(EnergyManagementDiagnostic.requestDiagnostic(WhereEnergyManagement.GENERAL.value()));
            for (OpenMessage msg : res.getResponseMessages()) {
                if (msg instanceof EnergyManagementDiagnostic) {
                    EnergyManagementDiagnostic edmsg = ((EnergyManagementDiagnostic) msg);
                    OpenDeviceType type = edmsg.detectDeviceType();
                    if (type != null) {
                        Where w = edmsg.getWhere();
                        notifyListeners((listener) -> listener.onNewDevice(w, type, edmsg));
                    }
                }
            }
            // DISCOVER THERMOREGULATION - request diagnostic for all thermoregulation devices:
            // *#1004*0*7##
            // response <<<< *#1004*WHERE*7*BITS##
            logger.debug("##BUS## ----- THERMOREGULATION discovery");
            List<DiscoveryResult> foundThermoDevices = new ArrayList<>(); // list of found thermo devices other than CU
            res = sendInternal(ThermoregulationDiagnostic.requestDiagnostic(WhereThermo.ALL_MASTER_PROBES.value()));
            boolean foundCU99 = false;
            DiscoveryResult cu = null;
            for (OpenMessage msg : res.getResponseMessages()) {
                if (msg instanceof ThermoregulationDiagnostic) {
                    ThermoregulationDiagnostic tdMsg = ((ThermoregulationDiagnostic) msg);
                    OpenDeviceType type = tdMsg.detectDeviceType();
                    if (type != null) {
                        Where w = tdMsg.getWhere();
                        if (OpenDeviceType.SCS_THERMO_CENTRAL_UNIT.equals(type)) {
                            if (!foundCU99) {
                                cu = new DiscoveryResult(w, type, tdMsg);
                                if (w.value().charAt(0) == '#') {
                                    foundCU99 = true;
                                    logger.debug("##BUS## ----- THERMOREGULATION discovery - FOUND 99-CU where={}", w);
                                } else {
                                    logger.debug("##BUS## ----- THERMOREGULATION discovery - FOUND 4/99-CU where={}",
                                            w);
                                }
                            }
                        } else {
                            foundThermoDevices.add(new DiscoveryResult(w, type, tdMsg));
                        }
                    }
                }
            }
            final DiscoveryResult foundCU = cu;
            if (foundCU != null) { // notify found CU...
                notifyListeners((listener) -> listener.onNewDevice(foundCU.where, foundCU.type, foundCU.message));
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            foundThermoDevices.forEach(fd -> { // ...then notify all found thermo zones and sensors
                if (!OpenDeviceType.SCS_THERMO_CENTRAL_UNIT.equals(fd.type)) {
                    notifyListeners((listener) -> listener.onNewDevice(fd.where, fd.type, fd.message));
                }
            });

            // DISCOVER DRY CONTACT / IR SENSOR - request: *#25*30##
            // response <<<< *25*WHAT#0*WHERE##
            logger.debug("##BUS## ----- DRY CONTACT / IR sensor discovery");
            res = sendInternal(CENPlusScenario.requestStatus("30")); // TODO use WhereScenario
            for (OpenMessage msg : res.getResponseMessages()) {
                if (msg instanceof CENPlusScenario) {
                    CENPlusScenario cenMsg = ((CENPlusScenario) msg);
                    OpenDeviceType type = cenMsg.detectDeviceType();
                    if (type != null) {
                        Where w = cenMsg.getWhere();
                        notifyListeners((listener) -> listener.onNewDevice(w, type, cenMsg));
                    }
                }
            }
            // DISCOVER AUX request:*#9*0##
            // response <<<< *9*WHAT*0
            logger.debug("##BUS## ----- AUX discovery");
            res = sendInternal(Auxiliary.requestStatus(WhereAuxiliary.GENERAL.value()));
            for (OpenMessage msg : res.getResponseMessages()) {
                if (msg instanceof Auxiliary) {
                    Auxiliary auxMsg = (Auxiliary) msg;
                    OpenDeviceType type = auxMsg.detectDeviceType();
                    if (type != null) {
                        Where w = auxMsg.getWhere();
                        notifyListeners((listener) -> listener.onNewDevice(w, type, auxMsg));
                    }
                }
            }
            // DISCOVER ALARM - request: *#5*0##
            logger.debug("##BUS## ----- ALARM discovery");
            res = sendInternal(Alarm.requestSystemStatus());
            boolean foundAlarmCentralUnit = false; // to notify central unit only once
            for (OpenMessage msg : res.getResponseMessages()) {
                if (msg instanceof Alarm) {
                    Alarm alarmMsg = ((Alarm) msg);
                    OpenDeviceType type = alarmMsg.detectDeviceType();
                    if (type != null) {
                        if (type == OpenDeviceType.SCS_ALARM_CENTRAL_UNIT) {
                            if (!foundAlarmCentralUnit) {
                                foundAlarmCentralUnit = true;
                                notifyListeners((listener) -> listener.onNewDevice(null, type, alarmMsg));
                            }
                        } else if (type == OpenDeviceType.SCS_ALARM_ZONE) {
                            Where w = alarmMsg.getWhere();
                            notifyListeners((listener) -> listener.onNewDevice(w, type, alarmMsg));
                        }
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
        if (isConnected && connector.isCmdConnected()
                && (now - connector.getLastCmdFrameSentTs() < CONNECTION_TIMEOUT_MS)) {
            return true;
        } else {
            return false;
        }
    }
}
