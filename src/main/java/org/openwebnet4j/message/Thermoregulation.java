/**
 * Copyright (c) 2020-2024 Contributors to the openwebnet4j project
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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openwebnet4j.OpenDeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OpenWebNet Thermoregulation messages (WHO=4)
 *
 * @author M. Valla - Initial contribution. Added VACATION/HOLIDAY support; re-factoring of WhatThermo (as class) and
 *         OperationMode enum.
 * @author G. Cocchi - Contribution for new lib
 * @author A. Conte - Completed Thermoregulation support
 */
public class Thermoregulation extends BaseOpenMessage {

    private static final Logger logger = LoggerFactory.getLogger(Thermoregulation.class);

    private static final String MODE_WEEKLY_STR = "WEEKLY";
    private static final String MODE_SCENARIO_STR = "SCENARIO";
    public static final String MODE_VACATION_STR = "VACATION";

    // @formatter:off
    /*
     * WHAT for Thermoregulation frames
     *
     * == Thermo What Table:
     * (f=Function: 1=HEATING, 2=COOLING, 3=GENERIC)
     * 0 - CONDITIONING
     * 1 - HEATING
     * 3 - GENERIC
     * f02 - PROTECTION
     * f03 - OFF
     * f10 - MANUAL
     * f11 - PROGRAM (AUTO)
     * f15 - HOLIDAY
     * f3ddd - VACATION for ddd [000-999] days
     * 3000 - LAST ACTIVATED PROGRAM/SCENARIO
     * f1pp - WEEKLY PROGRAM pp [01-03]
     * f2ss - SCENARIO ss [01-16]
     * ......
     * TODO to be checked / completed!!!!!!!!!!!!!!
     * ......
     * 20 - Remote control disabled (central unit)
     * 21 - Remote control enabled (central unit)
     * 22 - At least one probe OFF (central unit)
     * 23 - At least one probe in Anti Freeze (central unit)
     * 24 - At least one probe in Manual (central unit)
     * 30 - Failure discovered (central unit)
     * 31 - Central Unit battery KO
     * 40 - Release of sensor local adjustment
     */
    // @formatter:on

    public enum WhatThermoType {
        CONDITIONING(0),

        HEATING(1),
        GENERIC(3),

        // for central unit only
        REMOTE_CONTROL_DISABLED(20),
        REMOTE_CONTROL_ENABLED(21),
        AT_LEAST_ONE_PROBE_OFF(22),
        AT_LEAST_ONE_PROBE_ANTIFREEZE(23),
        AT_LEAST_ONE_PROBE_MANUAL(24),
        FAILURE_DISCOVERED(30),
        BATTERY_KO(31),
        RELEASE_SENSOR_LOCAL_ADJUST(40),

        VACATION_DEACTIVATION(3000),

        // these values do not exist in the WHAT table (Thermoregulation docs pag. 5), are defined here to be selected
        // from OperationMode in the WhatThermo constructor
        PROTECTION(9002),
        OFF(9003),
        MANUAL(9010),
        AUTO(9011),
        HOLIDAY(9015),

        WEEKLY(9100),
        SCENARIO(9200),
        VACATION(12000);

        private int value;

        private @Nullable static Map<Integer, WhatThermoType> mapping;

        private WhatThermoType(int value) {
            this.value = value;
        }

        private static void initMapping() {
            mapping = new HashMap<Integer, WhatThermoType>();
            for (WhatThermoType t : values()) {
                mapping.put(t.value, t);
            }
        }

        public @Nullable static WhatThermoType fromValue(int i) {
            if (mapping == null) {
                initMapping();
            }
            Optional<WhatThermoType> wtt = Arrays.stream(values()).filter(val -> val.value == i).findFirst();
            return wtt.orElse(null);
        }
    }

    public class WhatThermo implements What {

        private final WhatThermoType type;
        private Function function;
        private OperationMode mode;
        private final int value;

