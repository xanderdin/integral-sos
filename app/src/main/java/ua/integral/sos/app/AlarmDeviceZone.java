package ua.integral.sos.app;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by aledin on 26.01.15.
 */
public class AlarmDeviceZone implements Comparable {

    public final static int ST_OK     = 0;
    public final static int ST_SHORT  = 1;
    public final static int ST_TORN   = 2;

    private final long devId;

    private final int zoneNum;
    private Integer zoneState;
    private Boolean isArmed;
    private Boolean isFired;
    private Boolean isTamperOpened;
    private Boolean isBatteryLow;
    private Boolean isLinkLost;
    private Boolean isZoneFailure;
    private String zoneName;

    private Uri zoneUri;
    private long rowId;

    private final Context context;

    public AlarmDeviceZone(Context context, long devId, int zoneNum) {
        this.context = context;
        this.devId = devId;
        this.zoneNum = zoneNum;
        tieWithDbRecord();
    }


    private void tieWithDbRecord() {

        String projection[] = {
                AppDb.AlarmDeviceZoneTable.COLUMN_ID,
                AppDb.AlarmDeviceZoneTable.COLUMN_ZONE_NAME,
                AppDb.AlarmDeviceZoneTable.COLUMN_STATE,
                AppDb.AlarmDeviceZoneTable.COLUMN_IS_ARMED,
                AppDb.AlarmDeviceZoneTable.COLUMN_IS_FIRED,
                AppDb.AlarmDeviceZoneTable.COLUMN_IS_TAMPER_OPENED,
                AppDb.AlarmDeviceZoneTable.COLUMN_IS_BATTERY_LOW,
                AppDb.AlarmDeviceZoneTable.COLUMN_IS_LINK_LOST,
                AppDb.AlarmDeviceZoneTable.COLUMN_IS_ZONE_FAILURE,
        };

        String selection = AppDb.AlarmDeviceZoneTable.COLUMN_DEV_ID + " = ? AND " +
                AppDb.AlarmDeviceZoneTable.COLUMN_ZONE_NUM + " = ?";

        String selectionArgs[] = { String.valueOf(devId), String.valueOf(zoneNum) };

        Cursor cursor = getContentResolver().query(
                AlarmDeviceZoneProvider.CONTENT_URI, projection, selection, selectionArgs, null);

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {

            rowId = cursor.getLong(0);

            zoneUri = ContentUris.withAppendedId(AlarmDeviceZoneProvider.CONTENT_URI_ID_BASE, rowId);

            if (!cursor.isNull(1)) zoneName = cursor.getString(1);
            if (!cursor.isNull(2)) zoneState = cursor.getInt(2);
            if (!cursor.isNull(3)) isArmed = (cursor.getInt(3) != 0);
            if (!cursor.isNull(4)) isFired = (cursor.getInt(4) != 0);
            if (!cursor.isNull(5)) isTamperOpened = (cursor.getInt(5) != 0);
            if (!cursor.isNull(6)) isBatteryLow = (cursor.getInt(6) != 0);
            if (!cursor.isNull(7)) isLinkLost = (cursor.getInt(7) != 0);
            if (!cursor.isNull(8)) isZoneFailure = (cursor.getInt(8) != 0);

        } else {

            ContentValues values = new ContentValues();

            values.put(AppDb.AlarmDeviceZoneTable.COLUMN_DEV_ID, devId);
            values.put(AppDb.AlarmDeviceZoneTable.COLUMN_ZONE_NUM, zoneNum);

            zoneUri = getContentResolver().insert(AlarmDeviceZoneProvider.CONTENT_URI, values);

            rowId = Long.parseLong(zoneUri.getPathSegments().get(1));
        }

        cursor.close();
    }


    private void setProviderBooleanValue(String column, Boolean value) {
        MiscProviderFunc.setProviderBooleanValue(getContext(), zoneUri, column, value);
    }


    private void setProviderIntegerValue(String column, Integer value) {
        MiscProviderFunc.setProviderIntegerValue(getContext(), zoneUri, column, value);
    }


    private void setProviderStringValue(String column, String value) {
        MiscProviderFunc.setProviderStringValue(getContext(), zoneUri, column, value);
    }


    private Context getContext() {
        return context;
    }


    private ContentResolver getContentResolver() {
        return getContext().getContentResolver();
    }


    public int getZoneNum() {
        return zoneNum;
    }

    public long getRowId() {
        return rowId;
    }

    public Integer getZoneState() {
        return zoneState;
    }


    public void setZoneState(Integer zoneState) {
        setProviderIntegerValue(AppDb.AlarmDeviceZoneTable.COLUMN_STATE, zoneState);
        this.zoneState = zoneState;
    }


    public Boolean getIsArmed() {
        return isArmed;
    }


    public void setIsArmed(Boolean isArmed) {
        setProviderBooleanValue(AppDb.AlarmDeviceZoneTable.COLUMN_IS_ARMED, isArmed);
        this.isArmed = isArmed;
    }


    public Boolean getIsFired() {
        return isFired;
    }


    public void setIsFired(Boolean isFired) {
        setProviderBooleanValue(AppDb.AlarmDeviceZoneTable.COLUMN_IS_FIRED, isFired);
        this.isFired = isFired;
    }


    public Boolean getIsTamperOpened() {
        return isTamperOpened;
    }


    public void setIsTamperOpened(Boolean isTamperOpened) {
        setProviderBooleanValue(AppDb.AlarmDeviceZoneTable.COLUMN_IS_TAMPER_OPENED, isTamperOpened);
        this.isTamperOpened = isTamperOpened;
    }


    public Boolean getIsBatteryLow() {
        return isBatteryLow;
    }


