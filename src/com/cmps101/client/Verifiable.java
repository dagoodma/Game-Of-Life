package com.cmps101.client;

public abstract class Verifiable {
	static int total = 0;
	final int id;
	CellList verifier;
	Verifiable nextVerified
	
	public Verifiable() {
		id = total++;
	}
	public int getId() { return id; }
	public void setVerifier(CellList verifier) { this.verifier = verifier; }
	public CellList getVerifier() { return verifier; }
	
	public boolean equals(Verifiable obj) {
		return id == obj.getId();
	}
	
	public static int getTotal() {
		return total;
	}
}
