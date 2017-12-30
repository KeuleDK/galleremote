package com.rene_arnold.galleremote.event;

public class SyncStartEvent {

	private int length;

	public SyncStartEvent(int length) {
		this.length = length;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return length;
	}

}
