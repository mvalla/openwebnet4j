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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for a serial port event.
 *
 * @author M. Valla - Initial contribution
 */
@NonNullByDefault
public interface SerialPortEvent {
    final int EVENT_DATA_AVAILABLE = 1;
    final int EVENT_PORT_DISCONNECTED = 268435456;

    /**
     * Get the type of the event.
     *
     * @return the event type
     */
    int getEventType();

}
