package ua.integral.sos.app;

import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;

/**
 * Created by aledin on 26.01.15.
 */
public class AlarmDeviceZoneProvider extends ContentProvider {

    private final static String AUTHORITY = AlarmDeviceZoneProvider.class.getName();

    private final static String TABLE_NAME = AppDb.AlarmDeviceZoneTable.TABLE_NAME;

    private final static String RECORD_REF = TABLE_NAME + "/#";

    public final static Uri CONTENT_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/" + TABLE_NAME);

    public final static Uri CONTENT_URI_ID_BASE = Uri.parse(CONTENT_URI + "/");

    private final static int LIST = 1;
    private final static int ITEM = 2;

    private final static UriMatcher sUriMatcher;

    private static final HashMap<String, String> sProjectionMap;

    static {

        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        sUriMatcher.addURI(AUTHORITY, TABLE_NAME, LIST);
        sUriMatcher.addURI(AUTHORITY, RECORD_REF, ITEM);

        sProjectionMap = new HashMap<String, String>();

        for (String s : AppDb.AlarmDeviceZoneTable.COLUMNS) {
            sProjectionMap.put(s, s);
        }
    }

    private AppDb appDb;


    @Override
    public boolean onCreate() {

        appDb = new AppDb(getContext());

        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = appDb.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(TABLE_NAME);
        qb.setProjectionMap(sProjectionMap);

        switch (sUriMatcher.match(uri)) {

            case LIST:

                break;

            case ITEM:

                qb.appendWhere(AppDb.AlarmDeviceZoneTable.COLUMN_ID + "=" + uri.getPathSegments().get(1));

                break;

            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor res = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        res.setNotificationUri(getContext().getContentResolver(), uri);

        return res;
    }


    @Override
    public String getType(Uri uri) {
        return null;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {

        if (sUriMatcher.match(uri) != LIST) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = appDb.getWritableDatabase();

        long rowId = db.insert(TABLE_NAME, null, values);

        if (rowId <= 0) {
            throw new SQLException("Failed to insert row into " + uri);
        }

        Uri res = ContentUris.withAppendedId(CONTENT_URI_ID_BASE, rowId);

        getContext().getContentResolver().notifyChange(res, null);

        return res;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = appDb.getWritableDatabase();

        int cnt;

        String where;


        switch (sUriMatcher.match(uri)) {

            case LIST:

                cnt = db.delete(TABLE_NAME, selection, selectionArgs);

                break;

            case ITEM:

                where = AppDb.AlarmDeviceZoneTable.COLUMN_ID + "=" + uri.getPathSegments().get(1);

                if (!TextUtils.isEmpty(selection)) {
                    where = "(" + where + ") AND (" + selection + ")";
                }

                cnt = db.delete(TABLE_NAME, where, selectionArgs);

                break;

            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return cnt;
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase db = appDb.getWritableDatabase();

        int cnt;

        String where;


        switch (sUriMatcher.match(uri)) {

            case LIST:

                cnt = db.update(TABLE_NAME, values, selection, selectionArgs);

                break;

            case ITEM:

                where = AppDb.AlarmDeviceZoneTable.COLUMN_ID + "=" + uri.getPathSegments().get(1);

                if (!TextUtils.isEmpty(selection)) {
                    where = "(" + where + ") AND (" + selection + ")";
                }

                cnt = db.update(AppDb.AlarmDeviceZoneTable.TABLE_NAME, values, where, selectionArgs);

                break;

            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return cnt;
    }
}
