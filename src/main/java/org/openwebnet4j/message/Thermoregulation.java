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

import static java.lang.String.format;
import static org.openwebnet4j.message.Who.THERMOREGULATION;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.openwebnet4j.OpenDeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenWebNet Thermoregulation messages (WHO=4)
 *
 * @author M. Valla - Initial contribution
 * @author G. Cocchi - Contribution for new lib
 */
public class Thermoregulation extends BaseOpenMessage {

    private static final Logger logger = LoggerFactory.getLogger(Thermoregulation.class);

    public enum WHAT implements What {
        CONDITIONING(0),
        HEATING(1),
        GENERIC(3),
        // protection
        PROTECTION_HEATING(102), // antifreeze
        PROTECTION_CONDITIONING(202),
        PROTECTION_GENERIC(302),
        // off
        OFF_HEATING(103),
        OFF_CONDITIONING(203),
        OFF_GENERIC(303),
        // manual
        MANUAL_HEATING(110),
        MANUAL_CONDITIONING(210),
        MANUAL_GENERIC(310),
        // programming (zone is following the program of the central unit)
        PROGRAM_HEATING(111),
        PROGRAM_CONDITIONING(211),
        PROGRAM_GENERIC(311),
        // holiday (zone is following the holiday program set on the central unit)
        HOLIDAY_HEATING(115),
        HOLIDAY_CONDITIONING(215),
        HOLIDAY_GENERIC(315);

        private static Map<Integer, WHAT> mapping;

        private final int value;

        private WHAT(int value) {
            this.value = value;
        }

        private static void initMapping() {
            mapping = new HashMap<Integer, WHAT>();
            for (WHAT w : values()) {
                mapping.put(w.value, w);
            }
        }

        public static WHAT fromValue(int i) {
            if (mapping == null) {
                initMapping();
            }
            return mapping.get(i);
        }

        @Override
        public Integer value() {
            return value;
        }
    }

    public enum MODE {
        HEATING(1),
        CONDITIONING(2),
        GENERIC(3);

        private final Integer value;

        private MODE(Integer value) {
            this.value = value;
        }

        public static MODE fromValue(Integer i) {
            Optional<MODE> m =
                    Arrays.stream(values())
                            .filter(val -> i.intValue() == val.value.intValue())
                            .findFirst();
            return m.orElse(null);
        }

        public Integer value() {
            return value;
        }
    }

    public enum LOCAL_OFFSET {
        PLUS_3("03", "+3"),
        PLUS_2("02", "+2"),
        PLUS_1("01", "+1"),
        NORMAL("00", "NORMAL"),
        MINUS_1("11", "-1"),
        MINUS_2("12", "-2"),
        MINUS_3("13", "-3"),
        OFF("4", "OFF"),
        PROTECTION("5", "PROTECTION");

        private final String value;
        private final String label;

        private LOCAL_OFFSET(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public static LOCAL_OFFSET fromValue(String s) {
            Optional<LOCAL_OFFSET> offset =
                    Arrays.stream(values()).filter(val -> s.equals(val.value)).findFirst();
            return offset.orElse(null);
        }

        public String getLabel() {
            return label;
        }
    }

    public enum DIM implements Dim {
        TEMPERATURE(0),
        TEMP_TARGET(12),
        OFFSET(13),
        TEMP_SETPOINT(14),
        PROBE_TEMPERATURE(15),
        VALVES_STATUS(19),
        ACTUATOR_STATUS(20);

        private static Map<Integer, DIM> mapping;

        private final int value;

        private DIM(Integer value) {
            this.value = value;
        }

        private static void initMapping() {
            mapping = new HashMap<Integer, DIM>();
            for (DIM d : values()) {
                mapping.put(d.value, d);
            }
        }

        public static DIM fromValue(int i) {
            if (mapping == null) {
                initMapping();
            }
            return mapping.get(i);
        }

        @Override
        public Integer value() {
            return value;
        }
    }

    public enum ACTUATOR_STATUS {
        OFF(0),
        ON(1);

        private final Integer value;

        private ACTUATOR_STATUS(Integer value) {
            this.value = value;
        }

        public static ACTUATOR_STATUS fromValue(Integer i) {
            Optional<ACTUATOR_STATUS> a =
                    Arrays.stream(values())
                            .filter(val -> i.intValue() == val.value.intValue())
                            .findFirst();
            return a.orElse(null);
        }

