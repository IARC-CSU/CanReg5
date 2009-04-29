package canreg.client.analysis;

/**
 * <p>Title: CI5-IX tools</p>
 *
 * <p>Description: Various tools for CI5-IX</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: IARC-DEP</p>
 *
 * @author Morten Johannes Ervik
 * @version 1.0
 */

class FieldDescription {
    public String name;
    public String description;
    public String contentType;
    public int offset;
    public int characters;
    public FieldDescription(String name) {
        this.name = name;
    }
}
