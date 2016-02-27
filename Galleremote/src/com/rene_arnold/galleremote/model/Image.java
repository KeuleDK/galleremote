package com.rene_arnold.galleremote.model;

import java.net.MalformedURLException;
import java.net.URL;

import android.net.Uri;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Image.TABLE_NAME)
public class Image implements Comparable<Image> {

	public static final String COLUMN_ID = "id";
	public static final String COLUMN_SAVE_POINT = "savePoint";
	public static final String COLUMN_IMAGE_ADDRESS = "imageAddress";
	public static final String COLUMN_POSITION = "position";

	public static final String TABLE_NAME = "image";

	@DatabaseField(generatedId = true, columnName = COLUMN_ID)
	private int id;

	@DatabaseField(columnName = COLUMN_IMAGE_ADDRESS)
	private String imageAddress;

	@DatabaseField(columnName = COLUMN_SAVE_POINT)
	private String savePoint;

	@DatabaseField(columnName = COLUMN_POSITION)
	private int position;

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
	 *          the imageAddress to set
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
	 *          the savePoint to set
	 */
	public void setSavePoint(Uri savePoint) {
		this.savePoint = savePoint.toString();
	}

	
	
	/**
	 * @return the position
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * @param position the position to set
	 */
	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result
				+ ((imageAddress == null) ? 0 : imageAddress.hashCode());
		result = prime * result + ((savePoint == null) ? 0 : savePoint.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Image other = (Image) obj;
		if (id != other.id)
			return false;
		if (imageAddress == null) {
			if (other.imageAddress != null)
				return false;
		} else if (!imageAddress.equals(other.imageAddress))
			return false;
		if (savePoint == null) {
			if (other.savePoint != null)
				return false;
		} else if (!savePoint.equals(other.savePoint))
			return false;
		return true;
	}

	@Override
	public int compareTo(Image another) {
		return position - another.position;
	}

}
