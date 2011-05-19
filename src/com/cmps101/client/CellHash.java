package com.cmps101.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public class CellHash implements Iterable<Cell> {
	/*
	 * Default Parameters
	 */
	private final int tableSize = 128; // size of the table (m=2^7)
	private final double keyA = (Math.sqrt(5.0) - 1)/4,
						 keyB = (Math.sqrt(3.0) - 1)/2.4;
	
	private CellList verifier;
	private CellList[] table;
	
	public CellHash() {
		verifier = new CellList();
		table = new CellList[tableSize];
	}
	
	/**
	 * Clear all element from the hash. O(n)
	 */
	public void clear() {
		for (Cell cell : verifier) {
			if (!delete(cell))
				GWT.log("Couldn't delete " + cell + " from hash.");
		}
		verifier.clear();
	}
	
	/**
	 * This will delete the given cell from the list. The given cell
	 * must be in the list, if not it will return false. Runs in O(1).
	 * @param cell to be deleted
	 * @return true if the cell could be deleted
	 */
	public boolean delete(Cell cell) {
		// Is it in the list? (valid 2-way pointer)
		if (cell.getVerifier() != verifier)
			return false;

		// Delete from hash table
		int index = hash(cell.getX(), cell.getY());
		table[index].remove(cell);
		
		// Delete from verifier
		cell.setVerifier(null); // clear pointer
		verifier.remove(cell);
		
		return true;
	}
	
	/**
	 * This will find the element at the given coordinates and
	 * delete it if it exists. This is O(n) because it uses search.
	 * @param x coordinate
	 * @param y coordinate
	 * @return true if an element was deleted
	 */
	public boolean deleteAt(int x, int y) {
		Cell cell = search(x,y);
		if (cell == null) return false;
		
		delete(cell);
		return true;
	}
	
	/**
	 * Returns this hash as a CellIterator.
	 */
	public CellIterator iterator() {
		return new CellIterator(verifier);
	}
	
	/**
	 * Generate a hash table index for the given coordinates.
	 * @param i x coordinate
	 * @param j y coordinate
	 * @return hash table index
	 */
	public int hash(int i, int j) {
		return (int)Math.floor( tableSize * (i*keyA + j*keyB - Math.floor(i*keyA + j*keyB)) );
	}
	
	/**
	 * Insert a cell into the hash table and add it to the verifier
	 * array. This will return false if the element is a duplicate.
	 * Runs in O(n) because it uses the search function.
	 * @param cell to be added
	 * @return true if the cell was inserted
	 */
	public boolean insert(Cell cell) {
		// generate the hash index
		int index = hash(cell.getX(), cell.getY());
		GWT.log("Adding " + cell + " to index " + index);
		
		Cell found = search(cell.getX(), cell.getY());
		if (found != null) {
			return false;
		}
		
		// Initialize table row if needed
		if (table[index] == null)
			table[index] = new CellList();
		
		table[index].addFirst(cell);
		verifier.addFirst(cell);
		cell.setVerifier(verifier);
		return true;
	}
	
	/** 
	 * Clears the hash and adopts the given list as the new verifier
	 * list by adding each cell to the hash table. This is used for
	 * loading preset boards and it runs in Θ(n).
	 * @param list
	 */
	public void setList(CellList list) {
		clear(); // O(n)
	
		for (Cell cell : list) {
			if (!insert(new Cell(cell.getX(), cell.getY(), cell.isAlive())))
				GWT.log("Couldn't insert " + cell + " into hash.");
			int i = 0;
			for (CellList listt : table) {
				if (listt != null)
					GWT.log(i + ": " + listt.toString());
				i++;
			}
		}
		
		
		
	}
	
	/**
	 * Find a cell with the given coordinates. Simply wraps
	 * CellList.getCellAt(), which iterates itself. Runs in O(n).
	 * @param i x coordinate
	 * @param j y coordinate
	 * @return cell with given coordinates or null if not found
	 */
	public Cell search(int i, int j) {
		Cell cell = verifier.getCellAt(i,j);
		return cell;
	}
	
	/**
	 * Wraps CellList.size() to provide the cell count
	 * @return the number of cells in the hash
	 */
	public int size() {
		return verifier.size();
	}
	
	/**
	 * Wraps CellList.toString().
	 */
	public String toString() {
		return verifier.toString();
	}
}
