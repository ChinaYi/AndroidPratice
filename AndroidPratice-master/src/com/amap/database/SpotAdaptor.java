package com.amap.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.amap.api.location.AMapLocation;
import com.amap.record.Spot;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库相关操作，用于存取轨迹记录
 * 
 */
public class SpotAdaptor {
	public static final String KEY_ROWID = "id";
	public static final String KEY_NAME = "spotname";
	public static final String KEY_SIZE = "size";
	public static final String KEY_CENTER_LNG = "centerlng";
	public static final String KEY_CENTER_LAT = "centerlat";

	private final static String DATABASE_PATH = android.os.Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/recordPath";
	
	static final String DATABASE_NAME = DATABASE_PATH + "/" + "record.db";
	private static final int DATABASE_VERSION = 1;
	private static final String SPOT_TABLE = "spot";
	private static final String SPOT_CREATE = "create table if not exists spot("
			+ KEY_ROWID
			+ " integer primary key autoincrement,"
			+ "spotname STRING,"
			+ "size STRING,"
			+ "centerlng STRING,"
			+ "centerlat STRING" + ");";

	public static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(SPOT_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}

	private Context mCtx = null;
	private DatabaseHelper dbHelper;
	private SQLiteDatabase db;

	// constructor
	public SpotAdaptor(Context ctx) {
		this.mCtx = ctx;
		dbHelper = new DatabaseHelper(mCtx);
	}

	public SpotAdaptor open() throws SQLException {

		db = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	public Cursor getall() {
		return db.rawQuery("SELECT * FROM record", null);
	}

	// remove an entry
	public boolean delete(long rowId) {

		return db.delete(SPOT_TABLE, "id=" + rowId, null) > 0;
	}

	
	/**
	 * 查询所有轨迹记录
	 * 
	 * @return
	 */
	public List<Spot> querySpotAll() {
		List<Spot> allSpot = new ArrayList<Spot>();
		Cursor allRecordCursor = db.query(SPOT_TABLE, getColumns(), null,
				null, null, null, null);
		while (allRecordCursor.moveToNext()) {
			Spot spot = new Spot();
			
			spot.setMid(allRecordCursor.getInt(allRecordCursor
					.getColumnIndex(SpotAdaptor.KEY_ROWID)));
			
			spot.setSpotName(allRecordCursor.getString(allRecordCursor
					.getColumnIndex(SpotAdaptor.KEY_NAME)));
			
			String size = allRecordCursor.getString(allRecordCursor.getColumnIndex(SpotAdaptor.KEY_SIZE));
			spot.setSize(Double.parseDouble(size));
			
			String centerLat = allRecordCursor.getString(allRecordCursor.getColumnIndex(SpotAdaptor.KEY_CENTER_LAT));
			spot.setSpotLat(Double.parseDouble(centerLat));
			
			String centerLng = allRecordCursor.getString(allRecordCursor.getColumnIndex(SpotAdaptor.KEY_CENTER_LNG));
			spot.setSpotLng(Double.parseDouble(centerLng));
			allSpot.add(spot);
		}
		Collections.reverse(allSpot);
		return allSpot;
	}
	

	/**
	 * 按照id查询
	 * 
	 * @param mSpotItemId
	 * @return
	 */
	public Spot querySpotById(int mSpotItemId) {
		String where = KEY_ROWID + "=?";
		String[] selectionArgs = new String[] { String.valueOf(mSpotItemId) };
		Cursor cursor = db.query(SPOT_TABLE, getColumns(), where,
				selectionArgs, null, null, null);
		Spot spot = new Spot();
		if (cursor.moveToNext()) {
			
			spot.setMid(cursor.getInt(cursor
					.getColumnIndex(SpotAdaptor.KEY_ROWID)));
			
			spot.setSpotName(cursor.getString(cursor
					.getColumnIndex(SpotAdaptor.KEY_NAME)));
			
			String size = cursor.getString(cursor.getColumnIndex(SpotAdaptor.KEY_SIZE));
			spot.setSize(Double.parseDouble(size));
			
			String centerLat = cursor.getString(cursor.getColumnIndex(SpotAdaptor.KEY_CENTER_LAT));
			spot.setSpotLat(Double.parseDouble(centerLat));
			
			String centerLng = cursor.getString(cursor.getColumnIndex(SpotAdaptor.KEY_CENTER_LNG));
			spot.setSpotLng(Double.parseDouble(centerLng));
		}
		return spot;
	}
	
	public List<AMapLocation> queryRecordsNearby(SpotAdaptor spot){
		
	}

	private String[] getColumns() {
		return new String[] { KEY_ROWID, KEY_NAME, KEY_CENTER_LAT, KEY_CENTER_LNG, KEY_SIZE };
	}
}
