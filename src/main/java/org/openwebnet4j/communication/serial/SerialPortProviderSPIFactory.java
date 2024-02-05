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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openwebnet4j.communication.serial.rxtx.RxTxSerialPortProvider;
import org.openwebnet4j.communication.serial.spi.SerialPortProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to look for {@link SerialPortProvider} implementations via SPI.
 *
 * @author M. Valla - Initial contribution
 */
@NonNullByDefault
public class SerialPortProviderSPIFactory {

    private final Logger logger = LoggerFactory.getLogger(SerialPortProviderSPIFactory.class);

    /**
     * Return first available {@link SerialPortProvider} found via SPI, or default implementation
     * if no provider can be found via SPI.
     *
     * @return first {@link SerialPortProvider} implementation found via SPI, or default implementation
     */
    public SerialPortProvider getProvider() {
        ServiceLoader<SerialPortProvider> loader = ServiceLoader.load(SerialPortProvider.class);
        printProviders(loader);
        SerialPortProvider prv;
        Iterator<SerialPortProvider> it = loader.iterator();
        logger.debug(
                "****#### SerialPortProviderSPIFactory *** Getting first available SerialPortProvider via ServiceLoader...");
        while (it.hasNext()) {
            prv = it.next();
            logger.debug(
                    "****#### SerialPortProviderSPIFactory *** FOUND first SerialPortProvider via ServiceLoader: {}",
                    prv);
            return prv;
        }
        logger.debug("****#### SerialPortProviderSPIFactory *** NO SerialPortProvider found via ServiceLoader!");
        prv = new RxTxSerialPortProvider();
        logger.debug("****#### SerialPortProviderSPIFactory *** Returning DEFAULT SerialPortProvider: {}", prv);
        return prv;
    }

    /**
     * Return given {@link SerialPortProvider} implementation loaded via SPI, or null if it cannot be found.
     *
     * @param providerName class name (FQCL) of {@link SerialPortProvider} implementation to be loaded
     * @returns {@link SerialPortProvider} implementation found via SPI, or null
     */
    public @Nullable SerialPortProvider getProvider(String providerName) {
        ServiceLoader<SerialPortProvider> loader = ServiceLoader.load(SerialPortProvider.class);
        Iterator<SerialPortProvider> it = loader.iterator();
        while (it.hasNext()) {
            SerialPortProvider prv = it.next();
            if (providerName.equals(prv.getClass().getName())) {
                return prv;
            }
        }
        logger.warn("SerialPortProvider {} not found!", providerName);
        return null;
    }

    private void printProviders(ServiceLoader<SerialPortProvider> ldr) {
        Iterator<SerialPortProvider> it2 = ldr.iterator();
        logger.debug(
                "****#### SerialPortProviderSPIFactory *** Listing SerialPortProviders found via ServiceLoader...");
        while (it2.hasNext()) {
            SerialPortProvider prv2 = it2.next();
            logger.debug("****#### SerialPortProviderSPIFactory ***    FOUND: {}", prv2);
        }
        logger.debug("****#### SerialPortProviderSPIFactory *** ...FINISHED listing! Reloading ServiceLoader.");
        ldr.reload();
    }

}