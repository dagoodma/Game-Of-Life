package com.cmps101.client;

import java.util.ArrayList;

import com.google.gwt.core.client.GWT;

/**
 * This class contains preset lists of cells
 * @author brelix
 *
 */
public enum CellPreset {
	CLEAR("Clear"),
	GLIDER("Glider", new Cell(22,20,true), new Cell(20,21,true),
			new Cell(22,21,true), new Cell(21,22,true),
			new Cell(22,22,true)),
	SMALL_EXPLODER("Small Exploder", new Cell(21,18,true),
			new Cell(20,19,true), new Cell(21,19,true),
			new Cell(22,19,true), new Cell(20,20,true),
			new Cell(22,20,true), new Cell(21,21,true)),
	BIG_EXPLODER("Big Exploder", new Cell(18,18,true),
			new Cell(20,18,true), new Cell(22,18,true),
			new Cell(18,19,true), new Cell(22,19,true),
			new Cell(18,20,true), new Cell(22,20,true),	
			new Cell(18,21,true), new Cell(22,21,true),
			new Cell(18,22,true), new Cell(20,22,true),
			new Cell(22,22,true)),
	TRAFFIC_LIGHT("Traffic Light", new Cell(20,18,true),
			new Cell(21,18,true), new Cell(22,18,true),
			new Cell(19,19,true), new Cell(23,19,true),
			new Cell(19,20,true), new Cell(23,20,true),
			new Cell(19,21,true), new Cell(23,21,true),
			new Cell(20,22,true), new Cell(21,22,true),
			new Cell(22,22,true)),
	TWO_OSCILLATORS("Two Oscillators", new Cell(30,19,true),
			new Cell(20,20,true), new Cell(21,20,true),
			new Cell(22,20,true), new Cell(30,20,true),
			new Cell(30,21,true)),
	LIGHTWEIGHT_SHIP("Lightweight Spaceship", new Cell(22,21,true),
			new Cell(25,21,true), new Cell(26,22,true),
			new Cell(22,23,true), new Cell(26,23,true),
			new Cell(23,24,true), new Cell(24,24,true),
			new Cell(25,24,true), new Cell(26,24,true)),
	GLIDER_GUN("Glider Gun", new Cell(25,4,true), new Cell(26,4,true),
			new Cell(36,4,true), new Cell(37,4,true),
			new Cell(24,5,true), new Cell(26,5,true),
			new Cell(36,5,true), new Cell(37,5,true),
			new Cell(2,6, true), new Cell(3,6,true),
			new Cell(11,6,true), new Cell(12,6,true),
			new Cell(24,6,true), new Cell(25,6,true),
			new Cell(2,7,true), new Cell(3,7,true),
			new Cell(10,7,true), new Cell(12,7,true),
			new Cell(10,8,true), new Cell(11,8,true),
			new Cell(18,8,true), new Cell(19,8,true),
			new Cell(18,9,true), new Cell(20,9,true),
			new Cell(18,10,true), new Cell(37,11,true),
			new Cell(38,11,true), new Cell(37,12,true),
			new Cell(39,12,true), new Cell(37,13,true),
			new Cell(26,16,true), new Cell(27,16,true),
			new Cell(28,16,true), new Cell(26,17,true),
			new Cell(27,18,true));
	
	private CellList list = new CellList();
	private String name = "";
	CellPreset(String name) {
		this.name = name;
		list = new CellList();
	}

	CellPreset(String name, Cell ... cells) {
		this(name);
		for (Cell c : cells) {
			list.add(c);
		}
	}
	
	public String toString() { return getName() + ": " + list; }
	public String getName() { return name; }
	
	/**
	 * Returns the cellList for the current preset.
	 * @return CellList of the preset
	 */
	public CellList getList() {
		return list;
	}
	
}
