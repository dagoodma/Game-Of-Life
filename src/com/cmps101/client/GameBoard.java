package com.cmps101.client;

import java.util.Iterator;
import java.util.LinkedList;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.graphics.client.GWTCanvas;
import com.google.gwt.widgetideas.graphics.client.Color;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;

public class GameBoard extends Composite {
	@UiTemplate("GameBoard.ui.xml")
	interface GameUi extends UiBinder<Widget, GameBoard> {}
	private static GameUi uiBinder = GWT.create(GameUi.class);
	
	@UiField GameResources res;
	@UiField VerticalPanel content;
	@UiField AbsolutePanel gameWindow;
	//@UiField AbsolutePanel buttonWindow;
	@UiField Button playButton;
	@UiField Button stopButton;
	@UiField ListBox speedListBox;
	
	private GameOfLife game;
	private GWTCanvas canvas;
	private CellList creatures; // creatures drawn
	private int height, width; // board dim. in px
	private int total, rows, columns; // cell count
	private final int cellSize = 5; // 10x10
	private final int widthOffset = 24;
	private final int heightOffset = 59;
	
	private int gridVisibleMinimum = 4;
	private boolean showGrid = true;
	private Color gridColor = new Color("#000000");
	private double gridThickness = 0.5;
	
	public GameBoard(GameOfLife game) {
		GameResources.INSTANCE.css().ensureInjected();
		initWidget(uiBinder.createAndBindUi(this));
		
		// Initialize the board
		this.game = game;
		initialize();
		canvas =  new GWTCanvas(width,height);
		drawGrid();
		
	    gameWindow.add(canvas);
	    
	    GWT.log("Board initialized: " + width + "x" + height);
	    
	    // Initialize the interface
	    stopButton.setTitle("Press once to pause the game.\n" +
	    					"Press twice to reset the game.");
	    
	    
	    // Speed select
	    int i = 0; 
	    for (Speed s : Speed.values()) {
	    	speedListBox.addItem(s.toString(), "" + s);
	    	if (game.getPace() == s) {
	    		speedListBox.setItemSelected(i, true);
	    	}
	    	i++;
	    }
	    
	}	
	
	/**
	 * Initializes the boards height and width and clips rest.
	 */
	public void initialize() {
		width = res.css().gameWidth() - widthOffset; // hard offsets
		height = res.css().gameHeight() - heightOffset;
		// Clip the board
		height -= height % cellSize;
		width -= width % cellSize;
		// Resize the window 
		gameWindow.setSize(width + "px", height + "px");
		
		// Determine indexing bounds
		rows = width / cellSize;
		columns = height / cellSize;
		total = rows * columns;
	}
	
	/**
	 * Draw the grid for the entire board.
	 */
	public void drawGrid() {
		drawGrid(0,0, width, height);
	}
	
	/**
	 * Draw the grid for the given cell, usually after clearing the
	 * cell, in order to fix the grid.
	 * 
	 * @param cell where the grid will be drawn.
	 */
	public void drawGrid(Cell cell) {
		int cellStartX = cellStart(cell.getX());
		int cellStartY = cellStart(cell.getY());
		int cellEndX = cellStartX + cellSize;
		int cellEndY = cellStartY + cellSize;
		drawGrid(cellStartX, cellStartY, cellEndX, cellEndY);
	}

	/**
	 * Draw the grid starting and ending with the given coordinates.
	 * 
	 * @param startX starting X coordinate to draw the grid from.
	 * @param startY starting Y coordinate to draw the grid from.
	 * @param endX ending X coordinate to draw the grid to.
	 * @param endY ending Y coordinate to draw the grid to.
	 */
	public void drawGrid(int startX, int startY, int endX, int endY) {
		
		// Only draw the grid if desired and if cellSize permits
		if (! showGrid || cellSize < gridVisibleMinimum)
			return;
		
		// Don't draw if the ending coordinates are invalid
		if (endX < (startX - cellSize) || endY < (startY - cellSize))
			return;
		
		// Round the starting coordinates up
		if (startX % cellSize > 0)
			startX += cellSize - startX % cellSize;
		if (startY % cellSize > 0)
			startY += cellSize - startY % cellSize;
		
		// Round the ending coordinates down
		if (endX < width)
			endX -= endX % cellSize;
		else
			endX = width;
		if (endY < height)
			endY -= endY % cellSize;
		else
			endY = height;
		
		// Don't draw if the starting coordinates are out of bounds
		if (! isDisplayableCoord(startX, startY))
			return;
		// Calculate the iterations to perform
		int rowsToDraw = (endX - startX) / cellSize;
		int colsToDraw = (endY - startY) / cellSize;

		//Window.alert("Have: " + startX + "," + startY + " to " + endX + "," + endY +
		//			 "\nFrom: " + rowsToDraw + "," + colsToDraw + " vs. " + rows + "," + columns);
		
		// Finally, draw the grid
		//canvas.setLineCap(GWTCanvas.SQUARE);
		//canvas.setMiterLimit(100);
		canvas.setLineWidth(gridThickness);
		canvas.setStrokeStyle(gridColor);
		canvas.beginPath();
		// Columns
		for (int i = 0; i <= rowsToDraw; ++i) {
			canvas.moveTo(cellSize*i + startX, startY);
			canvas.lineTo(cellSize*i + startX, endY);
		}
		// Rows
	    for (int j = 0; j <= colsToDraw; ++j) {
	    	canvas.moveTo(startX, cellSize*j + startY);
	    	canvas.lineTo(endX, cellSize*j + startY);
	    }
	    canvas.closePath();
	    canvas.stroke();
	}
	
