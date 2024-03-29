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
package org.openwebnet4j.communication.serial.rxtx;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openwebnet4j.communication.serial.spi.SerialPortEvent;

/**
 * Specific SerialPortEvent implementation based on RxTx gnu.io.SerialPortEvent.
 *
 * @author M. Valla - Initial contribution
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

}
