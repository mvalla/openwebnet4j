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
package org.openwebnet4j.communication.serial;

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class to manage Serial Port
 *
 * @author M. Valla - Initial contribution, inspired by OH Serial Transport
 */
@NonNullByDefault
public class SerialPortManager {

    private static final String DEFAULT_PROVIDER = "org.openwebnet4j.communication.serial.rxtx.RxTxPortProvider";

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
     * Gets a serial port identifier for a given name.
     *
     * @param name the name
     * @return a serial port identifier or null
     */
    public @Nullable SerialPortIdentifier getIdentifier(final String name) {
        final Optional<SerialPortIdentifier> opt = getIdentifiers().filter(id -> id.getName().equals(name)).findFirst();
        if (opt.isPresent()) {
            return opt.get();
        } else {
            return null;
        }
    }

    /**
     * Gets the discovered serial port identifiers.
     *
     * {@link SerialPortProvider}s may not be able to discover any or all identifiers.
     * When the port name is known, the preferred way to get an identifier is by using {@link #getIdentifier(String)}.
     *
     * @return stream of discovered serial port identifiers
     */
    public Stream<SerialPortIdentifier> getIdentifiers() {
        if (provider != null) {
            return provider.getSerialPortIdentifiers();
        } else {
            return Stream.empty();
        }
    }
}