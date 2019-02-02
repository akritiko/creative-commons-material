package dbms.bplus;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import dbms.util.DiskHandler;
import dbms.util.Page;
import dbms.util.Record;

/**
 * Implemenents a B+ Tree that is being used as a secondary index in our DBMS
 * System.
 */
public class SecondaryBPlusTree implements Serializable {
	/**
	 * The autogenerated serial version UID number.
	 */
	private static final long serialVersionUID = -3067354847061997555L;

	/**
	 * The root node.
	 */
	private Node root;

	/**
	 * The first leaf (<code>SecondaryLeaf</code>) of our B+ Tree.
	 */
	private SecondaryLeaf firstLeaf;

	/**
	 * The bucket factor.
	 */
	private final int bucketFactor;

	/**
	 * The ceiling of the <code>bucketFactor</code> divided by 2. It is used to
	 * define the minimum number of children a node can have in the B+ Tree.
	 */
	private final int bucketFactorDiv2;

	/**
	 * The key index.
	 */
	private final int keyIndex;

	/**
	 * The name.
	 */
	@SuppressWarnings("unused")
	private final String name;

	/**
	 * Creates the secondary B+ tree by assigning values to the <code>bucketFactor</code>
	 * field taken from the primary B+ Tree, the <code>keyIndex</code>
	 * and the <code>name</code> fields with values taken from the parameters.
	 * 
	 * @param keyIndex
	 *            The key index.
	 * @param primaryTree
	 *            The primary tree (index).
	 * @param name
	 *            The name.
	 */
	@SuppressWarnings("unchecked")
	public SecondaryBPlusTree(final int keyIndex,
			final PrimaryBPlusTree primaryTree, final String name) {
		this.bucketFactor = primaryTree.getBucketFactor();
		this.bucketFactorDiv2 = (int) Math.ceil((double) bucketFactor / 2);

		this.keyIndex = keyIndex;
		this.name = name;

		// ���������� �������� ���� ��� �������� ��� ����������� �������.
		LinkedList<Reference> references = this.createReferences(keyIndex,
				primaryTree);

		if (references.size() == 0) {
			this.firstLeaf = new SecondaryLeaf(this.bucketFactor);
			this.root = this.firstLeaf;
			return;
		} // end if

		// ���������� ��� �������� ����� ��� �������� �������� �� �����.
		Collections.sort(references, new Comparator<Reference>() {
			public int compare(Reference firstReference,
					Reference secondReference) {
				if (firstReference.getKey() < secondReference.getKey())
					return -1;
				else if (firstReference.getKey() > secondReference.getKey())
					return 1;
				else
					return 0;
			}
		});

		// ������� ��� ������ �� ��� ��������.
		LinkedList<SecondaryLeaf> allLeaves = this.fillLeaves(references);

		if (allLeaves.size() == 1) {
			this.firstLeaf = allLeaves.get(0);
			this.root = this.firstLeaf;
			return;
		} // end if

		InnerNode currentNode = new InnerNode(this.bucketFactor);

		// ��������� ��� ������� ���������� ��' �� ����� ���� �� ����.
		InnerNode newNode;
		int key;
		while (allLeaves.size() > 0) {
			// � ���������� ������ ������� �� ������� ����� �� ������������ �
			// �� ��� �������� ���� ������� ��� �� �����.
			this.fillInnerNode(currentNode, allLeaves);

			if (currentNode.getParent() == null) {
				this.root = currentNode;
			} // end if

			if (currentNode.isReplete()) {
				// �������� ��� ������ ��� �������� ��� ������� ��������
				// ���� ����� - �����.
				key = currentNode.removeKey(this.bucketFactorDiv2 - 1);

				newNode = this.splitInnerNode(currentNode);

				if (currentNode.getParent() == null) {
					// ���������� ���� �����, � ������ �� ��������� ��� ����.
					this.createNewParent(currentNode, newNode, key);
				} // end if
				else {
					// ��������� ��� ���������� ������ (����� �� ���� ��
					// ���������).
					this.reconstructInnerNodes(key, currentNode, newNode);
				} // end else

				currentNode = newNode;
			} // end if
			else if (currentNode.getNumOfKeys() < this.bucketFactorDiv2
					&& !(currentNode == this.root)) {
				// ������ � ������ ��� ���� ������ �������� ������� ��� ��������
				// ������� ��� ��� ������� ��' ����� ������� - ������. �����
				// ������� ��� � �������� ����� ����� ������ �������������, ���
				// ���� �������� ��� �� �����.

				// � ������ ��� ���������� ����� ������� �� ��������� ����� ���
				// �����, ��� ������ ��������� ��� �������� ��� �������.
				int index = currentNode.getParent().getNumOfKeys() - 2;
				InnerNode neighbourNode = (InnerNode) currentNode.getParent()
						.getChild(index);

				// �������� �������� ��� �������.
				while (currentNode.getNumOfKeys() < this.bucketFactorDiv2) {
					key = neighbourNode
							.removeKey(neighbourNode.getNumOfKeys() - 1);

					newNode = (InnerNode) neighbourNode
							.removeChild(neighbourNode.getNumOfKeys());

					currentNode.addFirst(key, newNode);
				} // end while
			} // end else
		}// end while
	} // end method SecondaryBPlusTree

