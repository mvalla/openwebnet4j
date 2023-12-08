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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception that marks that a port is currently already in use.
 *
 * @author M. Valla - Initial contribution, inspired by OH Serial Transport
 */
@NonNullByDefault
public class PortInUseException extends Exception {

    private static final long serialVersionUID = -2345480420743139383L;

    public PortInUseException(String message, Exception cause) {
        super(message, cause);
    }

    public PortInUseException(Exception cause) {
        super(cause);
    }

    public PortInUseException(String message) {
        super(message);
    }
}
