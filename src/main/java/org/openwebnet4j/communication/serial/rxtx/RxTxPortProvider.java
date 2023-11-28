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

import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openwebnet4j.communication.serial.SerialPortIdentifier;
import org.openwebnet4j.communication.serial.SerialPortProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;

/**
 *
 * @author M. Valla - Initial contribution, inspired by OH Serial Transport
 */
@NonNullByDefault
public class RxTxPortProvider implements SerialPortProvider {

    private final Logger logger = LoggerFactory.getLogger(RxTxPortProvider.class);

    @Override
    public @Nullable SerialPortIdentifier getPortIdentifier(URI port) {
        String portPathAsString = port.getPath();
        try {
            CommPortIdentifier ident = CommPortIdentifier.getPortIdentifier(portPathAsString);
            return new RxTxSerialPortIdentifier(ident);
        } catch (NoSuchPortException e) {
            logger.debug("No SerialPortIdentifier found for: {}", portPathAsString, e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<SerialPortIdentifier> getSerialPortIdentifiers() {
        Enumeration<CommPortIdentifier> identifiers = CommPortIdentifier.getPortIdentifiers();
        System.out.println("Identifiers: " + identifiers);

        Stream<CommPortIdentifier> scanIds = Collections.list(identifiers).stream();

        return scanIds.filter(id -> id.getPortType() == CommPortIdentifier.PORT_SERIAL)
                .map(sid -> new RxTxSerialPortIdentifier(sid));
    }

}