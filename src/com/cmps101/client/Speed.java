package com.cmps101.client;

public enum Speed {
	SLOWEST (1000, "Slowest"),
	SLOW (500, "Slow"),
	NORMAL (175, "Normal"),
	FAST (69, "Fast"),
	FASTEST (8, "Fastest");
	
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
