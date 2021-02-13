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
package org.openwebnet4j.message;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

import org.openwebnet4j.OpenDeviceType;

/**
 * OpenWebNet Energy Management messages (WHO=18)
 *
 * @author Andrea Conte - Initial contribution
 */
public class EnergyManagement extends BaseOpenMessage {

    public enum WHAT implements What {
        AUTOMATIC_RESET_ON(26),
        AUTOMATIC_RESET_OFF(27);

        private static Map<Integer, WHAT> mapping;

        private final int value;

        private WHAT(int value) {
            this.value = value;
        }

        private static void initMapping() {
            mapping = new HashMap<Integer, WHAT>();
            for (WHAT w : values()) {
                mapping.put(w.value, w);
            }
        }

        public static WHAT fromValue(int i) {
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

    public enum DIM implements Dim {
        ACTIVE_POWER(113),
        ACTIVE_POWER_NOTIFICATION_TIME(1200);

        private static Map<Integer, DIM> mapping;

        private final int value;

        private DIM(Integer value) {
            this.value = value;
        }

        private static void initMapping() {
            mapping = new HashMap<Integer, DIM>();
            for (DIM d : values()) {
                mapping.put(d.value, d);
            }
        }

        public static DIM fromValue(int i) {
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

    private static final int WHO = org.openwebnet4j.message.Who.ENERGY_MANAGEMENT.value();

    protected EnergyManagement(String value) {
        super(value);
        this.who = Who.ENERGY_MANAGEMENT;
    }

    @Override
    protected void parseWhere() throws FrameException {
        if (whereStr == null) {
            throw new FrameException("Frame has no WHERE part: " + whereStr);
        } else {
            if (whereStr.endsWith(WhereZigBee.ZB_NETWORK)) {
                where = new WhereZigBee(whereStr);
            } else {
                where = new WhereEnergyManagement(whereStr);
            }
        }
    }

    @Override
    protected Dim dimFromValue(int i) {
        return DIM.fromValue(i);
    }

    @Override
    protected What whatFromValue(int i) {
        return WHAT.fromValue(i);
    }

    @Override
    public OpenDeviceType detectDeviceType() throws FrameException {
        if (getWhere().value().startsWith("5")) {
            return OpenDeviceType.SCS_ENERGY_CENTRAL_UNIT;
        } else {
            return null;
        }
    }

    /**
     * OpenWebNet message request to get active power <code>*#18*WHERE*113##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static EnergyManagement requestActivePower(String where) {
        return new EnergyManagement(format(FORMAT_DIMENSION_REQUEST, WHO, where, DIM.ACTIVE_POWER.value()));
    }

    /**
     * OpenWebNet message to set for how many minutes instantaneous active power change
     * notifications will be sent <code>*#18*WHERE*#1200#1*TIME##</code>.
     *
     * @param where WHERE string
     * @param time For how many minutes (0-255) active power change notifications will be sent. With
     *            time=0 active power change notifications will be stopped.
     * @return message
     */
    public static EnergyManagement setActivePowerNotificationsTime(String where, int time) {
        if (time < 0 || time > 255) {
            time = 0;
        }
        return new EnergyManagement(format(FORMAT_DIMENSION_WRITING_1P_1V, WHO, where,
                DIM.ACTIVE_POWER_NOTIFICATION_TIME.value(), 1, time));
    }
}
