package org.openwebnet4j.message;
/**
 * WHERE for Auxiliary frames
 *
 *
 * @author G.Fabiani- Initial contribution
 */

public class WhereAuxiliary extends Where{
    public static final Where GENERAL = new WhereAuxiliary("0");
    public WhereAuxiliary(String w) throws NullPointerException {
        // TODO check range for WHERE
        super(w);
    }
}
