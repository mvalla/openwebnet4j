/**
 * Copyright (c) 2022 Contributors to the openwebnet4j project
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

import java.util.HashMap;
import java.util.Map;

import org.openwebnet4j.OpenDeviceType;

/**
 * OpenWebNet class for Scenario (WHO=0)
 *
 * @author M. Valla - Initial contribution
 */
public class Scenario extends BaseOpenMessage {
    // private static final Logger logger = LoggerFactory.getLogger(Scenario.class);

    public enum WhatScenario implements What {
        // In F420 (03551) it is possible to program up to 16 scenarios.
        // In the IR 3456 (88301) it is possible to recall up to 20 different scenarios.
        SCENARIO_01(1),
        SCENARIO_02(2),
        SCENARIO_03(3),
        SCENARIO_04(4),
        SCENARIO_05(5),
        SCENARIO_06(6),
        SCENARIO_07(7),
        SCENARIO_08(8),
        SCENARIO_09(9),
        SCENARIO_10(10),
        SCENARIO_11(11),
        SCENARIO_12(12),
        SCENARIO_13(13),
        SCENARIO_14(14),
        SCENARIO_15(15),
        SCENARIO_16(16),
        // Supported only by module IR 3456 (88301):
        SCENARIO_17(17),
        SCENARIO_18(18),
        SCENARIO_19(19),
        SCENARIO_20(20),
        // Commands only for F420:
        START_RECORDING_SCENARIO(40),
        END_RECORDING_SCENARIO(41),
        ERASE_SCENARIO(42),
        LOCK_SCENARIOS_CU(43),
        UNLOCK_SCENARIOS_CU(44),
        UNAVAILABLE_SCENARIO_CU(45),
        MEMORY_FULL_CU(46);

        private static Map<Integer, WhatScenario> mapping;

        private final int value;

        private WhatScenario(Integer value) {
            this.value = value;
        }

        private static void initMapping() {
            mapping = new HashMap<Integer, WhatScenario>();
            for (WhatScenario w : values()) {
                mapping.put(w.value, w);
            }
        }

        public static WhatScenario fromValue(int i) {
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

    @Override
    protected Dim dimFromValue(int i) {
        // no Dims for this WHO
        return null;
    }

    private static final int WHO = Who.SCENARIO.value();

    protected Scenario(String value) {
        super(value);
        this.who = Who.SCENARIO;

    }

    @Override
    protected What whatFromValue(int i) {
        return WhatScenario.fromValue(i);
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
    public OpenDeviceType detectDeviceType() {
        if (!isCommand()) { // ignore status/dimension frames for discovery
            return null;
        }
        return OpenDeviceType.BASIC_SCENARIO;
    }

}
