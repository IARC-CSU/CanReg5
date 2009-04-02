package canreg.common;

/**
 *
 * @author ervikm
 */
public interface CalendarCanReg {
     /**
     * @return the unkownMonth
     */
    public abstract boolean isUnknownMonth();

    /**
     * @param unkownMonth the unkownMonth to set
     */
    public abstract void setUnkownMonth(boolean unkownMonth);

    /**
     * @return the unknownDay
     */
    public abstract boolean isUnknownDay();

    /**
     * @param unknownDay the unknownDay to set
     */
    public abstract void setUnknownDay(boolean unknownDay);

    /**
     * @return the unknownYear
     */
    public abstract boolean isUnknownYear();

    /**
     * @param unknownYear the unknownYear to set
     */
    public abstract void setUnknownYear(boolean unknownYear);
}
