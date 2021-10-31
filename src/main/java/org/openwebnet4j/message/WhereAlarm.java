package org.openwebnet4j.message;

/**
 * WHERE for Alarm frames
 * <p>
 * == Where Table:
 *
 * - empty : GENERIC (SYSTEM)
 * - 1 : CONTROL PANEL
 * - #Z : zone Z [0-8] via Central Unit or special Zone C (#12) or Zone F (#15)
 * - Zs : zone Z [0-8] sensor s
 *
 * Zone 0 is for inputs and the 3 internal sirens
 * Zone C (zone 12) is a special zone comprising: power feeder, external sirens, mechanical key, communicator
 *
 * @author M. Valla - Initial contribution
 */

public class WhereAlarm extends Where {
    public static final Where SYSTEM = new WhereAlarm("");
    private final int zone;
    private final int sensor;

    public WhereAlarm(String w) throws IllegalArgumentException, NumberFormatException {
        super(w);
        if (w == "") {
            zone = -1;
            sensor = -1;
        } else {
            int z = -1, s = -1;
            int pos = whereStr.indexOf("#");
            if (pos >= 0) { // # is present
                z = Integer.parseInt(whereStr.substring(1));
            } else { // no # present
                z = Integer.parseInt(whereStr.substring(0, 0));
                if (whereStr.length() > 1) {
                    s = Integer.parseInt(whereStr.substring(1));
                }
            }
            if ((z <= 8 && z >= 0) || z == 12 || z == 15) {
                zone = z;
            } else {
                throw new IllegalArgumentException(
                        "WHERE address '" + w + "' is invalid: zone not in range [0-8,12,15]");
            }
            sensor = s;
        }
    }

    /**
     * Returns the Zone for this WHERE
     *
     * @return Integer Zone (0-8) for this WHERE or null if no zone is present
     */
    public Integer getZone() {
        if (zone > -1) {
            return zone;
        } else {
            return null;
        }
    }

    /**
     * Returns the Sensor for this WHERE
     *
     * @return Integer Sensor for this WHERE or null if no sensor is present
     */
    public Integer getSensor() {
        if (sensor > -1) {
            return sensor;
        } else {
            return null;
        }
    }
}