        /**
         * Constructor for WhatThermo object with OperationMode and the Function calculated from a WHAT int
         *
         * @param value e.g. 3215
         */
        public WhatThermo(int value) {
            this.value = value;
            if (value <= 40) {
                // WHAT less than 40 (defined in WhatThermoType enum) represent states (e.g.: Battery KO (31)).
                this.type = WhatThermoType.fromValue(value);

                // for WHAT=0 and WHAT=1 update Function field accordingly
                if (this.type == WhatThermoType.HEATING) {
                    this.function = Function.HEATING;
                }
                if (this.type == WhatThermoType.CONDITIONING) {
                    this.function = Function.COOLING;
                }
            } else if (value == 3000) {
                this.type = WhatThermoType.VACATION_DEACTIVATION;
                this.function = Function.GENERIC;
            } else {
                // WHAT like 105, 3215... represent a combination of function and mode: first digit is Function,
                // remaining digits are OperationMode
                int num = value, divisor = 1;
                while (num >= 10) { // divide by 10 until num is equal to first digit
                    num /= 10;
                    divisor *= 10;
                }
                this.function = Function.fromValue(num);
                this.mode = OperationMode.fromValue(value - num * divisor);
                this.type = WhatThermoType.fromValue(9000 + mode.value);
            }

        }

        @Deprecated
        public void setModeAndFuntion(OperationMode newMode, Function newFunction) {
            this.mode = newMode;
            this.function = newFunction;
        }

        /*
         * @Deprecated
         * public void setValue(int i) {
         * this.value = i;
         * }
         */

        /**
         * Return the {@link WhatThermoType} for this WHAT
         *
         * @return the {@link WhatThermoType}
         */
        public WhatThermoType getType() {
            return this.type;
        }

        /**
         * Return the Function for this WHAT
         *
         * @return the Function
         */
        public Function getFunction() {
            return this.function;
        }

        /**
         * Return the {@link OperationMode} for this WHAT
         *
         * @return the {@link OperationMode}
         */
        public OperationMode getMode() {
            return this.mode;
        }

        /**
         * Return a WHAT String composing {@link OperationMode} and {@link Function}
         *
         * @param mode (e.g. WEEKLY_2)
         * @param function (e.g. COOLING)
         * @return WHAT String (e.g. 2102)
         */
        @Deprecated
        String fromModeAndFunction(OperationMode mode, Function function) {
            String what = function.value().toString();

            if (mode != OperationMode.MANUAL) {
                what += mode.value();
            }

            return what;
        }

        /**
         * Check for complex WHAT string: WEEKLY, SCENARIO
         *
         * @param what String representing a WHAT mode
         * @return true if the WHAT String parameter is a complex WHAT
         */
        @Deprecated
        public Boolean isComplex(String what) {
            return what.equalsIgnoreCase(MODE_WEEKLY_STR) || what.equalsIgnoreCase(MODE_SCENARIO_STR);
        }

        /**
         * Return the Program Number associated to a WEEKLY or SCENARIO {@link OperationMode}
         *
         * @return Integer the program number (e.g. 2102 --> 2, 1216 --> 16)
         */
        public int programNumber() {
            return value % 100;
        }

        /**
         * Return vacation days associated to VACATION {@link OperationMode}
         */
        public int vacationDays() {
            return value % 1000;
        }

        @Override
        public Integer value() {
            return value;
        }

        @Override
        public String toString() {
            if (value <= 40 || value == 3000) {
                return type.name();
            } else {
                return function + "-" + mode;
            }
        }
    }

    /**
     * {@link OperationMode} enumeration
     */
    public enum OperationMode {
        /**
         * {@link OperationMode} PROTECTION
         */
        PROTECTION(2),
        /**
         * {@link OperationMode} OFF
         */
        OFF(3),
        /**
         * {@link OperationMode} MANUAL
         */
        MANUAL(10),
        /**
         * {@link OperationMode} AUTO
         */
        AUTO(11),
        /**
         * {@link OperationMode} HOLIDAY
         */
        HOLIDAY(15),
        /**
         * {@link OperationMode} WEEKLY
         */
        WEEKLY(100),
        /**
         * {@link OperationMode} SCENARIO
         */
        SCENARIO(200),
        /**
         * {@link OperationMode} VACATION
         */
        VACATION(3000);

        private int value;

        private OperationMode(int value) {
            this.value = value;
        }

