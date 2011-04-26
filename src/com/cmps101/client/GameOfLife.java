package com.cmps101.client;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import com.cmps101.shared.FieldVerifier;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
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
	private boolean wasReset = false;
	private int totalTurns = 19;
	private int turn = 0;
	private Speed paceOfLife = Speed.NORMAL;
	private int gameSpeed = paceOfLife.value();
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
		/* Exploder 
	    
		creatures.add(new Cell(20,19,true));
		creatures.add(new Cell(20,20,true));
		creatures.add(new Cell(21,20,true));
		creatures.add(new Cell(19,21,true));
		creatures.add(new Cell(21,21,true));
		creatures.add(new Cell(20,22,true));
		*/
		
		/* Glider */
		creatures.add(new Cell(19,17,true));
		creatures.add(new Cell(20,18,true));
		creatures.add(new Cell(18,19,true));
		creatures.add(new Cell(19,19,true));
		creatures.add(new Cell(20,19,true));
		
		
		// draw the initial state
		gameBoard.update();
	}
	
	/**
	 * Either starts the game or stops it depending on its current
	 * state.
	 * @return whether the game was started or not
	 */
	public boolean togglePlay() {
		wasReset = false; // reset stop clear
		if (!isPlaying) {
			play();
			return true;
		}
		else {
			pause();
			return false;
		}
	}
	
	/**
	 * Starts the game.
	 */
	public void play() {
		isPlaying = true;
		
		Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
			@Override
			public boolean execute() {
				if (! isPlaying || ! nextTurn()) {
					return false;
				}
				turn++;
				gameBoard.update();
				return true;
			}
		}, gameSpeed);
	}
	
	/**
	 * Pauses the game.
	 */
	public void pause() {
		isPlaying = false;
	}
	
	/**
	 * Either stops the game or resets it depending on its current state.
	 * @return the wasStopped flag.
	 */
	public boolean toggleReset() {
		if (!wasReset) {
			// Reset
			if (isPlaying())
				pause();
			initialize(); // resets board
			turn = 0;
			wasReset = true;
		}
		else  {
			// Clear the board
			gameBoard.reset(); // clears the board
			wasReset = false;
		}
		
		return wasReset;
	}
	
	
	public boolean nextTurn() {
		CellList nextCreatures = creatures.update();
		creatures = nextCreatures;
		
		gameBoard.update();
		
		// TODO add creatures.equals checking
		return creatures.size() > 0;
	}
		
	public CellList getCreatures() {
		return creatures;
	}
	
	public boolean isPlaying() {
		return isPlaying;
	}
	
	public Speed getPace() {
		return paceOfLife;
	}
	
	public boolean setPace(String pace) {
		Speed speed = null;
		for (Speed s : Speed.values()) {
			if (s.toString().equals(pace)) {
				speed = s;
				break;
			}
		}
		if (speed == null)
			return false;
		
		paceOfLife = speed;
		gameSpeed = speed.value();
		
		// Pause and start the game again
		if (isPlaying) {
			pause();
			Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
				@Override
				public boolean execute() {
					play();
					return false;
				}
			}, gameSpeed);
		}
		return true;
	}
	
	public int getTurns() {
		return turn;
	}
	
	public boolean wasReset() {
		return wasReset;
	}

}
