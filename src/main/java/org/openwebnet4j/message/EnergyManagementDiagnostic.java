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
 */
 
 /*
 * OpenWebNet Energy Management Diagnostic messages (WHO=1018)
 *
 * @author Andrea Conte - Initial contribution
 */
package org.openwebnet4j.message;

import org.openwebnet4j.OpenDeviceType;

public class EnergyManagementDiagnostic extends BaseOpenMessage {

    protected EnergyManagementDiagnostic(String value) {
        super(value);
    }

    @Override
    protected void parseWhere() throws FrameException {
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
    public OpenDeviceType detectDeviceType()  {
        if (getWhere().value().startsWith("5")) {
            return OpenDeviceType.SCS_ENERGY_CENTRAL_UNIT;
        } else {
            return null;
        }
    }
    
}  /* class */