        public Integer value() {
            return value;
        }
    }

    private static final int WHO = THERMOREGULATION.value();

    protected Thermoregulation(String value) {
        super(value);
    }

    @Override
    protected What whatFromValue(int i) {
        return WHAT.fromValue(i);
    }

    @Override
    protected Dim dimFromValue(int i) {
        return DIM.fromValue(i);
    }

    /**
     * OpenWebNet message to Manual setting of "N" zone to T temperature <code>*#4*where*#14*T*M##
     * </code>.
     *
     * @param where Zone between #1 and #99
     * @param newSetPointTemperature temperature T between 5.0° and 40.0° (with 0.5° step)
     * @param mode MODE
     * @return message
     * @throws MalformedFrameException in case of error in parameters
     */
    public static Thermoregulation requestWriteSetpointTemperature(
            String where, double newSetPointTemperature, Thermoregulation.MODE mode)
            throws MalformedFrameException {
        if (newSetPointTemperature < 5 || newSetPointTemperature > 40) {
            throw new MalformedFrameException(
                    "Set Point Temperature should be between 5° and 40° Celsius.");
        }
        // Round new Set Point Temperature to close 0.5° C value
        return new Thermoregulation(
                format(
                        FORMAT_DIMENSION_WRITING_2V,
                        WHO,
                        where,
                        DIM.TEMP_SETPOINT.value(),
                        encodeTemperature(Math.rint(newSetPointTemperature * 2) / 2),
                        mode.value()));
    }

    /**
     * OpenWebNet to set the Thermoregulation device mode.
     *
     * @param where Zone between #1 and #99
     * @param newMode the new MODE
     * @return message
     * @throws MalformedFrameException in case of error in parameters
     */
    public static Thermoregulation requestWriteSetMode(String where, Thermoregulation.WHAT newMode)
            throws MalformedFrameException {
        return new Thermoregulation(format(FORMAT_REQUEST, WHO, newMode.value(), where));
    }

    /**
     * OpenWebNet message request to turn off the thermostat <code>OFF</code> <code>*4*303*where##
     * </code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestTurnOff(String where) {
        return new Thermoregulation(format(FORMAT_REQUEST, WHO, WHAT.OFF_GENERIC.value, where));
    }

    /**
     * OpenWebNet message request temperature <code>*#4*where*0##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestTemperature(String where) {
        return new Thermoregulation(
                format(FORMAT_DIMENSION_REQUEST, WHO, where, DIM.TEMPERATURE.value()));
    }

    /**
     * OpenWebNet message request the current Thermostat Set Point temperature <code>*#4*where*14##
     * </code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestSetPointTemperature(String where) {
        return new Thermoregulation(
                format(FORMAT_DIMENSION_REQUEST, WHO, where, DIM.TEMP_SETPOINT.value()));
    }

    /**
     * OpenWebNet message N actuator status request <code>*#4*where*20##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestActuatorStatus(String where) {
        return new Thermoregulation(
                format(FORMAT_DIMENSION_REQUEST, WHO, where, DIM.ACTUATOR_STATUS.value()));
    }

    /**
     * OpenWebNet message N zone device status request <code>*#4*where##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestStatus(String where) {
        return new Thermoregulation(format(FORMAT_STATUS, WHO, where));
    }

    /**
     * OpenWebNet message N zone valves status request <code>*#4*where*19##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestValvesStatus(String where) {
        return new Thermoregulation(
                format(FORMAT_DIMENSION_REQUEST, WHO, where, DIM.VALVES_STATUS.value()));
    }

    @Override
    protected void parseWhere() throws FrameException {
        if (whereStr == null) {
            throw new FrameException("Frame has no WHERE part: " + whereStr);
        } else {
            // TODO the original where string of the message should not be modified here: instead
            // thermo id and actuator
            // id should be parsed in WhereThermo
            if (whereStr.indexOf("#") > 0) {
                // Correct Actuator Where value x#y to value x in case of Thermostat device without
                // Central Unit
                whereStr = whereStr.substring(0, whereStr.indexOf("#"));
            }
            where = new WhereThermo(whereStr);
        }
    }

    /**
     * Returns the actuator form the message WHERE part. WHERE=Z#N --&gt; returns N
     *
     * @return id (int 1-9) of the actuator
     */
    public int getActuator() {
        // TODO move this parsing to WhereThermo and here just return the actuator part of the where
        // object
        return Integer.parseInt(where.value().substring(where.value().lastIndexOf("#") + 1));
    }