	/**
	 * Creates references to the primary B+ tree which serves as the primary
	 * index.
	 * 
	 * @param keyIndex
	 *            The key index.
	 * @param primaryTree
	 *            The primary tree(index).
	 * @return The references created.
	 */
	private LinkedList<Reference> createReferences(final int keyIndex,
			final PrimaryBPlusTree primaryTree) {
		LinkedList<Reference> allReferences = new LinkedList<Reference>();

		PrimaryLeaf leaf = primaryTree.getFirstLeaf();

		Page page;
		Reference reference;
		do {
			// ������� ��� ���������� ������� ��� �����.
			page = DiskHandler.loadPage(leaf.getPageNumber(), primaryTree
					.getBucketFactor(), primaryTree.getEmptyRecordCopy());

			// ���������� �������� ��� ��� �������� ������.

			// ����� ������ � ����� <<�����>> ������� ������� �
			// ���������� �������� (��� �� ������ ����), �����
			// ���������� ��� ���� �� �������� �������� �����
			// <<������>>.
			int i = 0;
			while (i < page.getPageLength() && !page.getRecord(i).isDead()) {
				// ���������� �������� ��� ��� �������� <<�� �����>> �������.
				reference = new Reference((Integer) page.getRecord(i).getKey(
						keyIndex), leaf.getPageNumber(), i);

				// �������� ��� ���� ��� ��������.
				allReferences.add(reference);

				i++;
			} // end for

			leaf = leaf.getRightLeaf();
		} // end do
		while (leaf != null);

		return allReferences;
	} // end method createReferences

	/**
	 * Fills the leaves using the references that have been previously created.
	 * 
	 * @param references
	 *            The references to the primary tree.
	 * @return A <code>LinkedList</code> of the <code>SecondaryLeaf</code> created.
	 */
	private LinkedList<SecondaryLeaf> fillLeaves(
			final LinkedList<Reference> references) {
		// � �������� ������� �������� ������ ����������� ���� ����� ����������
		// ��� ��� �������� ��������.

		LinkedList<SecondaryLeaf> allLeaves = new LinkedList<SecondaryLeaf>();

		if (references.size() < this.bucketFactor) {
			// ������� ��� ������ �� ���� �������� ����� ���������.
			LinkedList<Reference> newLeafReferences = new LinkedList<Reference>(
					references.subList(0, references.size()));

			SecondaryLeaf leaf = new SecondaryLeaf(this.bucketFactor,
					newLeafReferences);

			// ������� ��� ������ �� ��� ����.
			allLeaves.add(leaf);
			return allLeaves;
		} // end if

		// ������� ��� ������ �� ������ �������� ��� �� bucketFactor.
		LinkedList<Reference> newLeafReferences = new LinkedList<Reference>(
				references.subList(0, this.bucketFactor));

		SecondaryLeaf leaf = new SecondaryLeaf(this.bucketFactor,
				newLeafReferences);

		// ������� ��� ������ �� ��� ����.
		allLeaves.add(leaf);

		// � �������� ������� �������� ��� ���� ������, ���� ��������
		// �������� ������ ��� ��� ��������� ��� �������.

		int lowerIndex = this.bucketFactor;
		int upperIndex = 2 * this.bucketFactor;
		while (upperIndex < references.size()) {
			// ������� ��� ������ �� ������ �������� ��� �� bucketFactor.
			newLeafReferences = new LinkedList<Reference>(references.subList(
					lowerIndex, upperIndex));

			leaf = new SecondaryLeaf(this.bucketFactor, newLeafReferences);

			// ������� ��� ������ �� ��� ����.
			allLeaves.add(leaf);

			// ��������� ��� ������� ��� ������.
			allLeaves.get(allLeaves.size() - 2).setRightLeaf(leaf);
			leaf.setLeftLeaf(allLeaves.get(allLeaves.size() - 2));

			lowerIndex = upperIndex;
			upperIndex += this.bucketFactor;
		} // end while
		if (lowerIndex < references.size()) {
			// ������� ��� ������ �� ������ �� ���� �������� ���������.
			newLeafReferences = new LinkedList<Reference>(references.subList(
					lowerIndex, references.size()));

			leaf = new SecondaryLeaf(this.bucketFactor, newLeafReferences);

			// ������� ��� ������ �� ��� ����.
			allLeaves.add(leaf);

			// ��������� ��� ������� ��� ������.
			allLeaves.get(allLeaves.size() - 2).setRightLeaf(leaf);
			leaf.setLeftLeaf(allLeaves.get(allLeaves.size() - 2));
		} // end if

		return allLeaves;
	} // end method fillLeaves

