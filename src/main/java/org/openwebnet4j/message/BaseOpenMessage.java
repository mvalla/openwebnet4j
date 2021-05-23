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

import java.util.Arrays;

import org.openwebnet4j.OpenDeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BaseOpenMessage class is the abstract base class for other OpenWebNet message types.
 *
 * @author M. Valla - Initial contribution
 */
public abstract class BaseOpenMessage extends OpenMessage {

    private final Logger logger = LoggerFactory.getLogger(BaseOpenMessage.class);

    protected static final int MAX_FRAME_LENGTH = 1024; // max OWN frame length

    // TODO change to factory methods? (formatDimRequest(...) )
    protected static final String FORMAT_DIMENSION_REQUEST = "*#%d*%s*%d##";
    protected static final String FORMAT_DIMENSION_WRITING_1V = "*#%d*%s*#%d*%s##";
    protected static final String FORMAT_DIMENSION_WRITING_2V = "*#%d*%s*#%d*%s*%s##";
    protected static final String FORMAT_DIMENSION_WRITING_1P_1V = "*#%d*%s*#%d#%s*%s##";
    protected static final String FORMAT_REQUEST = "*%d*%d*%s##";
    protected static final String FORMAT_STATUS = "*#%d*%s##";

    private String whoStr = null; // WHO part of the frame
    private String whatStr = null; // WHAT part of the frame
    protected String whereStr = null; // WHERE part of the frame
    private String dimStr = null; // DIM part of the frame

    protected Who who = null;
    private What what = null;
    protected Where where = null;
    private Dim dim = null;

    private Boolean isCommand = null;
    private Boolean isCommandTranslation = null;

    private Boolean isDimWriting = null; // true if dim writing *#WHO*WHERE*#DIM...##
    private int[] dimParams = null; // list of dimension params PAR1...PARn in the frame
    // *#WHO*WHERE*DIM#PAR1...#PARn*...##
    private String[] dimValues = null; // list of dimension values VAL1...VALn in the frame
    // *#WHO*WHERE*DIM...*VAL1*...*VALn##

    private int[] commandParams = null; // list of command parameters PAR1...PARn in the frame
    // *WHO*WHAT#PAR1...#PARn*WHERE##

    protected BaseOpenMessage(String frame) {
        this.frameValue = frame;
    }

    @Override
    public boolean isCommand() {
        if (isCommand == null) {
            isCommand = Boolean.valueOf(!(frameValue.startsWith(FRAME_START_DIM)));
        }
        return isCommand;
    }

    /**
     * Parses the frame and returns a new OpenMessage object. This parser uses a "lazy approach": other parts (WHERE,
     * WHAT, DIM, parameters, etc.) are not parsed until requested.
     *
     * @param frame the frame String to parse
     *
     * @return a new {@link OpenMessage} object representing the OpenWebNet frame
     *
     * @throws MalformedFrameException in case the provided frame String is not a valid OpenWebNet frame
     * @throws UnsupportedFrameException in case the provided frame String is not a supported OpenWebNet frame
     *
     */
    public static OpenMessage parse(String frame) throws MalformedFrameException, UnsupportedFrameException {
        boolean isCmd = true;
        if (frame == null) {
            throw new MalformedFrameException("Frame is null");
        }
        if (OpenMessage.FRAME_ACK.equals(frame) || OpenMessage.FRAME_NACK.equals(frame)
                || OpenMessage.FRAME_BUSY_NACK.equals(frame)) {
            return new AckOpenMessage(frame);
        }
        if (!frame.endsWith(OpenMessage.FRAME_END)) {
            throw new MalformedFrameException("Frame does not end with terminator " + OpenMessage.FRAME_END);
        }
        if (frame.startsWith(OpenMessage.FRAME_START_DIM)) {
            isCmd = false;
        } else if (!frame.startsWith(FRAME_START)) {
            throw new MalformedFrameException("Frame does not start with '*' or '*#'");
        }
        if (frame.length() > MAX_FRAME_LENGTH) {
            throw new MalformedFrameException("Frame length is > " + MAX_FRAME_LENGTH);
        }
        // check if there are bad characters
        for (char c : frame.toCharArray()) {
            if (!Character.isDigit(c)) {
                if (c != '#' && c != '*') {
                    throw new MalformedFrameException("Frame can only contain '#', '*' or digits [0-9]");
                }
            }
        }
        String[] parts = getPartsStrings(frame);
        // parts[0] is empty, first is WHO
        String whoStr = parts[1];
        if (!isCmd) {
            whoStr = parts[1].substring(1); // remove '#' from WHO part
        }
        BaseOpenMessage baseMsg = parseWho(whoStr, frame);
        baseMsg.isCommand = isCmd;
        baseMsg.parseParts(parts);
        return baseMsg;
    }

