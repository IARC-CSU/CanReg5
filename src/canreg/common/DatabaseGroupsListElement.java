package canreg.common;

/**
 *
 * @author ervikm
 */
public class DatabaseGroupsListElement {
    private String groupName;
    private int groupIndex;
    private int groupPosition;

    DatabaseGroupsListElement(String groupName, int index) {
        this.groupName = groupName;
        this.groupIndex = index;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
    }

    public int getGroupPosition() {
        return groupPosition;
    }

    public void setGroupPosition(int groupPosition) {
        this.groupPosition = groupPosition;
    }
}
