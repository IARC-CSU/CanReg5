package canreg.common;

import java.io.Serializable;

/**
 *
 * @author ervikm
 */
public class DatabaseGroupsListElement implements Serializable, DatabaseElement {
    private String groupName;
    private int groupIndex;
    private int groupPosition;

    public DatabaseGroupsListElement(String groupName, int index, int position) {
        this.groupName = groupName;
        this.groupIndex = index;
        this.groupPosition = position;
    }

    /**
     * 
     * @return
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * 
     * @param groupName
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * 
     * @return
     */
    public int getGroupIndex() {
        return groupIndex;
    }

    /**
     * 
     * @param groupIndex
     */
    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    /**
     * 
     * @return
     */
    public int getGroupPosition() {
        return groupPosition;
    }

    /**
     * 
     * @param groupPosition
     */
    public void setGroupPosition(int groupPosition) {
        this.groupPosition = groupPosition;
    }

    @Override
    public String toString(){
        return groupName;
    }
}