        public static OperationMode fromValue(int value) {
            /// 2, 3, 10, 11, 15, 3xxx, 1xx, 2xx
            int val = value;
            if (value >= 3000) {
                val = 3000;
            } else if (value >= 100) {
                val = value - (value % 100);
            }
            final int vv = val;
            Optional<OperationMode> m = Arrays.stream(values()).filter(e -> vv == e.value).findFirst();
            return m.orElse(null);
        }

        /**
         * Returns the value for this {@link OperationMode}
         *
         * @return value
         */
        public int value() {
            return value;
        }

        /**
         * @deprecated --> use Enum.name() instead
         *             The mode string for this OperationMode object: MANUAL, OFF, WEEKLY, PROGRAM, HOLIDAY, PROTECTION,
         *             etc.
         */
        @Deprecated
        public String mode() {
            return this.name();
        }

        /**
         * @deprecated
         *             Return if current {@link OperationMode} is SCENARIO
         *
         * @return Boolean (e.g. 2102 = true, 103 = false)
         */
        @Deprecated
        public Boolean isScenario() {
            return mode() == MODE_SCENARIO_STR;
        }

        /**
         * @deprecated
         *             Return if current {@link OperationMode} is WEEKLY
         *
         * @return Boolean (e.g. 3101 = true, 110 = false)
         */
        @Deprecated
        public Boolean isWeekly() {
            return mode() == MODE_WEEKLY_STR;
        }

        /**
         * @deprecated
         *             Return if current {@link OperationMode} is VACATION
         *
         * @return Boolean
         */
        @Deprecated
        public Boolean isVacation() {
            return mode() == MODE_VACATION_STR;
        }

    }

    public enum Function {
        HEATING(1),
        COOLING(2),
        GENERIC(3);

        private final Integer value;

        private Function(Integer value) {
            this.value = value;
        }

        public static Function fromValue(Integer i) {
            Optional<Function> m = Arrays.stream(values()).filter(val -> i.intValue() == val.value.intValue())
                    .findFirst();
            return m.orElse(null);
        }

        public Integer value() {
            return value;
        }
    }

    public enum LocalOffset {
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

        private LocalOffset(String value, String label) {
            this.value = value;
            this.label = label;
        }

        public static LocalOffset fromValue(String s) {
            Optional<LocalOffset> offset = Arrays.stream(values()).filter(val -> s.equals(val.value)).findFirst();
            return offset.orElse(null);
        }

        public String getLabel() {
            return label;
        }
    }

    public enum FanCoilSpeed {
        AUTO(0),
        SPEED_1(1),
        SPEED_2(2),
        SPEED_3(3),
        OFF(15); // present in documentation but not handled on real bus

        private final Integer value;

        private FanCoilSpeed(Integer value) {
            this.value = value;
        }

        public static FanCoilSpeed fromValue(Integer i) {
            Optional<FanCoilSpeed> fcs = Arrays.stream(values()).filter(val -> i.intValue() == val.value.intValue())
                    .findFirst();
            return fcs.orElse(null);
        }

        public Integer value() {
            return value;
        }
    }

    public enum ValveOrActuatorStatus {
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
        OFF_SPEED_1(14),
        OFF_SPEED_2(15),
        OFF_SPEED_3(16);

        private final Integer value;

        private ValveOrActuatorStatus(Integer value) {
            this.value = value;
        }

        public static ValveOrActuatorStatus fromValue(Integer i) {
            Optional<ValveOrActuatorStatus> fcs = Arrays.stream(values())
                    .filter(val -> i.intValue() == val.value.intValue()).findFirst();
            return fcs.orElse(null);
        }

        public Integer value() {
            return value;
        }
    }

    public enum DimThermo implements Dim {
        TEMPERATURE(0),
        FAN_COIL_SPEED(11),
        COMPLETE_PROBE_STATUS(12),
        OFFSET(13),
        TEMP_SETPOINT(14),
        PROBE_TEMPERATURE(15),
        VALVES_STATUS(19),
        ACTUATOR_STATUS(20);

        private static Map<Integer, DimThermo> mapping;

        private final int value;

        private DimThermo(Integer value) {
            this.value = value;
        }

        private static void initMapping() {
            mapping = new HashMap<Integer, DimThermo>();
            for (DimThermo d : values()) {
                mapping.put(d.value, d);
            }
        }

