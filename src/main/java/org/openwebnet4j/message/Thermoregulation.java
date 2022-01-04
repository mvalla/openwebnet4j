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
 * @author A. Conte - Added standalone and central unit
 */
public class Thermoregulation extends BaseOpenMessage {

    private static final Logger logger = LoggerFactory.getLogger(Thermoregulation.class);

    public enum WhatThermo implements What {
        CONDITIONING(0),
        HEATING(1),
        GENERIC(3),
        // protection
        PROTECTION_HEATING(102, Function.HEATING, OperationMode.PROTECTION), // antifreeze
        PROTECTION_CONDITIONING(
                202, Function.COOLING, OperationMode.PROTECTION), // thermal-protection
        PROTECTION_GENERIC(302, Function.GENERIC, OperationMode.PROTECTION),
        // off
        OFF_HEATING(103, Function.HEATING, OperationMode.OFF),
        OFF_CONDITIONING(203, Function.COOLING, OperationMode.OFF),
        OFF_GENERIC(303, Function.GENERIC, OperationMode.OFF),
        // manual
        MANUAL_HEATING(110, Function.HEATING, OperationMode.MANUAL),
        MANUAL_CONDITIONING(210, Function.COOLING, OperationMode.MANUAL),
        MANUAL_GENERIC(310, Function.GENERIC, OperationMode.MANUAL),

        // // programming (zone is following the program of the central unit)
        // PROGRAM_HEATING(111, Function.HEATING, OperationMode.PROGRAM),
        // PROGRAM_CONDITIONING(211, Function.COOLING, OperationMode.PROGRAM),
        // PROGRAM_GENERIC(311, Function.GENERIC, OperationMode.PROGRAM),
        // // holiday (zone is following the holiday program set on the central unit)
        // HOLIDAY_HEATING(115, Function.HEATING, OperationMode.HOLIDAY),
        // HOLIDAY_CONDITIONING(215, Function.COOLING, OperationMode.HOLIDAY),
        // HOLIDAY_GENERIC(315, Function.GENERIC, OperationMode.HOLIDAY),

        // weekly program (zone is following the program of the central unit)
        WEEKLY1_HEATING(1101, Function.HEATING, OperationMode.WEEKLY_1),
        WEEKLY1_CONDITIONING(2101, Function.COOLING, OperationMode.WEEKLY_1),
        WEEKLY1_GENERIC(3101, Function.GENERIC, OperationMode.WEEKLY_1),
        WEEKLY2_HEATING(1102, Function.HEATING, OperationMode.WEEKLY_2),
        WEEKLY2_CONDITIONING(2102, Function.COOLING, OperationMode.WEEKLY_2),
        WEEKLY2_GENERIC(3102, Function.GENERIC, OperationMode.WEEKLY_2),
        WEEKLY3_HEATING(1103, Function.HEATING, OperationMode.WEEKLY_3),
        WEEKLY3_CONDITIONING(2103, Function.COOLING, OperationMode.WEEKLY_3),
        WEEKLY3_GENERIC(3103, Function.GENERIC, OperationMode.WEEKLY_3),

