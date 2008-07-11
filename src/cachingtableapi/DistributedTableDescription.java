package cachingtableapi;

import java.io.Serializable;

/**
 * Class that holds all the descriptive data for a table.
 * @author Jeremy Dickson, 2003.
 */
public class DistributedTableDescription implements Serializable {

	private String[] columnNames;
	private Class[] columnClasses;
	private int rowCount;

	/**
	 * Constructor for DistributedTableDescription.
	 * @exception If the columnNames or columnClasses array are null or of differing
	 * lengths.
	 */	
	public DistributedTableDescription(String[] columnNames, Class[] columnClasses, int rowCount) throws Exception {
		if(columnNames == null || columnClasses == null || columnNames.length != columnClasses.length) {
			throw new Exception("Either the columnNames array or the columnClasses array is null or the lengths of the arrays are not equal.");	
		}
		this.columnNames = columnNames;
		this.columnClasses = columnClasses;	
		this.rowCount = rowCount;
	}

	/**
	 * Returns an array of the column names.
	 */
	public String[] getColumnNames() {
		return columnNames;		
	}
	
	/**
	 * Returns an array of the column classes.
	 */
	public Class[] getColumnClasses() {
		return columnClasses;	
	}
	
	/**
	 * Returns the row count.
	 */
	public int getRowCount() {
		return rowCount;	
	}
	
	/**
	 * Returns the column count.
	 */
	public int getColumnCount() {
		return columnNames.length;	
	}
}