        public static DimThermo fromValue(int i) {
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
        return new WhatThermo(i);
    }

    @Override
    protected Dim dimFromValue(int i) {
        return DimThermo.fromValue(i);
    }

    /**
     * OpenWebNet message to set set point temperature T <code>*#4*where*#14*T*M##</code>.
     *
     * @param where WHERE string
     * @param newSetPointTemperature temperature T between 5.0&deg;C and 40.0&deg;C (with 0.5&deg; step)
     * @param function HEATING/COOLING/GENERIC
     * @return message
     * @throws MalformedFrameException in case of error in parameters
     */
    public static Thermoregulation requestWriteSetpointTemperature(String where, double newSetPointTemperature,
            Thermoregulation.Function function) throws MalformedFrameException {
        if (newSetPointTemperature < 5 || newSetPointTemperature > 40) {
            throw new MalformedFrameException("Set Point Temperature should be between 5° and 40° Celsius.");
        }

        // Round new Set Point Temperature to close 0.5&deg;C value
        return new Thermoregulation(format(FORMAT_DIMENSION_WRITING_2V, WHO, where, DimThermo.TEMP_SETPOINT.value(),
                encodeTemperature(Math.rint(newSetPointTemperature * 2) / 2), function.value()));
    }

    /**
     * OpenWebNet message to set fan coil speed <code>*#4*where*#11*speed##</code>.
     *
     * @param where WHERE string
     * @param newFanCoilSpeed Speed of the fan coil
     * @return message
     */
    public static Thermoregulation requestWriteFanCoilSpeed(String where,
            Thermoregulation.FanCoilSpeed newFanCoilSpeed) {
        return new Thermoregulation(format(FORMAT_DIMENSION_WRITING_1V, WHO, where, DimThermo.FAN_COIL_SPEED.value(),
                newFanCoilSpeed.value()));
    }

    /**
     * OpenWebNet message to request the current fan coil speed <code>*#4*where*11##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestFanCoilSpeed(String where) {
        return new Thermoregulation(format(FORMAT_DIMENSION_REQUEST, WHO, where, DimThermo.FAN_COIL_SPEED.value()));
    }

    /**
     * OpenWebNet message to set the function (HEATING, COOLING, GENERIC):
     * <ul>
     * <li>HEATING <code>*4*102*where##</code></li>
     * <li>COOLING <code>*4*202*where##</code></li>
     * <li>GENERIC <code>*4*302*where##</code></li>
     * </ul>
     *
     * @param where WHERE string
     * @param newFunction {@link Function} (HEATING, COOLING, GENERIC)
     * @return message
     */
    public static Thermoregulation requestWriteFunction(String where, Thermoregulation.Function newFunction) {

        return new Thermoregulation(format(FORMAT_REQUEST_WHAT_STR, WHO,
                "" + (newFunction.value * 100 + OperationMode.PROTECTION.value), where));
    }

    /**
     * OpenWebNet message to change the {@link OperationMode} to MANUAL, PROTECTION, OFF, AUTO:
     * <ul>
     * <li>MANUAL -> <code>*#4*where*#14*T*M##</code> (requestWriteSetPointTemperature)</li>
     * <li>PROTECTION -> <code>*4*302*where##</code> (generic PROTECTION)</li>
     * <li>OFF -> <code>*4*303*where##</code> (generic OFF)</li>
     * <li>AUTO -> <code>*4*311*where##</code> (generic AUTO/PROGRAM)</li>
     * </ul>
     *
     * @param where WHERE string
     * @param newOperationMode {@link OperationMode}
     * @param currentFunction current zone {@link Function} (HEATING/COOLING/GENERIC)
     * @param setPointTemperature temperature T between 5.0&deg;C and 40.0&deg;C (with 0.5&deg; step) to be set
     *            when switching to mode=MANUAL
     * @return message
     */
    public static Thermoregulation requestWriteMode(String where, Thermoregulation.OperationMode newOperationMode,
            Thermoregulation.Function currentFunction, double setPointTemperature) {

        if (newOperationMode == OperationMode.MANUAL) {
            try {
                return requestWriteSetpointTemperature(where, setPointTemperature, currentFunction);
            } catch (MalformedFrameException ex) {
                return null;
            }
        } else {
            return new Thermoregulation(format(FORMAT_REQUEST_WHAT_STR, WHO,
                    "" + (currentFunction.value * 100 + newOperationMode.value), where));
        }
    }

