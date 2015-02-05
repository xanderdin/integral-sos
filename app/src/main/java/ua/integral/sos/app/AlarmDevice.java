package ua.integral.sos.app;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aledin on 26.01.15.
 */
public class AlarmDevice implements Comparable {

    private final static String PATTERN_USER                = "((\\s+(P|П)([0-9]{1,2})){0,1})";
    private final static String PATTERN_ZONES               = "((:([0-9]{1,2}(,[0-9]{1,2}){0,15})){0,1})";

    private final static String PATTERN_MSG_TREVOGA         = "(TREVOGA|ТРЕВОГА)" + PATTERN_ZONES;
    private final static String PATTERN_MSG_PODBOR_KODA     = "(PODBOR\\s+KODA|ПОДБОР\\s+КОДА)";
    private final static String PATTERN_MSG_NAPADENIE       = "(NAPADENIE|НАПАДЕНИЕ)" + PATTERN_USER;
    private final static String PATTERN_MSG_VSKRIT          = "(VSKRIT|ВСКРЫТ)" + PATTERN_ZONES;
    private final static String PATTERN_MSG_OTKLUCHENIE     = "(OTKLUCHENIE|ОТКЛЮЧЕНИЕ)";
    private final static String PATTERN_MSG_ZAKRIT          = "(zakrit|закрыт)" + PATTERN_ZONES;
    private final static String PATTERN_MSG_220_NET         = "220\\s+(NET|НЕТ)";
    private final static String PATTERN_MSG_220_EST         = "220\\s+(est\\'|есть)";
    private final static String PATTERN_MSG_BAT_NE_NORMA    = "(bat.NEnorma|бат.НЕнорма)" + PATTERN_ZONES;
    private final static String PATTERN_MSG_BAT_NORMA       = "(bat.norma|бат.норма)" + PATTERN_ZONES;
    private final static String PATTERN_MSG_NET_SVYAZI      = "(NET\\s+svyazi|НЕТ\\s+связи)" + PATTERN_ZONES;
    private final static String PATTERN_MSG_EST_SVYAZ       = "(est\\'\\s+svyaz|есть\\s+связь)" + PATTERN_ZONES;
    private final static String PATTERN_MSG_NORMA           = "(norma|норма)" + PATTERN_ZONES;
    private final static String PATTERN_MSG_NE_NORMA        = "(NE\\s+norma|НЕ\\s+норма)" + PATTERN_ZONES;
    private final static String PATTERN_MSG_NEISPRAVEN      = "(NEispraven|НЕисправен)" + PATTERN_ZONES;
    private final static String PATTERN_MSG_OSHIBKA         = "(oshibka|ошибка)" + PATTERN_ZONES;
    private final static String PATTERN_MSG_TEST            = "(test|тест)" + PATTERN_ZONES;
    private final static String PATTERN_MSG_SNYAT           = "(snyat|снят)" + PATTERN_USER + PATTERN_ZONES;
    private final static String PATTERN_MSG_VZYAT           = "(vzyat|взят)" + PATTERN_USER + PATTERN_ZONES;
    private final static String PATTERN_MSG_BALANS          = "(Balans|Баланс):\\s+([0-9,\\.]+)\\s*(\\S+)";

    private final static String PATTERN_SMS = "((" +
            PATTERN_MSG_TREVOGA        + ")|(" +
            PATTERN_MSG_PODBOR_KODA    + ")|(" +
            PATTERN_MSG_NAPADENIE      + ")|(" +
            PATTERN_MSG_VSKRIT         + ")|(" +
            PATTERN_MSG_OTKLUCHENIE    + ")|(" +
            PATTERN_MSG_ZAKRIT         + ")|(" +
            PATTERN_MSG_220_NET        + ")|(" +
            PATTERN_MSG_220_EST        + ")|(" +
            PATTERN_MSG_BAT_NE_NORMA   + ")|(" +
            PATTERN_MSG_BAT_NORMA      + ")|(" +
            PATTERN_MSG_NET_SVYAZI     + ")|(" +
            PATTERN_MSG_EST_SVYAZ      + ")|(" +
            PATTERN_MSG_NORMA          + ")|(" +
            PATTERN_MSG_NE_NORMA       + ")|(" +
            PATTERN_MSG_NEISPRAVEN     + ")|(" +
            PATTERN_MSG_OSHIBKA        + ")|(" +
            PATTERN_MSG_TEST           + ")|(" +
            PATTERN_MSG_SNYAT          + ")|(" +
            PATTERN_MSG_VZYAT          + ")|(" +
            PATTERN_MSG_BALANS         + "))";

