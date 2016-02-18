package com.rene_arnold.galleremote.model;

import java.net.MalformedURLException;
import java.net.URL;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import android.net.Uri;

@DatabaseTable(tableName = Image.TABLE_NAME)
public class Image {

	public static final String COLUMN_ID = "id";
	public static final String COLUMN_SAVE_POINT = "savePoint";
	public static final String COLUMN_IMAGE_ADDRESS = "imageAddress";

	public static final String TABLE_NAME = "image";

	@DatabaseField(generatedId = true, columnName = COLUMN_ID)
	private int id;

	@DatabaseField(columnName = COLUMN_IMAGE_ADDRESS)
	private String imageAddress;

	@DatabaseField(columnName = COLUMN_SAVE_POINT)
	private String savePoint;

	/**
	 * @return the imageAddress
	 */
	public URL getImageAddress() {
		try {
			return new URL(imageAddress);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * @param imageAddress
	 *            the imageAddress to set
	 */
	public void setImageAddress(URL imageAddress) {
		this.imageAddress = imageAddress.toString();
	}

	/**
	 * @return the savePoint
	 */
	public Uri getSavePoint() {
		return Uri.parse(savePoint);
	}

	/**
	 * @param savePoint
	 *            the savePoint to set
	 */
	public void setSavePoint(Uri savePoint) {
		this.savePoint = savePoint.toString();
	}

}
