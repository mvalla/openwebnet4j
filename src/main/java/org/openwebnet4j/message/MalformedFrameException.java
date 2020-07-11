/**
 * Copyright (c) 2020 Contributors to the openwebnet4j project
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
package org.openwebnet4j.message;

/**
 * MalformedFrameException class is used when a OpenWebNet frame is malformed (not valid according to the specs).
 *
 * @author M. Valla - Initial contribution
 *
 */
public class MalformedFrameException extends FrameException {
    private static final long serialVersionUID = 787536576655854L;

    /**
     * Constructs a new <code>MalformedFrameException</code> without a detail message.
     */
    public MalformedFrameException() {
        super();
    }

    /**
     * Constructs a new <code>MalformedFrameException</code> with the specified detail message.
     *
     * @param s the detail message
     */
    public MalformedFrameException(final String s) {
        super(s);
    }

    /**
     * Constructs a new <code>MalformedFrameException</code> with the specified detail message and cause.
     *
     * @param s the detail message
     * @param cause the cause in form of a throwable object, can be <code>null</code>
     */
    public MalformedFrameException(final String s, final Throwable cause) {
        super(s, cause);
    }
}
