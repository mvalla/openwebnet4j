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
 * Abstract OpenWebNet messages.
 *
 * @author M. Valla - Initial contribution
 */

public abstract class OpenMessage {

    public static final String FRAME_ACK = "*#*1##";
    public static final String FRAME_NACK = "*#*0##";
    public static final String FRAME_BUSY_NACK = "*#*6##";

    public static final String FRAME_ACK_NACK_BUSY_START = "*#*";
    public static final String FRAME_START = "*";
    public static final String FRAME_START_DIM = "*#";
    public static final String FRAME_END = "##";

    protected String frameValue;

    /**
     * Gets the raw frame value
     *
     * @return the raw frame value as String
     */
    public String getFrameValue() {
        return frameValue;
    }

    /**
     * Is this OpenMessage a command? (<b>*WHO..</b>).
     *
     * @return true if it's a command frame
     */
    public abstract boolean isCommand();

    /**
     * Is this OpenMessage an ACK? (<b>*#*1##</b>).
     *
     * @return true if it's an ACK
     */
    public boolean isACK() {
        return FRAME_ACK.equals(frameValue);
    }

    /**
     * Is this OpenMessage an NACK? (<b>*#*0##</b>).
     *
     * @return true if it's an NACK
     */
    public boolean isNACK() {
        return FRAME_NACK.equals(frameValue);
    }

    /**
     * Is this OpenMessage an BUSY_NACK? (<b>*#*6##</b>).
     *
     * @return true if it's an BUSY_NACK
     */
    public boolean isBUSY_NACK() {
        return FRAME_BUSY_NACK.equals(frameValue);
    }

    @Override
    public String toString() {
        return "<" + frameValue + ">";
    }

    /**
     * Get a verbose representation of this message.
     *
     * @return verbose string representation
     */
    public abstract String toStringVerbose();

    @Override
    public boolean equals(Object obj) {
        // If the object is compared with itself then return true
        if (obj == this) {
            return true;
        }
        /*
         * Check if obj is an instance of OpenMessage or not
         * "null instanceof [type]" also returns false
         */
        if (!(obj instanceof OpenMessage)) {
            return false;
        }

        // typecast obj to OpenMessage so that we can compare frame
        OpenMessage c = (OpenMessage) obj;

        // Compare data
        return c.frameValue.equals(frameValue);
    }
}
