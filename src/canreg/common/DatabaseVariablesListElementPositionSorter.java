package canreg.common;

import java.util.Comparator;

/**
 *
 * @author ervikm
 */
public class DatabaseVariablesListElementPositionSorter implements Comparator<DatabaseVariablesListElement> {

    public int compare(DatabaseVariablesListElement o1, DatabaseVariablesListElement o2) {
        if (o1.getYPos() > o2.getYPos()) {
            return 1;
        } else if (o1.getYPos() < o2.getYPos()) {
            return -1;
        } else {
            if (o1.getXPos() > o2.getXPos()) {
                return 1;
            } else {
                return -1;
            }
        }
    }
}
