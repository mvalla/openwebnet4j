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
package org.openwebnet4j.communication.serial.rxtx;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openwebnet4j.communication.serial.SerialPortEvent;

/**
 * Specific serial port event implementation.
 *
 * @author M. Valla - Initial contribution, inspired by OH Serial Transport
 */
@NonNullByDefault
public class RxTxSerialPortEvent implements SerialPortEvent {

    private final gnu.io.SerialPortEvent event;

    /**
     * Constructor.
     *
     * @param event the underlying event implementation
     */
    public RxTxSerialPortEvent(final gnu.io.SerialPortEvent event) {
        this.event = event;
    }

    @Override
    public int getEventType() {
        return event.getEventType();
    }

    @Override
    public boolean getNewValue() {
        return event.getNewValue();
    }
}
