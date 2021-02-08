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
 * @author M. Valla - Initial contribution
 * @author Andrea Conte - Energy manager contribution
 */

package org.openwebnet4j.message;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

import org.openwebnet4j.OpenDeviceType;

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
        ACTIVE_POWER(113);

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
                where = new WhereEnergyManager(whereStr);
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
        // if (isCommand()) { // ignore status/dimension frames for detecting device type
            return OpenDeviceType.SCS_ENERGY_CENTRAL_UNIT;
        // } else {
        //     return null;
        // }
    }    
    
    /**
     * OpenWebNet message request to get active power <b>*#18*<WHERE>*113##</b>.
     *
     * @param where WHERE string
     * @return message
     */
    public static EnergyManagement requestActivePower(String where) {
        return new EnergyManagement(format(FORMAT_DIMENSION, WHO, where, DIM.ACTIVE_POWER.value()));
    }


    /**  setActivePowerNotificationsTime
    *
    * OpenWebNet message request to set <i>Automatic Update Size</i> <b>*#18*<Where>*#1200#<Type>*<Time>##</b>.
    *
    * @param where WHERE string
    * @return message
    */
    public static EnergyManagement setActivePowerNotificationsTime(String where, int time) {
        return new EnergyManagement(format(FORMAT_DIMENSION2, WHO, where, 1200, 1, time));
    }
}
