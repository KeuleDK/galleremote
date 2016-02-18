package com.rene_arnold.galleremote.event;

import java.util.List;

import android.net.Uri;

public class ImagesGotEvent {

	private List<Uri> images;
	private long delay;

	public ImagesGotEvent(List<Uri> images, long delay) {
		this.images = images;
		this.delay = delay;
	}

	public List<Uri> getImages() {
		return images;
	}

	public long getDelay() {
		return delay;
	}

}