    private final static Pattern patternSms = Pattern.compile(PATTERN_SMS);

    private final static Pattern patternMsgTrevoga       = Pattern.compile(PATTERN_MSG_TREVOGA);
    private final static Pattern patternMsgPodborKoda    = Pattern.compile(PATTERN_MSG_PODBOR_KODA);
    private final static Pattern patternMsgNapadenie     = Pattern.compile(PATTERN_MSG_NAPADENIE);
    private final static Pattern patternMsgVskrit        = Pattern.compile(PATTERN_MSG_VSKRIT);
    private final static Pattern patternMsgOtkluchenie   = Pattern.compile(PATTERN_MSG_OTKLUCHENIE);
    private final static Pattern patternMsgZakrit        = Pattern.compile(PATTERN_MSG_ZAKRIT);
    private final static Pattern patternMsg220Net        = Pattern.compile(PATTERN_MSG_220_NET);
    private final static Pattern patternMsg220Est        = Pattern.compile(PATTERN_MSG_220_EST);
    private final static Pattern patternMsgBatNeNorma    = Pattern.compile(PATTERN_MSG_BAT_NE_NORMA);
    private final static Pattern patternMsgBatNorma      = Pattern.compile(PATTERN_MSG_BAT_NORMA);
    private final static Pattern patternMsgNetSvyazi     = Pattern.compile(PATTERN_MSG_NET_SVYAZI);
    private final static Pattern patternMsgEstSvyaz      = Pattern.compile(PATTERN_MSG_EST_SVYAZ);
    private final static Pattern patternMsgNorma         = Pattern.compile(PATTERN_MSG_NORMA);
    private final static Pattern patternMsgNeNorma       = Pattern.compile(PATTERN_MSG_NE_NORMA);
    private final static Pattern patternMsgNeispraven    = Pattern.compile(PATTERN_MSG_NEISPRAVEN);
    private final static Pattern patternMsgOshibka       = Pattern.compile(PATTERN_MSG_OSHIBKA);
    private final static Pattern patternMsgTest          = Pattern.compile(PATTERN_MSG_TEST);
    private final static Pattern patternMsgSnyat         = Pattern.compile(PATTERN_MSG_SNYAT);
    private final static Pattern patternMsgVzyat         = Pattern.compile(PATTERN_MSG_VZYAT);
    private final static Pattern patternMsgBalans        = Pattern.compile(PATTERN_MSG_BALANS);


    private final String contactLookupKey;

    private Boolean isTamperOpened;
    private Boolean isBatteryLow;
    private Boolean isPowerLost;
    private Boolean isDevFailure;

    private final SortedSet<AlarmDeviceZone> zones = Collections.synchronizedSortedSet(new TreeSet<AlarmDeviceZone>());

    private String lastEventText;
    private boolean isLastEventTextChanged;

    private String textForAlertDialog;

    private Uri devUri;
    private long rowId;

    private final Context context;

    public interface AlarmDeviceListener {
        public void onDataChanged();
    }

    private final ArrayList<AlarmDeviceListener> listeners = new ArrayList<AlarmDeviceListener>();

    public AlarmDevice(Context context, String contactLookupKey) {
        this.context = context;
        this.contactLookupKey = contactLookupKey;
        tieWithDbRecord();
        loadZones();
    }