	/**
	 * Fills a node with the leaves previously created until the node is
	 * replete.
	 * 
	 * @param node
	 *            The inner node.
	 * @param allLeaves
	 *            The list with all the leaves created.
	 */
	private void fillInnerNode(final InnerNode node,
			final LinkedList<SecondaryLeaf> allLeaves) {
		SecondaryLeaf child;
		// � ������ ������� �� ������� (��� ������) ����� �� ������������!
		for (int i = 0; !node.isReplete() && allLeaves.size() > 1; i++) {
			child = allLeaves.remove(0);
			child.setParent(node);

			node.addLast(child.getLastReference().getKey(), child);
		} // end for

		// ��� �� �������� � �������� ������ �� ����������� � + 1 ������.
		// ���� ��� ����������� ��� ����� ���� ��� ��� ������.
		child = allLeaves.remove(0);
		child.setParent(node);
		node.addLastChild(child);
	} // end method fillInnerNode

	/**
	 * Creates a new parent node if there are orphan node(s).
	 * 
	 * @param firstChild
	 *            The first child.
	 * @param secondChild
	 *            The second child.
	 * @param key
	 *            Parent's key.
	 */
	private void createNewParent(final Node firstChild, final Node secondChild,
			final int key) {
		// � ���� ������ ������� ����.
		InnerNode parent = new InnerNode(this.bucketFactor);
		this.root = parent;

		// ��������� �������.
		firstChild.setParent(parent);
		secondChild.setParent(parent);

		// �������� �������� - ������� ���� ��� �����.
		parent.addFirst(key, firstChild);
		parent.addLastChild(secondChild);
	} // end method createNewParent

	/**
	 * Reconstructs inner nodes after new nodes have been created (perhaps from
	 * a split procedure). Also checks if replition takes place.
	 * 
	 * @param key
	 *            The current node's key.
	 * @param currentNode
	 *            The current node.
	 * @param newChild
	 *            The new node.
	 */

	private void reconstructInnerNodes(int key, InnerNode currentNode,
			Node newChild) {
		boolean repeat = true;
		while (repeat) {
			// �������� �������� ���� �����.
			currentNode.insertKey(key, newChild);

			// ������� ������������.
			if (currentNode.isReplete()) {
				// ���������� �� ������ ������...
				key = currentNode.removeKey(this.bucketFactorDiv2);

				// ��� ������� �������� ��� ������.
				newChild = this.splitInnerNode(currentNode);

				// ������� ��� ��� ������ �����.
				if (currentNode.getParent() == null) {
					// ���������� �����.
					createNewParent(currentNode, newChild, key);

					// ����������� ��� ����������.
					repeat = false;
				} // end if
				else {
					// � ������� ����� ���� �����.
					currentNode = currentNode.getParent();

					// ��������� �������.
					newChild.setParent(currentNode);
				} // end else
			} // end if
			else {
				// ����������� ��� ����������.
				repeat = false;
			} // end else
		} // end while
	} // end method reconstructInnerNodes

	/**
	 * Splits an inner node to two and places <code>bucketFactorDiv2</code> children to the first and
	 * the rest to the second. The parent(s) and the appropriate fields are
	 * updated too.
	 * 
	 * @param currentNode
	 *            The node to be splited.
	 * @return The new InnerNode.
	 */

	private InnerNode splitInnerNode(final InnerNode currentNode) {
		// ���� ��������� � �������� ��� ������� ������� ��� ������!

		Node child;
		InnerNode newNode = new InnerNode(this.bucketFactor);

		// �������� ��� ����� ��������� �� ���� ��� �����.
		for (int i = this.bucketFactorDiv2 + 1; i < this.bucketFactor; i++) {
			child = currentNode.removeChild(this.bucketFactorDiv2);
			// ���� � ������ ������� ����� ������ �� ���������� ��� ��
			// ���������� ����� ���.
			child.setParent(newNode);
			newNode
					.addLast(currentNode.removeKey(this.bucketFactorDiv2),
							child);
		} // end for

		child = currentNode.removeChild(this.bucketFactorDiv2);
		child.setParent(newNode);
		newNode.addLastChild(child);

		return newNode;
	} // end method splitInnerNode

	/**
	 * Searches for a secondary leaf using its key. When the secondary leaf is
	 * found it is returned.
	 * 
	 * @param key
	 *            The key of the primary leaf-target.
	 * @return The desired <code>SecondaryLeaf</code>.
	 */
	private SecondaryLeaf search(final int key) {
		// ���������� ��' �� ����...
		Node currentNode = this.root;

		// ����������� �� ������ ����� �� ������ �� ����� �� �� ���������
		// ������.
		int index = 0;
		while (currentNode.getNodeType() == NodeType.INNER_NODE
				&& index <= this.bucketFactor) {
			InnerNode currentInnerNode = (InnerNode) currentNode;
			if (currentInnerNode.isLastKeyEntry(index)
					|| key <= currentInnerNode.getKey(index)) {
				currentNode = currentInnerNode.getChild(index);
				index = 0;
			} // end if
			else {
				// �� �� ��������� ������ ���� ���� ���������� ��' ���� ���
				// ����������, ���� ���������� ���� ������� ��� ��������.
				index++;
			} // end else
		}// end while
		return (SecondaryLeaf) currentNode;
	} // end method search

	/**
	 * Inserts a new record to the appropriate position inside the Secondary B+
	 * Tree.
	 * 
	 * @param reference
	 *            The reference to the leaf that the insertion must take place.
	 */

