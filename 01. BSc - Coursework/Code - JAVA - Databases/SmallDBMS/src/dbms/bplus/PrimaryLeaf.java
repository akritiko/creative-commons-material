package dbms.bplus;

import java.io.Serializable;

/**
 * Implements a leaf which is going to be used by a <code>PrimaryBPlusTree</code>.
 */
public class PrimaryLeaf extends Node implements Serializable {

	/**
	 * The autogenerated serialVersionUID.
	 */
	private static final long serialVersionUID = 3173286168799241658L;

	/**
	 * The page number of the primary leaf.
	 */
	private final int pageNumber;

	/**
	 * The right primary leaf of this leaf.
	 */
	private PrimaryLeaf rightLeaf;

	/**
	 * the left primary leaf of this leaf.
	 */
	private PrimaryLeaf leftLeaf;

	/**
	 * Creates a primary leaf with the given parameters.
	 * Then, it assigns default values (null) to the right and left leaves.
	 * 
	 * @param pageIndex
	 *            The index of the host page.
	 */
	public PrimaryLeaf(final int pageIndex) {
		super(NodeType.PRIMARY_LEAF);
		this.pageNumber = pageIndex;
		this.leftLeaf = null;
		this.rightLeaf = null;
	} // end method PrimaryLeaf

	/**
	 * Retrieves the page number to which this leaf resides.
	 * 
	 * @return The <code>page number</code>.
	 */
	public int getPageNumber() {
		return this.pageNumber;
	} // end method getPageNumber

	/**
	 * Retrieves the right leaf of this leaf.
	 * 
	 * @return The PrimaryLeaf <code>rightLeaf</code>.
	 */
	public PrimaryLeaf getRightLeaf() {
		return this.rightLeaf;
	} // end method getRightLeaf

	/**
	 * Assigns the new right leaf to the field <code>rightLeaf</code>
	 * 
	 * @param rightLeaf
	 *            The new right leaf.
	 */
	public void setRightLeaf(final PrimaryLeaf rightLeaf) {
		this.rightLeaf = rightLeaf;
	} // end method setRightLeaf

	/**
	 * Retrieves the left leaf of this leaf.
	 * 
	 * @return The PrimaryLeaf <code>leftLeaf</code>.
	 */
	public PrimaryLeaf getLeftLeaf() {
		return this.leftLeaf;
	} // end method getLeftLeaf

	/**
	 * Assigns the new left leaf to the field <code>leftLeaf</code>.
	 * 
	 * @param leftLeaf
	 *            The new left leaf.
	 */
	public void setLeftLeaf(final PrimaryLeaf leftLeaf) {
		this.leftLeaf = leftLeaf;
	} // end method setLeftLeaf

	/**
	 * Checks if the primary leaf is replete or not. This class never uses this
	 * method. Though, it must be implemented because it exists to the abstract
	 * class <code>Node</code>, which this class extends.
	 * 
	 * @return <code>false</code> (always)
	 */
	public boolean isReplete() {
		// ��� ���� ��� ����� ��� ��������������� ����.
		return false;
	} // end method isReplete
} // end class PrimaryLeaf
