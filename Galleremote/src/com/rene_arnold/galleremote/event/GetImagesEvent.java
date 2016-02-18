package com.rene_arnold.galleremote.event;

import android.app.Activity;

public class GetImagesEvent {

	private Activity publisher;

	public GetImagesEvent(Activity publisher) {
		this.publisher = publisher;
	}

	public Activity getPublisher() {
		return publisher;
	}
}
