package dbms.exececution;

/**
 * This exception is thrown whenever we try to create a record which exceeds
 * Main.Page_Size, the limit we have set of a page size, typically, at 512 bytes.
 */

public class EnormousRecordException extends Exception
{
	
	/**
	 * The autogenerated serialVersionUID.
	 */
	private static final long serialVersionUID = 4095672477147331475L;
	
	/**
	 * @param message The message of the exception.
	 */
	
	public EnormousRecordException(String message)
	{
		super(message);
	}
}
