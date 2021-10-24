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
        BATTERY_FAULT(4),
        BATTERY_OK(5),
        NETWORK_NOT_OK(6),
        NETWORK_OK(7),
        SYSTEM_ENGAGED(8),
        SYSTEM_NOT_ENGAGED(9),
        BATTERY_UNLOADED(10),
        ZONE_ENGAGED(11),
        ZONE_TECHNICAL_ALARM(12),
        ZONE_RESET_TECHNICAL_ALARM(13),
        NO_CONNECTION_TO_DEVICE(14),
        ZONE_INTRUSION_ALARM(15),
        ZONE_TAMPERING_ALARM(16),
        ZONE_ANTI_PANIC_ALARM(17),
        ZONE_NOT_ENGAGED(18),
        START_PROGRAMMING(26),
        STOP_PROGRAMMING(27),
        ZONE_SILENT_ALARM(31);

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
     * OpenWebNet message to request alarm system status <code>*#5##</code>.
     *
     * @return message
     */
    public static Alarm requestSystemStatus() {
        // return new Alarm(format(FORMAT_STATUS_NO_WHERE, WHO)); TODO
        return new Alarm(format(FORMAT_STATUS, WHO, "0"));

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
            return OpenDeviceType.SCS_ALARM_CENTRAL_UNIT;
        } else {
            return null;
        }
    }
}