	public void insert(final Reference reference) {
		// ����������� �� ����� ��� ����� �� ����� � ��������.
		SecondaryLeaf currentLeaf = this.search(reference.getKey());

		// ���������������� � �������� ��� �����.
		currentLeaf = currentLeaf.insertReference(reference);

		if (currentLeaf.isReplete()) {
			// ��� �������� �����������, ���� ������� �������� ��� ������...
			SecondaryLeaf newNode = this.splitLeaf(currentLeaf);

			// ��� ��������� ��� ���������.
			this.reconstructTree(currentLeaf, newNode);
		} // end if
	} // end method insert

	/**
	 * Splits a secondary leaf to two and adjusts the connections of the children-nodes
	 * with their neighbours.
	 * 
	 * @param currentLeaf
	 *            The current secondary leaf.
	 * @return The new leaf.
	 */

	private SecondaryLeaf splitLeaf(final SecondaryLeaf currentLeaf) {
		SecondaryLeaf newLeaf = new SecondaryLeaf(this.bucketFactor);

		// �������� ��� ����� �������� ��� ��� �����.
		for (int i = this.bucketFactorDiv2; i < this.bucketFactor; i++) {
			// ��������� ��� �������� ��� ��� ������.
			newLeaf.setReferenceLast(currentLeaf.getReference(
					this.bucketFactorDiv2).clone());

			// �������� ��� ������� ��� ������� �������.
			currentLeaf.deleteReference(this.bucketFactorDiv2);
		} // end for

		// � ��������� ������� ����������, ����� �� �������������� ��� ��� ����.
		// �� ���� ��� ����� ������������� ��� � ������ �������� ������ ��������
		// ��� �� bucketFactor.
		newLeaf.setReferenceLast(currentLeaf
				.removeReference(this.bucketFactorDiv2));

		// ��������� ��� ������� ��� ������ ���� �� ��������� ����.
		SecondaryLeaf thirdLeaf = currentLeaf.getRightLeaf();

		newLeaf.setRightLeaf(thirdLeaf);
		currentLeaf.setRightLeaf(newLeaf);

		if (thirdLeaf != null) {
			thirdLeaf.setLeftLeaf(newLeaf);
		} // end if
		newLeaf.setLeftLeaf(currentLeaf);

		return newLeaf;
	} // end method splitPage

	/**
	 * Reconstructs the tree in order to adjust it to changes that may have
	 * happened (e.g. after secondary leaf's split).
	 * 
	 * @param currentLeaf
	 *            The current leaf.
	 * @param newLeaf
	 *            The new leaf.
	 */

	private void reconstructTree(final SecondaryLeaf currentLeaf,
			final SecondaryLeaf newLeaf) {
		// ������� ��� ������� ������ ��� ��� �������� �����.
		if (currentLeaf.getParent() == null) {
			// ���������� ����� (������� �������� ����).
			this.createNewParent(currentLeaf, newLeaf, currentLeaf
					.getLastReference().getKey());
		} // end if
		else {
			// ��������� ����� ��� �������.
			InnerNode currentNode = currentLeaf.getParent();

			newLeaf.setParent(currentNode);
			Node newNode = newLeaf;

			// ��������� ��� ���������.
			this.reconstructInnerNodes(currentLeaf.getLastReference().getKey(),
					currentNode, newNode);
		} // end else
	} // end method reconstructTree

	/**
	 * Deletes a record (the one with key equal to the one given in the parameter)
	 * and creates references to the elements of the primary tree that must be
	 * deleted.
	 * 
	 * @param secondaryKey
	 *            the desired key
	 * @return A <code>Result [2]</code> array which contains: <br>
	 *         <ul>
	 *         <li>A <code>boolean</code> variable to verify the success of the
	 *         search.
	 *         <li>A <code>LinkedList</code> object with the references of
	 *         the records that must be deleted.
	 *         </ul>
	 */

	public Object[] delete(final int secondaryKey) {
		// ��� �������� ���� ������������� �������� - ������ �� ������� ���
		// ����� �������� ������ �� ���������� ������������ ��� ���� ��������.

		Object[] result = new Object[2];
		LinkedList<Reference> deleted = new LinkedList<Reference>();

		// ����������� �� ����� ��' �� ����� �� ���������� �� ��������� ���
		// �������� - ��� ������� �� ������ ����.
		SecondaryLeaf leaf = this.search(secondaryKey);

		// � �������� ������ �� ��������� �� ����������� ��� ���� �����.
		boolean stop = false;
		int index;
		while (leaf != null && !stop) {
			index = 0;
			while (index < leaf.getNumOfReferences()) {
				if (leaf.getReference(index).getKey() == secondaryKey) {
					// ������������ ��� ������� ��� �������� ��� �� ������ ��
					// ��������� ��' ��� ���������� ��������.
					deleted.add(leaf.getReference(index));

					// �������� ��� ��������.
					leaf.deleteReference(index);
				} // end if
				else if (leaf.getReference(index).getKey() > secondaryKey) {
					// ��� �������� ����� � ������� �������� ���� ��� �� ������
					// ��� ������� ���� ��� ��� ������� ������ �� ����������
					// �����������, ����� ��� ���������� �� ����������� ���
					// ��������� ��������.
					stop = true;
					break;
				} // end else
				else {
					// ���������� ���� ������� �������.
					index++;
				} // end else
			} // end for

			// ���������� �� ������� �����.
			leaf = leaf.getRightLeaf();
		} // end while

		// � ������� ���������� ��� �����, ��� ����������� ��� ��� ������ ���
		// ����������� ��� �������� � ��� �������� ��� ��������� ��� ��� �����
		// �� ��� �������� ��� �������� ��� ������ �� ����������.
		result[0] = deleted.size() == 0 ? false : true;
		result[1] = deleted;

		return result;
	} // end method delete

