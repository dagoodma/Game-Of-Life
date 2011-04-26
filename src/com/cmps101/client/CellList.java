package com.cmps101.client;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public class CellList implements Iterable<Cell> {
	private int count, modCount;
	private Cell head, tail;
	/**
	 * The CellList is similar to a linked list, but with various extra
	 * methods for merging, adding, and updating the list. 
	 */
	private static final long serialVersionUID = 8440756334158714038L;

	public CellList() {
		clear();
	}
	
	public void clear() {
		count = 0;
		modCount = 0;
		head = null;
		tail = null;
	}

	
	/**
	 * Adds an element to the end of the list.
	 */
	public boolean add(Cell cell) {
		addLast(cell);
		return true;
	}
	
	public void addLast(Cell cell) {
		if (head == null)
			head = cell;
		if (tail != null)
			tail.setNext(cell);
		cell.setPrev(tail);
		cell.setNext(null);
		tail = cell;
		
		count++;
		modCount++;
	}
	
	public void addFirst(Cell cell) {
		if (head != null) {
			head.setPrev(cell);
			cell.setNext(head);
		}
		else { 
			tail = cell; // no tail either
		}
		cell.setPrev(null);
		head = cell;
		count++;
		modCount++;
	}
	
	protected boolean addBefore(Cell cell, Cell element) {
		if (element.hasPrev()) {
			element.getPrev().setNext(cell);
			cell.setPrev(element.getPrev());
		}
		if (element == head)
			head = cell;
		element.setPrev(cell);
		cell.setNext(element);
		modCount++;
		count++;
		return true;
	}
	
	
	public CellIterator iterator() {
		return new CellIterator(this);
	}
	
	public Cell getFirst() {
		return head;
	}
	
	public Cell getLast() {
		return tail;
	}

	protected Cell remove(Cell cell) {
		if (head == cell) {
			if (cell.hasNext())
				cell.getNext().setPrev(null);	
			head = cell.getNext();
		}
		else if (tail == cell) {
			if (cell.hasPrev())
				cell.getPrev().setNext(null);
			tail = cell.getPrev();
		}
		else if (cell.hasNext() && cell.hasPrev()) { //    cell
			cell.getPrev().setNext(cell.getNext()); // prev => next
			cell.getNext().setPrev(cell.getPrev()); // prev <= next
			
		}
		cell.setNext(null);
		cell.setPrev(null);
		modCount++;
		count--;
		return cell;
	}
	
	public boolean contains(Cell cell) {
		for (Cell cell_i : this) {
			if (cell_i.getX() == cell.getX()
					&& cell_i.getY() == cell.getY())
				return true;
		}
		return false;
	}
	
	public int size() {
		return count;
	}
	
	/**
	 * Adds then given cell to the list maintaining a sorted order.
	 * This is Θ(n), and it is only used for drawing new tiles by
	 * the user interface.
	 * @param cell is to be added into the list
	 */
	public void addToOrder(Cell cell) {
		if (size() == 0) {
			add(cell);
		}
		// Find where to add the cell
		for (Cell cell_i : this) {
			if ( cell.compareTo(cell_i) <= 0 ) {
				addBefore(cell, cell_i);
				return;
			}
		}
		addLast(cell);
	}
	
	public CellList merge(CellList list) {
		CellList union = new CellList();
		CellIterator left = iterator();
		CellIterator right = list.iterator();

		while (left.hasNext() || right.hasNext()) {
			if (left.hasNext() && right.hasNext()) {
				if (left.seeNext().compareTo(right.seeNext()) <= 0) 
					union.add(left.next());
				else
					union.add(right.next());
			}
			else if (left.hasNext()) {
				union.add(left.next());
			}
			else if (right.hasNext()) {
				union.add(right.next());
			}
		} // end of while
		
		return union;
	} // end of merge()
	
	
	/**
	 * This method generates and returns a new list that consists of
	 * all of the elements in this list with 8 neighbor tiles for each
	 * element inserted in order.
	 * This is the 1st pass and it is Θ(n).
	 * 
	 * @return a new list with dead neighbor cells in it
	 */
	public CellList withNeighbors() {
		Cell curr = getFirst();
		CellList finalList;
		
		// These are our neighbor lists in which we will
		// 	   throw the new neighbor cells that we are generating.
		CellList topList = new CellList();
		CellList midList = new CellList();
		CellList bottomList = new CellList();
		// Neighbor pointers for order keeping
		Cell top = null, mid = null, bot = null;
		
		/*
		 * 1st pass:
		 * Generate the neighbor lists. This is Θ(n).
		 */
		while (curr != null) {
			curr.clearNeighbors(); // Clear the neighbor count from
								   // last turn
			Cell parent = curr.copy();

			/*
			 * Loop over this cell's adjacent neighbor cells. There
			 * are 8 in total: NW, N, NE, E, SE, S, SW, and W.
			 * 
			 *  All the generated neighbors are stored in their
			 *  respective lists: NW, N, and NE are put in the
			 *  topList, W, E, and the parent cell will be stored
			 *  in the midList, and SW, S, and SE are in the bottomList. 
			 */
			for (int y = -1; y <= 1; y++) {
				for (int x = -1; x <= 1; x++) {
					Cell neighbor = new Cell(curr.getX() + x,
							curr.getY() + y, parent);
					neighbor.incrementNeighbors();
					
					// Don't use this neighbor if it is at the origin,
					//   use the alive parent cell itself instead
					if (x == 0 && y == 0)
						neighbor = parent;
					
					// Determine the list to put it in and update
					// our pointers.
					switch (y) {
						case -1: 
							// Top List
							if (top != null && neighbor.compareTo(top) <= 0)
								topList.addBefore(neighbor, top);
							else
								topList.add(neighbor);
							if (x == 1)
								top = neighbor;
							break;
						case 0:
							// Middle list
							if (mid != null && neighbor.compareTo(mid) <= 0)
								midList.addBefore(neighbor, mid);
							else
								midList.add(neighbor);
							if (x == 1)
								mid = neighbor;
							break;
						case 1:
							// Bottom list
							if (bot != null && neighbor.compareTo(bot) <= 0)
								bottomList.addBefore(neighbor, bot);
							else
								bottomList.add(neighbor);
							if (x == 1)
								bot = neighbor;
					} // end of switch(y)
				} // end of for k
			} // end of for j
			curr = curr.getNext();
		} // end of while
		
		/*
		 * 2 more passes (2nd and 3rd) merge the lists together in
		 * order. Each pass is in Θ(n) time.
		 */
		//GWT.log("Top: " + topList);
		//GWT.log("Mid: " + midList);
		//GWT.log("Bot: " + bottomList);
		finalList = topList;
		finalList = finalList.merge(midList);
		finalList = finalList.merge(bottomList);

		return finalList;
	}
	
	/**
	 * This method will generate and return the next generation of
	 * itself. It does this by first iterating over a this list and
	 * over each cell's neighboring tiles (8 in total). It will then
	 * remove all duplicate entries and update the neighbor counts of
	 * each accordingly. Finally, the states of the remaining cells
	 * will be updated and dead ones removed.
	 * 
	 * This method encompasses the entire updating procedure for the
	 * game of life. There are a total of 5 passes over this list
	 * within and each one is Θ(n).
	 * 
	 * @return a new list representing the next generation of this list
	 */
	public CellList update() {
		/*
		 * First we get this list with all of its neighboring tiles
		 * in it. (3 passes).
		 */
		//GWT.log("Current  list:" + this);
		CellList newList = withNeighbors();
		Cell curr = newList.getFirst();
		//GWT.log("Neighbor list: " + newList);
		
		/*
		 * Then we iterate over the list and remove the duplicate
		 * entries, counting neighbors along the way. This is Θ(n).
		 * (1 pass)
		*/
		while(curr != null && curr.hasNext()) {
			Cell next = curr.getNext();
			if (curr.getX() == next.getX()
					&& curr.getY() == next.getY()) {
				// Found a duplicate
				//GWT.log(curr + " ==? " + next);
				// Dead on dead = increment dead, remove next
				// Alive on dead = increment dead.parent, remove next
				// Dead on alive = increment dead.parent, remove curr
				// Alive on alive = remove duplicate
				
				//Dead on Dead, Alive on Dead
				if (!next.isAlive()) {
					// Only worry about our parent,
					if (!curr.isAlive())
						curr.incrementNeighbors();
					else
						next.getParent().incrementNeighbors();
					
					newList.remove(next);
				}
				//Dead on Alive
				else if (!curr.isAlive()) {
					curr.getParent().incrementNeighbors();
					newList.remove(curr);
					curr = next;
				}
				// Alive on Alive
				else {
					//error
					Window.alert("Error! Duplicates alives at "
							+ next);
					return new CellList();
				}
			}
			else
				curr = curr.getNext();
		} // end while

		//GWT.log("Neighbor sort: " + newList.toString());
		
		/*
		 * Finally, we iterate a last time and update the state of
		 * each remaining cell and remove the dead cells in Θ(n).
		 * (1 pass)
		 */
		curr = newList.getFirst();
		while(curr != null && curr.hasNext()) {
			Cell nextCurr;
			
			// Update and delete the dead cells
			if (!curr.nextState()) {
				nextCurr = curr.getNext();
				newList.remove(curr);
			}
			else {
				nextCurr = curr.getNext();
			}
			curr = nextCurr;
		} // end of for loop
		if (!curr.nextState())
			newList.remove(curr);
		return newList;
	} // end of update()
	
	/**
	 * This will iterate the list and remove the element with
	 * coordinates that match the given cell's
	 * 
	 * @param cell coordinates are used to detect and remove
	 * @return true if an item was found and removed
	 
	public boolean remove(Cell cell) {
		int i = 0;
		for (Cell cell_i : this) {
			if (cell_i.getX() == cell.getX() && cell_i.getY() == cell.getY()) {
				remove(i);
				count--;
				return true;
			}
			i++;
		}
		return false;
	}
	*/

	
	/**
	 * Iterates the list and returns the cell with the given
	 * row and column (coordinates) if it exists.
	 * 
	 * @param i represents the row coordinate
	 * @param j is the column coordinate
	 * @return the cell desired, or null if non exists
	 */
	public Cell getCellAt(int i, int j) {
		for (Cell cell : this) {
			if (cell.getX() == i && cell.getY() == j)
				return cell;
		}
		return null;
	}
	
	/**
	 * Copy this list's collection and use it to build a clone.
	 
	public CellList clone() {
		CellList list = new CellList((Collection)this);
		return list;
	}
	*/
	
	/**
	 * Iterates over the list and appends each element as a string.
	 */
	public String toString() {
		String str = "";
		Cell curr = getFirst();
		while (curr != null) {
			str += curr.toString() + ",  ";
			curr = curr.getNext();
		}
		
		return str;
	}

}
