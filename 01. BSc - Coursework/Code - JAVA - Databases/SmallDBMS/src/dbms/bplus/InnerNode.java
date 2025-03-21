package dbms.bplus;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * This class implements the inner node of a B+ tree.
 */

public class InnerNode extends Node implements Serializable {
	/**
	 * The autogenerated serialVersionUID.
	 */
	private static final long serialVersionUID = -8020134692152768209L;

	/**
	 * Contains the keys of the inner node.
	 */
	private LinkedList<Integer> keys;

	/**
	 * Contains the children-nodes of the Inner Node.
	 */
	private LinkedList<Node> children;

	/**
	 * The bucket factor of the B+ Tree. We need this in
	 * order to know the minimum and maximum keys and chilren that we are allowed to
	 * have for our inner nodes.
	 */
	private final int bucketFactor;

	/**
	 * Creates a node with the specified <code>bucketFactor</code>.
	 * @param bucketFactor The value of the bucket factor for the B+ Tree.
	 */
	public InnerNode(final int bucketFactor) {
		super(NodeType.INNER_NODE);
		this.bucketFactor = bucketFactor;
		this.keys = new LinkedList<Integer>();
		this.children = new LinkedList<Node>();
	} // end method InnerNode

	/**
	 * Returns the value of the key with index equal to <code>keyIndex</code>
	 * 
	 * @param keyIndex The index of the key.
	 * @return The value of the key.
	 */
	public int getKey(final int keyIndex) {
		return this.keys.get(keyIndex);
	} // end method getKey

	/**
	 * It replaces the key with index <code>keyIndex</code> inside the
	 * <code>keys</code> field with <code>newKey</code>.
	 * 
	 * @param keyIndex 
	 *            The index of the key.
	 * @param newKey 
	 *            The value of the key.
	 */
	public void setKey(final int keyIndex, final int newKey) {
		this.keys.set(keyIndex, newKey);
	} // end method setKey

	/**
	 * Inserts a new key into the inner node in such a position so as to keep the
	 * elements inside the inner node sorted.
	 * 
	 * @param newKey 
	 *            The new key (value).
	 * @param child 
	 *            The child-node to which the new key will lead us.
	 */
	public void insertKey(final int newKey, final Node child) {
		// ������������ ����������� ���������� �� ����� �� �� ������
		// insertRecord ��� ������ Page, ����� ��� �� ������ ��� ���������
		// �������� ��� 0 ��� bucketFactor, ��� ���� �������������� �� ������
		// ��� ��������� ����� ������� ��� ��� �� bucketFactor.
		int i;
		for (i = 0; i < this.keys.size(); i++) {
			if (newKey < this.keys.get(i)) {
				this.keys.add(i, newKey);
				this.children.add(i + 1, child);
				break;
			} // end if
		} // end for
		if (i == this.keys.size()) {
			this.keys.add(newKey);
			this.children.add(child);
		} // end if
	} // end method insertKey

	/**
	 * Inserts a new key to the last position of the inner node.
	 * 
	 * @param newKey 
	 *            The new key (value).
	 * @param child 
	 *            The child-node to which the new key will lead us.
	 */
	public void addLast(final int newKey, final Node child) {
		this.keys.addLast(newKey);
		this.children.addLast(child);
	} // end method addLast

	/**
	 * Inserts a new key to the first position of the inner node.
	 * 
	 * @param key 
	 *            The new key (value).
	 * @param child 
	 *            The child node to which the new key will lead us.
	 */
	public void addFirst(final int key, final Node child) {
		this.keys.addFirst(key);
		this.children.addFirst(child);
	} // end method addFirst

	/**
	 * Removes the key with <code>index</code> from the inner node.
	 * 
	 * @param index 
	 *            The index of the key which is about to be deleted.
	 * @return The key which was deleted.
	 */
	public int removeKey(final int index) {
		return this.keys.remove(index);
	} // end method removeKey

	/**
	 * Removes the child with <code>childIndex</code> from the inner
	 * node.
	 * 
	 * @param childIndex 
	 *            The index of the child node which is about to be deleted.
	 * @return The child-node which was deleted.
	 */
	public Node removeChild(final int childIndex) {
		return this.children.remove(childIndex);
	} // end method removeChild

	/**
	 * Retrieves the key with <code>keyIndex</code> from the inner node.
	 * 
	 * @param keyIndex 
	 *            The index of the key which is about to be deleted.
	 * @return The child-node with index <code>keyIndex</code>.
	 */
	public Node getChild(final int keyIndex) {
		return this.children.get(keyIndex);
	} // end method getChild

	/**
	 * Inserts a child node to the inner node.
	 * 
	 * @param child 
	 *            The node which is about to be inserted to the innerNode.
	 */
	public void addLastChild(final Node child) {
		this.children.addLast(child);
	} // end method addLastChild

	/**
	 * The number of keys the inner node contains.
	 * 
	 * @return The number of keys which exist inside the inner node.
	 */
	public int getNumOfKeys() {
		return this.keys.size();
	} // end method getNumOfKeys

	/**
	 * Checks if the key with <code>keyIndex</code> is the last key into
	 * the inner node.
	 * 
	 * @param keyIndex 
	 *            The index of the key we are interested in.
	 * @return <code>true</code> if the key is indeed the last one and
	 *         <code>false</code> otherwise.
	 */
	public boolean isLastKeyEntry(final int keyIndex) {
		return keyIndex == this.keys.size();
	} // end method isLastKeyEntry

	/**
	 * Finds the index of the node <code>child</code>
	 * 
	 * @param child
	 *            The child-node of which the index we want to find
	 * @return The index of the node <code>child</code>.
	 */
	public int findIndexOfChild(Node child) {
		int index = 0;
		while (this.children.get(index) != child
				&& index < this.children.size()) {
			index++;
		} // end while
		return index;
	} // end method findIndexOfChild

	/**
	 * Checks if the inner node is replete or not.
	 * 
	 * @return <code>true</code> If the inner node is replete and
	 *         <code>false</code> otherwise.
	 */
	public boolean isReplete() {
		return this.keys.size() > this.bucketFactor;
	} // end method isReplete
} // end class InnerNode
