/**
 * Copyright (c) 2021 Contributors to the openwebnet4j project
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
package org.openwebnet4j.communication;

/**
 * Authorisation exception
 *
 * @author M. Valla - Initial contribution
 *
 */
public class OWNAuthException extends OWNException {

    private static final long serialVersionUID = 564111573600046L;

    /**
     * Constructs a new <code>OWNAuthException</code> without a detail message.
     */
    public OWNAuthException() {
    }

    /**
     * Constructs a new <code>OWNAuthException</code> with the specified detail message.
     *
     * @param s the detail message
     */
    public OWNAuthException(final String s) {
        super(s);
    }

    /**
     * Constructs a new <code>OWNAuthException</code> with the specified detail message and
     * cause.
     *
     * @param s the detail message
     * @param cause the cause in form of a throwable object, can be <code>null</code>
     */
    public OWNAuthException(final String s, final Throwable cause) {
        super(s, cause);
    }
}
