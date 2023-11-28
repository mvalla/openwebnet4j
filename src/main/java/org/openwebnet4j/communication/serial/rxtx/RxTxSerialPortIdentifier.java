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
import org.eclipse.jdt.annotation.Nullable;
import org.openwebnet4j.communication.serial.PortInUseException;
import org.openwebnet4j.communication.serial.SerialPort;
import org.openwebnet4j.communication.serial.SerialPortIdentifier;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;

/**
 * Specific serial port identifier implementation using RxTx CommPortIdentifier
 *
 * @author M. Valla - Initial contribution, inspired by OH Serial Transport
 */
@NonNullByDefault
public class RxTxSerialPortIdentifier implements SerialPortIdentifier {

    final CommPortIdentifier id;

    /**
     * Constructor.
     *
     * @param id the underlying comm port identifier implementation
     */
    public RxTxSerialPortIdentifier(final CommPortIdentifier id) {
        this.id = id;
    }

    @Override
    public String getName() {
        final String name = id.getName();
        return name != null ? name : "";
    }

    @Override
    public SerialPort open(String owner, int timeout) throws PortInUseException {
        try {
            final CommPort cp = id.open(owner, timeout);
            if (cp instanceof gnu.io.SerialPort) {
                gnu.io.SerialPort port = (gnu.io.SerialPort) cp;
                return new RxTxSerialPort(port);
            } else {
                throw new IllegalStateException(
                        String.format("We expect a serial port instead of '%s'", cp.getClass()));
            }
        } catch (gnu.io.PortInUseException e) {
            String message = e.getMessage();
            if (message != null) {
                throw new PortInUseException(message, e);
            } else {
                throw new PortInUseException(e);
            }
        }
    }

    @Override
    public boolean isCurrentlyOwned() {
        return id.isCurrentlyOwned();
    }

    @Override
    public @Nullable String getCurrentOwner() {
        return id.getCurrentOwner();
    }
}