        // scenarios (zone is set by the central unit)
        SCENARIO1_HEATING(1201, Function.HEATING, OperationMode.SCENARIO_1),
        SCENARIO1_CONDITIONING(2201, Function.COOLING, OperationMode.SCENARIO_1),
        SCENARIO1_GENERIC(3201, Function.GENERIC, OperationMode.SCENARIO_1),
        SCENARIO2_HEATING(1202, Function.HEATING, OperationMode.SCENARIO_2),
        SCENARIO2_CONDITIONING(2202, Function.COOLING, OperationMode.SCENARIO_2),
        SCENARIO2_GENERIC(3202, Function.GENERIC, OperationMode.SCENARIO_2),
        SCENARIO3_HEATING(1203, Function.HEATING, OperationMode.SCENARIO_3),
        SCENARIO3_CONDITIONING(2203, Function.COOLING, OperationMode.SCENARIO_3),
        SCENARIO3_GENERIC(3203, Function.GENERIC, OperationMode.SCENARIO_3),
        SCENARIO4_HEATING(1204, Function.HEATING, OperationMode.SCENARIO_4),
        SCENARIO4_CONDITIONING(2204, Function.COOLING, OperationMode.SCENARIO_4),
        SCENARIO4_GENERIC(3204, Function.GENERIC, OperationMode.SCENARIO_4),
        SCENARIO5_HEATING(1205, Function.HEATING, OperationMode.SCENARIO_5),
        SCENARIO5_CONDITIONING(2205, Function.COOLING, OperationMode.SCENARIO_5),
        SCENARIO5_GENERIC(3205, Function.GENERIC, OperationMode.SCENARIO_5),
        SCENARIO6_HEATING(1206, Function.HEATING, OperationMode.SCENARIO_6),
        SCENARIO6_CONDITIONING(2206, Function.COOLING, OperationMode.SCENARIO_6),
        SCENARIO6_GENERIC(3206, Function.GENERIC, OperationMode.SCENARIO_6),
        SCENARIO7_HEATING(1207, Function.HEATING, OperationMode.SCENARIO_7),
        SCENARIO7_CONDITIONING(2207, Function.COOLING, OperationMode.SCENARIO_7),
        SCENARIO7_GENERIC(3207, Function.GENERIC, OperationMode.SCENARIO_7),
        SCENARIO8_HEATING(1208, Function.HEATING, OperationMode.SCENARIO_8),
        SCENARIO8_CONDITIONING(2208, Function.COOLING, OperationMode.SCENARIO_8),
        SCENARIO8_GENERIC(3208, Function.GENERIC, OperationMode.SCENARIO_8),
        SCENARIO9_HEATING(1209, Function.HEATING, OperationMode.SCENARIO_9),
        SCENARIO9_CONDITIONING(2209, Function.COOLING, OperationMode.SCENARIO_9),
        SCENARIO9_GENERIC(3209, Function.GENERIC, OperationMode.SCENARIO_9),
        SCENARIO10_HEATING(1210, Function.HEATING, OperationMode.SCENARIO_10),
        SCENARIO10_CONDITIONING(2210, Function.COOLING, OperationMode.SCENARIO_10),
        SCENARIO10_GENERIC(3210, Function.GENERIC, OperationMode.SCENARIO_10),
        SCENARIO11_HEATING(1211, Function.HEATING, OperationMode.SCENARIO_11),
        SCENARIO11_CONDITIONING(2211, Function.COOLING, OperationMode.SCENARIO_11),
        SCENARIO11_GENERIC(3211, Function.GENERIC, OperationMode.SCENARIO_11),
        SCENARIO12_HEATING(1212, Function.HEATING, OperationMode.SCENARIO_12),
        SCENARIO12_CONDITIONING(2212, Function.COOLING, OperationMode.SCENARIO_12),
        SCENARIO12_GENERIC(3212, Function.GENERIC, OperationMode.SCENARIO_12),
        SCENARIO13_HEATING(1213, Function.HEATING, OperationMode.SCENARIO_13),
        SCENARIO13_CONDITIONING(2213, Function.COOLING, OperationMode.SCENARIO_13),
        SCENARIO13_GENERIC(3213, Function.GENERIC, OperationMode.SCENARIO_13),
        SCENARIO14_HEATING(1214, Function.HEATING, OperationMode.SCENARIO_14),
        SCENARIO14_CONDITIONING(2214, Function.COOLING, OperationMode.SCENARIO_14),
        SCENARIO14_GENERIC(3214, Function.GENERIC, OperationMode.SCENARIO_14),
        SCENARIO15_HEATING(1215, Function.HEATING, OperationMode.SCENARIO_15),
        SCENARIO15_CONDITIONING(2215, Function.COOLING, OperationMode.SCENARIO_15),
        SCENARIO15_GENERIC(3215, Function.GENERIC, OperationMode.SCENARIO_15),
        SCENARIO16_HEATING(1216, Function.HEATING, OperationMode.SCENARIO_16),
        SCENARIO16_CONDITIONING(2216, Function.COOLING, OperationMode.SCENARIO_16),
        SCENARIO16_GENERIC(3216, Function.GENERIC, OperationMode.SCENARIO_16),

