package com.rene_arnold.galleremote.event;

import android.util.Log;

public class DelayChangedEvent {

	private Long newDelay;

	public DelayChangedEvent(Long newDelay) {
		Log.d(DelayChangedEvent.class.getSimpleName(), "DelayChangedEvent created");
		this.newDelay = newDelay;
	}

	public Long getNewDelay() {
		return newDelay;
	}
}
