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
 * @author A. Conte - Added fancoil speed support
 */
public class Thermoregulation extends BaseOpenMessage {

    private static final Logger logger = LoggerFactory.getLogger(Thermoregulation.class);

    public enum WHAT implements What {
        CONDITIONING(0),
        HEATING(1),
        GENERIC(3),
        // protection
        PROTECTION_HEATING(102, FUNCTION.HEATING, OPERATION_MODE.PROTECTION), // antifreeze
        PROTECTION_CONDITIONING(202, FUNCTION.COOLING, OPERATION_MODE.PROTECTION), // thermal-protection
        PROTECTION_GENERIC(302, FUNCTION.GENERIC, OPERATION_MODE.PROTECTION),
        // off
        OFF_HEATING(103, FUNCTION.HEATING, OPERATION_MODE.OFF),
        OFF_CONDITIONING(203, FUNCTION.COOLING, OPERATION_MODE.OFF),
        OFF_GENERIC(303, FUNCTION.GENERIC, OPERATION_MODE.OFF),
        // manual
        MANUAL_HEATING(110, FUNCTION.HEATING, OPERATION_MODE.MANUAL),
        MANUAL_CONDITIONING(210, FUNCTION.COOLING, OPERATION_MODE.MANUAL),
        MANUAL_GENERIC(310, FUNCTION.GENERIC, OPERATION_MODE.MANUAL);
        // // programming (zone is following the program of the central unit)
        // PROGRAM_HEATING(111),
        // PROGRAM_CONDITIONING(211),
        // PROGRAM_GENERIC(311),
        // // holiday (zone is following the holiday program set on the central unit)
        // HOLIDAY_HEATING(115),
        // HOLIDAY_CONDITIONING(215),
        // HOLIDAY_GENERIC(315);

        private static Map<Integer, WHAT> mapping;

        private final int value;
        private final FUNCTION function;
        private final OPERATION_MODE mode;

        private WHAT(int value) {
            this.value = value;

            this.function = FUNCTION.GENERIC;
            this.mode = OPERATION_MODE.MANUAL;
        }

        private WHAT(int value, FUNCTION function, OPERATION_MODE mode) {
            this.value = value;
            this.function = function;
            this.mode = mode;
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

        public FUNCTION function() {
            return function;
        }

        public OPERATION_MODE mode() {
            return mode;
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
            Optional<LOCAL_OFFSET> offset = Arrays.stream(values()).filter(val -> s.equals(val.value)).findFirst();
            return offset.orElse(null);
        }

        public String getLabel() {
            return label;
        }
    }

    public enum FAN_COIL_SPEED {
        AUTO(0),
        SPEED_1(1),
        SPEED_2(2),
        SPEED_3(3);
        // OFF(15); present in documentation but not handled on real bus

        private final Integer value;

        private FAN_COIL_SPEED(Integer value) {
            this.value = value;
        }

        public static FAN_COIL_SPEED fromValue(Integer i) {
            Optional<FAN_COIL_SPEED> fcs = Arrays.stream(values()).filter(val -> i.intValue() == val.value.intValue())
                    .findFirst();
            return fcs.orElse(null);
        }

        public Integer value() {
            return value;
        }
    }

    public enum OPERATION_MODE {
        MANUAL(1),
        PROTECTION(2),
        OFF(3);

        private final Integer value;

        private OPERATION_MODE(Integer value) {
            this.value = value;
        }

        public static OPERATION_MODE fromValue(Integer i) {
            Optional<OPERATION_MODE> m = Arrays.stream(values()).filter(val -> i.intValue() == val.value.intValue())
                    .findFirst();
            return m.orElse(null);
        }

        public Integer value() {
            return value;
        }
    }

    public enum FUNCTION {
        HEATING(1),
        COOLING(2),
        GENERIC(3);

        private final Integer value;

        private FUNCTION(Integer value) {
            this.value = value;
        }

        public static FUNCTION fromValue(Integer i) {
            Optional<FUNCTION> m = Arrays.stream(values()).filter(val -> i.intValue() == val.value.intValue())
                    .findFirst();
            return m.orElse(null);
        }

        public Integer value() {
            return value;
        }
    }

    public enum VALVE_OR_ACTUATOR_STATUS {
        OFF(0),
        ON(1),
        OPENED(2),
        CLOSED(3),
        STOP(4),
        OFF_FAN_COIL(5),
        ON_SPEED_1(6),
        ON_SPEED_2(7),
        ON_SPEED_3(8),
        ON_FAN_COIL(9),
        STANDBY_FAN_COIL(14); // undocumented

        private final Integer value;

        private VALVE_OR_ACTUATOR_STATUS(Integer value) {
            this.value = value;
        }

