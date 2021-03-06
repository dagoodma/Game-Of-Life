package com.cmps101.client;


import java.util.ArrayList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
//import com.google.gwt.user.client.Event;
//import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
//import com.google.gwt.user.client.ui.Grid;
//import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.widgetideas.graphics.client.GWTCanvas;
import com.google.gwt.widgetideas.graphics.client.Color;
//import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
//import com.google.gwt.event.dom.client.ClickHandler;
//import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseDownHandlers;
import com.google.gwt.event.dom.client.HasMouseMoveHandlers;
import com.google.gwt.event.dom.client.HasMouseUpHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
//import com.google.gwt.event.shared.EventHandler;
//import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * The GameBoard class is the user interface for the Game of Life.
 * It handles drawing the board as the user sees it including drawing
 * the grid and black cells that represent the creatures in the
 * game, providing the user with the ability to draw tiles
 * individually or while dragging, controlling the state and speed of
 * the game, choosing preset boards, and viewing the information 
 * window.
 * 
 * @author <a href="mailto:dagoodma@ucsc.edu">David Goodman</a>
 */
public class GameBoard extends Composite
	implements HasMouseDownHandlers, HasMouseUpHandlers,
			   HasMouseMoveHandlers {
	// UiBinder XML template file
	@UiTemplate("GameBoard.ui.xml")
	interface GameUi extends UiBinder<Widget, GameBoard> {}
	private static GameUi uiBinder = GWT.create(GameUi.class);
	
	// UiBinder declarations
	@UiField GameResources res;
	@UiField VerticalPanel content;
	@UiField AbsolutePanel gameWindow;
	@UiField Button playButton;
	@UiField Button stopButton;
	@UiField Button infoButton;
	@UiField ListBox speedListBox;
	@UiField ListBox presetListBox;

	// -------------- Game Fields --------------
	private GameOfLife game; // pointer to the game object
	private GWTCanvas canvas; // our html5 canvas for drawing on
	private GameInfo info = null; // popup information window
	private CellHash creatures; // points to the game's cellHash
	private int height, width; // board dim. in px
	private final int clickOffset = -1; // border width
	private int total, rows, columns; // cell count
	
	// Board settings
	private final int cellSize = 10; // 10x10 size cell
	private final int widthOffset = 24; // hard-coded CSS offsets =(
	private final int heightOffset = 59; // =(
	// Board grid settings
	private int gridVisibleMinimum = 4; // minimum cellSize to draw
										// grid--any less than this
										// and its all jumbled together
	private boolean showGrid = true;
	private Color gridColor = new Color("#000000"); // black
	private double gridThickness = 0.5; // grid line width
	
	// Event handler registrations (just mouse events for now)
	private ArrayList<HandlerRegistration> registrations 
	= new ArrayList<HandlerRegistration>();
	
	// Mouse drawing/erasing handling
	private boolean mouseDown = false, mouseDraw = true,
		mouseDownPause = false;
	private Cell lastModified = null; // mouse drag draws/erases only
								      // once per tile it's being
									  // dragged over
	
	/* 
	 * Constructor
	 */
	public GameBoard(GameOfLife game) {
		GameResources.INSTANCE.css().ensureInjected();
		initWidget(uiBinder.createAndBindUi(this));
		
		// Initialize the board
		this.game = game;
		initialize();
		canvas =  new GWTCanvas(width,height);
		drawGrid();
		creatures = game.getCreatures(); // get the cellHash
		update(); // draw the cells
	
		// Draw the canvas
	    gameWindow.add(canvas);
	    
	    // Initialize the buttons and dropdowns
	    stopButton.setTitle("Press once to reset the board.\n" +
	    					"Press twice to clear the board.");
	    playButton.setTitle("Play!");
	    
	    // Build the speed chooser dropdown
	    speedListBox.setTitle("Speed");
	    int i = 0; 
	    for (Speed s : Speed.values()) {
	    	speedListBox.addItem(s.toString(), "" + s);
	    	if (game.getPace() == s) {
	    		speedListBox.setItemSelected(i, true);
	    	}
	    	i++;
	    } 
	    
	    // Build the preset chooser dropdown
	    presetListBox.setTitle("Choose a preset board.");
	    i = 0;
	    for (CellPreset p : CellPreset.values()) {
	    	presetListBox.addItem(p.getName());
	    	if (game.getPreset() == p)
	    		presetListBox.setItemSelected(i, true);
	    	i++;
	    }
	    
	}	
	
	public HandlerRegistration addMouseDownHandler(
		final MouseDownHandler handler) {
			return addHandler(handler, MouseDownEvent.getType());
	}
	public HandlerRegistration addMouseUpHandler(
		final MouseUpHandler handler) {
			return addHandler(handler, MouseUpEvent.getType());
	}
	public HandlerRegistration addMouseMoveHandler(
		final MouseMoveHandler handler) {
			return addHandler(handler, MouseMoveEvent.getType());
	}	
	
	/**
	 * Initializes the boards height and width and clips rest.
	 */
	public void initialize() {
		width = res.css().gameWidth() - widthOffset; // hard offsets
		height = res.css().gameHeight() - heightOffset;
		// Clip the board by rounding it down to within the cellSize
		height -= height % cellSize;
		width -= width % cellSize;
		// Resize the window 
		gameWindow.setSize(width + "px", height + "px");
		
		// Determine indexing bounds
		rows = width / cellSize;
		columns = height / cellSize;
		total = rows * columns;
		
		GWT.log("Board initialized: " + width + "x" + height);
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
		int cellStartX = getCellPixel(cell.getX());
		int cellStartY = getCellPixel(cell.getY());
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
		if (! isDisplayablePixel(startX, startY))
			return;
		// Calculate the row column iterations to perform
		int rowsToDraw = (endX - startX) / cellSize;
		int colsToDraw = (endY - startY) / cellSize;
		
		// Finally, draw the grid
		canvas.setLineWidth(0.3);
		canvas.setLineCap(GWTCanvas.SQUARE);
		canvas.setStrokeStyle(gridColor);
		canvas.beginPath(); // start draw
		// Draw Columns
		for (int i = 0; i <= rowsToDraw; ++i) {
			canvas.moveTo(cellSize*i + startX, startY);
			canvas.lineTo(cellSize*i + startX, endY);
		}
		// Draw Rows
	    for (int j = 0; j <= colsToDraw; ++j) {
	    	canvas.moveTo(startX, cellSize*j + startY);
	    	canvas.lineTo(endX, cellSize*j + startY);
	    }
	    canvas.closePath();
	    canvas.stroke(); // end draw
	}
	
	/*
	 * Redraws the grid for the given cell only.
	 */
	public void patchCellGrid(Cell cell) {
		int x = cell.getX();
		int y = cell.getY();
		// How I figured this out I have no idea
		canvas.setGlobalAlpha(0.6);
		canvas.setStrokeStyle(gridColor);
		canvas.strokeRect(getCellPixel(x) + 0.5, getCellPixel(y) + 0.5, cellSize - 1,  cellSize - 1 );
		canvas.setGlobalAlpha(1);	
	}
	
	/**
	 * Updates the cells on the board by clearing the old and
	 * drawing the new cells.
	 * 
@note Clearing every time may be inefficient but it's difficult to
	  determine and paint only the differences in the updated board
	 */
	public void update() {
		clear();
		creatures = game.getCreatures();
		/*
		if (creatures != null) {
			clear(); // Clear the old creatures
		}
		*/
		for (Cell cell : creatures) {
			if (cell.isAlive())
				fillCell(cell.getX(), cell.getY());
		}
	}
	
	/**
	 * Clears the creatures cellHash and clears the board.
	 */
	public void reset() {
		if (creatures != null)
			creatures.clear();
		clear();
	}
	
	/**
	 * Clears the board and draws the grid.
	 */
	public void clear() {
		canvas.clear();
		//Window.alert("here!");
		drawGrid();
	}

	/**
	 * Draw a cell at the given coordinates.
	 * 
	 * @param x Row of the cell to fill.
	 * @param y Column of the cell to fill.
	 * @return Valid cell location was given.
	 */
	public boolean fillCell(int x, int y)
	{
		// Only draw if it's in bounds
		if (! isDisplayable(x, y)) {
			return false;
		}
	
		canvas.setFillStyle(Color.BLACK);
		canvas.fillRect(getCellPixel(x), getCellPixel(y), cellSize, cellSize);
		return true;
	}
	
	/**
	 * Erases the given cell from sight on the board.
	 * 
	 * @param x Row of the cell to clear.
	 * @param y Column of the cell to clear.
	 * @return Valid cell location was given.
	 */
	public boolean clearCell(Cell cell) 
	{
		int x = cell.getX();
		int y = cell.getY();
		
		// Only erase if it's in bounds
		if (! isDisplayable(x,y)) {
			return false;
		}

		// Fill with white
		canvas.setGlobalCompositeOperation(GWTCanvas.SOURCE_OVER);
		canvas.setFillStyle(Color.WHITE);
		canvas.fillRect(getCellPixel(x), getCellPixel(y), cellSize, cellSize);
		patchCellGrid(cell);
		return true;
	}
	
	/*
	 * Called when the board is initialized. This hooks the mouse
	 * events for the user.
	 * @see com.google.gwt.user.client.ui.Widget#onLoad()
	 */
	@Override
	protected void onLoad() {
		super.onLoad();
		/* Start drawing */
		registrations.add(addDomHandler(new MouseDownHandler() {
			public void onMouseDown(final MouseDownEvent event) {
				if (isCanvasClick(event)) {
					int clickX = event.getRelativeX(canvas.getElement());
					int clickY = event.getRelativeY(canvas.getElement());
					int cellX = getCellCoordinate(clickX);
					int cellY = getCellCoordinate(clickY);
					Cell cell = creatures.search(cellX, cellY);
					mouseDown = true;
					if (game.isPlaying()) {
						game.pause();
						mouseDownPause = true;
					}

					if (cell != null) {
						mouseDraw = false; //erase
						eraseCell(cell);
					}
					else {
						mouseDraw = true; // draw
						drawCell(new Cell(cellX, cellY, true));
					}
				} // end of isCanvasClick()
			} // end of onMouseDown()
		}, MouseDownEvent.getType())
		);
		/* Stop drawing */
		registrations.add(addDomHandler(new MouseUpHandler() {
			public void onMouseUp(final MouseUpEvent event) {
				if (mouseDown) {
					mouseDown = false;
					if (mouseDownPause) {
						game.play();
						mouseDownPause = false;
					}
					lastModified = null;
					
				} // end of isCanvasClick()
			} // end of onMouseDown()
		}, MouseUpEvent.getType())
		);
		/* Drag drawing */
		registrations.add(addDomHandler(new MouseMoveHandler() {
			public void onMouseMove(final MouseMoveEvent event) {
				if (mouseDown) {
					if (!isCanvasClick(event)) {
						return;
					}
					int clickX = event.getRelativeX(
							canvas.getElement());
					int clickY = event.getRelativeY(
							canvas.getElement());
					int cellX = getCellCoordinate(clickX);
					int cellY = getCellCoordinate(clickY);
					Cell cell = creatures.search(cellX, cellY);
					// Draw once per the user's mouse entering a tile
					if (lastModified != null &&
							cellX == lastModified.getX() &&
							cellY == lastModified.getY())
						return;
					if (mouseDraw) {
						// we're drawing
						if (cell == null)
							drawCell(new Cell(cellX, cellY, true));
					}
					else {
						// we're erasing
						if (cell != null)
							eraseCell(cell);
					}
					
				} // end of mouseDown()
			} // end of onMouseMove()
		}, MouseMoveEvent.getType())
		);
	}
	
	@Override
	protected void onUnload() {
		super.onUnload();
		for (HandlerRegistration reg : registrations) {
			reg.removeHandler();
			reg = null;
		}
	}
	
	/**
	 * Adds the given cell to the cellHash and draws it.
	 * @param cell to be drawn
	 * @return true if the cell was not already drawn
	 */
	public boolean drawCell(Cell cell) {
		// Insert new creature
		if (! creatures.add(cell))
			return false; // already exists
		
		fillCell(cell.getX(), cell.getY());
		lastModified = cell;
		return true;
	}
	
	/**
	 * Erases the given cell from the cellHash and from sight.
	 * @param cell to be erased
	 * @return true if the cell could be erased
	 */
	public boolean eraseCell(Cell cell) {
		// Remove creature
		clearCell(cell);
		creatures.delete(cell);
		lastModified = cell;
		return true;
	}
	
	
	/**
	 * Check if the given coordinates are displayable on the board.
	 * 
	 * @param x Row index of cell.
	 * @param y Column index of cell.
	 * @return Whether the given indexes are valid.
	 */
	public boolean isDisplayable(int x, int y) {
		return ! (x > rows || y > columns || x < 0 || y < 0);
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
	 * Determine whether the given pixel coordinate location is valid.
	 * 
	 * @param x coordinate of a row
	 * @param y coordinate of a column
	 * @return
	 */
	public boolean isDisplayablePixel(int x, int y) {
		return ! (x > width || y > height || x < 0 || y < 0);
	}
	
	public boolean isCanvasClick(MouseEvent<?> click) {
		int clickX = click.getRelativeX(canvas.getElement());
		int clickY = click.getRelativeY(canvas.getElement());
		//clickX += clickOffset;
		//clickY += clickOffset;
		//Window.alert(clickX + "," + clickY + " vs. " + width + "x" + height + (clickX >= 0 && clickY >= 0 && clickX <= width && clickY <= height));
		return (clickX >= 0 && clickY >= 0 && clickX <= width && clickY <= height);
	}
	
	/**
	 * Convert a cell index to a pixel location.
	 * 
	 * @param n Cell index can be either a row or a column index.
	 * @return Starting pixel of the given index.
	 */
	public int getCellPixel(int n) { return cellSize * n; }
	
	public int getCellCoordinate(int z) { return z / cellSize; } // round down
	
	public void updatePlayButton() {
		
		if (!game.isPlaying())
			playButton.setText("Play");
		else
			playButton.setText("Pause");
	}
	
	public void updateResetButton() {
		if (!game.wasReset())
			stopButton.setText("Reset");
		else
			stopButton.setText("Clear");
	}
	
	@UiHandler("playButton")
	void doPlay(ClickEvent event) {
		game.togglePlay();
		updatePlayButton();
		updateResetButton();
	}
	
	@UiHandler("stopButton")
	void doStop(ClickEvent event) {
		if (game.toggleReset()) {
			updatePlayButton();
			updateResetButton();
		}
	}
	
	@UiHandler("infoButton")
	void doInfo(ClickEvent event) {
		if (info == null) {
			info = new GameInfo();
			info.show();
			return;
		}
		if (info.isShowing())
			info.hide();
		else 
			info.show();
	}
	
	@UiHandler("speedListBox")
	void doSpeedChange(ChangeEvent event) {
		String newSpeed = speedListBox.getValue(speedListBox.getSelectedIndex());
		game.setPace(newSpeed);
	}
	
	@UiHandler("presetListBox")
	void doPresetChange(ChangeEvent event) {
		String newPreset = presetListBox.getValue(presetListBox.getSelectedIndex());
		game.setPreset(newPreset);
	}
}
