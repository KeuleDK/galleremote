package com.rene_arnold.galleremote.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Setting.TABLE_NAME)
public class Setting {

	public static final String TABLE_NAME = "settings";
	public static final String COLUMN_KEY = "key";
	public static final String COLUMN_VALUE = "value";

	@DatabaseField(generatedId = true)
	public int id;

	@DatabaseField(columnName = COLUMN_KEY)
	public String key;

	@DatabaseField(columnName = COLUMN_VALUE)
	public String value;

	@Deprecated
	public Setting() {
	}

	public Setting(String key, String value) {
		super();
		this.key = key;
		this.value = value;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
