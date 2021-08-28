package org.openwebnet4j.message;

import org.openwebnet4j.OpenDeviceType;

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
        UP(4);

        private static Map<Integer, WhatAuxiliary> mapping;

        private final int value;

        private WhatAuxiliary(int value) {
            this.value = value;
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

    }

    @Override
    protected Dim dimFromValue(int i) {
        return null;
    }

    @Override
    protected What whatFromValue(int i) {
        return null;
    }

    @Override
    public OpenDeviceType detectDeviceType() throws FrameException {
        return null;
    }
}