	/**
	 * Same as the <code>delete</code> method except here we delete all the
	 * records with key greater or equal to the one given to the parameter.
	 * 
	 * @param secondaryKey
	 *            The desired key.
	 * @return A <code>Result [2]</code> array which contains: <br>
	 *         <ul>
	 *         <li>A <code>boolean</code> variable to verify the success of the
	 *         search
	 *         <li>A <code>LinkedList</code> object with the references of
	 *         the records that must be deleted.
	 *         </ul>
	 */

	public Object[] deleteAllGreaterOrEqual(final int secondaryKey) {
		// ��� �������� ���� ��� ������������� �������� �� ���� ���������� � ���
		// ��' ���� ��� ������.

		Object[] result = new Object[2];
		LinkedList<Reference> deleted = new LinkedList<Reference>();

		// ��������� ��� ������ ��� ���������� �� ���� �� ������.
		SecondaryLeaf leaf = this.search(secondaryKey);

		// ������ �� ������� ����� ������������, ��������� �� ������ ��� ���
		// ����������...
		int index = 0;
		while (index < leaf.getNumOfReferences()
				&& leaf.getReference(index).getKey() < secondaryKey) {
			// ��� � ������� ��� ���� �� ������ ��� �������, ���� ����������
			// ���� �������.
			index++;
		} // end while

		// ��� ��� �������� ����������� ���� ��� ��� ���������� ����� ��' ����.
		for (int i = leaf.getNumOfReferences() - 1; i > index - 1; i--) {
			// ������������ ��� ������� ��� �������� ��� �� ������ ��
			// ��������� ��' ��� ���������� ��������.
			deleted.add(leaf.getReference(i));

			// �������� ��� ��������.
			leaf.deleteReference(i);
		} // end for

		// ���� ����������� ���� ��� �������� �� ��� �� ����� ��� ����������
		// ����� ��� ������������.
		leaf = leaf.getRightLeaf();

		while (leaf != null) {
			for (int i = leaf.getNumOfReferences() - 1; i > -1; i--) {
				// ������������ ��� ������� ��� �������� ��� �� ������ ��
				// ��������� ��' ��� ���������� ��������.
				deleted.add(leaf.getReference(i));

				// �������� ��� ��������.
				leaf.deleteReference(i);
			} // end for

			// ���������� �� ������� �����.
			leaf = leaf.getRightLeaf();
		} // end while

		// � ������� ���������� ��� �����, ��� ����������� ��� ��� ������ ���
		// ����������� ��� �������� � ��� �������� ��� ��������� ��� ��� �����
		// �� ��� �������� ��� �������� ��� ������ �� ����������.
		result[0] = deleted.size() == 0 ? false : true;
		result[1] = deleted;

		return result;
	} // end method deleteAllGreaterOrEqual

	/**
	 * Same as the <code>delete</code> method except that it deletes all the records
	 * with keys less or equal to <code>secondaryKey/</code>.
	 * 
	 * @param secondaryKey
	 *            Tthe desired key.
	 * @return A <code>Result [2]</code> array which contains: <br>
	 *         <ul>
	 *         <li>A <code>boolean</code> variable to verify the success of the
	 *         search.
	 *         <li>A <code>LinkedList</code> object with the references of
	 *         the records that must be deleted.
	 *         </ul>
	 */
	public Object[] deleteAllLessOrEqual(final int secondaryKey) {
		// ��� �������� ���� ��� ������������� �������� �� ���� ��������� � ���
		// ��' ���� ��� ������.

		Object[] result = new Object[2];
		LinkedList<Reference> deleted = new LinkedList<Reference>();

		// ��������� ��� ������ ��� ���������� �� ���� �� ������.
		SecondaryLeaf leaf = this.search(secondaryKey);

		// ������ �� ������� ����� ������������, ��������� �� ������ ��� ���
		// ����������...
		int index = leaf.getNumOfReferences() - 1;
		while (index > -1 && leaf.getReference(index).getKey() > secondaryKey) {
			// ��� � ������� ��� ���� �� ������ ��� �������, ���� ����������
			// ���� �������.
			index--;
		} // end while

		// ��� ��� �������� ����������� ���� ��� ��� ���������� �������� ��'
		// ����.
		for (int i = index; i > -1; i--) {
			// ������������ ��� ������� ��� �������� ��� �� ������ ��
			// ��������� ��' ��� ���������� ��������.
			deleted.add(leaf.getReference(i));

			// �������� ��� ��������.
			leaf.deleteReference(i);
		} // end for

		// ���� ����������� ���� ��� �������� �� ��� �� ����� ��� ����������
		// �������� ��� ������������.
		leaf = leaf.getLeftLeaf();
		while (leaf != null) {
			for (int i = leaf.getNumOfReferences() - 1; i > -1; i--) {
				// ������������ ��� ������� ��� �������� ��� �� ������ ��
				// ��������� ��' ��� ���������� ��������.
				deleted.add(leaf.getReference(i));

				// �������� ��� ��������.
				leaf.deleteReference(i);
			} // end for

			// ���������� �� ������� �����.
			leaf = leaf.getLeftLeaf();
		} // end while

		// � ������� ���������� ��� �����, ��� ����������� ��� ��� ������ ���
		// ����������� ��� �������� � ��� �������� ��� ��������� ��� ��� �����
		// �� ��� �������� ��� �������� ��� ������ �� ����������.
		result[0] = deleted.size() == 0 ? false : true;
		result[1] = deleted;

		return result;
	} // end method deleteAllLessOrEqual

