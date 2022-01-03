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
package org.openwebnet4j.message;

import static java.lang.String.format;
import static org.openwebnet4j.message.Who.GATEWAY_MANAGEMENT;

import java.util.HashMap;
import java.util.Map;
import org.openwebnet4j.OpenDeviceType;

/** OpenWebNet GatewayManagmenet messages */

/** @author M. Valla - Initial contribution */
public class GatewayMgmt extends BaseOpenMessage {

    public enum WhatGatewayMgmt implements What {
        // USB Gateway
        BOOT_MODE(12),
        RESET_DEVICE(22),
        CREATE_NETWORK(30),
        CLOSE_NETWORK(31),
        OPEN_NETWORK(32),
        JOIN_NETWORK(33),
        LEAVE_NETWORK(34),
        KEEP_CONNECT(60),
        SCAN(65),
        SUPERVISOR(66),
        TEST(9999); // not defined in OWN specs, only for testing

        private static Map<Integer, WhatGatewayMgmt> mapping;

        private final int value;

        private WhatGatewayMgmt(int value) {
            this.value = value;
        }

        private static void initMapping() {
            mapping = new HashMap<Integer, WhatGatewayMgmt>();
            for (WhatGatewayMgmt w : values()) {
                mapping.put(w.value, w);
            }
        }

        public static WhatGatewayMgmt fromValue(int i) {
            if (mapping == null) {
                initMapping();
            }
            return mapping.get(i);
        }

        @Override
        public Integer value() {
            return value;
        }
    }

    @Override
    protected What whatFromValue(int i) {
        return WhatGatewayMgmt.fromValue(i);
    }

    public enum DimGatewayMgmt implements Dim {
        MAC_ADDRESS(12),
        MODEL(15),
        FIRMWARE_VERSION(16),
        HARDWARE_VERSION(17),
        WHO_IMPLEMENTED(26),
        PRODUCT_INFO(66),
        NB_NETW_PROD(67),
        IDENTIFY(70),
        ZIGBEE_CHANNEL(71);

        private static Map<Integer, DimGatewayMgmt> mapping;

        private final int value;

        private DimGatewayMgmt(Integer value) {
            this.value = value;
        }

        private static void initMapping() {
            mapping = new HashMap<Integer, DimGatewayMgmt>();
            for (DimGatewayMgmt d : values()) {
                mapping.put(d.value, d);
            }
        }

        public static DimGatewayMgmt fromValue(int i) {
            if (mapping == null) {
                initMapping();
            }
            return mapping.get(i);
        }

        @Override
        public Integer value() {
            return value;
        }
    }

    @Override
    protected Dim dimFromValue(int i) {
        return DimGatewayMgmt.fromValue(i);
    }

    private static final int WHO = GATEWAY_MANAGEMENT.value();

    protected GatewayMgmt(String value) {
        super(value);
        this.who = Who.GATEWAY_MANAGEMENT;
    }

    /**
     * OpenWebNet message request for supervisor mode <code>*13*66*##</code>.
     *
     * @return GatewayMgmt message
     */
    public static GatewayMgmt requestSupervisor() {
        return new GatewayMgmt(format(FORMAT_REQUEST, WHO, WhatGatewayMgmt.SUPERVISOR.value(), ""));
    }

    /**
     * OpenWebNet message request keep connect <code>*13*60*##</code>.
     *
     * @return GatewayMgmt message
     */
    public static GatewayMgmt requestKeepConnect() {
        return new GatewayMgmt(
                format(FORMAT_REQUEST, WHO, WhatGatewayMgmt.KEEP_CONNECT.value(), ""));
    }

    /**
     * OpenWebNet message request for gateway MAC address <code>*#13**12##</code>.
     *
     * @return GatewayMgmt message
     */
    public static GatewayMgmt requestMACAddress() {
        return new GatewayMgmt(
                format(FORMAT_DIMENSION_REQUEST, WHO, "", DimGatewayMgmt.MAC_ADDRESS.value()));
    }

    /**
     * Parse MAC address in the OWN message and return values in byte[]
     *
     * @param msg the message to parse
     * @return byte[] MAC address values
     * @throws FrameException in case of error in frame
     */
    public static byte[] parseMACAddress(GatewayMgmt msg) throws FrameException {
        // MAC address is returned in VAL1-VAL6 or VAL1-VAL8 decimal dimensions of the MAC address
        // response frame
        String[] values = msg.getDimValues();
        byte[] mac = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            mac[i] = Integer.valueOf(values[i]).byteValue();
        }
        return mac;
    }

    /**
     * OpenWebNet message request for gateway model <code>*#13**15##</code>.
     *
     * @return GatewayMgmt message
     */
    public static GatewayMgmt requestModel() {
        return new GatewayMgmt(
                format(FORMAT_DIMENSION_REQUEST, WHO, "", DimGatewayMgmt.MODEL.value()));
    }

    /**
     * OpenWebNet message request for gateway firmware version <code>*#13**16##</code>.
     *
     * @return GatewayMgmt message
     */
    public static GatewayMgmt requestFirmwareVersion() {
        return new GatewayMgmt(
                format(FORMAT_DIMENSION_REQUEST, WHO, "", DimGatewayMgmt.FIRMWARE_VERSION.value()));
    }

    public static String parseFirmwareVersion(GatewayMgmt msg) throws FrameException {
        // fw version is returned in VAL1-VAL3 decimal dimensions of Firmware version response frame
        String[] values = msg.getDimValues();
        return values[0] + "." + values[1] + "." + values[2];
    }

    /**
     * OpenWebNet message request to scan network <code>*13*65*##</code>.
     *
     * @return GatewayMgmt message
     */
    public static GatewayMgmt requestScanNetwork() {
        return new GatewayMgmt(format(FORMAT_REQUEST, WHO, WhatGatewayMgmt.SCAN.value(), ""));
    }

    /**
     * OpenWebNet message request for product information <code>*#13**66*index##</code>.
     *
     * <p><b>NOTE</b> Due to a bug in the USB gateway, request product info message must use <code>*
     * </code> to separate index instead of <code>#</code>: <code>*#13**66*index##</code> instead of
     * <code>*#13**66#index##</code> as documented in OpenWebNet specs.
     *
     * @param index The index of the product inside the gateway products database as returned from
     *     network scan. Index starts at 0.
     * @return GatewayMgmt message
     */
    public static GatewayMgmt requestProductInfo(int index) {
        // we must use here addValues instead of addDimensions to be compatible with gateway bug
        String req = format(FORMAT_DIMENSION_REQUEST, WHO, "", DimGatewayMgmt.PRODUCT_INFO.value());
        req = addValues(req, index + "");
        return new GatewayMgmt(req);
    }

    @Override
    protected void parseWhere() throws FrameException {
        if (whereStr != null) {
            where = new WhereZigBee(whereStr);
        }
    }

    @Override
    public OpenDeviceType detectDeviceType() {
        return null;
    }
}
