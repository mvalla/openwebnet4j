/**
 * Copyright (c) 2020-2021 Contributors to the openwebnet4j project
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
 * FrameException class is used when a problem with a OpenWebNet frame occurred.
 *
 * @author M. Valla - Initial contribution
 */
public class FrameException extends Exception {

    private static final long serialVersionUID = 445764573655546L;

    /** Constructs a new <code>FrameException</code> without a detail message. */
    public FrameException() {
        super();
    }

    /**
     * Constructs a new <code>FrameException</code> with the specified detail message.
     *
     * @param s the detail message
     */
    public FrameException(final String s) {
        super(s);
    }

    /**
     * Constructs a new <code>FrameException</code> with the specified detail message and cause.
     *
     * @param s the detail message
     * @param cause the cause in form of a throwable object, can be <code>null</code>
     */
    public FrameException(final String s, final Throwable cause) {
        super(s, cause);
    }
}