	/**
	 * It is used to delete records after a deletion made in a primary index.
	 * There is a possibility that we may have a value that spans over multiple leaves.
	 * This method checks these leaves too and if such values exist it deletes them also.
	 * 
	 * @param deleted
	 *            The list with the references that were deleted from the primary
	 *            index.
	 */
	public void delete(final LinkedList<Object[]> deleted) {
		// ��� ��������� ��� ������������� ��������� ���� ��� �������� ����
		// ���������� ��������.

		// ������������ �� �������� ��� �������� ���� �������� ��� ��� ������
		// ��� �������� � �����.

		SecondaryLeaf leaf;
		boolean stop;

		// ��� ���� �������� ��� ������ ����������� ��� ���������� �������.
		for (int i = 0; i < deleted.size(); i++) {
			leaf = this.search((Integer) ((Record) deleted.get(i)[0])
					.getKey(this.keyIndex));

			stop = false;

			// ������� ��������� �� �������� ���� ������ ������ ����� ��� ��
			// �������������� ��� �� ������� <<�������>> ��� ������� �����. ����
			// �� ������ �� �� ��������� ��� ����, ����� � ������� ���
			// ���������� ������ �� ��������� ����.
			while (leaf != null && !stop) {
				// ��� ���������� ���� �� �������� ��� ������.
				int index = 0;
				while (index < leaf.getNumOfReferences()) {
					if (((Reference) deleted.get(i)[1]).getPageNumber() == leaf
							.getReference(index).getPageNumber()
							&& ((Reference) deleted.get(i)[1]).getOffset() == leaf
									.getReference(index).getOffset()) {
						// �������� ��� ��������.
						leaf.deleteReference(index);
						stop = true;
						break;
					} // end if

					// ��� � ������� ��� ������� ��� ������ ��� ��� ���� ���
					// �������, ���� ���������� ���� �������.
					index++;
				} // end while

				// ��������� �� ������� �����.
				leaf = leaf.getRightLeaf();
			} // end while
		} // end for
	} // end mehtod delete

	/**
	 * Finds a record (the one with secondary key equal to the one given in the
	 * parameter) and returns it.
	 * 
	 * @param secondaryKey
	 *            The desired key.
	 * @return A <code>Result [2]</code> array which contains: <br>
	 *         <ul>
	 *         <li>A <code>boolean</code> variable to verify the success of the
	 *         search process.
	 *         <li>A <code>LinkedList</code> object with the references that
	 *         must be retrieved.
	 *         </ul>
	 */

	public Object[] find(final int secondaryKey) {
		// ��� ������ ���� ������������� �������� - ������ �� ������� ��� ���
		// ����� �������� ������ �� ����������� ������������ ��� ���� ��������.

		Object[] result = new Object[2];
		LinkedList<Reference> selected = new LinkedList<Reference>();

		// ����������� �� ����� ��' �� ����� �� ���������� � ���������� �������
		// ��� �������� - ��� ������� �� ������ ����.
		SecondaryLeaf leaf = this.search(secondaryKey);

		// � ���������� ������� ������ �� ��������� �� ����������� ��� ����
		// �����.
		boolean stop = false;
		int index;
		while (leaf != null && !stop) {
			index = 0;
			while (index < leaf.getNumOfReferences()) {
				if (leaf.getReference(index).getKey() == secondaryKey) {
					// ������������ ��� ������� ��� �������� ��� �� ������ ��
					// ������ ���� ���������� ��������.
					selected.add(leaf.getReference(index));
				} // end if
				else if (leaf.getReference(index).getKey() > secondaryKey) {
					// ��� �������� ����� � ������� �������� ���� ��� �� ������
					// ��� ������� ���� ��� ��� ������� ������ �� �������
					// ��������, ����� ��� ���������� �� ����������� ���
					// ��������� ��������.
					stop = true;
					break;
				} // end else

				// ���������� ���� ������� �������.
				index++;
			} // end for

			// ���������� �� ������� �����.
			leaf = leaf.getRightLeaf();
		} // end while

		// � ������� ���������� ��� �����, ��� ����������� ��� ��� ������ ���
		// ����������� ��� �������� � ��� �������� ��� ������� ��� ��� �����
		// �� ��� �������� ��� �������� ��� ������ �� �������.
		result[0] = selected.size() == 0 ? false : true;
		result[1] = selected;

		return result;
	} // end method find

