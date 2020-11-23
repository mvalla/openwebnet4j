package org.openwebnet4j.message;

import static java.lang.String.format;
import static org.openwebnet4j.message.Who.THERMOREGULATION;

import java.util.HashMap;
import java.util.Map;

import org.openwebnet4j.OpenDeviceType;

/**
 * OpenWebNet Thermostat messages (WHO=4)
 *
 * @author G. Cocchi - Initial contribution
 */
public class Thermoregulation extends BaseOpenMessage {

    public enum WHAT implements What {
        UNKNOWN(-1),
        COOL(0),
        HEAT(1),
        OFF(303);

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

    public enum DIM implements Dim {
        REQUEST_TEMPERATURE(0),
        SET_POINT_TEMPERATURE(14),
        REQUEST_SET_POINT_TEMPERATURE(14);

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
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation setPointTemperature(String what, String temperature, String mode) {
        System.out.println(format(FORMAT_SETTING, WHO, what, DIM.SET_POINT_TEMPERATURE.value(), temperature, mode));
        return new Thermoregulation(
                format(FORMAT_SETTING, WHO, what, DIM.SET_POINT_TEMPERATURE.value(), temperature, mode));
    }

    /**
     * OpenWebNet message request to turn off the thermostat <i>OFF</i> <b>*4*303*where##</b>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestTurnOff(String w) {
        return new Thermoregulation(format(FORMAT_REQUEST, WHO, WHAT.OFF.value, w));
    }

    /**
     * OpenWebNet message request temperature <b>*#4*where*0##</b>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestTemperature(String w) {
        return new Thermoregulation(format(FORMAT_DIMENSION, WHO, w, DIM.REQUEST_TEMPERATURE.value()));
    }

    /**
     * OpenWebNet message request the current Thermostat Set Point temperature<b>*#4*where*14##</b>.
     *
     * @param where WHERE string
     * @return message
     */
    public static Thermoregulation requestSetPointTemperature(String w) {
        return new Thermoregulation(format(FORMAT_DIMENSION, WHO, w, DIM.REQUEST_SET_POINT_TEMPERATURE.value()));
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