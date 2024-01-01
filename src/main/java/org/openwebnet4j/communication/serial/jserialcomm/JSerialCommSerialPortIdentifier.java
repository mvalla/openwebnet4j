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
package org.openwebnet4j.communication.serial.jserialcomm;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openwebnet4j.communication.serial.PortInUseException;
import org.openwebnet4j.communication.serial.SerialPort;
import org.openwebnet4j.communication.serial.SerialPortIdentifier;

/**
 * Specific serial port identifier implementation
 *
 * @author M. Valla - Initial contribution
 */
@NonNullByDefault
public class JSerialCommSerialPortIdentifier implements SerialPortIdentifier {

    final com.fazecast.jSerialComm.SerialPort sp;

    /**
     * Constructor.
     *
     * @param sp the underlying SerialPort
     */
    public JSerialCommSerialPortIdentifier(final com.fazecast.jSerialComm.SerialPort sp) {
        this.sp = sp;
    }

    @Override
    public String getName() {
        final String name = sp.getSystemPortName();
        return name != null ? name : "";
    }

    @Override
    public SerialPort open(String owner, int timeout) throws PortInUseException {
        // sp.setComPortTimeouts(com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 200, 0);
        // sp.setComPortTimeouts(com.fazecast.jSerialComm.SerialPort.TIMEOUT_NONBLOCKING, 0, 0);

        System.out.println("jSerialComm SerialPort.getReadTimeout() = " + sp.getReadTimeout());
        System.out.println("jSerialComm SerialPort.getPortDescription() = " + sp.getPortDescription());

        boolean success = sp.openPort();
        if (success) {
            return new JSerialCommSerialPort(sp);
        } else {
            throw new PortInUseException("Could not open port: " + sp.getSystemPortName());
        }
    }

    @Override
    public boolean isCurrentlyOwned() {
        return false;
    }

    @Override
    public @Nullable String getCurrentOwner() {
        return "massi no one";
    }
}
