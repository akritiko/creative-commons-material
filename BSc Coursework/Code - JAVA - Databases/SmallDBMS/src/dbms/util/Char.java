package dbms.util;

import java.io.Serializable;
import java.nio.ByteBuffer;

import dbms.exececution.EnormousCharacterException;

/**
 * A class to represent Char values. It limits every value to 50 characters.
 */
public class Char implements Serializable, Comparable {
	/**
	 * The autogenerated serial version UID.
	 */
	private static final long serialVersionUID = -5053016501647419254L;

	/**
	 * The word (character array).
	 */
	private final char[] word;

	/**
	 * Constructs a <code>Char</code> object. It takes a <code>string</code>
	 * as a parameter.
	 * 
	 * @param word
	 *            The word we want to manipulate.
	 * @throws EnormousCharacterException
	 *             In the case of a very big input.
	 */
	public Char(final String word) throws EnormousCharacterException {
		if (word.length() > 49)
			throw new EnormousCharacterException("@");

		this.word = new char[50];
		char arrayForm[] = word.toCharArray();

		System.arraycopy(arrayForm, 0, this.word, 0, arrayForm.length);
		// ��� ���������� �� �������� ��� ��������� '\0' ���� ��� ���������
		// ������ ���������. � ����� ����� ��� � ������� ���������� �����������
		// ��� �� �������� ��� �� ���� ��� ��������� ��� ��� �������� ����
		// �������������� ����� � ���������� ��� ������� ����.
	} // end method Char

	/**
	 * Constructs a <code>Char</code> object. It takes a <code>Char[]</code>
	 * as a parameter.
	 * 
	 * @param word
	 *            The word we want to manipulate.
	 * @throws EnormousCharacterException.
	 *            In the case of a very big input.
	 */
	public Char(final char[] word) {
		this.word = word;
	} // end method Char

	/**
	 * It transforms a <code>Char</code> object to an array of bytes.
	 * 
	 * @return A <code>byte</code> array.
	 */
	public byte[] toByteArray() {
		ByteBuffer buffer = ByteBuffer.allocate(100);

		for (int i = 0; i < 50; i++)
			buffer.putChar(this.word[i]);

		return buffer.array();
	} // end method toByteArray

	/**
	 * It overrides the method <code>toString()</code>.
	 * 
	 * @return A <code>String</code>.
	 */
	@Override
	public String toString() {
		int i = 0;
		while (this.word[i] != '\0') {
			i++;
		} // end while
		String stringForm = new String(this.word, 0, i);

		return stringForm;
	} // end method toString

	/**
	 * It overrides the method <code>equals</code>.
	 * 
	 * @return <code>true</code> if the two objects are equal and
	 *         <code>false</code> otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Char) {
			for (int i = 0; i < word.length
					&& (word[i] != '\0' && ((Char) obj).word[i] != '\0'); i++) {
				if (word[i] != ((Char) obj).word[i])
					return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param obj
	 *            The object to compare.
	 * @return Positive if this Char precedes lexicographically the Char that was passed as a parameter,
	 * 			otherwise returns negative.
	 */
	public int compareTo(Object obj) {
		if (obj instanceof Char) {
			int i;
			for (i = 0; i < word.length
					&& (word[i] != '\0' && ((Char) obj).word[i] != '\0'); i++) {
				if (word[i] != ((Char) obj).word[i])
					return word[i] - ((Char) obj).word[i];
			}
			return word[i] - ((Char) obj).word[i];
		}
		return -1;
	}
} // end class Char
