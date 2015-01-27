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
public class EventHistoryProvider extends ContentProvider {

    private final static String AUTHORITY = EventHistoryProvider.class.getName();

    private final static String TABLE_NAME = AppDb.EventHistoryTable.TABLE_NAME;

    private final static String RECORD_REF = TABLE_NAME + "/#";

    public final static Uri CONTENT_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTHORITY + "/" + TABLE_NAME);

    private final static Uri CONTENT_URI_ID_BASE = Uri.parse(CONTENT_URI + "/");

    // The incoming URI matches the table URI pattern
    private final static int LIST = 1;

    // The incoming URI matches the record ID URI pattern
    private final static int ITEM = 2;

    // A UriMatcher instance
    private final static UriMatcher sUriMatcher;

    // A projection map used to select columns from the database
    private static final HashMap<String, String> sProjectionMap;

    // A block that instantiates and sets static objects
    static {

        // Creates and initializes URI matcher
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Add a pattern that routes table URI to the LIST operation
        sUriMatcher.addURI(AUTHORITY, TABLE_NAME, LIST);

        // Add a pattern that routes table URI plus an integer to the ITEM operation
        sUriMatcher.addURI(AUTHORITY, RECORD_REF, ITEM);

        // Create and initialize a projection map
        sProjectionMap = new HashMap<String, String>();

        for (String s : AppDb.EventHistoryTable.COLUMNS) {
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

                qb.appendWhere(AppDb.EventHistoryTable.COLUMN_ID + "=" + uri.getPathSegments().get(1));

                break;

            default:

                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Cursor res = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        // Tells the cursor what URI to watch, so it knows when its source data changes
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

        // A map to hold the new record's values
        ContentValues newValues;

        // If the incoming values map is not null, uses it for the new values,
        // Otherwise, create a new value map
        if (values != null) {
            newValues = new ContentValues(values);
        } else {
            newValues = new ContentValues();
        }

        if (newValues.containsKey(AppDb.EventHistoryTable.COLUMN_DATE_TIME) == false) {
            newValues.put(AppDb.EventHistoryTable.COLUMN_DATE_TIME, MiscFunc.now());
        }

        long rowId = db.insert(TABLE_NAME, null, newValues);

        if (rowId <= 0) {
            throw new SQLException("Failed to insert row into " + uri);
        }

        // Creates a URI with the record ID pattern and the new row ID appended to it
        Uri res = ContentUris.withAppendedId(CONTENT_URI_ID_BASE, rowId);

        deleteOldRecords(db, uri, rowId, newValues);

        // Notify observers registered against this provider that the data changed.
        getContext().getContentResolver().notifyChange(res, null);

        return res;
    }


    private void deleteOldRecords(SQLiteDatabase db, Uri uri, long rowId, ContentValues contentValues) {

        int mr = CommonVar.getMaxHistoryRecords();

        if (mr <= 0) {
            return;
        }

        if (!contentValues.containsKey(AppDb.EventHistoryTable.COLUMN_DEV_ID)) {
            return;
        }

        String selection = AppDb.EventHistoryTable.COLUMN_DEV_ID + " = ? ";

        String selectionArgs[] = {
                contentValues.getAsString(AppDb.EventHistoryTable.COLUMN_DEV_ID)
        };

        String qry = "SELECT " + AppDb.EventHistoryTable.COLUMN_ID + " FROM " +
                AppDb.EventHistoryTable.TABLE_NAME + " WHERE " + selection +
                " ORDER BY " + AppDb.EventHistoryTable.COLUMN_ID + " DESC";

        Cursor cursor = db.rawQuery(qry, selectionArgs);

        cursor.move(mr);

        while (cursor.moveToNext()) {
            db.delete(AppDb.EventHistoryTable.TABLE_NAME,
                    AppDb.EventHistoryTable.COLUMN_ID + " = ?",
                    new String[] { cursor.getString(0) });
        }

        cursor.close();
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

                where = AppDb.EventHistoryTable.COLUMN_ID + "=" + uri.getPathSegments().get(1);

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
        return 0;
    }
}
