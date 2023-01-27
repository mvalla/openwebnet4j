/**
 * Copyright (c) 2020-2023 Contributors to the openwebnet4j project
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

/**
 * WHERE for Thermoregulation frames
 *
 * <p>
 * == Where Table:
 *
 * <p>
 * === Probes - 0 : all master probes - Z : zone Z [1-99] master probe - 0ZZ : zone ZZ [01-99]
 * all probes (master and slave) - pZZ : zone ZZ [01-99] slave probe p[1-8] - p00 : external zone 00
 * slave probe p[1-9]
 *
 * <p>
 * === Actuators (thermostats) - #0 : central unit - #Z : zone Z [1-99] via central unit - 0#0 :
 * all zones, all actuators - Z#0 : zone Z [1-99], all actuators - Z#N : zone Z [1-99], actuator N
 * [1-9]
 *
 * @author M. Valla - Initial contribution
 */
public class WhereThermo extends Where {

    public static final Where ALL_MASTER_PROBES = new WhereThermo("0");
    private final int zone;
    private final int probe;
    private final int actuator;
    private final boolean standalone;

    public WhereThermo(String w) throws NullPointerException, IllegalArgumentException {
        super(w);
        int z, p = -1, a = -1;
        int pos = whereStr.indexOf("#");
        if (pos >= 0) { // # is present
            if (pos == 0) { // case '#x'
                standalone = false;
                z = Integer.parseInt(whereStr.substring(1));
            } else { // case 'x#x'
                standalone = true;
                z = Integer.parseInt(whereStr.substring(0, pos));
                a = Integer.parseInt(whereStr.substring(pos + 1));
            }
        } else { // no # present
            standalone = true;
            z = Integer.parseInt(whereStr);
            if (z > 99) { // case 'pZZ'
                p = Integer.parseInt(whereStr.substring(0, 1));
                z = Integer.parseInt(whereStr.substring(1));
            } else if (whereStr.startsWith("0") && whereStr.length() > 1) { // case '0ZZ'
                p = 0;
                z = Integer.parseInt(whereStr.substring(1));
            }
        }
        if (z <= 99 && z >= 0) {
            zone = z;
        } else {
            throw new IllegalArgumentException("WHERE address '" + w + "' is invalid: zone not in range [0-99]");
        }
        probe = p;
        if (p < -1 || p > 9) {
            throw new IllegalArgumentException("WHERE address '" + w + "' is invalid: probe not in range [0-9]");
        }
        actuator = a;
        if (a < -1 || a > 9) {
            throw new IllegalArgumentException("WHERE address '" + w + "' is invalid: actuator not in range [0-9]");
        }
    }

    /**
     * Returns the Zone for this WHERE
     *
     * @return int Zone (0-99) for this WHERE
     */
    public int getZone() {
        return zone;
    }

    /**
     * Returns the probe for this WHERE, 0 for all probes, or -1 if no probe is present
     *
     * @return int probe for this WHERE
     */
    public int getProbe() {
        return probe;
    }

    /**
     * Returns the actuator (1-9) for this WHERE, 0 for all actuators, or -1 if no actuator is
     * present
     *
     * @return int actuator for this WHERE
     */
    public int getActuator() {
        return actuator;
    }

    /**
     * Returns true if WHERE is a standalone configuration
     *
     * @return true if standalone configuration
     */
    public boolean isStandalone() {
        return standalone;
    }

    /**
     * Returns true if WHERE is Central Unit (where=<code>#0</code>)
     *
     * @return true if Central Unit
     */
    public boolean isCentralUnit() {
        return (zone == 0) && (!standalone);
    }

    /**
     * Returns true if WHERE is a probe address (where=<code>pZZ</code>)
     *
     * @return true if probe address
     */
    public boolean isProbe() {
        return (probe >= 0);
    }
}
