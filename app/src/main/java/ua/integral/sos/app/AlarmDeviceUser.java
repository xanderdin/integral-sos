package ua.integral.sos.app;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by aledin on 26.01.15.
 */
public class AlarmDeviceUser implements Comparable {

    private final long devId;

    private final int userNum;
    private String userName;

    private Uri userUri;
    private long rowId;

    private final Context context;

    public AlarmDeviceUser(Context context, long devId, int userNum) {
        this.context = context;
        this.devId = devId;
        this.userNum = userNum;
        tieWithDbRecord();
    }


    public AlarmDeviceUser(Context context, long devId, int userNum, String userName) {
        this(context, devId, userNum);
        setUserName(userName);
    }

    private void tieWithDbRecord() {

        String projection[] = {
                AppDb.AlarmDeviceUserTable.COLUMN_ID,
                AppDb.AlarmDeviceUserTable.COLUMN_USER_NUM,
                AppDb.AlarmDeviceUserTable.COLUMN_USER_NAME,
        };

        String selection = AppDb.AlarmDeviceUserTable.COLUMN_DEV_ID + " = ? AND " +
                AppDb.AlarmDeviceUserTable.COLUMN_USER_NUM + " = ?";

        String selectionArgs[] = { String.valueOf(devId), String.valueOf(userNum) };

        Cursor cursor = getContentResolver().query(
                AlarmDeviceUserProvider.CONTENT_URI, projection, selection, selectionArgs, null);

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {

            rowId = cursor.getLong(0);

            userUri = ContentUris.withAppendedId(AlarmDeviceUserProvider.CONTENT_URI_ID_BASE, rowId);

            if (!cursor.isNull(1)) userName = cursor.getString(1);

            cursor.close();

            return;
        }

        cursor.close();

        ContentValues values = new ContentValues();

        values.put(AppDb.AlarmDeviceUserTable.COLUMN_DEV_ID, devId);
        values.put(AppDb.AlarmDeviceUserTable.COLUMN_USER_NUM, userNum);
        values.put(AppDb.AlarmDeviceUserTable.COLUMN_USER_NAME, userName);

        userUri = getContentResolver().insert(AlarmDeviceUserProvider.CONTENT_URI, values);

        rowId = Long.valueOf(userUri.getPathSegments().get(1));
    }


    private void setProviderBooleanValue(String column, Boolean val) {
        MiscProviderFunc.setProviderBooleanValue(getContext(), userUri, column, val);
    }


    private void setProviderStringValue(String column, String val) {
        MiscProviderFunc.setProviderStringValue(getContext(), userUri, column, val);
    }


    private Context getContext() {
        return context;
    }


    private ContentResolver getContentResolver() {
        return getContext().getContentResolver();
    }


    public int getUserNum() {
        return userNum;
    }


    public String getUserName() {
        return userName;
    }


    public void setUserName(String userName) {
        setProviderStringValue(AppDb.AlarmDeviceUserTable.COLUMN_USER_NAME, userName);
        this.userName = userName;
    }


    public long getRowId() {
        return rowId;
    }


    public synchronized void clear() {
    }


    public synchronized void remove() {
        clear();
        MiscProviderFunc.removeProviderRecord(getContext(), userUri);
    }

    @Override
    public int compareTo(Object another) {

        if (this.userNum < ((AlarmDeviceUser) another).userNum) {
            return -1;
        } else if (this.userNum > ((AlarmDeviceUser) another).userNum) {
            return 1;
        }

        return 0;
    }
}
