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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openwebnet4j.communication.serial.spi.SerialPort;
import org.openwebnet4j.communication.serial.spi.SerialPortEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPortEvent;
import gnu.io.UnsupportedCommOperationException;

/**
 * Specific SerialPort implementation based on RxTx gnu.io.SerialPort and gnu.io.CommPortIdentifier.
 *
 * @author M. Valla - Initial contribution
 */
@NonNullByDefault
public class RxTxSerialPort implements SerialPort {

    private static final Logger logger = LoggerFactory.getLogger(RxTxSerialPort.class);

    private class RxTxEvListener implements gnu.io.SerialPortEventListener {

        @Nullable
        SerialPortEventListener lsnr;

        void subscribe(SerialPortEventListener listener) {
            lsnr = listener;
        }

        @Override
        public void serialEvent(@Nullable SerialPortEvent ev) {
            if (ev != null && lsnr != null) {
                lsnr.serialEvent(new RxTxSerialPortEvent(ev));
            }
        }
    }

    private final gnu.io.CommPortIdentifier cpid;
    private gnu.io.@Nullable SerialPort sp = null;
    @Nullable
    RxTxEvListener rxtxListener = null;

    public RxTxSerialPort(final CommPortIdentifier cpid) {
        this.cpid = cpid;
    }

    @Override
    public boolean open() {
        CommPort cp;
        try {
            cp = cpid.open("openwebnet4j", 1000);
        } catch (PortInUseException e) {
            logger.error("PortInUseException while opening port: " + e.getMessage());
            return false;
        }
        if (cp instanceof gnu.io.SerialPort) {
            sp = (gnu.io.SerialPort) cp;
            return true;
        } else {
            logger.error("Error while opening port: we expect a serial port but port is of type {}", cp.getClass());
            return false;
        }
    }

    @Override
    public void close() {
        gnu.io.@Nullable SerialPort lsp = sp;
        if (lsp != null) {
            lsp.close();
        }
    }

    @Override
    public boolean setSerialPortParams(int baudrate, int dataBits, int stopBits, int parity) {
        gnu.io.@Nullable SerialPort lsp = sp;
        if (lsp != null) {
            try {
                lsp.setSerialPortParams(baudrate, dataBits, stopBits, parity);
                return true;
            } catch (UnsupportedCommOperationException e) {
                logger.error("Exception while setting port params: " + e.getMessage());
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public @Nullable InputStream getInputStream() throws IOException {
        gnu.io.@Nullable SerialPort lsp = sp;
        if (lsp != null) {
            return lsp.getInputStream();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable OutputStream getOutputStream() throws IOException {
        gnu.io.@Nullable SerialPort lsp = sp;
        if (lsp != null) {
            return lsp.getOutputStream();
        } else {
            return null;
        }
    }

    @Override
    public boolean addEventListener(@NonNull SerialPortEventListener listener) {
        gnu.io.@Nullable SerialPort lsp = sp;
        if (lsp != null) {
            rxtxListener = new RxTxEvListener();
            rxtxListener.subscribe(listener);
            try {
                lsp.notifyOnDataAvailable(true);
                lsp.addEventListener(rxtxListener);
                return true;
            } catch (TooManyListenersException e) {
                logger.error("Exception while adding event listener: " + e.getMessage());
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public @Nullable String getName() {
        return cpid.getName();
    }

}