    /**
     * Extracts status of actuator N from message
     *
     * @param n the actuator number
     * @return 0=OFF, 1=ON
     * @throws FrameException in case of frame error
     */
    // TODO return Enum instead of int
    public int getActuatorStatus(int n) throws FrameException {
        String[] values = getDimValues();
        return Integer.parseInt((values[0]));
    }

    /**
     * Extracts the Local Offset value
     *
     * @return localOffset
     * @throws FrameException in case of frame error
     */
    public LOCAL_OFFSET getLocalOffset() throws FrameException {
        String[] values = getDimValues();
        return LOCAL_OFFSET.fromValue(values[0]);
    }

    /*
     * Parse temperature from a Thermoregulation msg (dimensions: 0, 12, 14 or 15)
     *
     * @param msg Thermoregulation message
     *
     * @return parsed temperature in degrees Celsius
     *
     * @throws NumberFormatException in case of error in msg
     */
    public static Double parseTemperature(Thermoregulation msg)
            throws NumberFormatException, FrameException {
        String[] values = msg.getDimValues();
        // temp is in the first dim value for thermostats (dim=0,12,14), in the second in case of
        // probes (dim=15)
        // TODO check min,max values
        if (msg.getDim() == DIM.TEMPERATURE
                || msg.getDim() == DIM.TEMP_SETPOINT
                || msg.getDim() == DIM.TEMP_TARGET) {
            return decodeTemperature(values[0]);
        } else if (msg.getDim() == DIM.PROBE_TEMPERATURE) {
            return decodeTemperature(values[1]);
        } else {
            throw new NumberFormatException(
                    "Could not parse temperature from: " + msg.getFrameValue());
        }
    }

    /**
     * Convert temperature from BTicino format to number For example: 0235 --&gt; +23.5 (°C) and
     * 1048 --&gt; -4.8 (°C)
     *
     * @param _temperature the temperature as String
     * @return the temperature as Double
     */
    public static Double decodeTemperature(String _temperature) throws NumberFormatException {
        int tempInt;
        int sign = 1;
        String temperature = _temperature;
        if (temperature.charAt(0) == '#') { // remove leading '#' if present
            temperature = temperature.substring(1);
        }
        if (temperature.length() == 4) {
            if (temperature.charAt(0) == '1') {
                sign = -1;
            }
            tempInt = Integer.parseInt(temperature.substring(1)); // leave out first sign digit
        } else if (temperature.length() == 3) { // 025 -> 2.5°C
            tempInt = Integer.parseInt(temperature);
        } else {
            throw new NumberFormatException("Unrecognized temperature format: " + temperature);
        }
        Double t = sign * tempInt / 10.0;
        return (Math.round(t * 100.0)) / 100.0; // round it to 2 decimal digits
    }

    /**
     * Encodes temperature from float to BTicino format
     *
     * @param temp temperature
     * @return String
     */
    public static String encodeTemperature(double temp) {
        // +23.51 °C --> '0235'; -4.86 °C --> '1049'
        // checkRange(5, 40, Math.round(temp));
        char sign = (temp >= 0 ? '0' : '1');
        String digits = "";
        int absTemp = (int) Math.abs(Math.round(temp * 10));
        if (absTemp < 100) {
            digits += "0";
        }
        if (absTemp < 10) {
            digits += "0";
        }
        digits += absTemp;
        if (digits == "000") {
            sign = '0';
        }
        logger.debug("====TEMPERATURE {} --> : <{}>", temp, sign + digits);
        return sign + digits;
    }

    @Override
    public OpenDeviceType detectDeviceType() {
        Where w = getWhere();
        if (w == null) {
            return null;
        } else {
            // TODO
            // if (w.toString().startsWith("5")) {
            // return OpenDeviceType.SCS_TEMP_SENSOR;
            // } else
            if (w.toString().startsWith("0") || w.toString().startsWith("#0")) {
                // Central unit or "all probes", not supported for now
                return null;
            } else {
                return OpenDeviceType.SCS_THERMOSTAT;
            }
        }
    }
}
