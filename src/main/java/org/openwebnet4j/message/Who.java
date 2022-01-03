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

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * OpenWebNet WHO types.
 *
 * @author M. Valla - Initial contribution for openwebnet4j
 */
public enum Who {
    SCENARIO(0),
    LIGHTING(1),
    AUTOMATION(2),
    @Deprecated
    LOAD_CONTROL(3),
    // heating
    THERMOREGULATION(4),
    THERMOREGULATION_DIAGNOSTIC(1004),
    // intrusion
    BURGLAR_ALARM(5),
    DOOR_ENTRY_SYSTEM(6),
    // multimedia
    VIDEO_DOOR_ENTRY_SYSTEM(7),
    AUX(9),
    // gateway management
    GATEWAY_MANAGEMENT(13),

    LIGHT_SHUTTER_ACTUATORS_LOCK(14),
    // CEN and CENPLUS
    CEN_SCENARIO_SCHEDULER(15),
    CEN_PLUS_SCENARIO_SCHEDULER(25),
    // audio
    @Deprecated
    SOUND_SYSTEM_1(16),
    SOUND_SYSTEM_2(22),
    // MH200N
    SCENARIO_PROGRAMMING(17),
    // energy management
    ENERGY_MANAGEMENT(18),
    ENERGY_MANAGEMENT_DIAGNOSTIC(1018),
    // audio
    LIGHTING_MANAGEMENT(24),
    // diagnostic
    DIAGNOSTIC(1000),
    AUTOMATION_DIAGNOSTIC(1001),
    DEVICE_DIAGNOSTIC(1013),
    UNKNOWN(9999);

    private final Integer value;

    Who(Integer value) {
        this.value = value;
    }

    public Integer value() {
        return value;
    }

    public static boolean isValidName(String name) {
        return name != null && findWho(isEqualName(name)).isPresent();
    }

    public static boolean isValidValue(Integer value) {
        return value != null && findWho(isEqualValue(value)).isPresent();
    }

    public static Who fromName(String name) {
        return findWho(isEqualName(name)).get();
    }

    public static Who fromValue(Integer value) {
        return findWho(isEqualValue(value)).get();
    }

    @Override
    public String toString() {
        return name();
    }

    private static Predicate<Who> isEqualName(String name) {
        return who -> who.name().equals(name);
    }

    private static Predicate<Who> isEqualValue(Integer value) {
        return who -> who.value().intValue() == value.intValue();
    }

    private static Optional<Who> findWho(Predicate<Who> isEqual) {
        return EnumSet.allOf(Who.class).stream().filter(isEqual).findFirst();
    }
}
