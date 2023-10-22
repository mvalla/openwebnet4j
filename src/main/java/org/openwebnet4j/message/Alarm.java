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

import java.util.HashMap;
import java.util.Map;

import org.openwebnet4j.OpenDeviceType;

/**
 * OpenWebNet Alarm messages (WHO=5)
 *
 * @author M. Valla - Initial contribution
 *
 */

public class Alarm extends BaseOpenMessage {

    public enum WhatAlarm implements What {
        SYSTEM_MAINTENANCE(0),
        SYSTEM_ACTIVE(1),
        SYSTEM_INACTIVE(2),
        DELAY_END(3),
        SYSTEM_BATTERY_FAULT(4),
        SYSTEM_BATTERY_OK(5),
        SYSTEM_NETWORK_ERROR(6),
        SYSTEM_NETWORK_OK(7),
        SYSTEM_ENGAGED(8),
        SYSTEM_DISENGAGED(9),
        SYSTEM_BATTERY_UNLOADED(10),
        ZONE_ENGAGED(11),
        ZONE_ALARM_TECHNICAL(12),
        ZONE_ALARM_TECHNICAL_RESET(13),
        NO_CONNECTION_TO_DEVICE(14),
        ZONE_ALARM_INTRUSION(15),
        ZONE_ALARM_TAMPERING(16),
        ZONE_ALARM_ANTI_PANIC(17),
        ZONE_DISENGAGED(18),
        START_PROGRAMMING(26),
        STOP_PROGRAMMING(27),
        ZONE_ALARM_SILENT(31);

        private static Map<Integer, WhatAlarm> mapping;

        private final int value;

        private WhatAlarm(int value) {
            this.value = value;
        }

        public static void initMapping() {
            mapping = new HashMap<Integer, WhatAlarm>();
            for (WhatAlarm w : values()) {
                mapping.put(w.value, w);
            }
        }

        /**
         * Return enum from value
         *
         * @param i the value
         * @return the corresponding enum
         */
        public static WhatAlarm fromValue(int i) {
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

    private static final int WHO = Who.BURGLAR_ALARM.value();

    protected Alarm(String value) {
        super(value);
        this.who = Who.BURGLAR_ALARM;
    }

    /**
     * OpenWebNet message to request alarm system status <code>*#5*0##</code>.
     *
     * @return message
     */
    public static Alarm requestSystemStatus() {
        return new Alarm(format(FORMAT_STATUS, WHO, "0"));

    }

    /**
     * OpenWebNet message to request alarm zone status <code>*#5*#Z##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Alarm requestZoneStatus(String where) {
        return new Alarm(format(FORMAT_STATUS, WHO, where));

    }

    @Override
    protected void parseWhere() throws FrameException {
        if (whereStr != null) {
            where = new WhereAlarm(whereStr);
        }
    }

    @Override
    protected Dim dimFromValue(int i) {
        return null;
    }

    @Override
    protected What whatFromValue(int i) {
        return WhatAlarm.fromValue(i);
    }

    @Override
    public OpenDeviceType detectDeviceType() {
        if (isCommand()) { // ignore status/dimension frames for detecting device type
            WhereAlarm w = (WhereAlarm) getWhere();
            if (w == null) {
                return OpenDeviceType.SCS_ALARM_CENTRAL_UNIT;
            } else {
                return OpenDeviceType.SCS_ALARM_ZONE;
            }
        } else {
            return null;
        }
    }
}