	/**
	 * Same as the <code>find</code> method except here we search for all the
	 * records with secondary key greater or equal to <code>secondaryKey</code>.
	 * 
	 * @param secondaryKey
	 *            The desired key.
	* @return A <code>Result [2]</code> array which contains: <br>
	 *         <ul>
	 *         <li>A <code>boolean</code> variable to verify the success of the
	 *         search process.
	 *         <li>A <code>LinkedList</code> object with the references that
	 *         must be retrieved.
	 *         </ul>
	 */
	 
	public Object[] findAllGreaterOrEqual(final int secondaryKey) {
		// ��� ������ ���� ��� ������������� �������� �� ���� ���������� � ���
		// ��' ���� ��� ������.

		Object[] result = new Object[2];
		LinkedList<Reference> selected = new LinkedList<Reference>();

		// ��������� ��� ������ ��� ���������� �� ���� �� ������.
		SecondaryLeaf leaf = this.search(secondaryKey);

		// ������ �� ������� ����� ������������, ��������� �� ������ ��� ���
		// ����������...
		int index = 0;
		while (index < leaf.getNumOfReferences()
				&& leaf.getReference(index).getKey() < secondaryKey) {
			// ��� � ������� ��� ���� �� ������ ��� �������, ���� ����������
			// ���� �������.
			index++;
		} // end while

		// ��� ��� �������� �������� ��� ���������� ����� ��' ����.
		for (int i = leaf.getNumOfReferences() - 1; i > index - 1; i--) {
			// ������������ ��� ������� ��� �������� ��� �� ������ ��
			// ������ ���� ���������� ��������.
			selected.addFirst(leaf.getReference(i));
		} // end for

		// ���� �������� ���� ��� �������� �� ���� ��� ������� ��� ����������
		// ����� ��� ������������.
		leaf = leaf.getRightLeaf();
		while (leaf != null) {
			for (int i = 0; i < leaf.getNumOfReferences(); i++) {
				// ������������ ��� ������� ��� �������� ��� �� ������ ��
				// ������ ���� ���������� ��������.
				selected.addLast(leaf.getReference(i));
			} // end for

			// ���������� �� ������� �����.
			leaf = leaf.getRightLeaf();
		} // end while

		// � ������� ���������� ��� �����, ��� ����������� ��� ��� ������ ���
		// ����������� ��� �������� � ��� �������� ��� ������� ��� ��� �����
		// �� ��� �������� ��� �������� ��� ������ �� �������.
		result[0] = selected.size() == 0 ? false : true;
		result[1] = selected;

		return result;
	} // end method findAllGreaterOrEqual

	/**
	 * Same as the <code>find</code> method except here we search for all the
	 * records with secondary key lesser or equal to <code>secondaryKey</code>
	 * 
	 * @param secondaryKey
	 *            The desired key.
	* @return A <code>Result [2]</code> array which contains: <br>
	 *         <ul>
	 *         <li>A <code>boolean</code> variable to verify the success of the
	 *         search process.
	 *         <li>A <code>LinkedList</code> object with the references that
	 *         must be retrieved.
	 *         </ul>
	 */
	public Object[] findAllLessOrEqual(final int secondaryKey) {
		// ��� ������ ���� ��� ������������� �������� �� ���� ��������� � ���
		// ��' ���� ��� ������.

		Object[] result = new Object[2];
		LinkedList<Reference> selected = new LinkedList<Reference>();

		// ��������� ��� ������ ��� ���������� �� ���� �� ������.
		SecondaryLeaf leaf = this.search(secondaryKey);

		// ������ �� ������� ����� ������������, ��������� �� ������ ��� ���
		// ����������...
		int index = leaf.getNumOfReferences() - 1;
		while (index > -1 && leaf.getReference(index).getKey() > secondaryKey) {
			// ��� � ������� ��� ���� �� ������ ��� �������, ���� ����������
			// ���� �������.
			index--;
		} // end while

		// ��� ��� �������� �������� ��� ���������� �������� ��' ����.
		for (int i = index; i > -1; i--) {
			// ������������ ��� ������� ��� �������� ��� �� ������ ��
			// ������ ���� ���������� ��������.
			selected.addFirst(leaf.getReference(i));
		} // end for

		// ���� �������� ���� ��� �������� �� ���� ��� ������� ��� ����������
		// �������� ��� ������������.
		leaf = leaf.getLeftLeaf();
		while (leaf != null) {
			for (int i = leaf.getNumOfReferences() - 1; i > -1; i--) {
				// ������������ ��� ������� ��� �������� ��� �� ������ ��
				// ������ ���� ���������� ��������.
				selected.addFirst(leaf.getReference(i));
			} // end for

			// ���������� �� ������� �����.
			leaf = leaf.getLeftLeaf();
		} // end while

		// � ������� ���������� ��� �����, ��� ����������� ��� ��� ������ ���
		// ����������� ��� �������� � ��� �������� ��� ������� ��� ��� �����
		// �� ��� �������� ��� �������� ��� ������ �� �������.
		result[0] = selected.size() == 0 ? false : true;
		result[1] = selected;

		return result;
	} // end method findAllLessOrEqual

