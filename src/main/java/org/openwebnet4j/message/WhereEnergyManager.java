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

/**
 * WHERE for Energy Manager frames
 *
 * == Where Table:
 *
 * 1N     N=[1-127]   Stop & Go
 * 5N     N= [1-255]  Energy Management Central Unit, Pulse Counter, Power Meter: Bticino reference: F520, F523, 3522. 
 *                                                                                Legrand reference:03555,03557, 03554
 * 7N#0   N= [1-255]  Energy Management Actuators: Bticino reference: F522, F523 
 *                                                 Legrand reference: 03558, 03559
 * @author M. Valla - Initial contribution
 * @author Andrea Conte - Energy manager contribution
 */
public class WhereEnergyManager extends Where {

    public static final Where GENERAL = new WhereEnergyManager("51");

    public WhereEnergyManager(String w) throws NullPointerException, IllegalArgumentException {
        super(w);

        try {            
            switch (w.charAt(0)) {
                case '1':
                    // check N=[1-127]  
                    int N1 = Integer.parseInt(w.substring(1, w.length()));
                    if (N1<1 || N1>127)
                        throw new IllegalArgumentException("WHERE address '" + w + "' is invalid: not in range [1-127].");
                    break;

                case '5':
                    // check N=[1-255]  
                    int N5 = Integer.parseInt(w.substring(1, w.length()));
                    if (N5<1 || N5>255)
                        throw new IllegalArgumentException("WHERE address '" + w + "' is invalid: not in range [1-255].");
                    break;

                case '7':
                    // check trailer '#0'
                    if (!w.endsWith("#0"))
                        throw new IllegalArgumentException("WHERE address '" + w + "' is invalid: missing '#0' trailer.");

                    // check N=[1-255]  
                    int N7 = Integer.parseInt(w.substring(1, w.length() - 2));
                    if (N7<1 || N7>127)
                        throw new IllegalArgumentException("WHERE address '" + w + "' is invalid: not in range [1-127].");

                    break;

                default:
                    throw new IllegalArgumentException("WHERE address '" + w + "' is invalid");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("WHERE address '" + w + "' is invalid: generic exception caught! " + e.getMessage());
        }
    }
}