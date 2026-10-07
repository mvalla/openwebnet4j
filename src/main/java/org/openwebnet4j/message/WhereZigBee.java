/**
 * Copyright (c) 2020-2026 Contributors to the openwebnet4j project
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

/**
 * WHERE for ZigBee Lighting and Automation frames
 *
 * OpenWebNet ZigBee WHERE address format:
 *
 * <ul>
 * <li>UNIT is 2 digit: 00-99
 * <li>ADDR is the MAC address of the ZigBee device converted in decimal format, for example: MAC ID = 0x000501F8 -&gt;
 * 0d328184
 * <li>Broadcast address format: 0# + UNIT + #9
 * <li>Unicast address format: ADDR + UNIT + #9
 * </ul>
 *
 * @author M. Valla - Initial contribution, added range checks in constructor
 */
public class WhereZigBee extends Where {

    /** ADDR part used in broadcast WHERE addresses: format is 0#&lt;UNIT&gt;#9 */

    private static final String BROADCAST_ADDR = "0#";

    public static final String UNIT_01 = "01";
    public static final String UNIT_02 = "02";
    public static final String UNIT_ALL = "00";
    public static final String ZB_NETWORK = "#9";

    private final String unit; // UNIT part of the WHERE address
    private final String addr; // ADDR part of the WHERE address
    private final boolean general; // true if WHERE is of type broadcast (0#UNIT#9)

    public WhereZigBee(String w) throws IllegalArgumentException, NullPointerException {
        super(w);
        final String invalidMsg = "WHERE address is invalid: ";
        int len = whereStr.length();
        // 1) WHERE must end with network suffix "#9" and be long enough for at least ADDR(1)+UNIT(2)+NETWORK(2)
        if (len < 5 || !whereStr.endsWith(ZB_NETWORK)) {
            throw new IllegalArgumentException(invalidMsg + "must be at least 5 chars and end with " + ZB_NETWORK);
        }
        // 2) UNIT: the 2 chars right before "#9", must be digits 00-99
        char u0 = whereStr.charAt(len - 4);
        char u1 = whereStr.charAt(len - 3);
        if (u0 < '0' || u0 > '9' || u1 < '0' || u1 > '9') {
            throw new IllegalArgumentException(invalidMsg + "UNIT must be in range 00-99");
        }
        unit = whereStr.substring(len - 4, len - 2);
        addr = whereStr.substring(0, len - 4);
        // 3) and 4) ADDR: either broadcast ("0#") or unicast (digits only, from MAC-derived decimal address)
        if (addr.equals(BROADCAST_ADDR)) {
            general = true;
        } else {
            general = false;
            if (addr.isEmpty()) {
                throw new IllegalArgumentException(invalidMsg + "ADDR part is missing");
            }
            for (int i = 0; i < addr.length(); i++) {
                char c = addr.charAt(i);
                if (c < '0' || c > '9') {
                    throw new IllegalArgumentException(invalidMsg + "ADDR must be numeric");
                }
            }
        }
    }

    /**
     * Returns a String with the value of this WHERE using the provided string as UNIT
     *
     * @param u the UNIT string
     * @return a String with the value of this WHERE ending with u as UNIT
     */
    public String valueWithUnit(String u) {
        return addr + u + ZB_NETWORK;
    }

    /**
     * Return the UNIT part (ex.: WHERE=123456702#9 -&gt; UNIT=02)
     *
     * @return a String with the UNIT part of this address
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Return the ADDR part by removing UNIT (e.g. '02') and network ('#9') Example:
     * WHERE=123456702#9 -&gt; ADDR=1234567
     *
     * @return a String with the ADDR part of this address
     */
    public String getAddr() {
        return addr;
    }

    /**
     * Return true if this WHERE address is of type broadcast (format: 0#UNIT#9)
     *
     * @return true if this WHERE is a broadcast address, false if it is a unicast address
     */
    public boolean isGeneral() {
        return general;
    }
}
