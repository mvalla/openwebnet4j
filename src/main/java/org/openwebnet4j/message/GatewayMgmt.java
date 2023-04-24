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
package org.openwebnet4j.message;

import static java.lang.String.format;
import static org.openwebnet4j.message.Who.GATEWAY_MANAGEMENT;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.openwebnet4j.OpenDeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** OpenWebNet GatewayManagmenet messages */

/** @author M. Valla - Initial contribution. Date and Time */
public class GatewayMgmt extends BaseOpenMessage {

    private static final Logger logger = LoggerFactory.getLogger(GatewayMgmt.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH*mm*ss");
    private static final DateTimeFormatter WEEKDAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("ee*dd*MM*yyyy");

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
        UPTIME(19),
        DATETIME(22),
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
        return new GatewayMgmt(format(FORMAT_REQUEST, WHO, WhatGatewayMgmt.KEEP_CONNECT.value(), ""));
    }

    /**
     * OpenWebNet message request for gateway MAC address <code>*#13**12##</code>.
     *
     * @return GatewayMgmt message
     */
    public static GatewayMgmt requestMACAddress() {
        return new GatewayMgmt(format(FORMAT_DIMENSION_REQUEST, WHO, "", DimGatewayMgmt.MAC_ADDRESS.value()));
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
     * OpenWebNet message request to set DateTime <code>*#13**#22*hh*mm*ss*zzz*ww*dd*MM*yyyy##</code>.
     *
     * @param zdt the ZonedDateTime to set
     * @return GatewayMgmt message
     */
    public static GatewayMgmt requestSetDateTime(ZonedDateTime zdt) {
        return new GatewayMgmt(
                format(FORMAT_DIMENSION_WRITING_1V, WHO, "", DimGatewayMgmt.DATETIME.value(), toOWNDateTime(zdt)));
    }

    /**
     * Parse date and time in the OWN message and return ZonedDateTime
     *
     * @param msg the message to parse
     * @return ZonedDateTime date time parsed from message
     * @throws FrameException in case of error while parsing frame
     */
    public static ZonedDateTime parseDateTime(GatewayMgmt msg) throws FrameException {
        // *#13**22*09*37*30*000*03*01*05*2019##
        // *#13**22* h* m* s* tz* w* d* m* y##
        String[] values = msg.getDimValues();
        try {
            String decodedTZ = (values[3].charAt(0) == '0' ? "+" : "-") + values[3].substring(1) + ":00";
            // System.out.println("Decoded TZ: " + decodedTZ);
            String iso = String.format("%s-%s-%sT%s:%s:%s%s", values[7], values[6], values[5], values[0], values[1],
                    values[2], decodedTZ);
            return ZonedDateTime.parse(iso);
        } catch (Exception e) {
            throw new FrameException("Cannot parse Date and Time message: " + msg.frameValue);
        }
    }

    /**
     * Return a OWN encoded date time from a given ZonedDateTime
     *
     * @param zdt the ZonedDateTime to encode
     * @return String OWN encoded date and time
     */
    public static String toOWNDateTime(ZonedDateTime zdt) {
        String time = zdt.format(TIME_FORMATTER);
        String offset = zdt.getOffset().getId();
        String ownTZ = (offset.charAt(0) == '+' ? '0' : '1') + offset.substring(1, 3);
        String date = zdt.format(WEEKDAY_DATE_FORMATTER);
        if (date.charAt(1) == '7') { // replace Sunday=07 with Sunday=00
            date = "00" + date.substring(2);
        }
        String dateTime = time + '*' + ownTZ + '*' + date;
        return dateTime;
    }

    /**
     * OpenWebNet message request for gateway model <code>*#13**15##</code>.
     *
     * @return GatewayMgmt message
     */
    public static GatewayMgmt requestModel() {
        return new GatewayMgmt(format(FORMAT_DIMENSION_REQUEST, WHO, "", DimGatewayMgmt.MODEL.value()));
    }

    /**
     * OpenWebNet message request for gateway firmware version <code>*#13**16##</code>.
     *
     * @return GatewayMgmt message
     */
    public static GatewayMgmt requestFirmwareVersion() {
        return new GatewayMgmt(format(FORMAT_DIMENSION_REQUEST, WHO, "", DimGatewayMgmt.FIRMWARE_VERSION.value()));
    }

    public static String parseFirmwareVersion(GatewayMgmt msg) throws FrameException {
        // fw version is returned in VAL1-VAL3 decimal dimensions of Firmware version response frame
        String[] values = msg.getDimValues();
        if (values.length == 3) {
            return values[0] + "." + values[1] + "." + values[2];
        } else {
            throw new FrameException("Cannot parse values for message: " + msg.frameValue);
        }
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
     * <p>
     * <b>NOTE</b> Due to a bug in the USB gateway, request product info message must use <code>*
     * </code> to separate index instead of <code>#</code>: <code>*#13**66*index##</code> instead of
     * <code>*#13**66#index##</code> as documented in OpenWebNet specs.
     *
     * @param index The index of the product inside the gateway products database as returned from
     *            network scan. Index starts at 0.
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
