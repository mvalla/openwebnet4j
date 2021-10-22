package org.openwebnet4j.message;

import org.openwebnet4j.OpenDeviceType;

import java.util.HashMap;
import java.util.Map;

/**
 * OpenWebNet Auxiliary messages (WHO=9)
 *
 * @author M. Valla - Initial contribution
 * @author G.Fabiani - Added auxiliary message support
 */

public class Auxiliary extends BaseOpenMessage {

    public enum WhatAuxiliary implements What {
        OFF(0),
        ON(1),
        TOGGLE(2),
        STOP(3),
        UP(4),
        DOWN(5),
        ENABLED(6),
        DISABLED(7),
        RESET_GEN(8),
        RESET_BI(9),
        RESET_TRI(10);

        private static Map<Integer, WhatAuxiliary> mapping;

        private final int value;

        private WhatAuxiliary(int value) {
            this.value = value;
        }

        public static void initMapping(){
            mapping = new HashMap<Integer,WhatAuxiliary>();
            for (WhatAuxiliary w:values()){
                mapping.put(w.value,w);
            }
        }

        public static WhatAuxiliary fromValue(int i) {
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

    protected Auxiliary(String value) {
        super(value);
        this.who = Who.AUX;
    }


    @Override
    protected void parseWhere() throws FrameException {
        if (whereStr == null) {
            throw new FrameException("Frame has no WHERE part: " + whereStr);
        } else {
            if (whereStr.endsWith(WhereZigBee.ZB_NETWORK)) {
                where = new WhereZigBee(whereStr);
            } else {
                where = new WhereLightAutom(whereStr);
            }
        }
    }

    @Override
    protected Dim dimFromValue(int i) {
        return null;
    }

    @Override
    protected What whatFromValue(int i) {
       return WhatAuxiliary.fromValue(i);
    }

    @Override
    public OpenDeviceType detectDeviceType() throws FrameException {
        if (isCommand()) { // ignore status/dimension frames for detecting device type
            OpenDeviceType type = null;
            What w = getWhat();
            if (w != null) {
                if (w == WhatAuxiliary.DOWN || w == WhatAuxiliary.ON || w == WhatAuxiliary.
                        OFF || w == WhatAuxiliary.TOGGLE || w == WhatAuxiliary.DISABLED || w == WhatAuxiliary.ENABLED
                        || w == WhatAuxiliary.STOP || w == WhatAuxiliary.UP || w == WhatAuxiliary.RESET_GEN ||
                        w == WhatAuxiliary.RESET_BI || w == WhatAuxiliary.RESET_TRI) {
                    type = OpenDeviceType.SCS_AUXILIARY_TOGGLE_CONTROL;
                }
            }
            return type;
        } else {
            return null;
        }
    }
}
