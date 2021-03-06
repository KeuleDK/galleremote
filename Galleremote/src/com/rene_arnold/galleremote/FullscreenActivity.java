package com.rene_arnold.galleremote;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.rene_arnold.galleremote.event.DelayChangedEvent;
import com.rene_arnold.galleremote.event.ImagesChangedEvent;
import com.rene_arnold.galleremote.event.ReloadEvent;
import com.rene_arnold.galleremote.event.SyncStartEvent;
import com.rene_arnold.galleremote.event.SyncUpdateEvent;
import com.rene_arnold.galleremote.model.Image;
import com.rene_arnold.galleremote.model.Setting;
import com.rene_arnold.galleremote.receivers.EventReceiver;
import com.rene_arnold.galleremote.util.DatabaseHelper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

/**
 *
 */
public class FullscreenActivity extends Activity {

	/**
	 * the {@link ImageSwitcher} where the {@link Image}s are shown
	 */
	private ImageSwitcher switcher;

	private ProgressBar progressBar;
	/**
	 * a pointer to the currently shown image
	 */
	private int counter = 0;

	/**
	 * the {@link List} of {@link Image}s
	 */
	private volatile List<Image> images;

	private EventReceiver eventReceiver;
	private DatabaseHelper databaseHelper;

	/**
	 * a {@link Handler} to post actions
	 */
	private Handler handler = new Handler();

	private static final int RELOAD_DELAY = 15 * 60 * 1000; // 15 min
	private static final long FALLBACK_DELAY = 2 * 60 * 1000; // 2 min

	private long exitRequest = 0;

	/**
	 * the time to wait until the next {@link Image} should be loaded
	 */
	private long delay = FALLBACK_DELAY;

	private Runnable showNextImage;
	private Runnable reloadRequest;
	private Runnable startImageScrollRunnable;

