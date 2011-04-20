package com.cmps101.client;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import com.cmps101.shared.FieldVerifier;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class GameOfLife implements EntryPoint {
	private boolean isPlaying = false;
	private boolean wasStopped = false;
	private int totalTurns = 19;
	private int turn = 0;
	private GameBoard gameBoard;
	private CellList creatures = new CellList();
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		// Initialize the board
	    gameBoard = new GameBoard(this);
	    // Add it to the DOM
	    RootPanel.get().add(gameBoard); 
		
		// Load initial state
	    initialize();
		
	}
	
	public void initialize() {
		if (creatures.size() > 0) 
			creatures.clear();
		/*
		creatures.add(new Cell(50,50,true));
		creatures.add(new Cell(51,50,true));
		creatures.add(new Cell(52,50,true));
		creatures.add(new Cell(52,49,true));
		creatures.add(new Cell(51,48,true));
		*/
		/* Exploder */
	    
		creatures.add(new Cell(50,49,true));
		creatures.add(new Cell(49,50,true));
		creatures.add(new Cell(50,50,true));
		creatures.add(new Cell(51,50,true));
		creatures.add(new Cell(49,51,true));
		creatures.add(new Cell(50,52,true));
		creatures.add(new Cell(51,51,true));
		
		/* Glider
		creatures.add(new Cell(50,48,true));
		creatures.add(new Cell(48,49,true));
		creatures.add(new Cell(50,49,true));
		creatures.add(new Cell(49,50,true));
		creatures.add(new Cell(50,50,true));
		*/
		
		// draw the initial state
		gameBoard.update();
	}
	
	public boolean togglePlay() {
		wasStopped = false; // reset stop clear
		if (!isPlaying) {
			play();
			return true;
		}
		else {
			pause();
			return false;
		}
	}
	
	public void play() {
		isPlaying = true;
		
		Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {
			@Override
			public boolean execute() {
				if (! isPlaying || ! nextTurn()) {
					return false;
				}
				turn++;
				gameBoard.update();
				return true;
			}
		}, 250);
	}
	
	public void pause() {
		isPlaying = false;
	}
	
	public boolean toggleStop() {
		if (isPlaying) {
			pause();
			wasStopped = true;
		}
		else if (wasStopped) {
			initialize(); // resets board
			wasStopped = false;
		}
		else if (!isPlaying) {
			wasStopped = true;
		}
		
		return wasStopped;
	}
	
	/**
	 * Calculates the number of neighbors for each cell including dead
	 * cells touching existing cells, and then updates each cell to
	 * its next state.
	 */
	public boolean nextTurn() {
		// Note: we are iterating over a different list than we
		// are appending to in order to gain the use of a more robust
		// add function via. the CellList instead of iter
		// and avoid a concurrent modification exception
		CellList passList = creatures.clone();
		
		ListIterator<Cell> iter = passList.listIterator();
		int i = 0;
	
		//GWT.log(passList.toString());
		// Create neighbor cells for each creature
		//  and append them to the passList
		while (iter.hasNext()) {
			Cell cell = iter.next();
			cell.setNeighbors(0);
			
			// Loop over each neighbor position
			//  and add a cell to the passList there
			for (int j = -1; j <= 1; j++) {
				for (int k = -1; k <= 1; k++) {
					Cell neighbor = new Cell(cell.getX() + j, cell.getY() + k, cell);
					neighbor.incrementNeighbors();
					
					// Skip this neighbor if it is the alive parent cell
					if (neighbor.getX() == cell.getX()
							&& neighbor.getY() == cell.getY()) {
						continue;
					}
					creatures.add(neighbor);
				} // end of k
			} // end of j
			i++;

		} // end of iterator
		
		// Merge overlapping cells
		creatures.update();
		return true;
	}
	
	public CellList getCreatures() {
		return creatures;
	}
	
	public boolean isPlaying() {
		return isPlaying;
	}
	
	public boolean wasStopped() {
		return wasStopped;
	}
}