	/**
	 * Updates the cells on the board by clearing the old and
	 * drawing the new cells.
	 * 
@note Clearing every time may be inefficient.
	 */
	public void update() {
		if (creatures != null) {
			clear(); // Clear the old creatures
		}
		
		creatures = game.getCreatures().clone();
		//GWT.log("Updating creatures: " + creatures.toString());

		for (Object obj : creatures) {
			Cell cell = (Cell)obj;
			if (cell.isAlive())
				fillCell(cell.getX(), cell.getY());
		}
	}
	
	/**
	 * Erases all of the creatures on the board.
	 */
	public void clear() {
		if (creatures == null) return;
		//GWT.log("Clearing " + creatures.size() + " creatures.");
		/*
		for (Object obj : creatures) {
			Cell cell = (Cell)obj;
			clearCell(cell);
		}
		*/
		canvas.clear();
		creatures.clear();
		drawGrid();
		//Window.alert("Cleared!");
	}

	/**
	 * Fill in the given cell index.
	 * 
	 * @param x Row of the cell to fill.
	 * @param y Column of the cell to fill.
	 * @return Valid cell location was given.
	 */
	public boolean fillCell(int x, int y)
	{
		if (! isDisplayable(x, y)) {
			//GWT.log("Could not fill index: " + x + "," + y);
			return false;
		}
		
		canvas.setFillStyle(Color.BLACK);
		canvas.fillRect(cellStart(x), cellStart(y), cellSize, cellSize);
		return true;
	}
	
	/**
	 * Clear the given cell's location on the board.
	 * 
	 * @param x Row of the cell to clear.
	 * @param y Column of the cell to clear.
	 * @return Valid cell location was given.
	 */
	public boolean clearCell(Cell cell) 
	{
		int x = cell.getX();
		int y = cell.getY();
		
		if (! isDisplayable(x,y)) {
			//GWT.log("Could not clear index: " + x + "," + y);
			return false;
		}
		
		// Fill with white
		canvas.setFillStyle(Color.WHITE);
		canvas.fillRect(cellStart(x) - 1, cellStart(y) - 1, cellSize + 1, cellSize + 1);
		
		// Fix the grid
		//Window.alert("Gridding: " + cellStart(x) + "," + cellStart(y));
		drawGrid(cellStart(x), cellStart(y), cellStart(x) + cellSize, cellStart(y) + cellSize);
		
		return true;
	}
	
	/**
	 * Check if the given cell is at a displayable position on 
	 * the board.
	 * 
	 * @param cell A cell to check the indexes of.
	 * @return Whether the given cell has a valid location.
	 */
	public boolean isDisplayable(Cell cell) {
		return isDisplayable(cell.getX(), cell.getY());
	}
	
	/**
	 * Check if the given index is displayable on the board.
	 * 
	 * @param x Row index of cell.
	 * @param y Column index of cell.
	 * @return Whether the given indexes are valid.
	 */
	public boolean isDisplayable(int x, int y) {
		return ! (x > rows || y > columns || x < 0 || y < 0);
	}
	
	/**
	 * Determine whether the given coordinate location is valid.
	 * 
	 * @param x coordinate of a row
	 * @param y coordinate of a column
	 * @return
	 */
	public boolean isDisplayableCoord(int x, int y) {
		return ! (x > width || y > height || x < 0 || y < 0);
	}
	
	/**
	 * Convert a cell index to a pixel location.
	 * 
	 * @param n Cell index can be either a row or a column index.
	 * @return Starting pixel of the given index.
	 */
	public int cellStart(int n) { return cellSize * n; }
	
	public void updatePlayButton() {
		
		if (!game.isPlaying())
			playButton.setText("Play");
		else
			playButton.setText("Pause");
	}
	
	public void updateStopButton() {
		if (game.wasStopped() && game.getTurns() > 0)
			stopButton.setText("Reset");
		else
			stopButton.setText("Stop");
	}
	
	public void setGameSpeed(String speed) {
		game.setPace(speed);
	}
	
	@UiHandler("playButton")
	void doPlay(ClickEvent event) {
		game.togglePlay();
		updatePlayButton();
		updateStopButton();
	}
	
	@UiHandler("stopButton")
	void doStop(ClickEvent event) {
		game.toggleStop();
		updatePlayButton();
		updateStopButton();
	}
	
	@UiHandler("speedListBox")
	void doChange(ChangeEvent event) {
		String newSpeed = speedListBox.getValue(speedListBox.getSelectedIndex());
		setGameSpeed(newSpeed);
	}

}