    /**
     * OpenWebNet message to set Central Unit {@link OperationMode} to HOLIDAY mode until midnight. The specified weekly
     * program will be activated at the end of holiday. Example: <code>*4*115#3103*where##</code>.
     *
     * @param where WHERE string
     * @param currentFunction current CU {@link Function}
     * @param returnWeeklyProgram 1..3 weekly program the CU will return to at the end of holiday
     * @return message
     */
    public @NonNull static Thermoregulation requestWriteHolidayMode(String where,
            Thermoregulation.Function currentFunction, int returnWeeklyProgram) {
        return new Thermoregulation(format(FORMAT_REQUEST_PARAM_STR, WHO,
                "" + (currentFunction.value * 100 + OperationMode.HOLIDAY.value), 3100 + returnWeeklyProgram, where));
    }

    /**
     * OpenWebNet message to set Central Unit {@link OperationMode} to WEEKLY or SCENARIO with specific program/scenario
     * number. For example:
     * <ul>
     * <li>WEEKLY -> <code>*4*3102*where##</code> weekly program number 2</li>
     * <li>SCENARIO -> <code>*4*3213*where##</code> scenario number 13</li>
     * </ul>
     *
     * @param where WHERE string
     * @param newOperationMode {@link OperationMode} (WEEKLY or SCENARIO)
     * @param currentFunction current {@link Function} (HEATING/COOLING/GENERIC)
     * @param program weekly program 1..3 or scenario 1..16
     *
     * @return message
     */
    public static Thermoregulation requestWriteWeeklyScenarioMode(String where,
            Thermoregulation.OperationMode newOperationMode, Thermoregulation.Function currentFunction, int program) {
        return new Thermoregulation(format(FORMAT_REQUEST_WHAT_STR, WHO,
                ("" + currentFunction.value) + (newOperationMode.value + program), where));
    }

    /**
     * OpenWebNet message to set Central Unit {@link OperationMode} to VACATION mode for N days. The specified weekly
     * program will be activated at the end of vacation. Example: <code>*4*33002#3103*where##</code>.
     *
     * @param where WHERE string
     * @param currentFunction current CU {@link Function}
     * @param vacationDays number of vacation days (1..255)
     * @param returnWeeklyProgram 1..3 weekly program the CU will return to at the end of vacation
     * @return message
     */
    public @NonNull static Thermoregulation requestWriteVacationMode(String where,
            Thermoregulation.Function currentFunction, int vacationDays, int returnWeeklyProgram) {
        return new Thermoregulation(format(FORMAT_REQUEST_PARAM_STR, WHO,
                ("" + currentFunction.value) + (3000 + vacationDays), 3100 + returnWeeklyProgram, where));
    }

    /**
     * OpenWebNet message to request the set point temperature with operation mode <code>*#4*where*12##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestMode(String where) {
        return new Thermoregulation(
                format(FORMAT_DIMENSION_REQUEST, WHO, where, DimThermo.COMPLETE_PROBE_STATUS.value()));
    }

    /**
     * OpenWebNet message to request the local offset (knob) <code>*#4*where*13##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestLocalOffset(String where) {
        return new Thermoregulation(format(FORMAT_DIMENSION_REQUEST, WHO, where, DimThermo.OFFSET.value()));
    }

    /**
     * OpenWebNet message to request current valves status (conditioning (CV) and heating (HV))
     * <code>*#4*where*19##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestValvesStatus(String where) {
        return new Thermoregulation(format(FORMAT_DIMENSION_REQUEST, WHO, where, DimThermo.VALVES_STATUS.value()));
    }

    /**
     * OpenWebNet message to set the zone mode ({@link OperationMode}) <code>*4*newMode*where##</code>.
     *
     * @param where WHERE string
     * @param newMode the new {@link OperationMode}
     * @return message
     * @throws MalformedFrameException in case of error in parameters
     */
    public static Thermoregulation requestWriteSetMode(String where, Thermoregulation.WhatThermo newMode)
            throws MalformedFrameException {
        return new Thermoregulation(format(FORMAT_REQUEST, WHO, newMode.value(), where));
    }

