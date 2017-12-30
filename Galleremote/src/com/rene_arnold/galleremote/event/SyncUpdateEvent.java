package com.rene_arnold.galleremote.event;

public class SyncUpdateEvent {

	private int pos;

	public SyncUpdateEvent(int pos) {
		this.pos = pos;
	}

	public int getPos() {
		return pos;
	}

}
