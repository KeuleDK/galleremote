package com.rene_arnold.galleremote.util;

import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.rene_arnold.galleremote.model.Image;
import com.rene_arnold.galleremote.model.Setting;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	public static final int DATABASE_VERSION = 3;
	public static final String DATABASE_NAME = "Galleremote.db";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, Image.class);
			TableUtils.createTable(connectionSource, Setting.class);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
			int oldVersion, int newVersion) {
		try {
			if (oldVersion < 2) {
				TableUtils.createTable(connectionSource, Setting.class);
			}
			if (oldVersion < 3) {
				TableUtils.dropTable(connectionSource, Image.class, true);
				TableUtils.createTable(connectionSource, Image.class);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
