/**
 * Copyright (c) 2020-2026 Contributors to the openwebnet4j project
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

import org.openwebnet4j.OpenDeviceType;

/**
 * OpenWebNet Default message used to represent any other not recognised/supported message on BUS.
 *
 * @author M. Valla - Initial contribution
 */
public class DefaultMessage extends BaseOpenMessage {

    protected DefaultMessage(String value) {
        super(value);
    }

    @Override
    protected void parseWhere() throws FrameException {
        if (whereStr == null) {
            throw new FrameException("Frame has no WHERE part: " + whereStr);
        } else {
            if (whereStr.endsWith(WhereZigBee.ZB_NETWORK)) {
                where = new WhereZigBee(whereStr);
            } else {
                where = new Where(whereStr);
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
        return OpenDeviceType.UNKNOWN;

    }
}
