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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openwebnet4j.communication.serial.SerialPort;
import org.openwebnet4j.communication.serial.SerialPortEventListener;
import org.openwebnet4j.communication.serial.UnsupportedCommOperationException;

import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

/**
 * Specific serial port implementation.
 *
 * @author M. Valla - Initial contribution
 */
@NonNullByDefault
public class JSerialCommSerialPort implements SerialPort {

    private class JSerialCommListener implements SerialPortDataListener {

        @Nullable
        SerialPortEventListener lsnr;

        void subscribe(SerialPortEventListener listener) {
            lsnr = listener;
        }

        @Override
        public int getListeningEvents() {
            return com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_AVAILABLE
                    // | com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_DATA_RECEIVED
                    | com.fazecast.jSerialComm.SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
        }

        @Override
        public void serialEvent(@Nullable SerialPortEvent event) {
            if (lsnr != null && event != null) {
                lsnr.serialEvent(new JSerialCommSerialPortEvent(event));
            }

        }

    }

    private final com.fazecast.jSerialComm.SerialPort sp;
    @Nullable
    JSerialCommListener massiL = null;

    /**
     * Constructor.
     *
     * @param sp the underlying serial port implementation
     */
    public JSerialCommSerialPort(final com.fazecast.jSerialComm.SerialPort sp) {
        this.sp = sp;
    }

    @Override
    public void close() {
        sp.closePort();
    }

    @Override
    public void setSerialPortParams(int baudrate, int dataBits, int stopBits, int parity)
            throws UnsupportedCommOperationException {
        if (!sp.setComPortParameters(baudrate, dataBits, stopBits, parity)) {
            throw new UnsupportedCommOperationException("Cannot set params");
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
        massiL = new JSerialCommListener();
        massiL.subscribe(listener);
        sp.addDataListener(massiL);
    }

    @Override
    public String getName() {
        return sp.getSystemPortName();
    }

}