        // central unit only
        REMOTE_CONTROL_DISABLED(20),
        REMOTE_CONTROL_ENABLED(21),
        AT_LEAST_ONE_PROBE_OFF(22),
        AT_LEAST_ONE_PROBE_ANTIFREEZE(23),
        AT_LEAST_ONE_PROBE_MANUAL(24),
        BATTERY_KO(31);

        private static Map<Integer, WhatThermo> mapping;

        private final int value;
        private final Function function;
        private final OperationMode mode;

        private WhatThermo(int value) {
            this.value = value;

            this.function = Function.GENERIC;
            this.mode = OperationMode.MANUAL;
        }

        private WhatThermo(int value, Function function, OperationMode mode) {
            this.value = value;
            this.function = function;
            this.mode = mode;
        }

        private static void initMapping() {
            mapping = new HashMap<Integer, WhatThermo>();
            for (WhatThermo w : values()) {
                mapping.put(w.value, w);
            }
        }

        public static WhatThermo fromValue(int i) {
            if (mapping == null) {
                initMapping();
            }
            return mapping.get(i);
        }

        @Override
        public Integer value() {
            return value;
        }

        public Function function() {
            return function;
        }

        public OperationMode mode() {
            return mode;
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
            Optional<LocalOffset> offset =
                    Arrays.stream(values()).filter(val -> s.equals(val.value)).findFirst();
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
        SPEED_3(3);
        // OFF(15); present in documentation but not handled on real bus

        private final Integer value;

        private FanCoilSpeed(Integer value) {
            this.value = value;
        }

        public static FanCoilSpeed fromValue(Integer i) {
            Optional<FanCoilSpeed> fcs =
                    Arrays.stream(values())
                            .filter(val -> i.intValue() == val.value.intValue())
                            .findFirst();
            return fcs.orElse(null);
        }

        public Integer value() {
            return value;
        }
    }

    public enum OperationMode {
        MANUAL(1),
        PROTECTION(2),
        OFF(3),

        WEEKLY_1(4),
        WEEKLY_2(5),
        WEEKLY_3(6),

        SCENARIO_1(7),
        SCENARIO_2(8),
        SCENARIO_3(9),
        SCENARIO_4(10),
        SCENARIO_5(11),
        SCENARIO_6(12),
        SCENARIO_7(13),
        SCENARIO_8(14),
        SCENARIO_9(15),
        SCENARIO_10(16),
        SCENARIO_11(17),
        SCENARIO_12(18),
        SCENARIO_13(19),
        SCENARIO_14(20),
        SCENARIO_15(21),
        SCENARIO_16(22);

        private final Integer value;

        private OperationMode(Integer value) {
            this.value = value;
        }

        public static OperationMode fromValue(Integer i) {
            Optional<OperationMode> m =
                    Arrays.stream(values())
                            .filter(val -> i.intValue() == val.value.intValue())
                            .findFirst();
            return m.orElse(null);
        }

        public Integer value() {
            return value;
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
            Optional<Function> m =
                    Arrays.stream(values())
                            .filter(val -> i.intValue() == val.value.intValue())
                            .findFirst();
            return m.orElse(null);
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
            Optional<ValveOrActuatorStatus> fcs =
                    Arrays.stream(values())
                            .filter(val -> i.intValue() == val.value.intValue())
                            .findFirst();
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
        return WhatThermo.fromValue(i);
    }

    @Override
    protected Dim dimFromValue(int i) {
        return DimThermo.fromValue(i);
    }

