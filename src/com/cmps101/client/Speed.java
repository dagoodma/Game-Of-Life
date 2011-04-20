package com.cmps101.client;

public enum Speed {
	SLOWEST (750, "Slowest"),
	SLOW (500, "Slow"),
	NORMAL (250, "Normal"),
	FAST (100, "Fast"),
	FASTEST (50, "Fastest");
	
	private final int value; // in ms
	private final String text;
	Speed(int speed, String text) {
		this.value = speed;
		this.text = text;
	}
		
	public int value() {
		return value;
	}
	
	public String toString() {
		return text;
	}
}
