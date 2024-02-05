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
package org.openwebnet4j.communication.serial.spi;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for a serial port to send receive data and listen to port events.
 *
 * @author M. Valla - Initial contribution
 */
@NonNullByDefault
public interface SerialPort extends Closeable {

    final int DATABITS_8 = 8;
    final int PARITY_NONE = 0;
    final int STOPBITS_1 = 1;

    /**
     * Sets serial port parameters.
     *
     * @param baudrate the baud rate
     * @param dataBits the number of data bits
     * @param stopBits the number of stop bits
     * @param parity the parity
     * @return true if parameters could be set successfully
     */
    public boolean setSerialPortParams(int baudrate, int dataBits, int stopBits, int parity);

    /**
     * Registers a {@link SerialPortEventListener} object to listen for {@link SerialPortEvent}s.
     *
     * Only one listener per SerialPort is allowed: subsequent call attempts to addEventListener will return false.
     *
     * After the port is closed, no more event will be generated.
     *
     * @param listener the listener
     * @return false if too many listeners has been added
     */
    public boolean addEventListener(SerialPortEventListener listener);

    /**
     * Opens the serial port for communicating.
     *
     * @return true if the serial port could be opened successfully
     */
    public boolean open();

    /**
     * Retrieves the name of the serial port.
     *
     * @return the name of the serial port
     */
    public @Nullable String getName();

    /**
     * Returns an input stream to read from the serial port.
     *
     * If the port is unidirectional and doesn't support receiving data, then getInputStream returns null.
     *
     * @return the input stream or null
     * @throws IOException on I/O error
     */
    @Nullable
    public InputStream getInputStream() throws IOException;

    /**
     * Returns an output stream after the SerialPort has been opened.
     *
     * If the port is unidirectional and doesn't support sending data, then getOutputStream returns null.
     *
     * @return the output stream or null
     * @throws IOException on I/O error
     */
    @Nullable
    public OutputStream getOutputStream() throws IOException;

    @Override
    public void close();

}