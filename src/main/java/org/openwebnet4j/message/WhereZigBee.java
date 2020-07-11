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
package org.openwebnet4j.message;

/**
 * WHERE for ZigBee Lighting and Automation frames
 *
 * @author M. Valla - Initial contribution
 */
public class WhereZigBee extends Where {

    public static final String UNIT_01 = "01";
    public static final String UNIT_02 = "02";
    public static final String UNIT_ALL = "00";
    public static final String ZB_NETWORK = "#9";

    private String unit = null; // UNIT part of the address
    private String addr = null; // ADDR part of the address

    public WhereZigBee(String w) throws IllegalArgumentException, NullPointerException {
        // TODO check range for WHERE
        super(w);
        if (whereStr.lastIndexOf('#') > 0 && whereStr.length() >= 4) {
            unit = whereStr.substring(whereStr.length() - 4, whereStr.length() - 2);
            addr = whereStr.substring(0, whereStr.length() - 4);
        } else {
            throw new IllegalArgumentException("WHERE address is invalid");
        }
    }

    public String valueWithUnit(String u) {
        return addr + u + ZB_NETWORK;
    }

    /**
     * Return the UNIT part (ex.: WHERE=123456702#9 -> UNIT=02)
     *
     * @return a String with the UNIT part of this address, null if no UNIT part is found
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Return the ADDR part by removing UNIT and network ('#9')
     * Example: WHERE=123456702#9 -> ADDR=1234567
     *
     * @return a String with the ADDR part of this address, null if no ADDR part is found
     */
    public String getAddr() {
        return addr;
    }

}