        public static VALVE_OR_ACTUATOR_STATUS fromValue(Integer i) {
            Optional<VALVE_OR_ACTUATOR_STATUS> fcs = Arrays.stream(values())
                    .filter(val -> i.intValue() == val.value.intValue()).findFirst();
            return fcs.orElse(null);
        }

        public Integer value() {
            return value;
        }
    }

    public enum DIM implements Dim {
        TEMPERATURE(0),
        FAN_COIL_SPEED(11),
        COMPLETE_PROBE_STATUS(12),
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
     * @param function HEATING/COOLING/GENERIC
     * @return message
     * @throws MalformedFrameException in case of error in parameters
     */
    public static Thermoregulation requestWriteSetpointTemperature(String where, double newSetPointTemperature,
            Thermoregulation.FUNCTION function) throws MalformedFrameException {
        if (newSetPointTemperature < 5 || newSetPointTemperature > 40) {
            throw new MalformedFrameException("Set Point Temperature should be between 5° and 40° Celsius.");
        }
        // Round new Set Point Temperature to close 0.5° C value
        return new Thermoregulation(format(FORMAT_DIMENSION_WRITING_2V, WHO, where, DIM.TEMP_SETPOINT.value(),
                encodeTemperature(Math.rint(newSetPointTemperature * 2) / 2), function.value()));
    }

    /**
     * OpenWebNet to set the Fan Coil Speed <code>*#4*where*#11*speed##</code>.
     *
     * @param where WHERE string
     * @param newFanCoilSpeed Speed of the Fan Coil
     * @return message
     */
    public static Thermoregulation requestWriteFanCoilSpeed(String where,
            Thermoregulation.FAN_COIL_SPEED newFanCoilSpeed) {
        return new Thermoregulation(
                format(FORMAT_DIMENSION_WRITING_1V, WHO, where, DIM.FAN_COIL_SPEED.value(), newFanCoilSpeed.value()));
    }

    /**
     * OpenWebNet message request the Fan Coil Speed <code>*#4*where*11##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestFanCoilSpeed(String where) {
        return new Thermoregulation(format(FORMAT_DIMENSION_REQUEST, WHO, where, DIM.FAN_COIL_SPEED.value()));
    }

    /**
     * OpenWebNet to set the funcion.
     *
     * @param where WHERE string
     * @param newFunction Function (HEATING, COOLING, GENERIC).
     *            HEATING <code> *4*102*where##</code>
     *            COOLING <code> *4*202*where##</code>
     *            GENERIC <code> *4*302*where##</code>
     * @return message
     */
    public static Thermoregulation requestWriteFunction(String where, Thermoregulation.FUNCTION newFunction) {

        switch (newFunction) {
            case HEATING:
                return new Thermoregulation(format(FORMAT_REQUEST, WHO, WHAT.PROTECTION_HEATING.value(), where));
            case COOLING:
                return new Thermoregulation(format(FORMAT_REQUEST, WHO, WHAT.PROTECTION_CONDITIONING.value(), where));

            // this is allow only with central unit
            case GENERIC:
                return new Thermoregulation(format(FORMAT_REQUEST, WHO, WHAT.PROTECTION_GENERIC.value(), where));
        }
        return null;
    }

    /**
     * OpenWebNet to set the operation mode.
     *
     * @param where WHERE string
     * @param newOperationMode Operation mode (MANUAL, PROTECTION, OFF).
     *            MANUAL <code>*#4*where*#14*T*M##</code> (requestWriteSetPointTemperature)
     *            PROTECTION <code>*4*302*where##</code> (generic protection)
     *            OFF <code>*4*303*where##</code> (generic OFF)
     * @param currentFunction current thermostat function (HEATING/COOLING/GENERIC)
     * @param setPointTemperature temperature T between 5.0° and 40.0° (with 0.5° step) to be set when switching to
     *            function=MANUAL
     * @return message
     */
    public static Thermoregulation requestWriteMode(String where, Thermoregulation.OPERATION_MODE newOperationMode,
            Thermoregulation.FUNCTION currentFunction, double setPointTemperature) {

        switch (newOperationMode) {
            case MANUAL:
                try {
                    return requestWriteSetpointTemperature(where, setPointTemperature, currentFunction);
                } catch (MalformedFrameException ex) {
                    return null;
                }
            case PROTECTION:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(FORMAT_REQUEST, WHO, WHAT.PROTECTION_HEATING.value(), where));
                    case COOLING:
                        return new Thermoregulation(
                                format(FORMAT_REQUEST, WHO, WHAT.PROTECTION_CONDITIONING.value(), where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(FORMAT_REQUEST, WHO, WHAT.PROTECTION_GENERIC.value(), where));
                }
            case OFF:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(format(FORMAT_REQUEST, WHO, WHAT.OFF_HEATING.value(), where));
                    case COOLING:
                        return new Thermoregulation(format(FORMAT_REQUEST, WHO, WHAT.OFF_CONDITIONING.value(), where));
                    case GENERIC:
                        return new Thermoregulation(format(FORMAT_REQUEST, WHO, WHAT.OFF_GENERIC.value(), where));
                }
        }
        return null;
    }

