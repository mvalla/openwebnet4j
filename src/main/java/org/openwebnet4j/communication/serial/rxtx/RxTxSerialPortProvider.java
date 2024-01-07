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

import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openwebnet4j.communication.serial.spi.SerialPort;
import org.openwebnet4j.communication.serial.spi.SerialPortProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;

/**
 *
 * @author M. Valla - Initial contribution
 */
@NonNullByDefault
public class RxTxSerialPortProvider implements SerialPortProvider {

    private final Logger logger = LoggerFactory.getLogger(RxTxSerialPortProvider.class);

    @Override
    public @Nullable SerialPort getSerialPort(String portName) {
        try {
            CommPortIdentifier cpidFound = CommPortIdentifier.getPortIdentifier(portName);
            return new RxTxSerialPort(cpidFound);
        } catch (NoSuchPortException e) {
            logger.debug("No SerialPort {} found: {}", portName, e);
            return null;
        }
    }

    @Override
    public Stream<SerialPort> getSerialPorts() {
        Enumeration<CommPortIdentifier> identifiers = CommPortIdentifier.getPortIdentifiers();
        Stream<CommPortIdentifier> ids = Collections.list(identifiers).stream();
        return ids.filter(id -> id.getPortType() == CommPortIdentifier.PORT_SERIAL).map(sid -> new RxTxSerialPort(sid));
    }

}