    private void tieWithDbRecord() {

        String projection[] = {
                AppDb.AlarmDeviceTable.COLUMN_ID,
                AppDb.AlarmDeviceTable.COLUMN_CONTACT_LOOKUP_KEY,
                AppDb.AlarmDeviceTable.COLUMN_IS_TAMPER_OPENED,
                AppDb.AlarmDeviceTable.COLUMN_IS_BATTERY_LOW,
                AppDb.AlarmDeviceTable.COLUMN_IS_POWER_LOST,
                AppDb.AlarmDeviceTable.COLUMN_IS_DEV_FAILURE,
        };

        String selection = AppDb.AlarmDeviceTable.COLUMN_CONTACT_LOOKUP_KEY + " = ?";

        String selectionArgs[] = { getContactLookupKey() };

        Cursor cursor = getContentResolver().query(
                AlarmDeviceProvider.CONTENT_URI, projection, selection, selectionArgs, null);

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {

            rowId = cursor.getLong(0);

            devUri = ContentUris.withAppendedId(AlarmDeviceProvider.CONTENT_URI_ID_BASE, rowId);

            if (!cursor.isNull(1)) isTamperOpened = (cursor.getInt(1) != 0);
            if (!cursor.isNull(2)) isBatteryLow = (cursor.getInt(2) != 0);
            if (!cursor.isNull(3)) isPowerLost = (cursor.getInt(3) != 0);
            if (!cursor.isNull(4)) isDevFailure = (cursor.getInt(4) != 0);

        } else {

            ContentValues values = new ContentValues();

            values.put(AppDb.AlarmDeviceTable.COLUMN_CONTACT_LOOKUP_KEY, getContactLookupKey());

            devUri = getContentResolver().insert(AlarmDeviceProvider.CONTENT_URI, values);

            rowId = Long.valueOf(devUri.getPathSegments().get(1));
        }

        cursor.close();
    }


    private void loadZones() {

        zones.clear();

        String projection[] = {
                AppDb.AlarmDeviceZoneTable.COLUMN_ZONE_NUM,
        };

        String selection = AppDb.AlarmDeviceZoneTable.COLUMN_DEV_ID + " = ?";

        String selectionArgs[] = { String.valueOf(getRowId()) };

        Cursor cursor = getContentResolver().query(
                AlarmDeviceZoneProvider.CONTENT_URI, projection, selection, selectionArgs, null);

        while (cursor.moveToNext()) {
            AlarmDeviceZone zone = new AlarmDeviceZone(getContext(), getRowId(), cursor.getInt(0));
            zones.add(zone);
        }

        cursor.close();
    }


    public String getDevName() {

        @SuppressLint("InlinedApi")
        String[] projection = {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                        ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
                        : ContactsContract.Contacts.DISPLAY_NAME,
        };

        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, getContactLookupKey());

        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        String res;

        if (cursor.moveToNext()) {
            res = cursor.getString(0);
        } else {
            res = getFirstDevTel();
        }

        cursor.close();

