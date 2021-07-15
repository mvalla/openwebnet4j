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
package org.openwebnet4j.message;

/**
 * OpenWebNet base class for CEN/CEN+
 *
 * @author M. Valla - Initial contribution
 */
public abstract class CEN extends BaseOpenMessage {

    public CEN(String value) {
        super(value);
    }

    public static CEN requestStatus(String where) {
        return null;
    }

    /**
     * Get button number from CEN/CEN+ message [0-31]
     *
     * @return button number or null
     * @throws FrameException in case of frame error
     */
    public abstract Integer getButtonNumber() throws FrameException;

}