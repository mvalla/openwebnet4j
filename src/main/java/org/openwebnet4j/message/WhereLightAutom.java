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
 * WHERE for Lighting and Automation frames
 * <p>
 * Where Table:
 *
 * <ul>
 *
 * <li>General GEN
 * <ul>
 * <li><code>0</code> : all lights/automations
 * </ul>
 * </li>
 * <li>Group G
 * <ul>
 * <li><code>#G</code> : group <code>G</code> [1−255]</li>
 * </ul>
 * </li>
 * <li>Area A
 * <ul>
 * <li><code>A</code> : area <code>A</code> [00, 1−9, 100]</li>
 * </ul>
 * </li>
 * <li>Point to point APL
 * <ul>
 * <li><code>APL</code> : light point <code>PL</code> in area <code>A</code> with the following possible combinations:
 * <ul>
 * <li>A = 00 and PL [01−15]</li>
 * <li>A [1−9] and PL [1−9]</li>
 * <li>A [01−09] and PL [10−15]</li>
 * <li>A = 10 and PL [01−15]</li>
 * </ul>
 * </ul>
 *
 * </ul>
 * Address can end with <code>#3</code> (private BUS) or with <code>#4#INTERFACE</code> (local BUS with INTERFACE
 * [0-1][1-9] )
 *
 * @author M. Valla - Initial contribution. Added General/Area/Group parsing
 */
public class WhereLightAutom extends Where {

    public static final Where GENERAL = new WhereLightAutom("0");
    private final int group; // 1-255
    private final int area; // 0-10
    private final int lightPoint; // 1-15
    private final String busIfc;
    private final boolean isAPL;

    public WhereLightAutom(String w) throws NullPointerException, IllegalArgumentException {
        super(w);
        int g = -1, a = -1, lp = -1;
        String ifc = null;
        boolean apl = false;

        String[] parts = w.split("#");
        try {
            if (w.indexOf("#") == 0) { // GROUP
                g = Integer.parseInt(parts[1]);
                if (g < 1 && g > 255) {
                    throw new IllegalArgumentException(
                            "WHERE address '" + w + "' is invalid: GROUP not in range [1-255]");
                }
            } else {
                String wh = parts[0];
                if (!wh.equals("0")) {
                    if (wh.equals("100")) { // 100 -> A=10
                        a = 10;
                    } else {
                        char c0 = wh.charAt(0);
                        char c1 = (wh.length() > 1 ? wh.charAt(1) : '-');
                        if (c0 == '0') { // A [00-09]
                            a = c1 - '0';
                            if (wh.length() > 2) {
                                lp = Integer.parseInt(wh.substring(2));
                            }
                        } else if (c0 == '1' && c1 == '0') { // 10xx -> A=10
                            a = 10;
                            if (wh.length() == 4) {
                                lp = Integer.parseInt(wh.substring(2));
                            } else {
                                throw new IllegalArgumentException(
                                        "WHERE address '" + w + "' is invalid: AREA=10 with invalid PL");
                            }
                        } else { // A [1-9], PL [1-9]
                            a = c0 - '0';
                            lp = c1 - '0';
                        }
                        if (a < 0 || a > 10) {
                            throw new IllegalArgumentException(
                                    "WHERE address '" + w + "' is invalid: AREA not in range [0-10]");
                        }
                        if (lp > -1) {
                            apl = true;
                            if (lp < 1 || lp > 15) {
                                throw new IllegalArgumentException(
                                        "WHERE address '" + w + "' is invalid: PL not in range [1-15]");
                            }
                        } else {
                            lp = -1; // no lp was found
                        }
                    }
                } else // 0 -> GEN
                {
                }
            }
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("invalid WHERE - NumberFormatException: " + nfe.getMessage());
        }
        // extract trailing INTERFACE, if present
        String wRest = w.substring(1);
        if (wRest != null) {
            int i = wRest.indexOf("#");
            if (i >= 0) {
                ifc = wRest.substring(i);
            }
        }

        group = g;
        area = a;
        lightPoint = lp;
        busIfc = ifc;
        isAPL = apl;
    }

    /**
     * Returns the A (area) part for this WHERE address
     *
     * @return area number or -1 if no area is present
     */
    public int getArea() {
        return area;
    }

    /**
     * Returns the PL (light point) part for this WHERE address
     *
     * @return PL number or -1 if no PL is present
     */
    public int getPL() {
        return lightPoint;
    }

    /**
     * Returns the GR (group) for this WHERE address
     *
     * @return GR number or -1 if no GR is present
     */
    public int getGroup() {
        return group;
    }

    public String getBUSIfc() {
        return busIfc;
    }

    /**
     * Returns true if WHERE is addressing a specific light/automation point (APL combination)
     *
     * @return true if is APL
     */
    public boolean isAPL() {
        return isAPL;
    }

    /**
     * Returns true if the WHERE address is an GR (group) address, false otherwise
     *
     * @return true if the WHERE address is an GR (group) address
     */
    public boolean isGroup() {
        return !isAPL && group != -1;
    }

    /**
     * Returns true if the WHERE address is an A (area) address, false otherwise
     *
     * @return true if the WHERE address is an A (area) address
     */
    public boolean isArea() {
        return !isAPL && area != -1;
    }

    /**
     * Returns true if the WHERE address is the GEN (general) address, false otherwise
     *
     * @return true if the WHERE address is the GEN (general) address
     */
    public boolean isGeneral() {
        return !isAPL && group == -1 && area == -1; // GEN if it's not APL/A/G
    }

}
