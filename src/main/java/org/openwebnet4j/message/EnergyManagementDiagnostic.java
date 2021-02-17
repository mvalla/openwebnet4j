/**
 * Copyright (c) 2020-2021 Contributors to the openwebnet4j project
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
 * OpenWebNet Energy Management Diagnostic messages (WHO=1018)
 *
 * @author Andrea Conte - Initial contribution
 * @author M. Valla - updated diagnostic part
 */
public class EnergyManagementDiagnostic extends BaseOpenMessage {

    private static final int WHO = Who.ENERGY_MANAGEMENT_DIAGNOSTIC.value();

    protected EnergyManagementDiagnostic(String value) {
        super(value);
        this.who = Who.ENERGY_MANAGEMENT_DIAGNOSTIC;
    }

    /**
     * OpenWebNet message to request diagnostic DIM 7 (undocumented) <code>*#1018*WHERE*7##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static EnergyManagementDiagnostic requestDiagnostic(String where) {
        return new EnergyManagementDiagnostic(format(FORMAT_DIMENSION_REQUEST, WHO, where, 7));
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
        return null;
    }

    @Override
    protected What whatFromValue(int i) {
        return null;
    }

    @Override
    public OpenDeviceType detectDeviceType() {
        if (getWhere().value().startsWith("5")) {
            return OpenDeviceType.SCS_ENERGY_METER;
        } else {
            return null;
        }
    }
}
