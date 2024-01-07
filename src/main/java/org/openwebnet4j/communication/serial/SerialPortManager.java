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
package org.openwebnet4j.communication.serial;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openwebnet4j.communication.serial.spi.SerialPort;
import org.openwebnet4j.communication.serial.spi.SerialPortProvider;

/**
 * Class to find available serial ports via a {@link SerialPortProvider} implementation
 *
 * @author M. Valla - Initial contribution
 */
@NonNullByDefault
public class SerialPortManager {

    private static final String DEFAULT_PROVIDER = "org.openwebnet4j.communication.serial.rxtx.RxTxSerialPortProvider";
    // private static final String DEFAULT_PROVIDER =
    // "org.openwebnet4j.communication.serial.jserialcomm.JSerialCommPortProvider";

    @Nullable
    private SerialPortProvider provider;

    public SerialPortManager() throws SerialPortException {
        loadProvider(DEFAULT_PROVIDER);
    }

    public SerialPortManager(String providerName) throws SerialPortException {
        loadProvider(providerName);
    }

    private void loadProvider(String providerName) throws SerialPortException {
        ServiceLoader<SerialPortProvider> loader = ServiceLoader.load(SerialPortProvider.class);
        Iterator<SerialPortProvider> it = loader.iterator();
        while (it.hasNext()) {
            SerialPortProvider provider = it.next();
            if (providerName.equals(provider.getClass().getName())) {
                this.provider = provider;
                return;
            }
        }
        throw new SerialPortException("SerialPortProvider " + providerName + " not found");
    }

    /**
     * Returns a serial port given a port name.
     *
     * @param name the name
     * @return a serial port identifier or null
     */
    public @Nullable SerialPort getSerialPort(final String name) {
        if (provider != null) {
            return provider.getSerialPort(name);
        } else {
            return null;
        }
    }

    /**
     * Returns a stream of available serial ports.
     *
     * {@link SerialPortProvider}s may not be able to list any or all serial ports.
     * When the port name is known, the preferred way to get a serial port is by using {@link #getSerialPort(String)}.
     *
     * @return stream of available serial ports
     */
    public Stream<SerialPort> getSerialPorts() {
        if (provider != null) {
            return provider.getSerialPorts();
        } else {
            return Stream.empty();
        }
    }

}