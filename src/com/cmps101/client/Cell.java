package com.cmps101.client;

public class Cell implements Comparable<Cell> {
	private int x, y, count;
	private boolean state;
	private Cell parent;
	
	public Cell(int x, int y, boolean state) {
		this.x = x;
		this.y = y;
		count = 0;
		this.state = state;
	}
	public Cell(int x, int y) {
		this(x,y,false);
	}
	public Cell(int x, int y, Cell parent) {
		this(x,y);
		this.parent = parent;
	}
	
	public int getX() { return x; }
		
	public int getY() { return y; }
	
	public Cell getParent() { return parent; }
	
	public int getNeighbors() { return count; }
	
	public void setNeighbors(int count) { this.count = count; }
	
	public void incrementNeighbors() { this.count++; }
	
	public boolean isAlive() { return state; }
	
	public String toString() {
		String str = "(" + getX() + "," + getY() + ")";
		str += (! isAlive())? "!" : "";
		//str += (parent != null)? "=>" + parent.toString() : "";
		str = count + "_" + str;
		return str;
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
