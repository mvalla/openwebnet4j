/**
 * Copyright (c) 2020-2024 Contributors to the openwebnet4j project
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

import org.openwebnet4j.OpenDeviceType;

/**
 * OpenWebNet Thermoregulation Diagnostic messages (WHO=1004) used to discover Thermo devices on BUS
 *
 * @author M. Valla - Initial contribution
 */
public class ThermoregulationDiagnostic extends BaseOpenMessage {

    private static final int WHO = Who.THERMOREGULATION_DIAGNOSTIC.value();

    protected ThermoregulationDiagnostic(String value) {
        super(value);
        this.who = Who.THERMOREGULATION_DIAGNOSTIC;
    }

    /**
     * OpenWebNet message to request diagnostic DIM 7 (undocumented) <code>*#1004*WHERE*7##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static ThermoregulationDiagnostic requestDiagnostic(String where) {
        return new ThermoregulationDiagnostic(format(FORMAT_DIMENSION_REQUEST, WHO, where, 7));
    }

    @Override
    protected void parseWhere() throws FrameException {
        if (whereStr == null) {
            throw new FrameException("Frame has no WHERE part: " + whereStr);
        } else {
            if (whereStr.endsWith(WhereZigBee.ZB_NETWORK)) {
                where = new WhereZigBee(whereStr);
            } else {
                where = new WhereThermo(whereStr);
            }
        }
    }

    @Override
    protected Dim dimFromValue(int i) {
        return null;
    }

    @Override
    protected What whatFromValue(int i) {
        return null;
    }

    @Override
    public OpenDeviceType detectDeviceType() {
        WhereThermo w = (WhereThermo) getWhere();
        if (w == null) {
            return null;
        } else {
            if (w.value().startsWith("0")) {
                return OpenDeviceType.SCS_THERMO_CENTRAL_UNIT;
            }
            if (w.isProbe()) {
                return OpenDeviceType.SCS_THERMO_SENSOR;
            } else if (w.isCentralUnit()) {
                return OpenDeviceType.SCS_THERMO_CENTRAL_UNIT;
            } else {
                return OpenDeviceType.SCS_THERMO_ZONE;
            }
        }
    }
}
