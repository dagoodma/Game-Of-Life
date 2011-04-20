package com.cmps101.client;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public class CellList extends LinkedList<Cell> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8440756334158714038L;

	public CellList() {
		super();
	}
	
	public CellList(Collection c) {
		super(c);
	}
	
	public boolean add(Cell cell) {
		super.add(cell);
		/*
		if (size() == 0) {
			super.add(cell);
			return true;
		}
		// Find where to add the creature
		for (int i = size() - 1; i >= 0; i--) {
			if ( cell.compareTo(get(i)) >= 0 ) {
				super.add(i + 1, cell);
				//GWT.log(this.toString());
				break;
			}
			else if (i == 0)
				super.add(0, cell);
		} // end of l
		*/
		return true;
	}
	
	public void update() {
		Collections.sort(this);
		
		// Count neighbors for each
		for(int i = 1; i < size() ; i++) {
			Cell first = get(i-1);
			Cell next = get(i);
			
			//GWT.log(i + ": " + first.toString() + " vs " + next.toString());
			// Duplicate found
			if (first.getX() == next.getX()
					&& first.getY() == next.getY()) {
				// Dead overlapping dead
				if (!first.isAlive() && !next.isAlive()) {
					first.incrementNeighbors();
				}
				
				// Alive overlapping a dead
				if (first.isAlive() && !next.isAlive()) {
					next.getParent().incrementNeighbors();
				}
				// Dead overlapping an alive
				/*
				if (!first.isAlive() && next.isAlive()) {
					remove(--i);
				}
				else {
				*/
					// Alive overlaps an alive...
					// discard duplicate
					remove(i--); 
			}
		}
		
		// Update the game
		for(int i = 0; i < size() ; i++) {
			Cell cell = get(i);
			
			// Update and delete the dead cells
			if (!cell.nextState()) {
				remove(i--);
			}
		}
	}
	
	public CellList clone() {
		CellList list = new CellList((Collection)this);
		return list;
	}

}
