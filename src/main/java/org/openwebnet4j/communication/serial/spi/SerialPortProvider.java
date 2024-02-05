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

import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Defines an interface for providers to return available serial port connections.
 *
 * @author M. Valla - Initial contribution
 */
@NonNullByDefault
public interface SerialPortProvider {

    /**
     * Returns a {@link SerialPort} based on portName if it is available or null otherwise.
     *
     * @param portName The requested port name.
     * @return The {@link SerialPort} that was found corresponding to portName.
     */
    public @Nullable SerialPort getSerialPort(String portName);

    /**
     * Gets all the available {@link SerialPort}s found by this {@link SerialPortProvider}.
     *
     * @return The available ports
     */
    public Stream<SerialPort> getSerialPorts();

}
