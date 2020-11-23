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
 * BaseOpenMessage class is the abstract base class for other OpenWebNet message types.
 *
 * @author M. Valla - Initial contribution
 */

import java.util.Arrays;

import org.openwebnet4j.OpenDeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseOpenMessage extends OpenMessage {

    private final Logger logger = LoggerFactory.getLogger(BaseOpenMessage.class);

    protected static final String FORMAT_DIMENSION = "*#%d*%s*%d##";
    protected static final String FORMAT_SETTING = "*#%d*%s*#%d*%s*%s##";
    protected static final String FORMAT_REQUEST = "*%d*%d*%s##";
    protected static final String FORMAT_STATUS = "*#%d*%s##";

    protected String whoStr; // WHO part of the frame
    protected String whatStr; // WHAT part of the frame
    protected String whereStr; // WHERE part of the frame
    protected String dimStr; // DIM part of the frame

    protected Who who = null;
    protected What what = null;
    protected Where where = null;
    protected Dim dim = null;

    protected boolean commandTranslation = false;
    private Boolean isCommand = null;

    protected boolean isDimWriting = false; // true if dim writing *#WHO*WHERE*#DIM...##
    protected int[] dimParams = null; // list of dimension params PAR1...PARn in the frame
    // *#WHO*WHERE*DIM#PAR1...#PARn*...##
    protected String[] dimValues = null; // list of dimension values VAL1...VALn in the frame
                                         // *#WHO*WHERE*DIM...*VAL1*...*VALn##

    protected int[] commandParams = null; // list of command parameters PAR1...PARn in the frame
    // *WHO*WHAT#PAR1...#PARn*WHERE##

    protected BaseOpenMessage(String frame) {
        this.frameValue = frame;
    }

    public static OpenMessage parse(String frame) throws FrameException {
        boolean isCmd = true;
        if (frame == null) {
            throw new FrameException("Frame is null");
        }
        if (OpenMessage.FRAME_ACK.equals(frame) || OpenMessage.FRAME_NACK.equals(frame)
                || OpenMessage.FRAME_BUSY_NACK.equals(frame)) {
            return new AckOpenMessage(frame);
        }
        if (!frame.endsWith(OpenMessage.FRAME_END)) {
            throw new MalformedFrameException("Frame does not end with frame terminator " + OpenMessage.FRAME_END);
        }
        if (frame.startsWith(OpenMessage.FRAME_START_DIM)) {
            isCmd = false;
        } else if (!frame.startsWith(FRAME_START)) {
            throw new MalformedFrameException("Frame does not start with '*' or '*#'");
        }
        // remove trailing "##" and get frame sections separated by '*'
        String[] parts = frame.substring(0, frame.length() - 2).split("\\*");
        if (parts.length == 0) {
            throw new MalformedFrameException("Invalid frame");
        }
        if (parts.length < 3) { // every OWN frame must have at least 2 '*'
            throw new MalformedFrameException("Every frame must have at least 2 '*'");
        }
        // parts[0] is empty, first is WHO
        String whoStr = parts[1];
        if (!isCmd) {
            whoStr = parts[1].substring(1); // remove '#' from WHO part
        }
        BaseOpenMessage baseMsg = parseWho(whoStr, frame);
        if (isCmd) {
            baseMsg.whatStr = parts[2]; // second part is WHAT
            if (parts.length > 3) {
                baseMsg.whereStr = parts[3]; // third part is WHERE (optional)
            }
        } else {
            if (!(parts[2].equals(""))) {
                baseMsg.whereStr = parts[2]; // second part is WHERE
            }
            if (parts.length >= 4) {
                baseMsg.dimStr = parts[3]; // third part is DIM
                // copy last parts of this frame as DIM values
                baseMsg.dimValues = Arrays.copyOfRange(parts, 4, parts.length);
            }
        }
        baseMsg.isCommand = isCmd;
        // NOTE: we use here a LAZY APPROACH: we do not parse other parts (WHERE, WHAT, DIM, etc.) until requested: here
        // we just return the identified BaseOpenMessage subclass
        return baseMsg;
    }

    @Override
    public boolean isCommand() {
        if (isCommand == null) {
            isCommand = super.isCommand();
        }
        return isCommand;
    }

    /**
     * Returns message WHO
     *
     * @return message WHO
     */
    public Who getWho() {
        return who;
    }

    /**
     * Returns message WHAT or null if message has no valid WHAT part
     *
     * @return message WHAT
     */
    public What getWhat() {
        if (what == null && whatStr != null) {
            // try to parse WHAT from frame
            try {
                parseWhat();
            } catch (FrameException e) {
                logger.warn("Exception parsing WHAT of frame {}: {}", frameValue, e.getMessage());
            }
        }
        return what;
    }

    /**
     * Returns message WHERE or null if message has no valid WHERE part
     *
     * @return message WHERE
     */
    public Where getWhere() {
        if (where == null && whereStr != null) {
            try { // try to parse WHERE from frame
                parseWhere();
            } catch (FrameException e) {
                logger.warn("Exception parsing WHERE of frame {}: {}", frameValue, e.getMessage());
            }
        }
        return where;
    }

    /**
     * Returns message DIM (dimension, *#WHO*#DIM*...##) or null id not DIM is present
     *
     * @return message DIM, or null if no DIM is present
     */
    public Dim getDim() {
        if (dim == null) {
            // try to parse DIM from frame
            try {
                parseDim();
            } catch (FrameException e) {
                logger.warn("Exception parsing DIM of frame {}: {}", frameValue, e.getMessage());
            }
        }
        return dim;
    }

    /**
     * Check if message is a dimension writing message (*#WHO*#DIM*...##)
     *
     * @return true if it's a dimension writing message
     */
    public boolean isDimWriting() {
        return isDimWriting;
    }

    /**
     * Parse WHO from frame whoPart sub-string and returns a BaseOpenMessage of the corresponding type
     *
     * @param whoPart String containing the WHO
     */
    protected static BaseOpenMessage parseWho(String whoPart, String frame) throws FrameException {
        Who who = null;
        try {
            int whoInt = Integer.parseInt(whoPart);
            if (Who.isValidValue(whoInt)) {
                who = Who.fromValue(whoInt);
            } else {
                throw new MalformedFrameException("WHO not recognized: " + whoPart);
            }
        } catch (NumberFormatException nf) {
            throw new MalformedFrameException("WHO not recognized: " + whoPart);
        }

        BaseOpenMessage baseopenmsg = null;
        switch (who) {
            case GATEWAY_MANAGEMENT:
                baseopenmsg = new GatewayMgmt(frame);
                break;
            case LIGHTING:
                baseopenmsg = new Lighting(frame);
                break;
            case AUTOMATION:
                baseopenmsg = new Automation(frame);
                break;
            case THERMOREGULATION:
                baseopenmsg = new Thermoregulation(frame);
                break;
            default:
                break;
        }
        if (baseopenmsg != null) {
            baseopenmsg.who = who;
            return baseopenmsg;
        } else {
            throw new UnsupportedFrameException("WHO not recognized/supported: " + who);
        }
    }

    /**
     * Parse WHAT and its parameters and assigns it to {@link what} and {@link commandParams} obj attributes
     *
     */
    protected void parseWhat() throws FrameException {
        if (whatStr == null) {
            throw new FrameException("Frame has null WHAT");
        }
        String[] parts = whatStr.split("\\#");
        if (parts != null) {
            int partsIndex = 0;
            try {
                if ((Integer.parseInt(parts[partsIndex]) == What.WHAT_COMMAND_TRANSLATION) && parts.length > 1) {
                    // commandTranslation: 1000#WHAT
                    commandTranslation = true;
                    partsIndex++; // skip first 1000 value
                }
                what = whatFromValue(Integer.parseInt(parts[partsIndex]));
                if (what == null) {
                    throw new UnsupportedFrameException("Unsupported WHAT: " + whatStr);
                }
                if (parts.length > 1) { // copy command parameters into commandParams
                    commandParams = new int[parts.length - partsIndex - 1];
                    for (int i = 0; i < commandParams.length; i++) {
                        commandParams[i] = Integer.parseInt(parts[i + partsIndex + 1]);
                    }
                }
            } catch (NumberFormatException e) {
                throw new MalformedFrameException("Invalid integer format in WHAT: " + whatStr);
            }
        }
    }

    /**
     * Parse WHERE and assigns it to {@link where} attribute
     *
     */
    protected abstract void parseWhere() throws FrameException;

    /**
     * Parse DIM, its params and values and assigns it to {@link dim}, {@link dimParams} and {@link dimValues}
     * attributes
     *
     */
    protected void parseDim() throws FrameException {
        if (dimStr == null) {
            throw new MalformedFrameException("Frame has no DIM");
        }
        String ds = dimStr;
        if (ds.startsWith("#")) { // Dim writing
            isDimWriting = true;
            ds = ds.substring(1);
        }
        String[] dimParts = ds.split("#");
        try {
            dim = dimFromValue(Integer.parseInt(dimParts[0]));
            if (dim == null) {
                throw new UnsupportedFrameException("Unsupported DIM: " + dimStr);
            }
            // copy last parts of dimStr as dim params
            setDimParams(Arrays.copyOfRange(dimParts, 1, dimParts.length));
        } catch (NumberFormatException nfe) {
            throw new MalformedFrameException("Invalid DIM in frame: " + dimStr);
        }
    }

    protected abstract Dim dimFromValue(int i);

    protected abstract What whatFromValue(int i);

    public abstract OpenDeviceType detectDeviceType() throws FrameException;

    /**
     * Check if message is a command translation (*WHO*1000#WHAT*...##)
     *
     * @return true if the WHAT part is prefixed with command translation: 1000#
     * @throws FrameException
     */
    public boolean isCommandTranslation() throws FrameException {
        if (isCommand && what == null) {
            parseWhat();
        }
        return commandTranslation;
    }

    /**
     * Returns message command parameters (*WHO*WHAT#Par1#Par2...#ParN*...), or null if no parameters are present
     *
     * @return int[] of command parameters, or null if no parameters
     * @throws FrameException
     */
    public int[] getCommandParams() throws FrameException {
        if (what == null) {
            parseWhat();
        }
        return commandParams;
    }

    /**
     * Returns an array with DIM parameters PAR1..PARN (*#WHO*DIM#PAR1..#PARN*...##)
     *
     * @return a int[] of DIM parameters
     * @throws FrameException
     */
    public int[] getDimParams() throws FrameException {
        if (dim == null) {
            parseDim();
        }
        return dimParams;
    }

    /**
     * Set Dim params to given array
     *
     * @param values the String[] of Dim params
     */
    protected void setDimParams(String[] params) throws NumberFormatException {
        int[] tempArr = Arrays.stream(params).mapToInt(Integer::parseInt).toArray();
        if (tempArr.length > 0) {
            dimParams = tempArr;
        }
    }

    /**
     * Returns and array with DIM values
     *
     * @return a String[] of DIM values
     * @throws FrameException
     */
    public String[] getDimValues() throws FrameException {
        if (dim == null) {
            parseDim();
        }
        return dimValues;
    }

    /**
     * Helper method to add to the given msg frame a list of values separated by '*' at the end of the frame:
     * *frame## --> *frame*val1*val2*..*valN##
     *
     * @param msgStr the input frame String
     * @param vals Strings containing values to be added to the frame
     * @return a String with the new msg frame with values added at the end
     */
    protected static String addValues(String msgStr, String... vals) {
        String str = msgStr.substring(0, msgStr.length() - 2);
        for (int i = 0; i < vals.length; ++i) {
            str = str + "*" + vals[i];
        }
        str += FRAME_END;
        return str;
    }

    @Override
    public String toStringVerbose() {
        String verbose = "<" + frameValue + ">{WHO=" + getWho();
        try {
            if (isCommand()) {
                verbose += ",WHAT=" + getWhat();
                if (commandTranslation) {
                    verbose += "(translation)";
                }
                if (getCommandParams() != null) {
                    verbose += ",cmdParams=" + Arrays.toString(getCommandParams());
                }
            } else {
                verbose += ",dim=" + getDim();
                if (isDimWriting()) {
                    verbose += "(writing)";
                }
                if (getWhere() != null) {
                    verbose += ",WHERE=" + getWhere().value();
                }
                if (getDimParams() != null) {
                    verbose += ",dimParams=" + Arrays.toString(getDimParams());
                }
                if (getDimValues() != null) {
                    verbose += ",dimValues=" + Arrays.toString(getDimValues());
                }
            }
        } catch (FrameException e) {
            logger.warn("error during toStringVerbose()", e);
            e.printStackTrace();
        }
        return verbose + "}";
    }
}