	private Map<Image, BitmapDrawable> bitmapTable = new HashMap<Image, BitmapDrawable>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_fullscreen);

		databaseHelper = new DatabaseHelper(this);
		eventReceiver = new EventReceiver(this);
		EventBus.getDefault().register(eventReceiver);
		EventBus.getDefault().register(this);

		View contentView = findViewById(R.id.image_switcher);

		if (contentView instanceof ImageSwitcher) {
			switcher = (ImageSwitcher) contentView;
			switcher.setFactory(new ViewFactory() {
				@Override
				public View makeView() {
					ImageView myView = new ImageView(getApplicationContext());
					myView.setScaleType(ImageView.ScaleType.FIT_CENTER);
					myView.setLayoutParams(
							new ImageSwitcher.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
					return myView;
				}
			});

			// add cross-fading
			Animation aniIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
			aniIn.setDuration(1000);
			Animation aniOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
			aniOut.setDuration(1000);

			switcher.setInAnimation(aniIn);
			switcher.setOutAnimation(aniOut);
			switcher.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					handler.removeCallbacks(startImageScrollRunnable);
					handler.removeCallbacks(showNextImage);
					handler.removeCallbacks(reloadRequest);
					nextImage();
					handler.postDelayed(startImageScrollRunnable, 30000);
				}
			});
		}

		View progressBar = findViewById(R.id.progressBar1);
		if (progressBar instanceof ProgressBar) {
			this.progressBar = (ProgressBar) progressBar;
			this.progressBar.setVisibility(View.INVISIBLE);
		}

		reloadRequest = new Runnable() {
			@Override
			public void run() {
				EventBus.getDefault().post(new ReloadEvent());
				handler.postDelayed(this, RELOAD_DELAY);
			}
		};

		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
		handler.postDelayed(reloadRequest, RELOAD_DELAY);
		startImageScrollRunnable = new Runnable() {

			@Override
			public void run() {
				handler.removeCallbacks(showNextImage);
				handler.removeCallbacks(reloadRequest);
				handler.postDelayed(showNextImage, delay);
				handler.postDelayed(reloadRequest, RELOAD_DELAY);
				Toast toast = Toast.makeText(FullscreenActivity.this, R.string.automatic_screenplay, Toast.LENGTH_LONG);
				toast.show();
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			}
		};
	}

	@Override
	protected void onDestroy() {
		EventBus.getDefault().unregister(eventReceiver);
		EventBus.getDefault().unregister(this);
		for (Image image : bitmapTable.keySet()) {
			BitmapDrawable bitmapDrawable = bitmapTable.get(image);
			bitmapDrawable.getBitmap().recycle();
		}
		images.clear();
		bitmapTable.clear();
		handler.removeCallbacks(reloadRequest);
		handler.removeCallbacks(startImageScrollRunnable);
		handler.removeCallbacks(showNextImage);
		super.onDestroy();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		try {
			QueryBuilder<Image, ?> queryBuilder = databaseHelper.getDao(Image.class).queryBuilder();
			queryBuilder.orderBy(Image.COLUMN_POSITION, true);
			this.images = queryBuilder.query();
		} catch (Exception e) {
			this.images = new ArrayList<Image>();
		}
		try {
			Dao<Setting, Integer> settingDao = databaseHelper.getDao(Setting.class);
			List<Setting> query = settingDao.queryForEq(Setting.COLUMN_KEY, "delay");
			if (query.size() == 1) {
				delay = Long.valueOf(query.get(0).getValue());
			}
		} catch (Exception e) {
		}
		for (Image image : images) {
			try {
				Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image.getSavePoint());
				BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
				bitmapTable.put(image, drawable);
			} catch (Exception e) {
				Log.w(FullscreenActivity.class.getSimpleName(), e.getClass().getSimpleName(), e);
			} catch (OutOfMemoryError e) {
				Log.w(FullscreenActivity.class.getSimpleName(), e.getClass().getSimpleName(), e);
			}
		}

		if (images.size() > 0) {
			setImage(images.iterator().next());
		}
		showNextImage = new Runnable() {
			@Override
			public void run() {
				FullscreenActivity.this.nextImage();
				handler.postDelayed(this, delay);
			}
		};
		handler.postDelayed(showNextImage, delay);
		EventBus.getDefault().post(new ReloadEvent());
	}

	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			handler.removeCallbacks(startImageScrollRunnable);
			handler.removeCallbacks(showNextImage);
			handler.removeCallbacks(reloadRequest);
			prevImage();
			handler.postDelayed(startImageScrollRunnable, 30000);
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			handler.removeCallbacks(startImageScrollRunnable);
			handler.removeCallbacks(showNextImage);
			handler.removeCallbacks(reloadRequest);
			nextImage();
			handler.postDelayed(startImageScrollRunnable, 30000);
			break;
		case KeyEvent.KEYCODE_DPAD_CENTER:
			handler.post(startImageScrollRunnable);
			break;
		case KeyEvent.KEYCODE_BACK:
			long now = System.currentTimeMillis();
			if (now - exitRequest < 5000) {
				finish();
			} else {
				exitRequest = System.currentTimeMillis();
				Toast t = Toast.makeText(this, R.string.exit_confirmation, Toast.LENGTH_LONG);
				t.show();
			}
			break;
		}

		return true;
	}

	private void nextImage() {
		if (images.isEmpty()) {
			counter = 0;
			// setImage(null);
			return;
		}
		counter = (counter + 1) % images.size();
		setImage(getCurrentImage());
	}

	private Image getCurrentImage() {
		return images.get(counter);
	}

	private void prevImage() {
		counter = (counter - 1) % images.size();
		if (counter < 0)
			counter += images.size();
		setImage(getCurrentImage());
	}

	public void setImageFile(File file) {
		// recyleOldImage();
		switcher.setImageURI(Uri.fromFile(file));
	}

	public void setImage(Image image) {
		if (image == null)
			setImage(null);
		BitmapDrawable drawable = bitmapTable.get(image);
		switcher.setImageDrawable(drawable);
	}

	public DatabaseHelper getDatabaseHelper() {
		return databaseHelper;
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onDelayChangedEvent(DelayChangedEvent event) {
		if (event.getNewDelay() == null)
			return;
		delay = event.getNewDelay().longValue();
		handler.removeCallbacks(showNextImage);
		showNextImage = new Runnable() {

			@Override
			public void run() {
				FullscreenActivity.this.nextImage();
				handler.postDelayed(this, delay);
			}
		};
		handler.postDelayed(showNextImage, delay);

		startImageScrollRunnable = new Runnable() {

			@Override
			public void run() {
				handler.removeCallbacks(showNextImage);
				handler.removeCallbacks(reloadRequest);
				handler.postDelayed(showNextImage, delay);
				handler.postDelayed(reloadRequest, RELOAD_DELAY);
				Toast toast = Toast.makeText(FullscreenActivity.this, R.string.automatic_screenplay, Toast.LENGTH_LONG);
				toast.show();
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
			}
		};

		try {
			Dao<Setting, Integer> dao = databaseHelper.getDao(Setting.class);
			List<Setting> query = dao.queryForEq(Setting.COLUMN_KEY, "delay");
			if (query.size() == 1) {
				Setting setting = query.get(0);
				setting.setValue(Long.valueOf(delay).toString());
				dao.update(setting);
			} else {
				dao.delete(query);
				Setting setting = new Setting("delay", Long.valueOf(delay).toString());
				dao.create(setting);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onImagesChangedEvent(ImagesChangedEvent event) {
		handler.removeCallbacks(showNextImage);
		boolean wasEmpty = images.isEmpty();
		Image currentImage = null;
		if (!wasEmpty)
			currentImage = getCurrentImage();
		images = event.getImages();
		for (Image image : images) {
			if (!bitmapTable.containsKey(image)) {
				try {
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image.getSavePoint());
					BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
					bitmapTable.put(image, drawable);
				} catch (Exception e) {
					Log.w(FullscreenActivity.class.getSimpleName(), e.getClass().getSimpleName(), e);
				} catch (OutOfMemoryError e) {
					Log.w(FullscreenActivity.class.getSimpleName(), e.getClass().getSimpleName(), e);
				}
			}
		}
		Image newImage = getCurrentImage();
		if (wasEmpty && !images.isEmpty()) {
			setImage(images.iterator().next());
		} else if (currentImage != null && !currentImage.getImageAddress().equals(newImage.getImageAddress())) {
			// if current image is replaced -> show new image
			setImage(newImage);
		}
		handler.postDelayed(showNextImage, delay);
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onSyncStartEvent(SyncStartEvent event) {
		this.progressBar.setVisibility(View.VISIBLE);
		this.progressBar.setProgress(0);
		this.progressBar.setMax(event.getLength());
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onSyncUpdateEvent(SyncUpdateEvent event) {
		this.progressBar.setProgress(event.getPos());
		if(progressBar.getProgress() == progressBar.getMax()){
			progressBar.setVisibility(View.INVISIBLE);
		}
	}
}
