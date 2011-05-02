package com.cmps101.client;

public abstract class Countable {
	static int total = 0;
	final int id;
	
	public Countable() {
		id = total++;
	}
	public int getId() { return id; }
	
	public boolean equals(Countable obj) {
		return id == obj.getId();
	}
	
	public static int getTotal() {
		return total;
	}
}
