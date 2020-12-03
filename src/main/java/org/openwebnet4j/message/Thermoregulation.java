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
 * OpenWebNet Thermostat messages (WHO=4)
 *
 * @author M. Valla - Initial contribution
 * @author G. Cocchi - Contributor
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
     * OpenWebNet message to Manual setting of “N” zone to T temperature <b>*#4*where*#14*T*M##</b>.
     *
     * @param where Zone between #1 and #99
     * @param temp temperature T between 5.0° and 40.0° (with 0.5° step)
     * @param mode
     * @return message
     */
    public static Thermoregulation requestWriteSetpointTemperature(
            String where, float newSetPointTemperature, String mode) {
        return new Thermoregulation(
                format(
                        FORMAT_SETTING,
                        WHO,
                        where,
                        DIM.TEMP_SETPOINT.value(),
                        encodeTemperature(newSetPointTemperature),
                        mode));
    }

    /**
     * OpenWebNet message request to turn off the thermostat <i>OFF</i> <b>*4*303*where##</b>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestTurnOff(String w) {
        return new Thermoregulation(format(FORMAT_REQUEST, WHO, WHAT.OFF_GENERIC.value, w));
    }

    /**
     * OpenWebNet message request temperature <b>*#4*where*0##</b>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestTemperature(String w) {
        return new Thermoregulation(format(FORMAT_DIMENSION, WHO, w, DIM.TEMPERATURE.value()));
    }

    /**
     * OpenWebNet message request the current Thermostat Set Point temperature<b>*#4*where*14##</b>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestSetPointTemperature(String w) {
        return new Thermoregulation(format(FORMAT_DIMENSION, WHO, w, DIM.TEMP_SETPOINT.value()));
    }

    /**
     * OpenWebNet message N zone device status request <b>*#4*where##</b>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestStatus(String w) {
        return new Thermoregulation(format(FORMAT_STATUS, WHO, w));
    }

    /**
     * OpenWebNet message N zone valves status request<b>*#4*where*19##</b>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestValvesStatus(String w) {
        return new Thermoregulation(format(FORMAT_DIMENSION, WHO, w, DIM.VALVES_STATUS.value()));
    }

    @Override
    protected void parseWhere() throws FrameException {
        if (whereStr == null) {
            throw new FrameException("Frame has no WHERE part: " + whereStr);
        } else {
            where = new WhereLightAutom(whereStr);
        }
    }

    /*
     * Parse temperature from Thermoregulation msg (dimensions: 0, 12, 14 or 15)
     *
     * @param msg Thermoregulation message
     *
     * @return parsed temperature in degrees Celsius
     *
     * @throws NumberFormatException
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

    public LOCAL_OFFSET getLocalOffset() throws FrameException {
        String[] values = getDimValues();
        return LOCAL_OFFSET.fromValue(values[0]);
    }

    /**
     * Convert temperature from BTicino format to number For example: 0235 --> +23.5 (°C) and 1048
     * --> -4.8 (°C)
     *
     * @param temperature the temperature as String
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
    public static String encodeTemperature(float temp) {
        // +23.51 °C --> '0235'; -4.86 °C --> '1049'
        // checkRange(5, 40, Math.round(temp));
        char sign = (temp >= 0 ? '0' : '1');
        String digits = "";
        int absTemp = Math.abs(Math.round(temp * 10));
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
        What what = getWhat();
        if (what.toString().startsWith("5")) {
            return OpenDeviceType.SCS_TEMP_SENSOR;
        } else if (what.toString().startsWith("0") || what.toString().startsWith("#0")) {
            // Central unit or "all
            // probes"
            return null;
        } else {
            return OpenDeviceType.SCS_THERMOSTAT;
        }
    }
}
