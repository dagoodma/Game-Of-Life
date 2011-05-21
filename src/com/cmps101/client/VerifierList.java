package com.cmps101.client;

import java.util.Iterator;

/**
 * The VerifiableList is similar to a linked list that is used
 * for verifying the live cells in the game of life. This is
 * different from a CellList because additional pointers are needed
 * to keep track of next and previous elements verified, which are
 * stored in the {@link Verifiable} interface, because they need to
 * be separate from the pointers that the CellList uses.
 * 
 * @author <a href="mailto:dagoodma@ucsc.edu">David Goodman</a>
 */
public class VerifierList implements Iterable<Cell>{
	private Cell head, tail;
	protected int count; // count the elements and modifications
	protected int modCount;

	public VerifierList() {
		clear();
	}
	
	/**
	 * Returns the number of verified elemets in the list.
	 */
	public int size() {
		return count;
	}
	
	/**
	 * Clear all elements from the list. Θ(1)
	 */
	public void clear() {
		count = 0;
		modCount = 0;
		head = null;
		tail = null;
	}
	
	/**
	 * Gets the element in the front of the list. Θ(1)
	 * @return the head of the list
	 */
	public Cell getFirst() {
		return head;
	}
	
	/**
	 * Gets the element at the end of the list. Θ(1)
	 * @return the tail of the list
	 */
	public Cell getLast() {
		return tail;
	}
	
	/**
	 * Add the given cell to the end of the list as the tail.
	 * @param cell to add to the end of the list. Θ(1)
	 */
	public void addLast(Cell verif) {
		if (head == null)
			head = verif;
		if (tail != null)
			tail.setNextVerified(verif);
		verif.setPrevVerified(tail);
		verif.setNextVerified(null);
		verif.setVerifier(this);
		tail = verif;
		
		count++;
		modCount++;
	}
	
	/**
	 * Add the given cell to the end of the list. This method is
	 * a wrapper for addLast(). Θ(1)
	 * @param cell to add to the end of the list
	 * @return
	 */
	public boolean add(Cell verif) {
		addLast(verif);
		return true;
	}
	
	/**
	 * Adds the given cell to the front of the list, making it the
	 * head of the list. Θ(1)
	 * @param cell to add to the front of the list
	 */
	public void addFirst(Cell verif) {
		if (head != null) {
			head.setPrevVerified(verif);
			verif.setNextVerified(head);
		}
		else { 
			tail = verif; // no tail either
		}
		verif.setPrevVerified(null);
		verif.setVerifier(this);
		head = verif;
		count++;
		modCount++;
	}
	
	/**
	 * Returns this list as a CellIterator.
	 */
	public CellIterator iterator() {
		return new CellIterator(this);
	}
	
	/**
	 * Removes the given cell from the list. This does not check
	 * if the cell is a member of the list or not! Θ(1)
	 * @param cell to be removed
	 * @return the cell that has been removed
	 */
	protected Cell remove(Cell verif) {
		if (head == verif) {
			if (verif.hasNextVerified())
				verif.getNextVerified().setPrevVerified(null);	
			head = (Cell)verif.getNextVerified();
		}
		else if (tail == verif) {
			if (verif.hasPrevVerified())
				verif.getPrevVerified().setNextVerified(null);
			tail = (Cell)verif.getPrevVerified();
		}
		else if (verif.hasNextVerified() && verif.hasPrevVerified()) {
			verif.getPrevVerified().setNextVerified(
					verif.getNextVerified()); // prev => next
			verif.getNextVerified().setPrevVerified(
					verif.getPrevVerified()); // prev <= next
			
		}
		verif.clearVerification();
		modCount++;
		count--;
		return verif;
	}

	/**
	 * Iterates over the list and returns a string with each element
	 * appended to it.
	 */
	public String toString() {
		String str = "";
		Verifiable curr = getFirst();
		while (curr != null) {
			str += curr.toString() + ",  ";
			curr = curr.getNextVerified();
		}
		
		return str;
	}
	
}
