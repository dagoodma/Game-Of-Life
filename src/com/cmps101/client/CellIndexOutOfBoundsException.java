/**
 * 
 */
package com.cmps101.client;

/**
 * Thrown by a GameBoard when a given coordinate location is not
 * a valid cell on the board.
 */
public class CellIndexOutOfBoundsException extends Error {
	private Cell cell;
	
	public CellIndexOutOfBoundsException(Cell cell) {
		this.cell = cell;
	}
	
	public CellIndexOutOfBoundsException(int x, int y) {
		this(new Cell(x, y));
	}
	
	public int getX() { return cell.getX(); }
	public int getY() { return cell.getY(); }
	
	
}
