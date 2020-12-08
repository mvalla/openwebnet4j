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
 * WHERE for Thermoregulation frames
 *
 * @author M. Valla - Initial contribution
 */
public class WhereThermo extends Where {

    public static final Where GENERAL = new WhereThermo("0#0");

    public WhereThermo(String w) throws NullPointerException {
        // TODO check range for WHERE
        super(w);
    }
}
