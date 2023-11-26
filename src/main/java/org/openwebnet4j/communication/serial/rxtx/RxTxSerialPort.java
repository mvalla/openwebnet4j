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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openwebnet4j.communication.serial.SerialPort;
import org.openwebnet4j.communication.serial.SerialPortEventListener;
import org.openwebnet4j.communication.serial.UnsupportedCommOperationException;

import gnu.io.SerialPortEvent;

/**
 * Specific serial port implementation.
 *
 * @author M. Valla - Initial contribution, inspired by OH Serial Transport
 */
@NonNullByDefault
public class RxTxSerialPort implements SerialPort {

    private final gnu.io.SerialPort sp;

    /**
     * Constructor.
     *
     * @param sp the underlying serial port implementation
     */
    public RxTxSerialPort(final gnu.io.SerialPort sp) {
        this.sp = sp;
    }

    @Override
    public void close() {
        sp.close();
    }

    @Override
    public void setSerialPortParams(int baudrate, int dataBits, int stopBits, int parity)
            throws UnsupportedCommOperationException {
        try {
            sp.setSerialPortParams(baudrate, dataBits, stopBits, parity);
        } catch (gnu.io.UnsupportedCommOperationException ex) {
            throw new UnsupportedCommOperationException(ex);
        }
    }

    @Override
    public @Nullable InputStream getInputStream() throws IOException {
        return sp.getInputStream();
    }

    @Override
    public @Nullable OutputStream getOutputStream() throws IOException {
        return sp.getOutputStream();
    }

    @Override
    public void addEventListener(SerialPortEventListener listener) throws TooManyListenersException {
        sp.addEventListener(new gnu.io.SerialPortEventListener() {
            @Override
            public void serialEvent(final @Nullable SerialPortEvent event) {
                if (event == null) {
                    return;
                }
                listener.serialEvent(new RxTxSerialPortEvent(event));
            }
        });
    }

    @Override
    public String getName() {
        return sp.getName();
    }

}
