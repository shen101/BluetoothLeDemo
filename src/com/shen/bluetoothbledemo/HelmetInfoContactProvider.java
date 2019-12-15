package com.shen.bluetoothbledemo;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.widget.Toast;
import java.util.HashMap;

public class HelmetInfoContactProvider extends ContentProvider {

	// Content provider
	static final String PROVIDER_NAME = "com.shen.bluetoothbleconnectdemo.ContactProvider";
	static final String INFO = "infos";
	static final String URL = "content://" + PROVIDER_NAME + "/" + INFO;
	public static final Uri CONTENT_URL = Uri.parse(URL);

	// Uri code
	static final int uriCode = 1;

	static HashMap<String, String> mValues;

	static SQLiteDatabase sqlDB;

	// Database infos
	static final String DATABASE_NAME = "HelmetData.db";
	static final String TABLE_NAME = "value_names";

	// is frist into values
	public static final String FIRST_INTO = "isfrist";
	// camera values
	public static final String FRONT_PHOTO_CAMERA = "front_photo_camera";
	public static final String FRONT_VIDEO_CAMERA = "front_video_camera";
	public static final String REAR_PHOTO_CAMERA = "rear_photo_camera";
	public static final String REAR_VIDEO_CAMERA = "rear_video_camera";
	public static final String CREATE_TIME = "create_time";
	// sos contact values
	public static final String SOS_CONTACT_NAME = "sos_contact_name";
	public static final String SOS_CONTACT_NUMBER = "sos_contact_number";
	public static final String SOS_CONTACT_EMAIL = "sos_contact_email";
	// ubind clent mac
	public static final String UBIND_CLIENT_MAC = "ubind_client_mac";
	public static final String UBIND_SERVICE_MAC = "ubind_service_mac";
	// taillight values
	public static final String REAR_TAILLIGHT_ON_OFF = "taillight_on_off";
	public static final String REAR_TAILLIGHT_RUNNING = "taillight_running";
	public static final String REAR_TAILLIGHT_EMERGENCY = "taillight_emergency";
	public static final String REAR_TAILLIGHT_STEADY = "taillight_steady";
	// helmet mac save address
	public static final String SERVICE_HELMET_MAC = "service_helmet_mac";
	public static final String CLIENT_CONTROL_MAC = "client_control_mac";
	// ubind values
	public static final String IS_UBIND = "is_ubind";
	public static final String CONTACT_SOS_SWITCH = "isopen";
	// playing music
	public static final String PLAYING_MUSIC_POSITION = "playing_position";
	public static final String PLAYING_MUSIC_PATH = "playing_path";
	static final int DATABASE_VERSION = 1;
	static final String CREATE_DB_TABLE = "CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ FIRST_INTO + " integer, " + SOS_CONTACT_NAME + " text, " + SOS_CONTACT_NUMBER + " text, "
			+ SOS_CONTACT_EMAIL + " text, " + IS_UBIND + " integer, " + UBIND_CLIENT_MAC + " text, " + UBIND_SERVICE_MAC
			+ " text, " + REAR_TAILLIGHT_ON_OFF + " integer, " + REAR_TAILLIGHT_RUNNING + " integer, "
			+ REAR_TAILLIGHT_EMERGENCY + " integer, " + REAR_TAILLIGHT_STEADY + " integer, " + PLAYING_MUSIC_POSITION
			+ " text, " + PLAYING_MUSIC_PATH + " text, " + FRONT_PHOTO_CAMERA + " text, " + FRONT_VIDEO_CAMERA
			+ " text, " + REAR_PHOTO_CAMERA + " text, " + REAR_VIDEO_CAMERA + " text, " + SERVICE_HELMET_MAC + " text, "
			+ CLIENT_CONTROL_MAC + " text, " + CREATE_TIME + " Long)";

	static final UriMatcher uriMatcher;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, INFO, uriCode);
	}

	public HelmetInfoContactProvider() {
	}

	/**
	 * Delete data
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int rowsDeleted = 0;
		switch (uriMatcher.match(uri)) {
		case uriCode:
			rowsDeleted = sqlDB.delete(TABLE_NAME, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("UNKNOWN Uri " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case uriCode:
			return "vnd.android.cursor.dir/" + INFO;
		default:
			throw new IllegalArgumentException("Unsupported Uri " + uri);
		}
	}

	/**
	 * Insert data
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long rowID = sqlDB.insert(TABLE_NAME, null, values);
		if (rowID > 0) {
			Uri _uri = ContentUris.withAppendedId(CONTENT_URL, rowID);
			getContext().getContentResolver().notifyChange(_uri, null);
			return _uri;
		} else {
			Toast.makeText(getContext(), "Row Insert Failed", Toast.LENGTH_SHORT).show();
			return null;
		}
	}

	/**
	 * When contactProvider is created
	 */
	@Override
	public boolean onCreate() {
		DataBaseHelper dbHelper = new DataBaseHelper(getContext());
		sqlDB = dbHelper.getWritableDatabase();
		if (sqlDB != null) {
			return true;
		}
		return false;
	}

	/**
	 * Query
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(TABLE_NAME);
		switch (uriMatcher.match(uri)) {
		case uriCode:
			queryBuilder.setProjectionMap(mValues);
			break;
		default:
			throw new IllegalArgumentException("UNKNOWN Uri " + uri);
		}
		Cursor cursor = queryBuilder.query(sqlDB, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	/**
	 * Update data
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int rowsUpdate = 0;
		switch (uriMatcher.match(uri)) {
		case uriCode:
			rowsUpdate = sqlDB.update(TABLE_NAME, values, selection, selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("UNKNOWN Uri " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdate;
	}

	/**
	 * Custom of SQLiteOpenHelper
	 */
	private static class DataBaseHelper extends SQLiteOpenHelper {

		public DataBaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase) {
			sqLiteDatabase.execSQL(CREATE_DB_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
			sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(sqLiteDatabase);
		}
	}
}
