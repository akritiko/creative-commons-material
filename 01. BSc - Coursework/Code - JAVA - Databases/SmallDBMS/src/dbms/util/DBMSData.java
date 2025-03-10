package dbms.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Holds description of the data that our DBMS handles.
 */
public class DBMSData implements Serializable {
	/**
	 * The autogenerated serial version UID.
	 */
	private static final long serialVersionUID = 1924308465733169906L;

	/**
	 * The tables of our DB.
	 */
	public ArrayList<Table> tables;

	/**
	 * The table names of our DB.
	 */
	public ArrayList<String> tableNames;

	/**
	 * The corresponding table for a secondary index name.
	 */
	public Hashtable<String, Table> indexNameToTable;

	/**
	 * Creates the object by assigning the values given via the parameters
	 * to the fields of the class.
	 * 
	 * @param tables
	 *            The tables.
	 * @param tableNames
	 *            The names of tables.
	 * @param indexNameToTable
	 *            The table names indexed to tables.
	 */
	public DBMSData(ArrayList<Table> tables, ArrayList<String> tableNames,
			Hashtable<String, Table> indexNameToTable) {
		this.tables = tables;
		this.tableNames = tableNames;
		this.indexNameToTable = indexNameToTable;
	}

	/**
	 * @return Returns the <code>indexNameToTable</code>.
	 */
	public Hashtable<String, Table> getIndexNameToTable() {
		return indexNameToTable;
	}

	/**
	 * @return Returns the <code>tableNames</code>.
	 */
	public ArrayList<String> getTableNames() {
		return tableNames;
	}

	/**
	 * @return Returns the <code>tables</code>.
	 */
	public ArrayList<Table> getTables() {
		return tables;
	}

}
