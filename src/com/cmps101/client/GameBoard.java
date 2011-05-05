package com.cmps101.client;


import java.util.ArrayList;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Event;
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
import com.google.gwt.event.dom.client.HasClickHandlers;
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
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

public class GameBoard extends Composite
	implements HasMouseDownHandlers, HasMouseUpHandlers,
			   HasMouseMoveHandlers {
	@UiTemplate("GameBoard.ui.xml")
	interface GameUi extends UiBinder<Widget, GameBoard> {}
	private static GameUi uiBinder = GWT.create(GameUi.class);
	
	@UiField GameResources res;
	@UiField VerticalPanel content;
	@UiField AbsolutePanel gameWindow;
	//@UiField AbsolutePanel buttonWindow;
	@UiField Button playButton;
	@UiField Button stopButton;
	@UiField Button infoButton;
	@UiField ListBox speedListBox;
	@UiField ListBox presetListBox;
	
	private GameOfLife game;
	private GWTCanvas canvas;
	private GameInfo info = null;
	private ArrayList<HandlerRegistration> registrations
		= new ArrayList<HandlerRegistration>();
	private CellList creatures; // creatures drawn
	private int height, width; // board dim. in px
	private final int clickOffset = -1; // border width
	private int total, rows, columns; // cell count
	private final int cellSize = 10; // 10x10
	private final int widthOffset = 24;
	private final int heightOffset = 59;
	
	private int gridVisibleMinimum = 4;
	private boolean showGrid = true;
	private Color gridColor = new Color("#000000");
	private double gridThickness = 0.5;
	
	// Mouse related variables
	private boolean mouseDown = false, mouseDraw = true,
		mouseDownPause = false;
	private Cell lastModified = null;
	
	
	public GameBoard(GameOfLife game) {
		GameResources.INSTANCE.css().ensureInjected();
		initWidget(uiBinder.createAndBindUi(this));
		
		// Initialize the board
		this.game = game;
		initialize();
		canvas =  new GWTCanvas(width,height);
		drawGrid();
		creatures = game.getCreatures();
		update();
	
	    gameWindow.add(canvas);
	    // Initialize the interface
	    stopButton.setTitle("Press once to reset the board.\n" +
	    					"Press twice to clear the board.");
	    
	    // Speed select
	    int i = 0; 
	    for (Speed s : Speed.values()) {
	    	speedListBox.addItem(s.toString(), "" + s);
	    	if (game.getPace() == s) {
	    		speedListBox.setItemSelected(i, true);
	    	}
	    	i++;
	    }
	    speedListBox.setTitle("Speed");
	    
	    // Preset select
	    i = 0;
	    for (CellPreset p : CellPreset.values()) {
	    	presetListBox.addItem(p.getName());
	    	if (game.getPreset() == p)
	    		presetListBox.setItemSelected(i, true);
	    	i++;
	    }
	    
	    presetListBox.setTitle("Choose a preset board.");
	    
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
	
	/*
	public HandlerRegistration addClickHandler (
			final ClickHandler handler) {
		return addHandler(handler, ClickEvent.getType());
	}
	*/
	
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
		int cellStartX = getCellCoordinate(cell.getX());
		int cellStartY = getCellCoordinate(cell.getY());
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
		canvas.setLineWidth(0.3);
		canvas.setLineCap(GWTCanvas.SQUARE);
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
	
	public void patchCellGrid(Cell cell) {
		int x = cell.getX();
		int y= cell.getY();
		// How I figured this out I have no idea
		canvas.setGlobalAlpha(0.6);
		canvas.setStrokeStyle(gridColor);
		canvas.strokeRect(getCellCoordinate(x) + 0.5, getCellCoordinate(y) + 0.5, cellSize - 1,  cellSize - 1 );
		canvas.setGlobalAlpha(1);	
	}
	
	/**
	 * Updates the cells on the board by clearing the old and
	 * drawing the new cells.
	 * 
@note Clearing every time may be inefficient.
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
	 * Erases all of the creatures on the board.
	 */
	public void reset() {
		if (creatures != null)
			creatures.clear();
		clear();
	}
	
	/**
	 * Erases the board.
	 */
	public void clear() {
		canvas.clear();
		//Window.alert("here!");
		drawGrid();
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
		canvas.fillRect(getCellCoordinate(x), getCellCoordinate(y), cellSize, cellSize);
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
		canvas.setGlobalCompositeOperation(GWTCanvas.SOURCE_OVER);
		canvas.setFillStyle(Color.WHITE);
		canvas.fillRect(getCellCoordinate(x), getCellCoordinate(y), cellSize, cellSize);
		patchCellGrid(cell);
		//canvas.setGlobalCompositeOperation(GWTCanvas.DESTINATION_OVER);
		//drawGrid(cell);
			
		// Fix the grid
		//Window.alert("Gridding: " + getCellCoordinate(x) + "," + getCellCoordinate(y));
		//drawGrid(getCellCoordinate(x), getCellCoordinate(y), getCellCoordinate(x) + cellSize, getCellCoordinate(y) + cellSize);
		
		return true;
	}
	
	@Override
	protected void onLoad() {
		super.onLoad();
		registrations.add(addDomHandler(new MouseDownHandler() {
			public void onMouseDown(final MouseDownEvent event) {
				if (isCanvasClick(event)) {
					int clickX = event.getRelativeX(canvas.getElement());
					int clickY = event.getRelativeY(canvas.getElement());
					int cellX = getCellIndex(clickX);
					int cellY = getCellIndex(clickY);
					Cell cell = creatures.getCellAt(cellX, cellY);
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
		
		registrations.add(addDomHandler(new MouseMoveHandler() {
			public void onMouseMove(final MouseMoveEvent event) {
				if (mouseDown) {
					if (!isCanvasClick(event)) {
						return;
					}
					int clickX = event.getRelativeX(canvas.getElement());
					int clickY = event.getRelativeY(canvas.getElement());
					int cellX = getCellIndex(clickX);
					int cellY = getCellIndex(clickY);
					Cell cell = creatures.getCellAt(cellX, cellY);
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
	
	public boolean drawCell(Cell cell) {
		// Add new creature
		creatures.addToOrder(cell);
		fillCell(cell.getX(), cell.getY());
		lastModified = cell;
		return true;
	}
	
	public boolean eraseCell(Cell cell) {
		// Remove creature
		clearCell(cell);
		creatures.remove(cell);
		lastModified = cell;
		return true;
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
	public int getCellCoordinate(int n) { return cellSize * n; }
	
	public int getCellIndex(int x) { return x / cellSize; } // round down
	
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