    /**
     * OpenWebNet message to set set point temperature T<code>*#4*where*#14*T*M##
     * </code>.
     *
     * @param where WHERE string
     * @param newSetPointTemperature temperature T between 5.0° and 40.0° (with 0.5° step)
     * @param function HEATING/COOLING/GENERIC
     * @return message
     * @throws MalformedFrameException in case of error in parameters
     */
    public static Thermoregulation requestWriteSetpointTemperature(
            String where, double newSetPointTemperature, Thermoregulation.Function function)
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
                        DimThermo.TEMP_SETPOINT.value(),
                        encodeTemperature(Math.rint(newSetPointTemperature * 2) / 2),
                        function.value()));
    }

    /**
     * OpenWebNet message to set fan coil speed <code>*#4*where*#11*speed##</code>.
     *
     * @param where WHERE string
     * @param newFanCoilSpeed Speed of the fan coil
     * @return message
     */
    public static Thermoregulation requestWriteFanCoilSpeed(
            String where, Thermoregulation.FanCoilSpeed newFanCoilSpeed) {
        return new Thermoregulation(
                format(
                        FORMAT_DIMENSION_WRITING_1V,
                        WHO,
                        where,
                        DimThermo.FAN_COIL_SPEED.value(),
                        newFanCoilSpeed.value()));
    }

    /**
     * OpenWebNet message to request the current fan coil speed <code>*#4*where*11##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestFanCoilSpeed(String where) {
        return new Thermoregulation(
                format(FORMAT_DIMENSION_REQUEST, WHO, where, DimThermo.FAN_COIL_SPEED.value()));
    }

    /**
     * OpenWebNet message to set the function (HEATING, COOLING, GENERIC). HEATING <code>
     * *4*102*where##</code> COOLING <code>*4*202*where##</code> GENERIC <code>*4*302*where##</code>
     *
     * @param where WHERE string
     * @param newFunction Function (HEATING, COOLING, GENERIC)
     * @return message
     */
    public static Thermoregulation requestWriteFunction(
            String where, Thermoregulation.Function newFunction) {

        switch (newFunction) {
            case HEATING:
                return new Thermoregulation(
                        format(FORMAT_REQUEST, WHO, WhatThermo.PROTECTION_HEATING.value(), where));
            case COOLING:
                return new Thermoregulation(
                        format(
                                FORMAT_REQUEST,
                                WHO,
                                WhatThermo.PROTECTION_CONDITIONING.value(),
                                where));

                // this is allow only with central unit
            case GENERIC:
                return new Thermoregulation(
                        format(FORMAT_REQUEST, WHO, WhatThermo.PROTECTION_GENERIC.value(), where));
        }
        return null;
    }

    /**
     * OpenWebNet message to set the operation mode (MANUAL, PROTECTION, OFF). MANUAL <code>
     * *#4*where*#14*T*M##</code> (requestWriteSetPointTemperature) PROTECTION <code>*4*302*where##
     * </code> (generic protection) OFF <code>*4*303*where##</code> (generic OFF)
     *
     * @param where WHERE string
     * @param newOperationMode Operation mode
     * @param currentFunction current zone function (HEATING/COOLING/GENERIC)
     * @param setPointTemperature temperature T between 5.0° and 40.0° (with 0.5° step) to be set
     *     when switching to function=MANUAL
     * @return message
     */
    public static Thermoregulation requestWriteMode(
            String where,
            Thermoregulation.OperationMode newOperationMode,
            Thermoregulation.Function currentFunction,
            double setPointTemperature) {

        switch (newOperationMode) {
            case MANUAL:
                try {
                    return requestWriteSetpointTemperature(
                            where, setPointTemperature, currentFunction);
                } catch (MalformedFrameException ex) {
                    return null;
                }
            case PROTECTION:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.PROTECTION_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.PROTECTION_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.PROTECTION_GENERIC.value(),
                                        where));
                }
            case OFF:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(FORMAT_REQUEST, WHO, WhatThermo.OFF_HEATING.value(), where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.OFF_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(FORMAT_REQUEST, WHO, WhatThermo.OFF_GENERIC.value(), where));
                }
            case WEEKLY_1:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.WEEKLY1_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.WEEKLY1_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.WEEKLY1_GENERIC.value(),
                                        where));
                }
            case WEEKLY_2:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.WEEKLY2_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.WEEKLY2_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.WEEKLY2_GENERIC.value(),
                                        where));
                }
            case WEEKLY_3:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.WEEKLY3_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.WEEKLY3_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.WEEKLY3_GENERIC.value(),
                                        where));
                }

            case SCENARIO_1:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO1_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO1_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO1_GENERIC.value(),
                                        where));
                }
            case SCENARIO_2:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO2_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO2_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO2_GENERIC.value(),
                                        where));
                }
            case SCENARIO_3:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO3_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO3_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO3_GENERIC.value(),
                                        where));
                }
            case SCENARIO_4:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO4_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO4_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO4_GENERIC.value(),
                                        where));
                }
            case SCENARIO_5:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO5_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO5_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO5_GENERIC.value(),
                                        where));
                }
            case SCENARIO_6:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO6_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO6_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO6_GENERIC.value(),
                                        where));
                }
            case SCENARIO_7:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO7_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO7_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO7_GENERIC.value(),
                                        where));
                }
            case SCENARIO_8:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO8_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO8_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO8_GENERIC.value(),
                                        where));
                }
            case SCENARIO_9:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO9_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO9_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO9_GENERIC.value(),
                                        where));
                }
            case SCENARIO_10:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO10_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO10_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO10_GENERIC.value(),
                                        where));
                }
            case SCENARIO_11:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO11_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO11_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO11_GENERIC.value(),
                                        where));
                }
            case SCENARIO_12:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO12_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO12_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO12_GENERIC.value(),
                                        where));
                }
            case SCENARIO_13:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO13_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO13_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO13_GENERIC.value(),
                                        where));
                }
            case SCENARIO_14:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO14_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO14_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO14_GENERIC.value(),
                                        where));
                }
            case SCENARIO_15:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO15_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO15_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO15_GENERIC.value(),
                                        where));
                }
            case SCENARIO_16:
                switch (currentFunction) {
                    case HEATING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO16_HEATING.value(),
                                        where));
                    case COOLING:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO16_CONDITIONING.value(),
                                        where));
                    case GENERIC:
                        return new Thermoregulation(
                                format(
                                        FORMAT_REQUEST,
                                        WHO,
                                        WhatThermo.SCENARIO16_GENERIC.value(),
                                        where));
                }
        }
        return null;
    }

    /**
     * OpenWebNet message to request the set point temperature with operation mode <code>
     * *#4*where*12##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestMode(String where) {
        return new Thermoregulation(
                format(
                        FORMAT_DIMENSION_REQUEST,
                        WHO,
                        where,
                        DimThermo.COMPLETE_PROBE_STATUS.value()));
    }

    /**
     * OpenWebNet message to request the local offset (knob) <code>*#4*where*13##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestLocalMode(String where) {
        return new Thermoregulation(
                format(FORMAT_DIMENSION_REQUEST, WHO, where, DimThermo.OFFSET.value()));
    }

    /**
     * OpenWebNet message to request current valves status (conditioning (CV) and heating (HV))
     * <code>*#4*where*19##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestValvesStatus(String where) {
        return new Thermoregulation(
                format(FORMAT_DIMENSION_REQUEST, WHO, where, DimThermo.VALVES_STATUS.value()));
    }

    /**
     * OpenWebNet message to set the zone mode.
     *
     * @param where WHERE string
     * @param newMode the new MODE
     * @return message
     * @throws MalformedFrameException in case of error in parameters
     */
    public static Thermoregulation requestWriteSetMode(
            String where, Thermoregulation.WhatThermo newMode) throws MalformedFrameException {
        return new Thermoregulation(format(FORMAT_REQUEST, WHO, newMode.value(), where));
    }

    /**
     * OpenWebNet message to request current sensed temperature <code>*#4*where*0##</code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestTemperature(String where) {
        return new Thermoregulation(
                format(FORMAT_DIMENSION_REQUEST, WHO, where, DimThermo.TEMPERATURE.value()));
    }

    /**
     * OpenWebNet message to request the current set point temperature <code>*#4*where*14##
     * </code>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestSetPointTemperature(String where) {
        return new Thermoregulation(
                format(FORMAT_DIMENSION_REQUEST, WHO, where, DimThermo.TEMP_SETPOINT.value()));
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
        return new Thermoregulation(
                format(FORMAT_DIMENSION_REQUEST, WHO, where, DimThermo.ACTUATOR_STATUS.value()));
    }

    /**
     * OpenWebNet message to request the central unit operation mode <code>*#4*#0##</code>.
     *
     * @return message
     */
    public static Thermoregulation requestCUOperationMode() {
        return new Thermoregulation(format(FORMAT_STATUS, WHO, "#0"));
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
     * Returns the actuator from the message WHERE part. WHERE=Z#N --&gt; returns N
     *
     * @return id (int 1-9) of the actuator
     */
    public int getActuator() {
        WhereThermo wt = (WhereThermo) where;
        return wt.getActuator();
    }

    /**
     * Returns if the thing is defined as stand-alone
     *
     * @return boolean
     */
    public boolean isStandalone() {
        WhereThermo wt = (WhereThermo) where;
        return wt.isStandalone();
    }

    /**
     * Returns if the thing is a central unit
     *
     * @return boolean
     */
    public boolean isCentralUnit() {
        WhereThermo wt = (WhereThermo) where;
        return wt.isCentralUnit();
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
    public static double parseTemperature(Thermoregulation msg)
            throws NumberFormatException, FrameException {
        String[] values = msg.getDimValues();
        // temp is in the first dim value for thermostats (dim=0,12,14), in the second in case of
        // probes (dim=15)
        // TODO check min,max values
        if (msg.getDim() == DimThermo.TEMPERATURE
                || msg.getDim() == DimThermo.TEMP_SETPOINT
                || msg.getDim() == DimThermo.COMPLETE_PROBE_STATUS) {
            return decodeTemperature(values[0]);
        } else if (msg.getDim() == DimThermo.PROBE_TEMPERATURE) {
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
     * @param temperature the temperature as String
     * @return the temperature as Double
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
     * Encodes temperature from float to BTicino format
     *
     * @param temp temperature
     * @return String
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
    public static FanCoilSpeed parseFanCoilSpeed(Thermoregulation msg)
            throws NumberFormatException, FrameException {
        String[] values = msg.getDimValues();
        if (msg.getDim() == DimThermo.FAN_COIL_SPEED) {
            return FanCoilSpeed.fromValue(Integer.parseInt(values[0]));
        } else {
            throw new NumberFormatException(
                    "Could not parse fan coil speed from: " + msg.getFrameValue());
        }
    }

    /**
     * Parse valve status (CV and HV) from Thermoregulation message (dimension: 19)
     *
     * @param msg Thermoregulation message
     * @param what Look for COOLING (CV) or HEATING (HV) valve
     * @return parsed valve status as {@link ValveOrActuatorStatus}
     * @throws NumberFormatException in case of invalid status
     * @throws FrameException in case of error in message
     */
    public static ValveOrActuatorStatus parseValveStatus(Thermoregulation msg, WhatThermo what)
            throws NumberFormatException, FrameException {
        if (what != WhatThermo.CONDITIONING && what != WhatThermo.HEATING) {
            throw new FrameException(
                    "Only CONDITIONING and HEATING are allowed as what input parameter.");
        }

        String[] values = msg.getDimValues();
        logger.debug(
                "====parseValveStatus {} --> : CV <{}> HV <{}>",
                msg.getFrameValue(),
                values[0],
                values[1]);

        if (msg.getDim() == DimThermo.VALVES_STATUS) {
            if (what == WhatThermo.CONDITIONING) {
                return ValveOrActuatorStatus.fromValue(Integer.parseInt(values[0]));
            }
            if (what == WhatThermo.HEATING) {
                return ValveOrActuatorStatus.fromValue(Integer.parseInt(values[1]));
            }

            return null;
        } else {
            throw new NumberFormatException(
                    "Could not parse valve status from: " + msg.getFrameValue());
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
            throw new NumberFormatException(
                    "Could not parse actuator status from: " + msg.getFrameValue());
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
    public static LocalOffset parseLocalOffset(Thermoregulation msg)
            throws NumberFormatException, FrameException {
        String[] values = msg.getDimValues();
        logger.debug("====parseLocalOffset {} --> : <{}>", msg.getFrameValue(), values[0]);

        if (msg.getDim() == DimThermo.OFFSET) {
            return LocalOffset.fromValue(values[0]);
        } else {
            throw new NumberFormatException(
                    "Could not parse local offset (knob) from: " + msg.getFrameValue());
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
                return OpenDeviceType.SCS_THERMO_SENSOR;
            } else if (w.isCentralUnit()) {
                return OpenDeviceType.SCS_THERMO_CENTRAL_UNIT;
            } else {
                return OpenDeviceType.SCS_THERMO_ZONE;
            }
        }
    }
}
