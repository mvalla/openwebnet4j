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
import static org.openwebnet4j.message.Who.LIGHTING;

import java.util.HashMap;
import java.util.Map;
import org.openwebnet4j.OpenDeviceType;

/**
 * OpenWebNet Lighting messages (WHO=1)
 *
 * @author M. Valla - Initial contribution
 */
public class Lighting extends BaseOpenMessage {

    public enum WhatLighting implements What {
        // base switch
        OFF(0), // OFF
        ON(1), // ON
        // dimmer
        DIMMER_LEVEL_2(2),
        DIMMER_LEVEL_3(3),
        DIMMER_LEVEL_4(4),
        DIMMER_LEVEL_5(5),
        DIMMER_LEVEL_6(6),
        DIMMER_LEVEL_7(7),
        DIMMER_LEVEL_8(8),
        DIMMER_LEVEL_9(9),
        DIMMER_LEVEL_10(10),
        DIMMER_LEVEL_UP(30), // dimmer up one level
        DIMMER_LEVEL_DOWN(31), // dimmer down one level
        DIMMER_TOGGLE(32), // toggle
        // green switch
        MOVEMENT_DETECTED(34),
        END_MOVEMENT_DETECTED(39);

        private static Map<Integer, WhatLighting> mapping;

        private final int value;

        private WhatLighting(int value) {
            this.value = value;
        }

        private static void initMapping() {
            mapping = new HashMap<Integer, WhatLighting>();
            for (WhatLighting w : values()) {
                mapping.put(w.value, w);
            }
        }

        public static WhatLighting fromValue(int i) {
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
        return WhatLighting.fromValue(i);
    }

    public enum DimLighting implements Dim {
        DIMMER_LEVEL_100(1);

        private static Map<Integer, DimLighting> mapping;

        private final int value;

        private DimLighting(Integer value) {
            this.value = value;
        }

        private static void initMapping() {
            mapping = new HashMap<Integer, DimLighting>();
            for (DimLighting d : values()) {
                mapping.put(d.value, d);
            }
        }

        public static DimLighting fromValue(int i) {
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
        return DimLighting.fromValue(i);
    }

    private static final int WHO = LIGHTING.value();

    public static final int DIMMER_LEVEL_100_OFF = 100;
    public static final int DIMMER_LEVEL_100_MAX = 200;

    protected Lighting(String value) {
        super(value);
        this.who = Who.LIGHTING;
    }

    /**
     * OpenWebNet message request to turn light ON <code>*1*1*WHERE##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Lighting requestTurnOn(String where) {
        return new Lighting(format(FORMAT_REQUEST, WHO, WhatLighting.ON.value, where));
    }

    /**
     * OpenWebNet message request to turn light OFF <code>*1*0*WHERE##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Lighting requestTurnOff(String where) {
        return new Lighting(format(FORMAT_REQUEST, WHO, WhatLighting.OFF.value, where));
    }

    /**
     * OpenWebNet message request to dim light to level <code>*1*level*WHERE##</code>.
     *
     * @param where WHERE string
     * @param level What level (0=Off, 1=On, 2-10=level, 30=Up one level, 31=Down one level,
     *     32=Toggle). See {@link WhatLighting}
     * @return message
     */
    public static Lighting requestDimTo(String where, What level) {
        return new Lighting(format(FORMAT_REQUEST, WHO, level.value(), where));
    }

    /**
     * OpenWebNet message request light status <code>*#1*WHERE##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Lighting requestStatus(String where) {
        return new Lighting(format(FORMAT_STATUS, WHO, where));
    }

    /**
     * Verify OpenWebNet message if light is ON (WHAT=1).
     *
     * @return true if light is ON
     */
    public boolean isOn() {
        if (getWhat() == null) {
            return false;
        } else {
            return getWhat().equals(WhatLighting.ON);
        }
    }

    /**
     * Verify OpenWebNet message if light is OFF (WHAT=0).
     *
     * @return true if light is OFF
     */
    public boolean isOff() {
        if (getWhat() == null) {
            return false;
        } else {
            return getWhat().equals(WhatLighting.OFF);
        }
    }

    /**
     * Parse dimmerLevel100 (DIM: 1)
     *
     * @return corresponding int percentage (0-100)
     * @throws FrameException in case of frame error
     */
    public int parseDimmerLevel100() throws FrameException {
        if (getDim() == Lighting.DimLighting.DIMMER_LEVEL_100) {
            int level100 = Integer.parseInt(getDimValues()[0]);
            if (level100 >= DIMMER_LEVEL_100_OFF && level100 <= DIMMER_LEVEL_100_MAX) {
                return level100 - 100;
            } else {
                throw new FrameException("Value for dimmerLevel100 our of range");
            }
        } else {
            throw new FrameException("Could not parse dimmerLevel100");
        }
    }

    /**
     * Transforms a 0-10 level (int) to a percent (0-100)
     *
     * @param level 0-10
     * @return int percent
     */
    // TODO for now, we use a linear mapping
    public static int levelToPercent(int level) {
        if (level >= 0 && level <= 10) {
            return level * 10;
        } else {
            throw new IllegalArgumentException("level must be between 0 and 10");
        }
    }

    /**
     * Return WHAT corresponding to the brightness percent.
     *
     * @param percent 0-100
     * @return What level (2-10) corresponding to percent
     */
    public static What percentToWhat(int percent) {
        if (percent >= 0 && percent <= 100) {
            return WhatLighting.fromValue(percentToWhatLevel(percent));
        } else {
            throw new IllegalArgumentException("Percent must be between 0 and 100");
        }
    }

    /* Transforms a percent int (0-100) into a 0,2-10 level (int) */
    // TODO for now, we use a linear mapping
    private static int percentToWhatLevel(int percent) {
        int level;
        if (percent == 0) {
            level = 0;
        } else if (percent > 0 && percent < 10) {
            level = 2;
        } else {
            level = (int) Math.floor(percent / 10.0);
            if (level == 1) {
                level++; // level 1 is not allowed -> move to 2
            }
        }
        return level;
    }

    @Override
    protected void parseWhere() throws FrameException {
        if (whereStr == null) {
            throw new FrameException("Lighting frame has no WHERE part: " + whereStr);
        } else {
            if (whereStr.endsWith(WhereZigBee.ZB_NETWORK)) {
                where = new WhereZigBee(whereStr);
            } else {
                where = new WhereLightAutom(whereStr);
            }
        }
    }

    @Override
    public OpenDeviceType detectDeviceType() {
        if (isCommand()) { // ignore status/dimension frames for detecting device type
            OpenDeviceType type = null;
            What w = getWhat();
            if (w != null) {
                if (w == WhatLighting.OFF
                        || w == WhatLighting.ON
                        || w == WhatLighting.MOVEMENT_DETECTED
                        || w == WhatLighting.END_MOVEMENT_DETECTED) {
                    type = OpenDeviceType.SCS_ON_OFF_SWITCH;
                } else if (w.value() >= 2 && w.value() <= 10) {
                    type = OpenDeviceType.SCS_DIMMER_SWITCH;
                }
            }
            return type;
        } else {
            return null;
        }
    }
}