	/**
	 * Same as the <code>find</code> method except that it finds all the records in
	 * the <u>open</u> range between <code>firstSecondaryKey</code> and <code>lastSecondaryKey</code>.
	 * @param firstSecondaryKey
	 *            The first key.
	 * @param lastSecondaryKey
	 *            The last key the index of the key.
	* @return A <code>Result [2]</code> array which contains: <br>
	 *         <ul>
	 *         <li>A <code>boolean</code> variable to verify the success of the
	 *         search process.
	 *         <li>A <code>LinkedList</code> object with the references that
	 *         must be retrieved.
	 *         </ul>
	 */
	public Object[] findAllInRange(final int firstSecondaryKey,
			final int lastSecondaryKey) {
		// ��� ������ ���� ��� ������������� �������� �� ���� ��� (�������)
		// �������� ��� �������.

		Object[] result = new Object[2];
		LinkedList<Reference> selected = new LinkedList<Reference>();

		// ��������� ��� ������ ��� ���������� �� ���� �� ������.
		SecondaryLeaf leaf = this.search(firstSecondaryKey);

		// ������ �� ������� ����� ������������, ��������� �� ������ ��� ���
		// ����������...
		int index = 0;
		while (index < leaf.getNumOfReferences()
				&& leaf.getReference(index).getKey() < firstSecondaryKey) {
			// ��� � ������� ��� ���� �� ������ ��� �������, ���� ����������
			// ���� �������.
			index++;
		} // end while

		// ��� ��� �������� �������� ��� ���������� ����� ��' ����.
		for (int i = index + 1; i < leaf.getNumOfReferences(); i++) {
			// ������������ ��� ������� ��� �������� ��� �� ������ ��
			// ������ ���� ���������� ��������.
			selected.add(leaf.getReference(index));
		} // end for

		// ���� �������� ���� ��� �������� �� ���� ��� ������� ��� ����������
		// ����� ��� ������������.
		leaf = leaf.getRightLeaf();

		while (leaf != null) {
			for (int i = 0; i < leaf.getNumOfReferences(); i++) {
				if (leaf.getReference(i).getKey() < lastSecondaryKey) {
					// ������������ ��� ������� ��� �������� ��� �� ������ ��
					// ������ ���� ���������� ��������.
					selected.add(leaf.getReference(index));
				} // end if
				else {
					break;
				} // end else
			} // end for

			// ���������� �� ������� �����.
			leaf = leaf.getRightLeaf();
		} // end while

		// � ������� ���������� ��� �����, ��� ����������� ��� ��� ������ ���
		// ����������� ��� �������� � ��� �������� ��� ������� ��� ��� �����
		// �� ��� �������� ��� �������� ��� ������ �� �������.
		result[0] = selected.size() == 0 ? false : true;
		result[1] = selected;

		return result;
	} // end method findAllGreater

	/**
	 * It updates the references using the reference lists according to
	 * which the records were moved.
	 * 
	 * @param recordsMoved
	 *            The records that were moved.
	 * @param oldReferenceList
	 *            The list with old references.
	 * @param newReferenceList
	 *            The list with the new references.
	 */
	public void updateReferences(final LinkedList<Record> recordsMoved,
			final LinkedList<Reference> oldReferenceList,
			final LinkedList<Reference> newReferenceList) {
		SecondaryLeaf leaf;
		boolean repeat;

		int index = 0;
		while (index < recordsMoved.size()) {
			repeat = true;

			// ��������� �� ����� ��� ����� ��������� � ������� ��� ��
			// ����������.
			leaf = this.search((Integer) recordsMoved.get(index).getKey(
					this.keyIndex));

			while (repeat && leaf != null) {
				for (int i = 0; i < leaf.getNumOfReferences(); i++) {
					if (leaf.getReference(i).getPageNumber() == oldReferenceList
							.get(index).getPageNumber()
							&& leaf.getReference(i).getOffset() == oldReferenceList
									.get(index).getOffset()) {
						// ������������ � ������� ��� �� ���������� ��� ��������
						// ���� ����� �����������.
						leaf.changePageAndOffset(i, newReferenceList.get(index)
								.getPageNumber(), newReferenceList.get(index)
								.getOffset());

						// ���������� ��� ������� �������� ��� ������.
						index++;

						// ���������� ��� ���������.
						repeat = false;
						break;
					} // end if
				} // end for

				// ���������� ��� ������� �����.
				leaf = leaf.getRightLeaf();
			} // end while
		} // end while
	} // end method updateReferences

	/**
	 * Retrieves the first leaf
	 * 
	 * @return The first <code>PrimaryLeaf</code>.
	 */
	public SecondaryLeaf getFirstLeaf() {
		return this.firstLeaf;
	} // end method getFirstLeaf
	/**
	 * Retrieves the key index
	 * 
	 * @return The key index.
	 */
	public int getKeyIndex() {
		return this.keyIndex;
	} // end method getKeyIndex

	/**
	* Retrieves the name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return this.name;
	} // end getName
} // end class SecondaryBPlusTree
