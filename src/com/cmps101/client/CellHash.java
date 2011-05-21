package com.cmps101.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

/**
 * 
 * @author <a href="mailto:dagoodma@ucsc.edu">David Goodman</a>
 *
 */
public class CellHash implements Iterable<Cell> {
	/*
	 * Default Parameters
	 */
	private final int tableSize = 128; // size of the table (m=2^7)
	private final double keyA = (Math.sqrt(5.0) - 1)/4,
						 keyB = (Math.sqrt(3.0) - 1)/2.4;
	
	private VerifierList verifier;
	private CellList[] table;
	
	public CellHash() {
		verifier = new VerifierList();
		table = new CellList[tableSize];
	}
	
	/**
	 * Clear all element from the hash. O(n)
	 */
	public void clear() {
		for (Verifiable verif : verifier) {
			Cell cell = (Cell)verif;
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
		
		// Delete from verifier O(1)
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
	 * Returns this hash as a CellIterator in O(1) time.
	 */
	public CellIterator iterator() {
		CellIterator iter = verifier.iterator();
		return iter;
	}
	
	/**
	 * Generate a hash table index for the given coordinates.
	 * @param i x coordinate
	 * @param j y coordinate
	 * @return hash table index
	 */
	public int hash(int i, int j) {
		return (int)Math.floor( tableSize * (i*keyA + j*keyB - 
				Math.floor(i*keyA + j*keyB)) );
	}
	
	/**
	 * Wraps the insert() method but insures that the element to be
	 * inserted doesn't already exist. This will return false if
	 * the element is a duplicate. Runs in O(n) because it uses the
	 * search function to check for redundancy.
	 * @param cell to be added
	 * @return true if the cell was inserted
	 */
	public boolean add(Cell cell) {
		Cell found = search(cell.getX(), cell.getY());
		if (found != null) {
			return false;
		}
		
		insert(cell);
		return true;
	}
	
	protected void insert(Cell cell) {
		// generate the hash index
		int index = hash(cell.getX(), cell.getY());
		
		// Initialize table row if needed
		if (table[index] == null)
			table[index] = new CellList();
		
		table[index].addFirst(cell);
		verifier.addFirst(cell);
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
			if (!add(new Cell(cell.getX(), cell.getY(), cell.isAlive())))
				GWT.log("Couldn't insert " + cell + " into hash.");
		}
	}
	
	/**
	 * Find a cell with the given coordinates.Iterates the verifier
	 * list to find the cell with the given coords. Runs in O(n).
	 * @param i x coordinate
	 * @param j y coordinate
	 * @return cell with given coordinates or null if not found
	 */
	public Cell search(int i, int j) {
		for (Verifiable verif : verifier) {
			Cell cell = (Cell)verif;
			if (cell.getX() == i && cell.getY() == j)
				return cell;
		}
		return null;
	}
	
	/**
	 * Updates the cell in the Game of Life to advance to the next
	 * turn. This is done in 2 passes: the first one is O(n*lg(n)),
	 * and the second is O(n).
	 */
	public void update() {
		/* First iteration creates dead neighboring cells and
		 * increments the count of existing cells. Runs n*lg(n)
		 */
		for (Cell liveCell : this) {
			/* Iterate the 3x3 tiles centered at this liveCell element,
			 * but skip the live parent cell. This will no doubt add a
			 * constant 9 multiplier to the running time (9*n).
			 */
			for (int i = -1; i <= 1; i++) {
				for (int j = -1; j <= 1; j++) {
					if (i == 0 && j == 0)
						continue;
					int x = liveCell.getX() + i,
						y = liveCell.getY() + j;
					Cell neighborCell = null;
					CellList rowList = table[hash(x,y)];
					
					/* Search the rowList, which contains a very
					 * small portion of the n cells in the hash,
					 * so this will likely run in O(1 + k/n) time
					 * due to lookup comparison, where k is the
					 * tableSize for the hash.
					 */ 
					if (rowList != null) {
						for (Cell cell_i : rowList) {
							if (cell_i.getX() == x &&
								cell_i.getY() == y) {
								neighborCell = cell_i;
								break;
							}
						}
					}
					/* Either increment the count of the existing cell
					 * or create a new neighbor cell to use here.
					 * If the neighboring cell existed and is alive
					 * then increment the alive parent cell only.
					 */
					if (neighborCell == null) {
						neighborCell = new Cell(x,y,false,liveCell);
						insert(neighborCell); // O(1) operation
					}
					
					if (neighborCell.isAlive()) {
						liveCell.incrementNeighbors();
					}
					else {
						neighborCell.incrementNeighbors();
					}
				} // end of for loop j
			} // end of for loop i
		} // end of foreach

		/* Lastly, iterate over all of the cells in the hash and
		 * update their states, deleting all of the dead cells. This
		 * runs in O(n) time.
		 */
		for (Cell cell : this) {
			// Update and delete the dead cells
			if (!cell.nextState())
				delete(cell); // O(1) operation
			// Reset count
			cell.clearNeighbors(); // O(1) operation
		}
		
		//toLog();
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
	
	public void toLog() {
		int i = 0;
		for (CellList listt : table) {
			if (listt != null && listt.size() > 0)
				GWT.log(i + ": " + listt.toString() + "\n");
			i++;
		}
	}
}