    public void setIsBatteryLow(Boolean isBatteryLow) {
        setProviderBooleanValue(AppDb.AlarmDeviceZoneTable.COLUMN_IS_BATTERY_LOW, isBatteryLow);
        this.isBatteryLow = isBatteryLow;
    }


    public Boolean getIsLinkLost() {
        return isLinkLost;
    }


    public void setIsLinkLost(Boolean isLinkLost) {
        setProviderBooleanValue(AppDb.AlarmDeviceZoneTable.COLUMN_IS_LINK_LOST, isLinkLost);
        this.isLinkLost = isLinkLost;
    }


    public Boolean getIsZoneFailure() {
        return isZoneFailure;
    }


    public void setIsZoneFailure(Boolean isZoneFailure) {
        setProviderBooleanValue(AppDb.AlarmDeviceZoneTable.COLUMN_IS_ZONE_FAILURE, isZoneFailure);
        this.isZoneFailure = isZoneFailure;
    }


    public String getZoneName() {
        return TextUtils.isEmpty(zoneName) ? String.valueOf(zoneNum) : zoneName;
    }


    public void setZoneName(String zoneName) {
        setProviderStringValue(AppDb.AlarmDeviceZoneTable.COLUMN_ZONE_NAME, zoneName);
        this.zoneName = zoneName;
    }


    public synchronized void remove() {
        MiscProviderFunc.removeProviderRecord(getContext(), zoneUri);
    }


    public static boolean isInAlarm(Boolean isArmed, Boolean isFired, Boolean isTamperOpened, Boolean isLinkLost) {

        // Check zone tamper state despite of 'armed' state

        if (null != isTamperOpened && isTamperOpened == true) {
            return true;
        }

        // Check other zone states only when zone is armed.

        if (null == isArmed || isArmed != true) {
            return false;
        }

        if (null != isFired && isFired == true) {
            return true;
        }

        if (null != isLinkLost && isLinkLost == true) {
            return true;
        }

        return false;
    }

    public boolean isInAlarm() {
        return isInAlarm(getIsArmed(), getIsFired(), getIsTamperOpened(), getIsLinkLost());
    }


    @Override
    public int compareTo(Object another) {

        if (this.zoneNum < ((AlarmDeviceZone) another).zoneNum) {
            return -1;
        } else if (this.zoneNum > ((AlarmDeviceZone) another).zoneNum) {
            return 1;
        }

        return 0;
    }

    public static int getImgResourceId(Boolean isArmed, boolean isInAlarm) {

        int res;

        if (null == isArmed) {
            res = R.drawable.ic_question;
        } else if (isArmed && isInAlarm) {
            res = R.drawable.ic_lock_broken;
        } else if (isArmed) {
            res = R.drawable.ic_lock_closed;
        } else {
            res = R.drawable.ic_lock_opened;
        }

        return res;
    }


    public int getImgResourceId() {
        return getImgResourceId(getIsArmed(), isInAlarm());
    }


    public static int getLinkImgResourceId(Boolean isLinkLost) {

        int res;

        if (null == isLinkLost) {
            res = R.drawable.ic_action_dash;
        } else if (isLinkLost) {
            res = R.drawable.ic_action_link_lost;
        } else {
            res = R.drawable.ic_action_link_ok;
        }

        return res;
    }


    public int getLinkImgResourceId() {
        return getLinkImgResourceId(getIsLinkLost());
    }


    public static int getBatteryImgResourceId(Boolean isBatteryLow) {

        int res;

        if (null == isBatteryLow) {
            res = R.drawable.ic_action_dash;
        } else if (isBatteryLow) {
            res = R.drawable.ic_action_battery_low;
        } else {
            res = R.drawable.ic_action_battery_ok;
        }

        return res;
    }


    public int getBatteryImgResourceId() {
        return getBatteryImgResourceId(getIsBatteryLow());
    }


    public static int getTamperImgResourceId(Boolean isTamperOpened) {

        int res;

        if (null == isTamperOpened) {
            res = R.drawable.ic_action_dash;
        } else if (isTamperOpened) {
            res = R.drawable.ic_action_tamper_opened;
        } else {
            res = R.drawable.ic_action_tamper_closed;
        }

        return res;
    }


    public int getTamperImgResourceId() {
        return getTamperImgResourceId(getIsTamperOpened());
    }


    public static int getZoneFailureImgResourceId(Boolean isZoneFalure) {

        int res;

        if (null == isZoneFalure || isZoneFalure == false) {
            res = R.drawable.ic_action_dash;
        } else {
            res = R.drawable.ic_action_failure;
        }

        return res;
    }


    public int getZoneFailureImgResourceId() {
        return getZoneFailureImgResourceId(getIsZoneFailure());
    }


    public static boolean hasAttentionInfo(Boolean isTamperOpened,
                                           Boolean isLinkLost,
                                           Boolean isBatteryLow,
                                           Boolean isZoneFailure) {

        if (null != isTamperOpened && isTamperOpened == true) {
            return true;
        }

        if (null != isLinkLost && isLinkLost == true) {
            return true;
        }

        if (null != isBatteryLow && isBatteryLow == true) {
            return true;
        }

        if (null != isZoneFailure && isZoneFailure == true) {
            return true;
        }

        return false;
    }


    public boolean hasAttentionInfo() {
        return hasAttentionInfo(getIsTamperOpened(), getIsLinkLost(), getIsBatteryLow(), getIsZoneFailure());
    }


    public boolean hasAdditionalInfo() {

        if (null != isTamperOpened) {
            return true;
        }

        if (null != isLinkLost) {
            return true;
        }

        if (null != isBatteryLow) {
            return true;
        }

        if (null != isZoneFailure) {
            return true;
        }

        return false;
    }
}
