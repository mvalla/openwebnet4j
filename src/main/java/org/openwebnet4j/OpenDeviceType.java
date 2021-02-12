/**
 * Copyright (c) 2021 Contributors to the openwebnet4j project
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

import java.util.HashMap;
import java.util.Map;

/**
 * @author M. Valla - Initial contribution
 */
public enum OpenDeviceType {
    /**
     * OpenWebNet Device types. See OWN ZigBee docs pages 50-51
     */

    UNKNOWN(0),
    SCENARIO_CONTROL(2),
    // lighting
    ZIGBEE_ON_OFF_SWITCH(256),
    ZIGBEE_DIMMER_CONTROL(257),
    ZIGBEE_DIMMER_SWITCH(258),
    ZIGBEE_SWITCH_MOTION_DETECTOR(259),
    ZIGBEE_DAYLIGHT_SENSOR(260),
    SCS_ON_OFF_SWITCH(261),
    SCS_DIMMER_CONTROL(262),
    SCS_DIMMER_SWITCH(263),
    ZIGBEE_WATERPROOF_1_GANG_SWITCH(264),
    ZIGBEE_AUTOMATIC_DIMMER_SWITCH(265),
    ZIGBEE_TOGGLE_CONTROL(266),
    SCS_TOGGLE_CONTROL(267),
    ZIGBEE_MOTION_DETECTOR(268),
    ZIGBEE_SWITCH_MOTION_DETECTOR_II(269),
    ZIGBEE_MOTION_DETECTOR_II(270),
    MULTIFUNCTION_SCENARIO_CONTROL(273),
    ZIGBEE_ON_OFF_CONTROL(274),
    // auxiliary
    ZIGBEE_AUXILIARY_MOTION_CONTROL(271),
    SCS_AUXILIARY_TOGGLE_CONTROL(272),
    ZIGBEE_AUXILIARY_ON_OFF_1_GANG_SWITCH(275),
    // automation
    ZIGBEE_SHUTTER_CONTROL(512),
    ZIGBEE_SHUTTER_SWITCH(513),
    SCS_SHUTTER_CONTROL(514),
    SCS_SHUTTER_SWITCH(515),
    // thermoregulation (not defined by BTicino)
    SCS_TEMP_SENSOR(410),
    SCS_THERMOSTAT(420),
    SCS_THERMO_CENTRAL_UNIT(430),
    // interface
    SCS_1_SYSTEM_1_4_GATEWAY(1024),
    SCS_2_SYSTEM_1_4_GATEWAY(1025),
    NETWORK_REPEATER(1029),
    OPENWEBNET_INTERFACE(1030),
    // video
    VIDEO_SWITCHER(1536),
    // energy management (not defined by BTicino)
    SCS_ENERGY_CENTRAL_UNIT(1830),
    // dry contacts and IR interfaces (not defined by BTicino)
    SCS_DRY_CONTACT_IR(2510);

    private final Integer value;
    private static Map<Integer, OpenDeviceType> mapping;

    private OpenDeviceType(int value) {
        this.value = value;
    }

    private static void initMapping() {
        mapping = new HashMap<Integer, OpenDeviceType>();
        for (OpenDeviceType t : values()) {
            mapping.put(t.value, t);
        }
    }

    public static OpenDeviceType fromValue(int value) {
        if (mapping == null) {
            initMapping();
        }
        return mapping.get(value);
    }
}
