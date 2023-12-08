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

import java.net.URI;
import java.util.Arrays;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openwebnet4j.communication.serial.SerialPortIdentifier;
import org.openwebnet4j.communication.serial.SerialPortProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortInvalidPortException;

/**
 *
 * @author M. Valla - Initial contribution
 */
// @ServiceProvider(value = SerialPortProvider.class)
@NonNullByDefault
public class JSerialCommPortProvider implements SerialPortProvider {

    private final Logger logger = LoggerFactory.getLogger(JSerialCommPortProvider.class);

    @Override
    public @Nullable SerialPortIdentifier getPortIdentifier(URI port) {
        String portPathAsString = port.getPath();
        try {
            SerialPort spFound = SerialPort.getCommPort(portPathAsString);
            return new JSerialCommSerialPortIdentifier(spFound);
        } catch (SerialPortInvalidPortException e) {
            logger.debug("No SerialPortr found for: {}", portPathAsString, e);
            return null;
        }
    }

    @Override
    public Stream<SerialPortIdentifier> getSerialPortIdentifiers() {
        SerialPort[] portsArray = SerialPort.getCommPorts();
        System.out.println("Ports: " + portsArray);

        Stream<SerialPort> ports = Arrays.stream(portsArray);

        return ports.map(sid -> new JSerialCommSerialPortIdentifier(sid));
    }

}