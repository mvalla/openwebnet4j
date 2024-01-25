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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to find available serial ports via a {@link SerialPortProvider} implementation
 *
 * @author M. Valla - Initial contribution
 */
@NonNullByDefault
public class SerialPortManager {

    private final Logger logger = LoggerFactory.getLogger(SerialPortManager.class);

    // private static final String DEFAULT_PROVIDER =
    // "org.openwebnet4j.communication.serial.rxtx.RxTxSerialPortProvider";

    private SerialPortProvider provider;

    /**
     * Constructor. Will use first available {@link SerialPortProvider} implementations, or use default implementation
     * if no implementation can be found
     *
     */
    public SerialPortManager() throws SerialPortException {
        provider = getFirstProvider();
    }

    private SerialPortProvider getFirstProvider() throws SerialPortException {
        ServiceLoader<SerialPortProvider> loader = ServiceLoader.load(SerialPortProvider.class);

        // FIXME -SPI- REMOVE.ME
        printProviders(loader);

        SerialPortProvider prv;
        Iterator<SerialPortProvider> it = loader.iterator();
        logger.info("**************** SerialPortManager *** Getting first SerialPortProvider...");
        while (it.hasNext()) {
            prv = it.next();
            logger.info("**************** SerialPortManager *** FOUND first SerialPortProvider via ServiceLoader: {}",
                    prv);
            logger.info("*************************************************************************************");
            return prv;
        }
        logger.info("**************** SerialPortManager *** NO SerialPortProvider found via ServiceLoader!");

        // prv = new RxTxSerialPortProvider();
        // logger.info("**************** SerialPortManager *** Using DEFAULT SerialPortProvider: {}", prv);
        // logger.info("*************************************************************************************");
        // return prv;
        throw new SerialPortException("No SerialPortProvider found");
    }

    /**
     * Constructor. Will load given {@link SerialPortProvider} implementation.
     *
     * @param providerName class name (FQCL) of {@link SerialPortProvider} implementation to be used
     * @throws SerialPortException in case no {@link SerialPortProvider} implementation with given class name
     *             can be found
     */
    public SerialPortManager(String providerName) throws SerialPortException {
        provider = getProvider(providerName);
    }

    private SerialPortProvider getProvider(String providerName) throws SerialPortException {
        ServiceLoader<SerialPortProvider> loader = ServiceLoader.load(SerialPortProvider.class);
        Iterator<SerialPortProvider> it = loader.iterator();
        while (it.hasNext()) {
            SerialPortProvider prv = it.next();
            if (providerName.equals(prv.getClass().getName())) {
                return prv;
            }
        }
        throw new SerialPortException("SerialPortProvider " + providerName + " not found");
    }

    // FIXME -SPI- REMOVE.ME
    private void printProviders(ServiceLoader<SerialPortProvider> ldr) {
        Iterator<SerialPortProvider> it2 = ldr.iterator();
        logger.info("****#### SerialPortManager *** Listing SerialPortProviders...");
        while (it2.hasNext()) {
            SerialPortProvider prv2 = it2.next();
            logger.info("****#### SerialPortManager *** FOUND SerialPortProvider: " + prv2);
        }
        logger.info("****#### SerialPortManager *** ...FINISHED listing!");
        ldr.reload();
    }
    // END-REMOVE.ME

    /**
     * Returns a serial port given a port name.
     *
     * @param name the name
     * @return a serial port identifier or null if the port is not available
     */
    public @Nullable SerialPort getSerialPort(final String name) {
        return provider.getSerialPort(name);
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
        return provider.getSerialPorts();
    }

}