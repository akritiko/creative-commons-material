package dbms.exececution;

/**
 * This exception is thrown whenever we try to create a table and try to set the Primary Key
 * of a column that does not exist in the table. It is also thrown whenever we try to set a
 * Primary Key that is not of type integer.
 */
public class InvalidPrimaryKeyException extends Exception
{
	
	/**
	 * The autogenerated serialVersionUID.
	 */
	private static final long serialVersionUID = -4813456383203261557L;

	/**
	 * @param message The message of the exception.
	 */
	public InvalidPrimaryKeyException(String message)
	{
		super(message);
	}

}