    private static String[] getPartsStrings(String frame) throws MalformedFrameException {
        // remove trailing "##" and get frame parts separated by '*'
        String[] parts = frame.substring(0, frame.length() - 2).split("\\*");
        if (parts.length == 0) {
            throw new MalformedFrameException("Invalid frame");
        }
        if (parts.length < 3) { // every OWN frame must have at least 2 '*'
            throw new MalformedFrameException(
                    "Cmd/Dim frames must have at least 2 non-empty sections separated by '*'");
        }
        return parts;
    }

    private void parseParts(String[] parts) {
        if (isCommand()) {
            whatStr = parts[2]; // second part is WHAT
            if (parts.length > 3) {
                whereStr = parts[3]; // third part is WHERE (optional)
            }
        } else {
            if (!(parts[2].equals(""))) {
                whereStr = parts[2]; // second part is WHERE
            }
            if (parts.length >= 4) {
                dimStr = parts[3]; // third part is DIM
                // copy last parts of this frame as DIM values
                dimValues = Arrays.copyOfRange(parts, 4, parts.length);
            }
        }
    }

    private void parseParts() throws MalformedFrameException {
        parseParts(getPartsStrings(frameValue));
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
        if (what == null) {
            try {
                if (whatStr == null) {
                    parseParts();
                }
                parseWhat();
            } catch (FrameException e) {
                logger.warn("{} for frame {}", e.getMessage(), frameValue);
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
        if (where == null) {
            try {
                if (whereStr == null) {
                    parseParts();
                }
                parseWhere();
            } catch (FrameException e) {
                logger.warn("{} for frame {}", e.getMessage(), frameValue);
            }
        }
        return where;
    }

    /**
     * Returns message DIM (dimension, <code>*#WHO*#DIM*...##</code>) or null id not DIM is present
     *
     * @return message DIM, or null if no DIM is present
     */
    public Dim getDim() {
        if (dim == null) {
            try {
                if (dimStr == null) {
                    parseParts();
                }
                parseDim();
            } catch (FrameException e) {
                logger.warn("{} - frame {}", e.getMessage(), frameValue);
            }
        }
        return dim;
    }

    /**
     * Check if message is a dimension writing message <code>*#WHO*#DIM*...##</code>
     *
     * @return true if it's a dimension writing message
     */
    public boolean isDimWriting() {
        if (isDimWriting == null) {
            getDim();
        }
        return isDimWriting;
    }

    /**
     * Parse WHO from given whoPart and returns a BaseOpenMessage of the corresponding type
     *
     * @param whoPart String containing the WHO
     * @param frame the frame string
     * @throws MalformedFrameException in case of error in frame
     */
    private static BaseOpenMessage parseWho(String whoPart, String frame)
            throws MalformedFrameException, UnsupportedFrameException {
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
            case ENERGY_MANAGEMENT:
                baseopenmsg = new EnergyManagement(frame);
                break;
            case THERMOREGULATION:
                baseopenmsg = new Thermoregulation(frame);
                break;
            // DIAGNOSTIC
            case ENERGY_MANAGEMENT_DIAGNOSTIC:
                baseopenmsg = new EnergyManagementDiagnostic(frame);
                break;
            case THERMOREGULATION_DIAGNOSTIC:
                baseopenmsg = new ThermoregulationDiagnostic(frame);
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
     * Parse WHAT and its parameters and assigns it to {@link what} and {@link commandParams} obj
     * attributes
     *
     * @throws FrameException in case of error in frame
     */
    private void parseWhat() throws FrameException {
        if (whatStr == null) {
            return;
        }
        String[] parts = whatStr.split("\\#");
        if (parts != null) {
            int partsIndex = 0;
            try {
                if ((Integer.parseInt(parts[partsIndex]) == What.WHAT_COMMAND_TRANSLATION) && parts.length > 1) {
                    // commandTranslation: 1000#WHAT
                    isCommandTranslation = true;
                    partsIndex++; // skip first 1000 value
                } else {
                    isCommandTranslation = false;
                }
                what = whatFromValue(Integer.parseInt(parts[partsIndex]));
                commandParams = new int[0];
                if (what == null) {
                    throw new UnsupportedFrameException("Unsupported WHAT=" + whatStr);
                }
                if (parts.length > 1) { // copy command parameters into commandParams
                    commandParams = new int[parts.length - partsIndex - 1];
                    for (int i = 0; i < commandParams.length; i++) {
                        commandParams[i] = Integer.parseInt(parts[i + partsIndex + 1]);
                    }
                }
            } catch (NumberFormatException e) {
                throw new MalformedFrameException("Invalid integer format in WHAT=" + whatStr);
            }
        }
    }

    /**
     * Parse WHERE and assigns it to {@link where} attribute
     *
     * @throws FrameException in case of error in frame
     */
    protected abstract void parseWhere() throws FrameException;

    /**
     * Parse DIM, its params and values and assigns it to {@link dim}, {@link dimParams} and {@link
     * dimValues} attributes
     */
    private void parseDim() throws FrameException {
        if (dimStr == null) {
            return;
        }
        String ds = dimStr;
        if (ds.startsWith("#")) { // Dim writing
            isDimWriting = true;
            ds = ds.substring(1);
        } else {
            isDimWriting = false;
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

    /**
     * Tries to return a {@link OpenDeviceType} based on frame value
     *
     * @return recognized device type or null if not device can be recognized
     * @throws FrameException in case of error in frame
     */
    public abstract OpenDeviceType detectDeviceType() throws FrameException;

    /**
     * Check if message is a command translation (*WHO*1000#WHAT*...##)
     *
     * @return true if the WHAT part is prefixed with command translation: 1000#
     * @throws FrameException in case of error in frame
     */
    public boolean isCommandTranslation() throws FrameException {
        if (isCommand()) {
            if (isCommandTranslation == null) {
                getWhat(); // by parsing what we get command translation
            }
        } else {
            isCommandTranslation = false;
        }
        if (isCommandTranslation != null) {
            return isCommandTranslation.booleanValue();
        } else {
            return false;
        }
    }

    /**
     * Returns message command parameters (*WHO*WHAT#Param1#Param2...#ParamN*...), or empty array if
     * no parameters are present
     *
     * @return int[] of command parameters, or empty array if no parameters are present
     * @throws FrameException in case of error in frame
     */
    public int[] getCommandParams() throws FrameException {
        if (commandParams == null) {
            getWhat();
        }
        return commandParams;
    }

    /**
     * Returns an array with DIM parameters PAR1..PARN (*#WHO*DIM#PAR1..#PARN*...##), or empty array
     * if no parameters are present
     *
     * @return a int[] of DIM parameters, or empty array if no parameters are present
     * @throws FrameException in case of error in frame
     */
    public int[] getDimParams() throws FrameException {
        if (dimParams == null) {
            getDim();
        }
        return dimParams;
    }

    /**
     * Set Dim params to given array
     *
     * @param params the String[] of Dim params
     */
    private void setDimParams(String[] params) throws NumberFormatException {
        int[] tempArr = Arrays.stream(params).mapToInt(Integer::parseInt).toArray();

        dimParams = tempArr;
    }

    /**
     * Returns and array with DIM values, or empty array if no values are present
     *
     * @return a String[] of DIM values, or empty array if no values are present
     * @throws FrameException in case of error in frame
     */
    public String[] getDimValues() throws FrameException {
        if (dimValues == null) {
            getDim();
        }
        return dimValues;
    }

    /**
     * Helper method to add to the given msg frame a list of values separated by <code>*</code> at
     * the end of the frame: <code>*frame##</code> --&gt; <code>*frame*val1*val2*..*valN##</code>
     *
     * @param msgStr the input frame String
     * @param vals Strings containing values to be added to the frame
     * @return a String with the new message frame with values added at the end
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
        String verbose = "<" + frameValue + ">{" + getWho();
        try {
            if (isCommand()) {
                verbose += "-" + getWhat();
                if (isCommandTranslation()) {
                    verbose += "(translation)";
                }
                if (getCommandParams().length > 0) {
                    verbose += ",cmdParams=" + Arrays.toString(getCommandParams());
                }
                if (getWhere() != null) {
                    verbose += "," + getWhere();
                }
            } else {
                verbose += "-" + getDim();
                if (isDimWriting()) {
                    verbose += " (writing)";
                }
                if (getWhere() != null) {
                    verbose += "," + getWhere();
                }
                if (getDimParams().length > 0) {
                    verbose += ",dimParams=" + Arrays.toString(getDimParams());
                }
                if (getDimValues().length > 0) {
                    verbose += ",dimValues=" + Arrays.toString(getDimValues());
                }
            }
        } catch (FrameException e) {
            logger.warn("Error during toStringVerbose()", e);
            e.printStackTrace();
        }
        return verbose + "}";
    }
}
