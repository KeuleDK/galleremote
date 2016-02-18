package com.rene_arnold.galleremote.receivers;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.j256.ormlite.dao.Dao;
import com.rene_arnold.galleremote.FullscreenActivity;
import com.rene_arnold.galleremote.event.DelayChangedEvent;
import com.rene_arnold.galleremote.event.ImagesChangedEvent;
import com.rene_arnold.galleremote.event.ReloadEvent;
import com.rene_arnold.galleremote.model.Image;
import com.rene_arnold.galleremote.services.HttpRestService;
import com.rene_arnold.galleremote.util.DatabaseHelper;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

public class EventReceiver {
	private static final String DEBUG_TAG = EventReceiver.class.getSimpleName();

	private Context context;

	private Long delayCache;
	private List<URL> imageCache;

	public EventReceiver(Context context) {
		this.context = context;
	}

	/**
	 * Gets the current {@link List} of Images and compares it to the
	 * {@link List} provided by the server. New {@link Image}s will be downloaded and added and
	 * no longer used {@link Image}s will be deleted from Database and
	 * File-System.
	 * 
	 * @param urls the {@link List} of {@link URL}s provided by the server
	 * @return the new {@link List} of {@link Image}s
	 * @throws SQLException
	 *             if the database is not accessible
	 */
	private List<Image> syncImages(List<URL> urls) throws SQLException {
		DatabaseHelper databaseHelper = ((FullscreenActivity) context).getDatabaseHelper();
		Dao<Image, Integer> dao = databaseHelper.getDao(Image.class);
		List<Image> imageList = dao.queryForAll();
		List<Image> newImages = new ArrayList<Image>();
		if (urls == null || urls.isEmpty()) {
			return imageList;
		}
		List<Image> unusedImages = new ArrayList<Image>(imageList);
		imageCache = new ArrayList<URL>(urls);
		if (context instanceof FullscreenActivity) {

			urlList: for (URL url : urls) {
				// search for existing
				for (Image image : imageList) {
					if (image.getImageAddress().equals(url)) {
						// if found -> take it
						unusedImages.remove(image);
						newImages.add(image);
						continue urlList;
					}
				}
				// if not found --> add it
				HttpRestService httpRestService = HttpRestService.getInstance(context);
				File document = httpRestService.downloadDocument(url);
				Uri uri = Uri.fromFile(document);
				Image image = new Image();
				image.setImageAddress(url);
				image.setSavePoint(uri);
				dao.create(image);
				newImages.add(image);
			}
			// at least -> delete unused images
			for (Image image : unusedImages) {
				deleteImage(image);
			}
			dao.delete(unusedImages);
		}
		return newImages;
	}

	private void deleteImage(Image image) {
		File file = new File(image.getSavePoint().getPath());
		file.delete();
	}

	@Subscribe(threadMode = ThreadMode.BACKGROUND)
	public void onReloadEvent(ReloadEvent event) throws SQLException {
		Log.d(DEBUG_TAG, "Reload data from Server");
		HttpRestService httpRestService = HttpRestService.getInstance(context);
		Long delay = httpRestService.getDelay();
		if (delay != null && (delayCache == null || delay.longValue() != delayCache.longValue())) {
			EventBus.getDefault().post(new DelayChangedEvent(delay));
			Log.d(DEBUG_TAG, "Delay changed");
			delayCache = delay;
		}
		List<URL> images = httpRestService.getImages();
		if (images == null)
			return;
		boolean equal = true;
		if (imageCache != null && imageCache.size() == images.size()) {
			for (int i = 0; i < imageCache.size(); i++) {
				if (!imageCache.get(i).equals(images.get(i))) {
					equal = false;
					break;
				}
			}
		} else {
			equal = false;
		}
		if (!equal) {
			imageCache = images;
			List<Image> syncImages = syncImages(images);
			Log.d(DEBUG_TAG, "Images changed");
			EventBus.getDefault().post(new ImagesChangedEvent(syncImages));
		}
	}
}