        return res;
    }


    public String getFirstDevTel() {

//        String[] projection = {
//                ContactsContract.CommonDataKinds.Phone.NUMBER,
//        };
//
//        String selection = ContactsContract.Data.LOOKUP_KEY + " = ? AND " +
//                ContactsContract.Data.HAS_PHONE_NUMBER + " = 1";
//
//        String[] selectionArgs = {
//                getContactLookupKey(),
//        };
//
//        Cursor cursor = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
//                projection, selection, selectionArgs, null);
//
//        String res;
//
//        if (cursor.moveToNext()) {
//            res = cursor.getString(0);
//        } else {
//            res = "";
//        }
//
//        cursor.close();
//
//        return res;

        ArrayList<String> tels = getDevTels();

        return tels.isEmpty() ? "" : tels.get(0);
    }


    public ArrayList<String> getDevTels() {

        ArrayList<String> res = new ArrayList<>();

        Cursor cursor = getContentResolver().query(
                Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, getContactLookupKey()),
                new String[] { ContactsContract.Contacts._ID },
                ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1",
                null,
                null
                );

        long contactId;

        if (cursor.moveToNext()) {
            contactId = cursor.getLong(0);
        } else {
            cursor.close();
            return res;
        }

        cursor.close();

        String[] projection = {
                ContactsContract.CommonDataKinds.Phone.NUMBER,
        };

        String selection = ContactsContract.Data.CONTACT_ID + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ?";

        String[] selectionArgs = {
                String.valueOf(contactId),
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
        };

        String orderBy = ContactsContract.Data._ID + " ASC";

        cursor = getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                projection, selection, selectionArgs, orderBy);

        while (cursor.moveToNext()) {
            res.add(cursor.getString(0));
        }

        cursor.close();

        for (String s : res) {
            Log.d("ZZZ", "num: " + s);
        }

        return res;
    }


    public String getLastEventText() {
        isLastEventTextChanged = false;
        return lastEventText;
    }


    private void setLastEventText(String text) {
        lastEventText = text;
        isLastEventTextChanged = true;
        notifyDataChanged();
    }


    private void putAlarmToEventHistory(long time, String text) {
        putToEventHistory(time, text, AppDb.EventHistoryTable.EVENT_TYPE_ALARM);
    }


    private void putWarningToEventHistory(long time, String text) {
        putToEventHistory(time, text, AppDb.EventHistoryTable.EVENT_TYPE_WARNING);
    }


    private void putToEventHistory(long time, String text) {
        putToEventHistory(time, text, null);
    }


    private void putToEventHistory(String text) {
        putToEventHistory(MiscFunc.now(), text);
    }


    private void putToEventHistory(long time, String text, Integer eventType) {

        final ContentValues values = new ContentValues();

        values.put(AppDb.EventHistoryTable.COLUMN_DATE_TIME, time);
        values.put(AppDb.EventHistoryTable.COLUMN_DEV_ID, getRowId());
        values.put(AppDb.EventHistoryTable.COLUMN_EVENT_TEXT, text);

        if (null != eventType) {
            values.put(AppDb.EventHistoryTable.COLUMN_EVENT_TYPE, eventType);
        }

        new Runnable() {
            @Override
            public void run() {
                getContentResolver().insert(EventHistoryProvider.CONTENT_URI, values);
            }
        }.run();
    }


    public boolean isLastEventTextChanged() {
        return isLastEventTextChanged;
    }


    private Context getContext() {
        return context;
    }


    private ContentResolver getContentResolver() {
        return getContext().getContentResolver();
    }


    private void setProviderBooleanValue(String column, Boolean val) {
        MiscProviderFunc.setProviderBooleanValue(getContext(), devUri, column, val);
    }

    private void setProviderStringValue(String column, String val) {
        MiscProviderFunc.setProviderStringValue(getContext(), devUri, column, val);
    }

    private void setIsTamperOpened(Boolean val) {
        setProviderBooleanValue(AppDb.AlarmDeviceTable.COLUMN_IS_TAMPER_OPENED, val);
        isTamperOpened = val;
    }


    public Boolean getIsTamperOpened() {
        return isTamperOpened;
    }


    private void setIsBatteryLow(Boolean val) {
        setProviderBooleanValue(AppDb.AlarmDeviceTable.COLUMN_IS_BATTERY_LOW, val);
        isBatteryLow = val;
    }

    public Boolean getIsBatteryLow() {
        return isBatteryLow;
    }


    private void setIsPowerLost(Boolean val) {
        setProviderBooleanValue(AppDb.AlarmDeviceTable.COLUMN_IS_POWER_LOST, val);
        isPowerLost = val;
    }


    public Boolean getIsPowerLost() {
        return isPowerLost;
    }


    private void setIsDevFailure(Boolean val) {
        setProviderBooleanValue(AppDb.AlarmDeviceTable.COLUMN_IS_DEV_FAILURE, val);
        isDevFailure = val;
    }


    public Boolean getIsDevFailure() {
        return isDevFailure;
    }


    public String getContactLookupKey() {
        return contactLookupKey;
    }


    public synchronized SortedSet<AlarmDeviceZone> getZones() {
        return zones;
    }


    public boolean isInAlarm() {

        for (AlarmDeviceZone alarmDeviceZone : getZones()) {
            if (alarmDeviceZone.isInAlarm()) {
                return true;
            }
        }

        if (null != getIsTamperOpened() && getIsTamperOpened() == true) {
            return true;
        }

        return false;
    }


    public boolean isArmed() {
        for (AlarmDeviceZone alarmDeviceZone : getZones()) {
            if (null != alarmDeviceZone.getIsArmed() && alarmDeviceZone.getIsArmed() == true) {
                return true;
            }
        }
        return false;
    }


    public long getRowId() {
        return rowId;
    }


    public int getNotificationId() {
        return (int) (getRowId() & 0x7fffffff);
    }


    public synchronized void clear() {
        zones.clear();
        getListeners().clear();
        cancelNotification();
    }


    public synchronized void remove() {
        clear();
        MiscProviderFunc.removeProviderRecord(getContext(), devUri);
    }


    public synchronized void removeZone(int num) {
        for (Iterator i = getZones().iterator(); i.hasNext();) {
            AlarmDeviceZone zone = (AlarmDeviceZone) i.next();
            if (zone.getZoneNum() == num) {
                i.remove();
                zone.remove();
                break;
            }
        }
    }


    public AlarmDeviceZone getZoneByNum(int num) {
        for (AlarmDeviceZone alarmDeviceZone : getZones()) {
            if (num == alarmDeviceZone.getZoneNum()) {
                return alarmDeviceZone;
            }
        }
        return null;
    }


    public AlarmDeviceZone getZoneByRowId(long rowId) {
        for (AlarmDeviceZone alarmDeviceZone : getZones()) {
            if (rowId == alarmDeviceZone.getRowId()) {
                return alarmDeviceZone;
            }
        }
        return null;
    }


    private TreeSet<Integer> zonesFromString(String zones) {
        TreeSet<Integer> res = new TreeSet<Integer>();
        if (TextUtils.isEmpty(zones)) {
            return res;
        }
        String[] zonesArr = zones.split(",");
        for (String zone : zonesArr) {
            try {
                res.add(Integer.valueOf(zone));
            } catch (Exception e) {
                e.printStackTrace();;
            }
        }
        return res;
    }


    public void parseMessage(String text) {

        Matcher matcherSms = patternSms.matcher(text);

        int cnt = 0;

        while (matcherSms.find()) {

            cnt++;

            Matcher m;

            AlarmDeviceZone alarmDeviceZone;
            StringBuilder infoSb = new StringBuilder();
            Uri sound = CommonVar.getTickSoundUri();
            Integer type = null;
            String userNumStr = null;
            String zonesStr = null;

            String msg = matcherSms.group();

            if ((m = patternMsgTrevoga.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_Alarm));
                sound = CommonVar.getAlarmSoundUri();
                type = AppDb.EventHistoryTable.EVENT_TYPE_ALARM;

                zonesStr = m.group(4);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setZoneState(AlarmDeviceZone.ST_TORN);
                    alarmDeviceZone.setIsFired(true);
                }

            } else if ((m = patternMsgPodborKoda.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_BruteForce));
                sound = CommonVar.getAlarmSoundUri();
                type = AppDb.EventHistoryTable.EVENT_TYPE_ALARM;

            } else if ((m = patternMsgNapadenie.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_Burglary));
                sound = CommonVar.getAlarmSoundUri();
                type = AppDb.EventHistoryTable.EVENT_TYPE_ALARM;
                userNumStr = m.group(7);

            } else if ((m = patternMsgVskrit.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_CaseOpened));

                int i = 0;

                zonesStr = m.group(4);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    i++;
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setIsTamperOpened(true);
                }

                if (i == 0) {
                    setIsTamperOpened(true);
                }

                if (isInAlarm()) {
                    sound = CommonVar.getAlarmSoundUri();
                    type = AppDb.EventHistoryTable.EVENT_TYPE_ALARM;

                } else {
                    sound = CommonVar.getInfoSoundUri();
                    type = AppDb.EventHistoryTable.EVENT_TYPE_WARNING;
                }

            } else if ((m = patternMsgOtkluchenie.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_DeviceOff));
                sound = CommonVar.getInfoSoundUri();

            } else if ((m = patternMsgZakrit.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_CaseClosed));

                int i = 0;

                zonesStr = m.group(4);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    i++;
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setIsTamperOpened(false);
                }

                if (i == 0) {
                    setIsTamperOpened(false);
                }

            } else if ((m = patternMsg220Net.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_BadAC));
                sound = CommonVar.getInfoSoundUri();
                type = AppDb.EventHistoryTable.EVENT_TYPE_WARNING;

                setIsPowerLost(true);

            } else if ((m = patternMsg220Est.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_GoodAC));
                setIsPowerLost(false);

            } else if ((m = patternMsgBatNeNorma.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_BadDC));
                sound = CommonVar.getInfoSoundUri();
                type = AppDb.EventHistoryTable.EVENT_TYPE_WARNING;

                int i = 0;

                zonesStr = m.group(4);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    i++;
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setIsBatteryLow(true);
                }

                if (i == 0) {
                    setIsBatteryLow(true);
                }

            } else if ((m = patternMsgBatNorma.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_GoodDC));

                int i = 0;

                zonesStr = m.group(4);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    i++;
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setIsBatteryLow(false);
                }

                if (i == 0) {
                    setIsBatteryLow(false);
                }

            } else if ((m = patternMsgNetSvyazi.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_LinkOff));
                sound = CommonVar.getInfoSoundUri();
                type = AppDb.EventHistoryTable.EVENT_TYPE_WARNING;

                zonesStr = m.group(4);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setIsLinkLost(true);
                }

            } else if ((m = patternMsgEstSvyaz.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_LinkOn));

                zonesStr = m.group(4);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setIsLinkLost(false);
                }

            } else if ((m = patternMsgNorma.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_StateOk));

                zonesStr = m.group(4);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setZoneState(AlarmDeviceZone.ST_OK);
                }

            } else if ((m = patternMsgNeNorma.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_Torn));

                zonesStr = m.group(4);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setZoneState(AlarmDeviceZone.ST_TORN);
                }

            } else if ((m = patternMsgNeispraven.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_Crash));
                sound = CommonVar.getInfoSoundUri();
                type = AppDb.EventHistoryTable.EVENT_TYPE_WARNING;

                int i = 0;

                zonesStr = m.group(4);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    i++;
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setIsZoneFailure(true);
                }

                if (i == 0) {
                    setIsDevFailure(true);
                }

            } else if ((m = patternMsgOshibka.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_Fault));
                sound = CommonVar.getInfoSoundUri();
                type = AppDb.EventHistoryTable.EVENT_TYPE_WARNING;

                int i = 0;

                zonesStr = m.group(4);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    i++;
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setIsZoneFailure(true);
                }

                if (i == 0) {
                    setIsDevFailure(true);
                }

            } else if ((m = patternMsgTest.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_Test));

            } else if ((m = patternMsgSnyat.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_Disarmed));

                userNumStr = m.group(5);

                zonesStr = m.group(8);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setIsArmed(false);
                    alarmDeviceZone.setIsFired(false);
                }

            } else if ((m = patternMsgVzyat.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_Armed));

                userNumStr = m.group(5);

                zonesStr = m.group(8);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setZoneState(AlarmDeviceZone.ST_OK);
                    alarmDeviceZone.setIsArmed(true);
                    alarmDeviceZone.setIsFired(false);
                }

            } else if ((m = patternMsgBalans.matcher(msg)).matches()) {

                infoSb.append(getContext().getString(R.string.MSG_Balance)).append(": ").append(m.group(2)).append(" ").append(m.group(3));

            } else {
                infoSb.append(text);
            }

            if (!TextUtils.isEmpty(zonesStr)) {
                infoSb.append(": ").append(zonesStr);
            }

            if (!TextUtils.isEmpty(userNumStr)) {
                infoSb.append("; ").append(getContext().getString(R.string.DEV_UserLetter)).append(userNumStr);
            }

            String info = infoSb.toString();
            long time = MiscFunc.now();
            setLastEventText(info);
            putToEventHistory(time, info, type);
            showNotification(time, info, type, sound);
        }
        if (cnt == 0) {
            putToEventHistory(text);
        }
    }


    private void showNotification(long time, String text, Integer type, Uri sound) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext());

        if (isInAlarm()) {
            builder.setSmallIcon(R.drawable.ic_stat_lock_broken);
            builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        } else if (null != type && type == AppDb.EventHistoryTable.EVENT_TYPE_WARNING) {
            builder.setSmallIcon(R.drawable.ic_stat_notification_warning);
            builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        } else {
            builder.setSmallIcon(R.drawable.ic_stat_communication_message);
            builder.setDefaults(Notification.DEFAULT_LIGHTS);
        }
        builder.setContentTitle(getDevName());
        builder.setContentText(text);
        builder.setTicker(getDevName() + ": " + text);
        //builder.setDefaults(Notification.DEFAULT_ALL);
        //builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        builder.setAutoCancel(true);
        //builder.setOnlyAlertOnce(true);
        builder.setSound(sound);
        builder.setWhen(time * 1000);

        Intent intent = new Intent(getContext(), DeviceDetailActivity.class);
        intent.setAction(getContext().getPackageName() + "." + getContactLookupKey()); // MAGIC!!! Without it extras won't be processed by activity!
        intent.putExtra(CommonDef.EXTRA_SELECTED_ID, getRowId());

        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

        getNotificationManager().notify(getContactLookupKey(), getNotificationId(), builder.build());

    }


    public void cancelNotification() {
        getNotificationManager().cancel(getContactLookupKey(), getNotificationId());
    }


    private synchronized NotificationManager getNotificationManager() {
        return (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }


    private synchronized void setTextForAlertDialog(String s) {
        textForAlertDialog = s;
    }


    public synchronized void clearTextForAlertDialog() {
        textForAlertDialog = null;
    }


    public synchronized String getTextForAlertDialog() {
        return textForAlertDialog;
    }


    private synchronized ArrayList<AlarmDeviceListener> getListeners() {
        return listeners;
    }


    public void addListener(AlarmDeviceListener l) {
        if (!getListeners().contains(l)) {
            getListeners().add(l);
        }
    }


    public void removeListener(AlarmDeviceListener l) {
        getListeners().remove(l);
    }


    private void notifyDataChanged() {
        for (AlarmDeviceListener l : getListeners()) {
            l.onDataChanged();
        }
    }


    public AlarmDeviceZone getZoneByNumOrCreateNew(int num) {

        AlarmDeviceZone zone = getZoneByNum(num);

        if (null != zone) {
            return zone;
        }

        zone = new AlarmDeviceZone(getContext(), getRowId(), num);

        zones.add(zone);

        return zone;
    }


    private String zoneNumsFromTreeSet(TreeSet<Integer> nums) {

        if (null == nums || nums.isEmpty()) {
            return "";
        }

        return nums.toString().replaceAll("[^0-9,]", "");
    }


    @Override
    public int compareTo(Object another) {
        return this.getContactLookupKey().compareToIgnoreCase(((AlarmDevice) another).getContactLookupKey());
    }


    public static int getLockImgResourceId(Boolean isSubscribed, boolean isArmed, boolean isInAlarm) {

        int res;

        if (null == isSubscribed || isSubscribed == false) {
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


    public int getLockImgResourceId() {
        return getLockImgResourceId(true, isArmed(), isInAlarm());
    }


    public static int getBatteryImgResourceId(Boolean isBatteryLow) {

        int res;

        if (null == isBatteryLow) {
            res = R.drawable.ic_action_question;
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


    public static int getPowerImgResourceId(Boolean isPowerLost) {

        int res;

        if (null == isPowerLost) {
            res = R.drawable.ic_action_question;
        } else if (isPowerLost) {
            res = R.drawable.ic_action_power_lost;
        } else {
            res = R.drawable.ic_action_power_ok;
        }

        return res;
    }


    public int getPowerImgResourceId() {
        return getPowerImgResourceId(getIsPowerLost());
    }


    public static int getTamperImgResourceId(Boolean isTamperOpened) {

        int res;

        if (null == isTamperOpened) {
            res = R.drawable.ic_action_question;
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
}
