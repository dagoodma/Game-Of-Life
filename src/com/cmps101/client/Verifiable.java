package com.cmps101.client;

public abstract class Verifiable {
	static int total = 0;
	final int id;
	VerifierList verifier;
	Verifiable prevVerified, nextVerified;
	
	public Verifiable() {
		id = total++;
	}
	public int getId() { return id; }
	public void setVerifier(VerifierList verifier) { this.verifier = verifier; }
	public VerifierList getVerifier() { return verifier; }
	public void clearVerification() {
		verifier = null;
		nextVerified = null;
		prevVerified = null;
	}
	
	public boolean hasNextVerified() {
		return nextVerified != null;
	}
	public boolean hasPrevVerified() {
		return prevVerified != null;
	}
	
	/* Verifier pointer accessors */
	public Verifiable getNextVerified() { return nextVerified; }
	public Verifiable getPrevVerified() { return prevVerified; }
	public void setNextVerified(Verifiable next) {
		nextVerified = next;
	}
	public void setPrevVerified(Verifiable prev) {
		prevVerified = prev;
	}
	
	public boolean equals(Verifiable obj) {
		return id == obj.getId();
	}
	
	public static int getTotal() {
		return total;
	}
}
