package com.cmps101.client;


public class Cell extends Verifiable implements Comparable<Cell> {
	static boolean printParent = false;
	private int x, y, count;
	private boolean state;
	private Cell parent; // used in updating
	private Cell next, prev;
	
	public Cell(int x, int y, boolean state) {
		this.x = x;
		this.y = y;
		count = 0;
		next = null;
		prev = null;

		this.state = state;
	}
	public Cell(int x, int y) {
		this(x,y,false);
	}
	public Cell(int x, int y, Cell parent) {
		this(x,y);
		this.parent = parent;
	}
	public Cell(int x, int y, boolean state, Cell parent) {
		this(x,y,state);
		this.parent = parent;
	}

	public boolean hasNext() { return next != null; }
	public boolean hasPrev() { return prev != null; }
	
	public Cell getNext() { return next; }
	public Cell getPrev() { return prev; }
	
	public void setNext(Cell cell) { next = cell; }
	public void setPrev(Cell cell) { prev = cell; }
	
	public int getX() { return x; }
	public int getY() { return y; }
	
	public Cell getParent() { return parent; }
	public boolean hasParent() { return parent != null; }
	
	
	public int getNeighbors() { return count; }
	public void clearNeighbors() { this.count = 0; }
	public void setNeighbors(int num) { this.count = num; }
	public void incrementNeighbors() { this.count++; }
	
	public boolean isAlive() { return state; }
	
	public String toString() {
		String str = "[" + getId() + "]";
		if (! isAlive())
			str += "!";
		str += "(" + getX() + "," + getY() + ")";
		str += "_" + count;
		if (hasParent() && printParent) {
			Cell parent = getParent();
			str += "=>(" + parent.getX() + "," + parent.getY() + ")";
		}
		return str;
	}
	
	// Doesn't copy links and neighbors
	public Cell copy() {
		Cell clone = new Cell(x,y,state,parent);
		return clone;
	}
	
	/**
	 * This will return true if this cell's coordinates match the ones
	 * given as x and y.
	 * @param x coordinate of cell
	 * @param y coordinate of cell
	 * @return True if this cell has the given coordinates x and y.
	 */
	public boolean isAt(int x,int y) {
		return getX() == x && getY() == y;
	}
	
	public int compareTo(Cell cell) {
		if (this.getY() < cell.getY()
				|| ( this.getY() == cell.getY() && this.getX() < cell.getX()) )
			return -1;
		
		if (this.getY() > cell.getY()
				|| ( this.getY() == cell.getY() && this.getX() > cell.getX()) )
			return 1;
		
		// They must be at the same position,
		//	sort order by alive and dead respectively
		if (! this.isAlive() && cell.isAlive())
			return 1;
		if (this.isAlive() && ! cell.isAlive())
			return -1;
		
		return 0; // they're equal
	}
	
	/* 
	 * Rules of the Game:
	 * 1) Any live cell with fewer than two live neighbors dies,
	 * 	  as if caused by under-population.
	 * 2) Any live cell with two or three live neighbors lives on
	 * 	  to the next generation.
	 * 3) Any live cell with more than three live neighbors dies,
	 * 	  as if by overcrowding.
	 * 4) Any dead cell with exactly three live neighbors becomes
	 *    a live cell, as if by reproduction.
	 */
	public boolean nextState() {
		int neighbors = getNeighbors();
		// Alive?
		if (state) {
			if (neighbors < 2 || neighbors > 3)
				state = false; // dies
			// has 2 or 3 neighbors lives on
		}
		else  {
			if (neighbors == 3)
				state = true; // born
		}
		return state;
	}
	

}
