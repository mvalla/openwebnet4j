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
import static org.openwebnet4j.message.Who.AUX;

import java.util.HashMap;
import java.util.Map;

import org.openwebnet4j.OpenDeviceType;

/**
 * OpenWebNet Auxiliary messages (WHO=9)
 *
 * @author M. Valla - Initial contribution
 * @author G.Fabiani - Added auxiliary message support
 */
public class Auxiliary extends BaseOpenMessage {

    public enum WhatAuxiliary implements What {
        OFF(0),
        ON(1),
        TOGGLE(2),
        STOP(3),
        UP(4),
        DOWN(5),
        ENABLED(6),
        DISABLED(7),
        RESET_GEN(8),
        RESET_BI(9),
        RESET_TRI(10);

        private static Map<Integer, WhatAuxiliary> mapping;

        private final int value;

        private WhatAuxiliary(int value) {
            this.value = value;
        }

        public static void initMapping() {
            mapping = new HashMap<Integer, WhatAuxiliary>();
            for (WhatAuxiliary w : values()) {
                mapping.put(w.value, w);
            }
        }

        public static WhatAuxiliary fromValue(int i) {
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

    private static final int WHO = AUX.value();

    protected Auxiliary(String value) {
        super(value);
        this.who = Who.AUX;
    }

    /**
     * OpenWebNet message request to turn auxiliary ON <code>*9*1*WHERE##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Auxiliary requestTurnOn(String where) {
        return new Auxiliary(format(FORMAT_REQUEST, WHO, WhatAuxiliary.ON.value, where));
    }

    /**
     * OpenWebNet message request to turn auxiliary OFF <code>*9*0*WHERE##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Auxiliary requestTurnOff(String where) {
        return new Auxiliary(format(FORMAT_REQUEST, WHO, WhatAuxiliary.OFF.value, where));
    }

    /**
     * OpenWebNet message request auxiliary status <code>*#9*WHERE##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Auxiliary requestStatus(String where) {
        return new Auxiliary(format(FORMAT_STATUS, WHO, where));
    }

    /**
     * Verify OpenWebNet message if auxiliary is ON (WHAT=1).
     *
     * @return true if auxiliary is ON
     */
    public boolean isOn() {
        if (getWhat() == null) {
            return false;
        } else {
            return getWhat().equals(Auxiliary.WhatAuxiliary.ON);
        }
    }

    /**
     * Verify OpenWebNet message if auxiliary is OFF (WHAT=0).
     *
     * @return true if auxiliary is OFF
     */
    public boolean isOff() {
        if (getWhat() == null) {
            return false;
        } else {
            return getWhat().equals(Auxiliary.WhatAuxiliary.OFF);
        }
    }

    @Override
    protected void parseWhere() throws FrameException {
        if (whereStr == null) {
            throw new FrameException("Frame has no WHERE part: " + whereStr);
        } else {
            if (whereStr.endsWith(WhereZigBee.ZB_NETWORK)) {
                where = new WhereZigBee(whereStr);
            } else {
                where = new WhereAuxiliary(whereStr);
            }
        }
    }

    @Override
    protected Dim dimFromValue(int i) {
        return null;
    }

    @Override
    protected What whatFromValue(int i) {
        return WhatAuxiliary.fromValue(i);
    }

    @Override
    public OpenDeviceType detectDeviceType() {
        if (isCommand()) { // ignore status/dimension frames for detecting device type
            OpenDeviceType type = null;
            What w = getWhat();
            if (w != null) {
                if (w == WhatAuxiliary.DOWN || w == WhatAuxiliary.ON || w == WhatAuxiliary.OFF
                        || w == WhatAuxiliary.TOGGLE || w == WhatAuxiliary.DISABLED || w == WhatAuxiliary.ENABLED
                        || w == WhatAuxiliary.STOP || w == WhatAuxiliary.UP || w == WhatAuxiliary.RESET_GEN
                        || w == WhatAuxiliary.RESET_BI || w == WhatAuxiliary.RESET_TRI) {
                    type = OpenDeviceType.SCS_AUXILIARY_TOGGLE_CONTROL;
                }
            }
            return type;
        } else {
            return null;
        }
    }
}