    /**
     * OpenWebNet message request set-point temperature with local offset and operation mode <code>
     * *#4*where*12##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestMode(String where) {
        return new Thermoregulation(format(FORMAT_DIMENSION_REQUEST, WHO, where, DIM.COMPLETE_PROBE_STATUS.value()));
    }

    /**
     * OpenWebNet message request valves status (conditioning (CV) and heating (HV)) <code>*#4*where*19##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestValveStatus(String where) {
        return new Thermoregulation(format(FORMAT_DIMENSION_REQUEST, WHO, where, DIM.VALVES_STATUS.value()));
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
        return new Thermoregulation(format(FORMAT_DIMENSION_REQUEST, WHO, where, DIM.TEMPERATURE.value()));
    }

    /**
     * OpenWebNet message request the current Thermostat Set Point temperature <code>*#4*where*14##
     * </code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestSetPointTemperature(String where) {
        return new Thermoregulation(format(FORMAT_DIMENSION_REQUEST, WHO, where, DIM.TEMP_SETPOINT.value()));
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
        return new Thermoregulation(format(FORMAT_DIMENSION_REQUEST, WHO, where, DIM.VALVES_STATUS.value()));
    }

    /**
     * OpenWebNet message N actuator status request <code>*#4*where*20##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestActuatorStatus(String where) {
        return new Thermoregulation(format(FORMAT_DIMENSION_REQUEST, WHO, where, DIM.ACTUATOR_STATUS.value()));
    }

    @Override
    protected void parseWhere() throws FrameException {
        if (whereStr == null) {
            throw new FrameException("Frame has no WHERE part: " + whereStr);
        } else {
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
        // return Integer.parseInt(where.value().substring(where.value().lastIndexOf("#") + 1));
        WhereThermo wt = (WhereThermo) where;
        return wt.getActuator();
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

    /**
     * Parse temperature from a Thermoregulation message (dimensions: 0, 12, 14 or 15)
     *
     * @param msg Thermoregulation message
     * @return parsed temperature in degrees Celsius
     * @throws NumberFormatException if the temperature cannot be parsed
     * @throws FrameException in case of error in message
     */
    public static Double parseTemperature(Thermoregulation msg) throws NumberFormatException, FrameException {
        String[] values = msg.getDimValues();
        // temp is in the first dim value for thermostats (dim=0,12,14), in the second in case of
        // probes (dim=15)
        // TODO check min,max values
        if (msg.getDim() == DIM.TEMPERATURE || msg.getDim() == DIM.TEMP_SETPOINT
                || msg.getDim() == DIM.COMPLETE_PROBE_STATUS) {
            return decodeTemperature(values[0]);
        } else if (msg.getDim() == DIM.PROBE_TEMPERATURE) {
            return decodeTemperature(values[1]);
        } else {
            throw new NumberFormatException("Could not parse temperature from: " + msg.getFrameValue());
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

    /**
     * Parse fan coil speed from Thermoregulation message (dimensions: 11)
     *
     * @param msg Thermoregulation message
     * @return parsed fan coil speed as {@link FAN_COIL_SPEED}
     * @throws NumberFormatException in case of invalid speed
     * @throws FrameException in case of error in message
     */
    public static FAN_COIL_SPEED parseFanCoilSpeed(Thermoregulation msg) throws NumberFormatException, FrameException {
        String[] values = msg.getDimValues();
        if (msg.getDim() == DIM.FAN_COIL_SPEED) {
            return FAN_COIL_SPEED.fromValue(Integer.parseInt(values[0]));
        } else {
            throw new NumberFormatException("Could not parse fancoil speed from: " + msg.getFrameValue());
        }
    }

    /**
     * Parse valve status (CV and HV) from Thermoregulation message (dimensions: 19)
     *
     * @param msg Thermoregulation message
     * @param what Look for COOLING (CV) or HEATING (HV) valve
     * @return parsed valve status as {@link VALVE_OR_ACTUATOR_STATUS}
     * @throws NumberFormatException in case of invalid status
     * @throws FrameException in case of error in message
     */
    public static VALVE_OR_ACTUATOR_STATUS parseValveStatus(Thermoregulation msg, WHAT what)
            throws NumberFormatException, FrameException {
        if (what != WHAT.CONDITIONING && what != WHAT.HEATING) {
            throw new FrameException("Only CONDITIONING and HEATING are allowed as what input parameter.");
        }

        String[] values = msg.getDimValues();
        logger.debug("====parseValveStatus {} --> : CV <{}> HV <{}>", msg.getFrameValue(), values[0], values[1]);

        if (msg.getDim() == DIM.VALVES_STATUS) {
            if (what == WHAT.CONDITIONING) {
                return VALVE_OR_ACTUATOR_STATUS.fromValue(Integer.parseInt(values[0]));
            }
            if (what == WHAT.HEATING) {
                return VALVE_OR_ACTUATOR_STATUS.fromValue(Integer.parseInt(values[1]));
            }

            return null;
        } else {
            throw new NumberFormatException("Could not parse valve status from: " + msg.getFrameValue());
        }
    }

    /**
     * Parse actuator status from Thermoregulation message (dimensions: 20)
     *
     * @param msg Thermoregulation message
     * @return parsed actuator status as {@link VALVE_OR_ACTUATOR_STATUS}
     * @throws NumberFormatException in case of invalid status
     * @throws FrameException in case of error in message
     */
    public static VALVE_OR_ACTUATOR_STATUS parseActuatorStatus(Thermoregulation msg)
            throws NumberFormatException, FrameException {
        String[] values = msg.getDimValues();
        logger.debug("====parseActuatorStatus {} --> : <{}>", msg.getFrameValue(), values[0]);

        if (msg.getDim() == DIM.ACTUATOR_STATUS) {
            return VALVE_OR_ACTUATOR_STATUS.fromValue(Integer.parseInt(values[0]));
        } else {
            throw new NumberFormatException("Could not parse actuator status from: " + msg.getFrameValue());
        }
    }

    /*
     * Parse mode from Thermoregulation msg (*4*what*where##)
     *
     * @param msg Thermoregulation message
     *
     * @return parsed mode as enumeration (MANUAL, PROTECTION, OFF)
     *
     * @throws FrameException
     */
    // public static OPERATION_MODE parseMode(Thermoregulation msg) throws FrameException {
    // if (msg.getWhat() == null)
    // throw new FrameException("Could not parse Mode from: " + msg.getFrameValue());
    // WHAT w = WHAT.fromValue(msg.getWhat().value());
    // switch (w) {
    // case CONDITIONING:
    // case HEATING:
    // case GENERIC:
    // return OPERATION_MODE.MANUAL;

    // case PROTECTION_HEATING:
    // case PROTECTION_CONDITIONING:
    // case PROTECTION_GENERIC:
    // return OPERATION_MODE.PROTECTION;

    // case OFF_HEATING:
    // case OFF_CONDITIONING:
    // case OFF_GENERIC:
    // return OPERATION_MODE.OFF;
    // }

    // throw new FrameException("Invalid Mode from: " + msg.getFrameValue());
    // }

    /*
     * Parse fuction from Thermoregulation msg (*4*what*where##)
     *
     * @param msg Thermoregulation message
     *
     * @return parsed mode as enumeration (COOLING, HEATING, GENERIC)
     *
     * @throws FrameException
     */
    // public static FUNCTION parseFunction(Thermoregulation msg) throws FrameException {

    // if (msg.getWhat() == null)
    // throw new FrameException("Could not parse Fuction from: " + msg.getFrameValue());

    // WHAT w = WHAT.fromValue(msg.getWhat().value());
    // switch (w) {
    // case CONDITIONING:
    // case PROTECTION_CONDITIONING:
    // case OFF_CONDITIONING:
    // return FUNCTION.COOLING;

    // case HEATING:
    // case PROTECTION_HEATING:
    // case OFF_HEATING:
    // return FUNCTION.HEATING;

    // case GENERIC:
    // case PROTECTION_GENERIC:
    // case OFF_GENERIC:
    // return FUNCTION.GENERIC;
    // }

    // throw new FrameException("Invalid Fuction from: " + msg.getFrameValue());
    // }

    @Override
    public OpenDeviceType detectDeviceType() {
        WhereThermo w = (WhereThermo) getWhere();
        if (w == null) {
            return null;
        } else {
            if (w.value().startsWith("0")) {
                // "all probes/zones", not supported for now
                return null;
            }
            if (w.isProbe()) {
                return OpenDeviceType.SCS_TEMP_SENSOR;
            } else if (w.isCentralUnit()) {
                return OpenDeviceType.SCS_THERMO_CENTRAL_UNIT;
            } else {
                return OpenDeviceType.SCS_THERMOSTAT;
            }
        }
    }
}
