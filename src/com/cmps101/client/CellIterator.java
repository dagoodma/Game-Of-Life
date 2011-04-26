package com.cmps101.client;

import java.util.Iterator;

public class CellIterator implements Iterator<Cell> {
	private int size;
	private CellList list;
	private Cell curr;
	
	public CellIterator(CellList list) {
		this.list = list;
		curr = list.getFirst();
		size = list.size();
	}
	
	public boolean hasNext() {
		return (curr != null);
	}
	
	public Cell next() {
		Cell next = curr;
		curr = curr.getNext();
		return next;
	}
	
	public Cell seeNext() {
		return curr;
	}

	@Override
	public void remove() {
		list.remove(curr.getPrev());
	}
}
