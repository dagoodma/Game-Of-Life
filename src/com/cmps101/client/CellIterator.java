package com.cmps101.client;

import java.util.Iterator;

/**
 * Handles both a CellList and a VerifierList.
 * 
 * @author <a href="mailto:dagoodma@ucsc.edu">David Goodman</a>
 */
public class CellIterator implements Iterator<Cell> {
	private int size;
	private CellList list;
	private VerifierList vList;
	private Cell curr;
	
	public CellIterator(CellList list) {
		this.list = list;
		curr = list.getFirst();
		size = list.size();
	}
	
	public CellIterator(VerifierList vList) {
		this.vList = vList;
		this.list = null;
		curr = vList.getFirst();
		size = vList.size();
	}
	
	public boolean hasNext() {
		return (curr != null);
	}
	
	public Cell next() {
		Cell next = curr;
		if (list != null)
			curr = curr.getNext();
		else
			curr = (Cell)curr.getNextVerified();
		return next;
	}
	
	public Cell seeNext() {
		return curr;
	}

	@Override
	public void remove() {
		if (list != null)
			list.remove(curr.getPrev());
		else
			vList.remove((Cell)curr.getPrevVerified());
	}
}
