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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception for serial port management
 *
 * @author M. Valla - Initial contribution
 */
@NonNullByDefault // FIXME remove
public class SerialPortException extends Exception {

    private static final long serialVersionUID = -2343335650743122283L;

    public SerialPortException(String message, Exception cause) {
        super(message, cause);
    }

    public SerialPortException(Exception cause) {
        super(cause);
    }

    public SerialPortException(String message) {
        super(message);
    }
}
