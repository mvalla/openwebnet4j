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
 * @author G. Cocchi - Initial contribution
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
            Optional<MODE> m = Arrays.stream(values()).filter(val -> i.intValue() == val.value.intValue()).findFirst();
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
            Optional<LOCAL_OFFSET> offset = Arrays.stream(values()).filter(val -> s.equals(val.value)).findFirst();
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
        ACTUATOR_STATUS(20),
        ACTUATOR_STATUS_ON(1),
        ACTUATOR_STATUS_OFF(0);

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

    private static final int WHO = THERMOREGULATION.value();

    /**
     * OpenWebNet message to Manual setting of “N” zone to T temperature <b>*#4*where*#14*T*M##</b>.
     *
     * @param where Zone between #1 and #99
     * @param temp temperature T between 5.0° and 40.0° (with 0.5° step)
     * @param mode
     * @return message
     */
    public static Thermoregulation requestWriteSetpointTemperature(String where, String temperature, String mode) {
        return new Thermoregulation(format(FORMAT_SETTING, WHO, where, DIM.TEMP_SETPOINT.value(), temperature, mode));
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
     * OpenWebNet message request to get a Thermostat device status <b>*#4*where##</b>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestStatus(String w) {
        return new Thermoregulation(format(FORMAT_STATUS, WHO, w));
    }

    @Override
    protected void parseWhere() throws FrameException {
        if (whereStr == null) {
            throw new FrameException("Frame has no WHERE part: " + whereStr);
        } else {
            where = new WhereLightAutom(whereStr);
        }
    }

    @Override
    public OpenDeviceType detectDeviceType() throws FrameException {
        if (isCommand()) { // ignore status/dimension frames for detecting device type
            OpenDeviceType type = null;
            What w = getWhat();
            type = OpenDeviceType.SCS_THERMOSTAT;
            return type;
        } else {
            return null;
        }
    }
}