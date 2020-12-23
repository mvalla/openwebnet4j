package org.openwebnet4j.message;

import static java.lang.String.format;
import static org.openwebnet4j.message.Who.ENERGY_MANAGEMENT;

import java.util.HashMap;
import java.util.Map;

import org.openwebnet4j.OpenDeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnergyManagement extends BaseOpenMessage {

    private static final Logger logger = LoggerFactory.getLogger(EnergyManagement.class);

    public enum WHAT implements What {
        ACTIVATION_AUTOMATIC_RESET(26), // Activation of the automatic reset
        DEACTIVATION_AUTOMATIC_RESET(27), // Deactivation of the automatic reset
        DAILY_TOTALIZERS(57), // Start sending daily totalizers on an hourly basis for 16-bit Daily graphics
        MONTHLY_TOTALIZERS(58), // Start sending monthly on an hourly basis for 16-bit graphics average Daily
        MONTHLY_DAILY_TOTALIZERS(59), // Start sending monthly totalizers current year on a daily basis for 32-bit
                                      // Monthly graphics
        ENABLE_ACTUATOR(71),
        FORCED_ACTUATOR_TIME(73),
        END_FORCED_ACTUATOR(74),
        RESET_TOTALIZERS(75);

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
        ACTIVE_POWER(113),
        END_AUTOMATIC_UPDATE_SIZE(1200),
        ENERGY_TOTALIZER(51),
        ENERGY_PE_MONTH(52),
        PARTIAL_TOTALIZER_CURRENT_MONTH(53),
        PARTIAL_TOTALIZER_CURRENT_DAY(54),
        ACTUATOR_STATUS(71),
        TOTALIZERS(72),
        DIFFERENTIAL_CURRENT_LEVEL(73);

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

    private static final int WHO = ENERGY_MANAGEMENT.value();

    protected EnergyManagement(String value) {
        super(value);
    }

    /**
     * OpenWebNet message N actuator status request <b>*#18*where*71##</b>.
     *
     * @param where WHERE string
     * @return message
     */
    public static EnergyManagement requestActuatorStatus(String w) {
        return new EnergyManagement(format(FORMAT_DIMENSION, WHO, w, DIM.ACTUATOR_STATUS.value()));
    }

    /**
     * OpenWebNet message active power request <b>*#18*<Where>*113##</b>.
     *
     * @param where WHERE string
     * @return message
     */
    public static EnergyManagement requestActivePower(String w) {
        return new EnergyManagement(format(FORMAT_DIMENSION, WHO, w, DIM.ACTIVE_POWER.value()));
    }

    @Override
    protected void parseWhere() throws FrameException {
        if (whereStr == null) {
            throw new FrameException("Energy Management frame has no WHERE part: " + whereStr);
        } else {
            where = new WhereEnergyManagement(whereStr);

        }
    }

    @Override
    protected What whatFromValue(int i) {
        return WHAT.fromValue(i);
    }

    @Override
    protected Dim dimFromValue(int i) {
        return DIM.fromValue(i);
    }

    @Override
    public OpenDeviceType detectDeviceType() {
        Where w = getWhere();
        if (w == null) {
            return null;
        } else {
            return OpenDeviceType.SCS_ENERGY_CENTRAL_UNIT;
        }
    }

}