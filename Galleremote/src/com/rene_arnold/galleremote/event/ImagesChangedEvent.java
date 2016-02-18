package com.rene_arnold.galleremote.event;

import java.util.List;

import com.rene_arnold.galleremote.model.Image;

import android.util.Log;

public class ImagesChangedEvent {

	private List<Image> images;

	public ImagesChangedEvent(List<Image> images) {
		Log.d(ImagesChangedEvent.class.getSimpleName(), "ImagesChangedEvent created");
		this.images = images;
	}

	public List<Image> getImages() {
		return images;
	}

}