    /**
     * OpenWebNet message to request current sensed temperature <code>*#4*where*0##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestTemperature(String where) {
        return new Thermoregulation(format(FORMAT_DIMENSION_REQUEST, WHO, where, DimThermo.TEMPERATURE.value()));
    }

    /**
     * OpenWebNet message to request the current set point temperature <code>*#4*where*14##
     * </code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestSetPointTemperature(String where) {
        return new Thermoregulation(format(FORMAT_DIMENSION_REQUEST, WHO, where, DimThermo.TEMP_SETPOINT.value()));
    }

    /**
     * OpenWebNet message to request the zone status <code>*#4*where##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestStatus(String where) {
        return new Thermoregulation(format(FORMAT_STATUS, WHO, where));
    }

    /**
     * OpenWebNet message to request actuators status <code>*#4*where*20##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestActuatorsStatus(String where) {
        return new Thermoregulation(format(FORMAT_DIMENSION_REQUEST, WHO, where, DimThermo.ACTUATOR_STATUS.value()));
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
     * Return the actuator from the message WHERE part. WHERE=Z#N --&gt; returns N
     *
     * @return id (int 1-9) of the actuator
     */
    public int getActuator() {
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
     * Extracts the LocalOffset value
     *
     * @return LocalOffset
     * @throws FrameException in case of frame error
     */
    public LocalOffset getLocalOffset() throws FrameException {
        String[] values = getDimValues();
        return LocalOffset.fromValue(values[0]);
    }

    /**
     * Parse temperature from a Thermoregulation message (dimensions: 0, 12, 14 or 15)
     *
     * @param msg Thermoregulation message
     * @return parsed temperature in degrees Celsius
     * @throws NumberFormatException if the temperature cannot be parsed
     * @throws FrameException in case of error in message
     */
    public static double parseTemperature(Thermoregulation msg) throws NumberFormatException, FrameException {
        String[] values = msg.getDimValues();
        // temp is in the first dim value for thermostats (dim=0,12,14), in the second in case of
        // probes (dim=15)
        // TODO check min,max values
        if (msg.getDim() == DimThermo.TEMPERATURE || msg.getDim() == DimThermo.TEMP_SETPOINT
                || msg.getDim() == DimThermo.COMPLETE_PROBE_STATUS) {
            return decodeTemperature(values[0]);
        } else if (msg.getDim() == DimThermo.PROBE_TEMPERATURE) {
            return decodeTemperature(values[1]);
        } else {
            throw new NumberFormatException("Could not parse temperature from: " + msg.getFrameValue());
        }
    }

    /**
     * Convert temperature from BTicino format to number For example: 0235 --&gt; +23.5 (&deg;C) and
     * 1048 --&gt; -4.8 (&deg;C)
     *
     * @param temperature the temperature as String
     * @return the temperature as double
     */
    public static double decodeTemperature(String temperature) throws NumberFormatException {
        int tempInt;
        int sign = 1;
        String t = temperature;
        if (t.charAt(0) == '#') { // remove leading '#' if present
            t = t.substring(1);
        }
        if (t.length() == 4) {
            if (t.charAt(0) == '1') {
                sign = -1;
            }
            tempInt = Integer.parseInt(t.substring(1)); // leave out first sign digit
        } else if (t.length() == 3) { // 025 -> 2.5°C
            tempInt = Integer.parseInt(t);
        } else {
            throw new NumberFormatException("Unrecognized temperature format: " + t);
        }
        double tempDouble = sign * tempInt / 10.0;
        return (Math.round(tempDouble * 100.0)) / 100.0; // round it to 2 decimal digits
    }

    /**
     * Encodes temperature from double to BTicino temperature format
     *
     * @param temp temperature
     * @return String encoded temperature
     */
    public static String encodeTemperature(double temp) {
        // +23.51 °C --> '0235'; -4.86 °C --> '1049'
        // TODO checkRange(5, 40, Math.round(temp)); ??
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
     * Parse fan coil speed from Thermoregulation message (dimension: 11)
     *
     * @param msg Thermoregulation message
     * @return parsed fan coil speed as {@link FanCoilSpeed}
     * @throws NumberFormatException in case of invalid speed
     * @throws FrameException in case of error in message
     */
    public static FanCoilSpeed parseFanCoilSpeed(Thermoregulation msg) throws NumberFormatException, FrameException {
        String[] values = msg.getDimValues();
        if (msg.getDim() == DimThermo.FAN_COIL_SPEED) {
            FanCoilSpeed result;
            result = FanCoilSpeed.fromValue(Integer.parseInt(values[0]));
            if (result != null) {
                return result;
            } else {
                throw new NumberFormatException("Unknwon/unsupported fan coil speed in frame: " + msg.getFrameValue());
            }
        }
        throw new FrameException("Could not parse fan coil speed in frame: " + msg.getFrameValue());
    }

    /**
     * Parse valve status (CV and HV) from Thermoregulation message (dimension: 19)
     *
     * @param msg Thermoregulation message
     * @param whatType Look for COOLING (CV) or HEATING (HV) valve
     * @return parsed valve status as {@link ValveOrActuatorStatus}
     * @throws NumberFormatException in case of invalid status
     * @throws FrameException in case of error in message
     */
    public static ValveOrActuatorStatus parseValveStatus(Thermoregulation msg, WhatThermoType whatType)
            throws NumberFormatException, FrameException {
        if (whatType != WhatThermoType.CONDITIONING && whatType != WhatThermoType.HEATING) {
            throw new FrameException("Only CONDITIONING and HEATING are allowed as what input parameter.");
        }

        String[] values = msg.getDimValues();
        logger.debug("====parseValveStatus {} --> : CV <{}> HV <{}>", msg.getFrameValue(), values[0], values[1]);

        if (msg.getDim() == DimThermo.VALVES_STATUS) {
            if (whatType == WhatThermoType.CONDITIONING) {
                return ValveOrActuatorStatus.fromValue(Integer.parseInt(values[0]));
            }
            if (whatType == WhatThermoType.HEATING) {
                return ValveOrActuatorStatus.fromValue(Integer.parseInt(values[1]));
            }

            return null;
        } else {
            throw new NumberFormatException("Could not parse valve status from: " + msg.getFrameValue());
        }
    }

    /**
     * Parse actuator status from Thermoregulation message (dimension: 20)
     *
     * @param msg Thermoregulation message
     * @return parsed actuator status as {@link ValveOrActuatorStatus}
     * @throws NumberFormatException in case of invalid status
     * @throws FrameException in case of error in message
     */
    public static ValveOrActuatorStatus parseActuatorStatus(Thermoregulation msg)
            throws NumberFormatException, FrameException {
        String[] values = msg.getDimValues();
        logger.debug("====parseActuatorStatus {} --> : <{}>", msg.getFrameValue(), values[0]);

        if (msg.getDim() == DimThermo.ACTUATOR_STATUS) {
            return ValveOrActuatorStatus.fromValue(Integer.parseInt(values[0]));
        } else {
            throw new NumberFormatException("Could not parse actuator status from: " + msg.getFrameValue());
        }
    }

    /**
     * Parse local offset (knob) from Thermoregulation message (dimension: 13)
     *
     * @param msg Thermoregulation message
     * @return parsed local offset (knob) as {@link LocalOffset}
     * @throws NumberFormatException in case of invalid status
     * @throws FrameException in case of error in message
     */
    public static LocalOffset parseLocalOffset(Thermoregulation msg) throws NumberFormatException, FrameException {
        String[] values = msg.getDimValues();
        logger.debug("====parseLocalOffset {} --> : <{}>", msg.getFrameValue(), values[0]);

        if (msg.getDim() == DimThermo.OFFSET) {
            return LocalOffset.fromValue(values[0]);
        } else {
            throw new NumberFormatException("Could not parse local offset (knob) from: " + msg.getFrameValue());
        }
    }

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
                return OpenDeviceType.SCS_THERMO_SENSOR;
            } else if (w.isCentralUnit()) {
                return OpenDeviceType.SCS_THERMO_CENTRAL_UNIT;
            } else {
                return OpenDeviceType.SCS_THERMO_ZONE;
            }
        }
    }

}
