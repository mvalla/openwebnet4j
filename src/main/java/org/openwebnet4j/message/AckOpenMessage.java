/**
 * Copyright (c) 2020-2022 Contributors to the openwebnet4j project
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
 * OpenWebNet acknowledge messages: ACK, NACK, BUSY-NACK.
 *
 * @author M. Valla - Initial contribution
 */
public class AckOpenMessage extends OpenMessage {

    public static final AckOpenMessage ACK = new AckOpenMessage(FRAME_ACK);
    public static final AckOpenMessage NACK = new AckOpenMessage(FRAME_NACK);
    public static final AckOpenMessage BUSY_NACK = new AckOpenMessage(FRAME_BUSY_NACK);

    protected AckOpenMessage(String frame) {
        this.frameValue = frame;
    }

    @Override
    public String toStringVerbose() {
        return "<" + frameValue + ">";
    }

    @Override
    public boolean isCommand() {
        return false;
    }
}
