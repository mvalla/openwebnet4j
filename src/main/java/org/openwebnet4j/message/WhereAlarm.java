package org.openwebnet4j.message;

/**
 * WHERE for Alarm frames
 *
 * @author M. Valla - Initial contribution
 */

public class WhereAlarm extends Where {
    public static final Where SYSTEM = new WhereAlarm("");

    public WhereAlarm(String w) throws NullPointerException {
        // TODO check range for WHERE
        super(w);
    }
}
