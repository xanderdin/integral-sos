package ua.integral.sos.app;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aledin on 26.01.15.
 */
public class AlarmDevice implements Comparable {

    private final static String PATTERN_USER                = "((\\s+(P|П)([0-9]{1,2})){0,1})";
    private final static String PATTERN_ZONES               = "((:([0-9]{1,2}(,[0-9]{1,2}){0,15})){0,1})";

    private final static String PATTERN_MSG_TREVOGA         = "(TREVOGA|ТРЕВОГА)" + PATTERN_ZONES; // z
    private final static String PATTERN_MSG_PODBOR_KODA     = "(PODBOR\\s+KODA|ПОДБОР\\s+КОДА)";
    private final static String PATTERN_MSG_NAPADENIE       = "(NAPADENIE|НАПАДЕНИЕ)" + PATTERN_USER; // u
    private final static String PATTERN_MSG_VSKRIT          = "(VSKRIT|ВСКРЫТ)" + PATTERN_ZONES; // z
    private final static String PATTERN_MSG_OTKLUCHENIE     = "(OTKLUCHENIE|ОТКЛЮЧЕНИЕ)";
    private final static String PATTERN_MSG_ZAKRIT          = "(zakrit|закрыт)" + PATTERN_ZONES; //z
    private final static String PATTERN_MSG_220_NET         = "220\\s+(NET|НЕТ)";
    private final static String PATTERN_MSG_220_EST         = "220\\s+(est\\'|есть)";
    private final static String PATTERN_MSG_BAT_NE_NORMA    = "(bat.NEnorma|бат.НЕнорма)" + PATTERN_ZONES; //z
    private final static String PATTERN_MSG_BAT_NORMA       = "(bat.norma|бат.норма)" + PATTERN_ZONES; //z
    private final static String PATTERN_MSG_NET_SVYAZI      = "(NET\\s+svyazi|НЕТ\\s+связи)" + PATTERN_ZONES; //z
    private final static String PATTERN_MSG_EST_SVYAZ       = "(est\\'svyaz|есть\\s+связь)" + PATTERN_ZONES; //z
    private final static String PATTERN_MSG_NORMA           = "(norma|норма)" + PATTERN_ZONES; //z
    private final static String PATTERN_MSG_NE_NORMA        = "(NE\\s+norma|НЕ\\s+норма)" + PATTERN_ZONES; //z
    private final static String PATTERN_MSG_NEISPRAVEN      = "(NEispraven|НЕисправен)" + PATTERN_ZONES; //z
    private final static String PATTERN_MSG_OSHIBKA         = "(oshibka|ошибка)" + PATTERN_ZONES; //z
    private final static String PATTERN_MSG_TEST            = "(test|тест)" + PATTERN_ZONES; //z
    private final static String PATTERN_MSG_SNYAT           = "(snyat|снят)" + PATTERN_USER + PATTERN_ZONES; //u z
    private final static String PATTERN_MSG_VZYAT           = "(vzyat|взят)" + PATTERN_USER + PATTERN_ZONES; //u z
    private final static String PATTERN_MSG_BALANS          = "(Balans|Баланс):\\s+([0-9,\\.]+)\\s+(\\S+)";

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


    private final String devTel;
    private String devName;

    private Boolean isTamperOpened;
    private Boolean isBatteryLow;
    private Boolean isPowerLost;
    private Boolean isDevFailure;

    private final SortedSet<AlarmDeviceZone> zones = Collections.synchronizedSortedSet(new TreeSet<AlarmDeviceZone>());

    private final SortedSet<AlarmDeviceUser> users = Collections.synchronizedSortedSet(new TreeSet<AlarmDeviceUser>());

    private String lastEventText;
    private boolean isLastEventTextChanged;

    private String textForAlertDialog;

    private Uri devUri;
    private long rowId;

    private final Context context;

    public AlarmDevice(Context context, String devTel) {
        this.context = context;
        this.devTel = devTel;
        tieWithDbRecord();
        loadZones();
        loadUsers();
    }

    public AlarmDevice(Context context, String devTel, String devName) {
        this(context, devTel);
        setDevName(devName);
    }


    private void tieWithDbRecord() {

        String projection[] = {
                AppDb.AlarmDeviceTable.COLUMN_ID,
                AppDb.AlarmDeviceTable.COLUMN_DEV_NAME,
                AppDb.AlarmDeviceTable.COLUMN_IS_TAMPER_OPENED,
                AppDb.AlarmDeviceTable.COLUMN_IS_BATTERY_LOW,
                AppDb.AlarmDeviceTable.COLUMN_IS_POWER_LOST,
                AppDb.AlarmDeviceTable.COLUMN_IS_DEV_FAILURE,
        };

        String selection = AppDb.AlarmDeviceTable.COLUMN_DEV_TEL + " = ?";

        String selectionArgs[] = { getTel() };

        Cursor cursor = getContentResolver().query(
                AlarmDeviceProvider.CONTENT_URI, projection, selection, selectionArgs, null);

        if (cursor.getCount() > 0 && cursor.moveToFirst()) {

            rowId = cursor.getLong(0);

            devUri = ContentUris.withAppendedId(AlarmDeviceProvider.CONTENT_URI_ID_BASE, rowId);

            if (!cursor.isNull(1)) devName = cursor.getString(1);
            if (!cursor.isNull(2)) isTamperOpened = (cursor.getInt(2) != 0);
            if (!cursor.isNull(3)) isBatteryLow = (cursor.getInt(3) != 0);
            if (!cursor.isNull(4)) isPowerLost = (cursor.getInt(4) != 0);
            if (!cursor.isNull(5)) isDevFailure = (cursor.getInt(5) != 0);

            cursor.close();

            return;
        }

        cursor.close();

        ContentValues values = new ContentValues();

        values.put(AppDb.AlarmDeviceTable.COLUMN_DEV_TEL, getTel());

        devUri = getContentResolver().insert(AlarmDeviceProvider.CONTENT_URI, values);

        rowId = Long.valueOf(devUri.getPathSegments().get(1));
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


    private void loadUsers() {

        for (AlarmDeviceUser user: users) {
            user.clear();
        }
        users.clear();

        String projection[] = {
                AppDb.AlarmDeviceUserTable.COLUMN_USER_NUM,
        };

        String selection = AppDb.AlarmDeviceUserTable.COLUMN_DEV_ID + " = ?";

        String selectionArgs[] = { String.valueOf(getRowId()) };

        Cursor cursor = getContentResolver().query(
                AlarmDeviceUserProvider.CONTENT_URI, projection, selection, selectionArgs, null);

        while (cursor.moveToNext()) {
            AlarmDeviceUser user = new AlarmDeviceUser(getContext(), getRowId(), cursor.getInt(0));
            users.add(user);
        }

        cursor.close();
    }


    public void setDevName(String val) {

        if (TextUtils.isEmpty(val)) {
            val = getTel();
        }

        setProviderStringValue(AppDb.AlarmDeviceTable.COLUMN_DEV_NAME, val);
        devName = val;
    }


    public String getDevName() {
        return TextUtils.isEmpty(devName) ? getTel() : devName;
    }


    public String getLastEventText() {
        isLastEventTextChanged = false;
        return lastEventText;
    }


    private void setLastEventText(String text) {
        lastEventText = text;
        isLastEventTextChanged = true;
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


    public String getTel() {
        return devTel;
    }


    public synchronized SortedSet<AlarmDeviceZone> getZones() {
        return zones;
    }


    public synchronized SortedSet<AlarmDeviceUser> getUsers() {
        return users;
    }


    public synchronized AlarmDeviceUser getUserByRowId(long rowId) {
        for (AlarmDeviceUser alarmDeviceUser : getUsers()) {
            if (rowId == alarmDeviceUser.getRowId()) {
                return alarmDeviceUser;
            }
        }
        return null;
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

        //mainService.cancelNotification(getTel(), getNotificationId());

        for (AlarmDeviceUser u : users) {
            u.clear();
        }

        users.clear();
        zones.clear();
    }


    public synchronized void remove() {
        clear();
        MiscProviderFunc.removeProviderRecord(getContext(), devUri);
    }


    public synchronized void removeUser(int num) {
        for (Iterator i = getUsers().iterator(); i.hasNext();) {
            AlarmDeviceUser user = (AlarmDeviceUser) i.next();
            if (user.getUserNum() == num) {
                i.remove();
                user.remove();
                break;
            }
        }
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
            String info = "";
            Uri sound = CommonVar.getTickSoundUri();
            Integer type = null;
            String userNumStr = null;
            String zonesStr = null;

            String msg = matcherSms.group();

            if ((m = patternMsgTrevoga.matcher(msg)).matches()) {

                info = getContext().getString(R.string.MSG_Alarm);
                sound = CommonVar.getAlarmSoundUri();
                type = AppDb.EventHistoryTable.EVENT_TYPE_ALARM;

                zonesStr = m.group(4);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setZoneState(AlarmDeviceZone.ST_TORN);
                    alarmDeviceZone.setIsFired(true);
                }

            } else if ((m = patternMsgPodborKoda.matcher(msg)).matches()) {
                // TODO

            } else if ((m = patternMsgNapadenie.matcher(msg)).matches()) {

                info = getContext().getString(R.string.MSG_Burglary);
                sound = CommonVar.getAlarmSoundUri();
                type = AppDb.EventHistoryTable.EVENT_TYPE_ALARM;
                userNumStr = m.group(7);

            } else if ((m = patternMsgVskrit.matcher(msg)).matches()) {

                info = getContext().getString(R.string.MSG_CaseOpened);

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

                info = getContext().getString(R.string.MSG_DeviceOff);
                sound = CommonVar.getInfoSoundUri();

            } else if ((m = patternMsgZakrit.matcher(msg)).matches()) {

                info = getContext().getString(R.string.MSG_CaseClosed);

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

                info = getContext().getString(R.string.MSG_BadAC);
                sound = CommonVar.getInfoSoundUri();
                type = AppDb.EventHistoryTable.EVENT_TYPE_WARNING;

                setIsPowerLost(true);

            } else if ((m = patternMsg220Est.matcher(msg)).matches()) {

                info = getContext().getString(R.string.MSG_GoodAC);
                setIsPowerLost(false);

            } else if ((m = patternMsgBatNeNorma.matcher(msg)).matches()) {

                info = getContext().getString(R.string.MSG_BadDC);
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

                info = getContext().getString(R.string.MSG_GoodDC);

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

                info = getContext().getString(R.string.MSG_LinkOff);
                sound = CommonVar.getInfoSoundUri();
                type = AppDb.EventHistoryTable.EVENT_TYPE_WARNING;

                zonesStr = m.group(4);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setIsLinkLost(true);
                }

            } else if ((m = patternMsgEstSvyaz.matcher(msg)).matches()) {

                info = getContext().getString(R.string.MSG_LinkOn);

                zonesStr = m.group(4);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setIsLinkLost(false);
                }

            } else if ((m = patternMsgNorma.matcher(msg)).matches()) {

                info = getContext().getString(R.string.MSG_StateOk);

                zonesStr = m.group(4);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setZoneState(AlarmDeviceZone.ST_OK);
                }

            } else if ((m = patternMsgNeNorma.matcher(msg)).matches()) {

                info = getContext().getString(R.string.MSG_Torn);

                zonesStr = m.group(4);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setZoneState(AlarmDeviceZone.ST_TORN);
                }

            } else if ((m = patternMsgNeispraven.matcher(msg)).matches()) {

                info = getContext().getString(R.string.MSG_Crash);
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

                info = getContext().getString(R.string.MSG_Fault);
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

                info = getContext().getString(R.string.MSG_Test);

            } else if ((m = patternMsgSnyat.matcher(msg)).matches()) {

                info = getContext().getString(R.string.MSG_Disarmed);

                userNumStr = m.group(5);

                zonesStr = m.group(8);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setIsArmed(false);
                    alarmDeviceZone.setIsFired(false);
                }

//                for (int i = 0; i < m.groupCount(); i++) {
//                    Log.d("AAAA", "i = " + i + "; " + m.group(i));
//                }

            } else if ((m = patternMsgVzyat.matcher(msg)).matches()) {

                info = getContext().getString(R.string.MSG_Armed);

                userNumStr = m.group(5);

                zonesStr = m.group(8);

                for (int zoneNum : zonesFromString(zonesStr)) {
                    alarmDeviceZone = getZoneByNumOrCreateNew(zoneNum);
                    alarmDeviceZone.setZoneState(AlarmDeviceZone.ST_OK);
                    alarmDeviceZone.setIsArmed(true);
                    alarmDeviceZone.setIsFired(false);
                }

            } else if ((m = patternMsgBalans.matcher(msg)).matches()) {

                info = getContext().getString(R.string.MSG_Balance) + ": " + m.group(2) + " " + m.group(3);

            } else {

                info = text;
            }

            if (!TextUtils.isEmpty(zonesStr)) {
                info += (": " + zonesStr);
            }

            if (!TextUtils.isEmpty(userNumStr)) {
                info += ("; " + getContext().getString(R.string.DEV_UserLetter) + userNumStr);
            }

            setLastEventText(info);
            putToEventHistory(MiscFunc.now(), info, type);

            //putToEventHistory("cnt: " + cnt + "; " + matcherSms.group());
            // TODO: show notification
        }
        if (cnt == 0) {
            putToEventHistory(text);
        }
    }

//    private void procData(Chat chat, Message message, PacketExtension packetExtension) {
//
//        Serializer serializer = new Persister();
//        DataExtension data;
//
//        try {
//            data = serializer.read(DataExtension.class, packetExtension.toXML().toString());
//        } catch (Exception e) {
////            Log.d(TAG, e.getMessage());
//            e.printStackTrace();
//            return;
//        }
//
//        long time = data.getTime();
//
//        AbstractData payload = data.getPayload();
//
//        if (null == payload) {
//            return;
//        }
//
//        if (payload instanceof DataSrvMsg) {
//            procSrvMsg(chat, message, time, (DataSrvMsg) payload);
//        } else if (payload instanceof DataIntOxCmd) {
//            procIntOxCmd(chat, message, time, (DataIntOxCmd) payload, null);
//        } else if (payload instanceof DataIntOxMsg) {
//            procIntOxMsg(chat, message, time, (DataIntOxMsg) payload);
//        }
//    }

    private synchronized void setTextForAlertDialog(String s) {
        textForAlertDialog = s;
    }

    public synchronized void clearTextForAlertDialog() {
        textForAlertDialog = null;
    }

    public synchronized String getTextForAlertDialog() {
        return textForAlertDialog;
    }


//    private void procSrvMsg(Chat chat, Message message, long time, DataSrvMsg data) {
//
//        AbstractData payload;
//
//        String text;
//
//        switch (data.getCode()) {
//            case DataSrvMsg.CODE_USER_ACTIVATION_REQUEST_EXPIRED:
//
//                text = mainService.getString(R.string.msg_user_activation_request_expired);
//
//                setTextForAlertDialog(text);
//                mainService.notifyAlarmDeviceChanged(getJid());
//                mainService.showInfoNotificationAboutDevice(this, time, text);
//
//                break;
//
//            case DataSrvMsg.CODE_USER_DEACTIVATION_REQUEST_EXPIRED:
//
//                text = mainService.getString(R.string.msg_user_deactivation_request_expired);
//
//                setTextForAlertDialog(text);
//                mainService.notifyAlarmDeviceChanged(getJid());
//                mainService.showInfoNotificationAboutDevice(this, time, text);
//
//                break;
//
//            case DataSrvMsg.CODE_DEVICE_IS_OFFLINE:
//
//                setIsOnline(false);
//
//                text = mainService.getString(R.string.msg_device_is_offline);
//
//                mainService.showInfoNotificationAboutDevice(this, time, text);
//                mainService.notifyAlarmDeviceChanged(getJid());
//                putWarningToEventHistory(time, text);
//
//                break;
//
//            case DataSrvMsg.CODE_DEVICE_IS_ONLINE:
//
//                setIsOnline(true);
//
//                text = mainService.getString(R.string.msg_device_is_online);
//
//                mainService.showInfoNotificationAboutDevice(this, time, text);
//                mainService.notifyAlarmDeviceChanged(getJid());
//                putToEventHistory(time, text);
//
//                break;
//
//            case DataSrvMsg.CODE_BAD_LANGUAGE:
//
//                break;
//
//            case DataSrvMsg.CODE_COMMAND_ACCEPTED:
//
//                break;
//
//            case DataSrvMsg.CODE_COMMAND_BAD_SYNTAX:
//
//                break;
//
//            case DataSrvMsg.CODE_NO_SUCH_DEVICE:
//
//                text = mainService.getString(R.string.msg_no_such_device);
//
//                setTextForAlertDialog(text);
//                mainService.notifyAlarmDeviceChanged(getJid());
//                mainService.showInfoNotificationAboutDevice(this, time, text);
//
//                break;
//
//            case DataSrvMsg.CODE_BURGLARY_CODE_REQUEST:
//
//                text = mainService.getString(R.string.msg_burglary_code_request);
//
//                setTextForAlertDialog(text);
//                mainService.notifyAlarmDeviceChanged(getJid());
//                mainService.showInfoNotificationAboutDevice(this, time, text);
//
//                break;
//
//            case DataSrvMsg.CODE_COMMAND_FAILED:
//
//                text = mainService.getString(R.string.msg_command_failed);
//
//                setTextForAlertDialog(text);
//                mainService.notifyAlarmDeviceChanged(getJid());
//                mainService.showInfoNotificationAboutDevice(this, time, text);
//
//                break;
//
//            case DataSrvMsg.CODE_USER_NOT_ACTIVATED:
//
//                text = mainService.getString(R.string.msg_user_not_activated);
//
//                setTextForAlertDialog(text);
//                mainService.notifyAlarmDeviceChanged(getJid());
//                mainService.showInfoNotificationAboutDevice(this, time, text);
//
//                setIsSubscribed(false);
//                mainService.requestUnsubscription(getJid());
//
//                break;
//
//            case DataSrvMsg.CODE_USER_ALREADY_ACTIVATED:
//
//                text = mainService.getString(R.string.msg_user_already_activated);
//
//                setTextForAlertDialog(text);
//                mainService.notifyAlarmDeviceChanged(getJid());
//                mainService.showInfoNotificationAboutDevice(this, time, text);
//
//                mainService.requestSubscription(getJid());
//
//                break;
//
//            case DataSrvMsg.CODE_DEVICE_PROPERTIES:
//
//                payload = data.getPayload();
//
//                if (null == payload) {
//                    return;
//                }
//
//                procMuzDev(chat, message, time, (DataMuzDev) payload);
//
//                break;
//
//            case DataSrvMsg.CODE_ACTIVE_USERS_LIST:
//
//                payload = data.getPayload();
//
//                if (null == payload) {
//                    return;
//                }
//
//                procDevUsrList(chat, message, time, (DataDevUsersList) payload);
//
//                break;
//
//            case DataSrvMsg.CODE_DEVICE_MUST_BE_DISARMED:
//
//                text = mainService.getString(R.string.msg_device_must_be_disarmed);
//
//                setTextForAlertDialog(text);
//                mainService.notifyAlarmDeviceChanged(getJid());
//                mainService.showInfoNotificationAboutDevice(this, time, text);
//
//                break;
//
//            case DataSrvMsg.CODE_NO_SUCH_USER:
//
//                text = mainService.getString(R.string.msg_no_such_user);
//
//                setTextForAlertDialog(text);
//                mainService.notifyAlarmDeviceChanged(getJid());
//                mainService.showInfoNotificationAboutDevice(this, time, text);
//
//                break;
//
//            case DataSrvMsg.CODE_USER_SENT_COMMAND:
//
//                payload = data.getPayload();
//
//                if (null == payload) {
//                    return;
//                }
//
//                procDevUsr(chat, message, time, (DataDevUser) payload);
//
//                break;
//
//            case DataSrvMsg.CODE_USER_ACTIVATED:
//
//                payload = data.getPayload();
//
//                if (null == payload) {
//                    return;
//                }
//
//                if (payload instanceof DataDevUser) {
//
//                    String usrJid = ((DataDevUser) payload).getJid();
//                    usrJid = MiscFunc.bareJid(usrJid);
//
//                    text = mainService.getString(R.string.msg_user_activated);
//                    text = text + ": " + MiscFunc.userFromJid(usrJid);
//
//                    mainService.showInfoNotificationAboutDevice(this, time, text);
//                    putToEventHistory(time, text);
//
//                    if (mainService.getCurrentUser().equals(usrJid)) { // you're activated
//
//                        setTextForAlertDialog(text);
//                        mainService.notifyAlarmDeviceChanged(getJid());
//
//                        // get subscription to device
//                        mainService.requestSubscription(getJid());
//
//                        setIsSubscribed(true);
//
//                    } else { // other user activated
//
//                        // add user to db and list
//                        AlarmDeviceUser user = new AlarmDeviceUser(mainService, getJabConn(),
//                                getTel(), (DataDevUser) payload);
//
//                        users.add(user);
//
//                        // add to roster and get subscription to that user
//                        mainService.addToRoster(usrJid, MiscFunc.userFromJid(usrJid));
//                        mainService.requestSubscription(usrJid);
//
//                    }
//                }
//
//                break;
//
//            case DataSrvMsg.CODE_USER_DEACTIVATED:
//
//                payload = data.getPayload();
//
//                if (null == payload) {
//                    return;
//                }
//
//                if (payload instanceof DataDevUser) {
//
//                    String usrJid = ((DataDevUser) payload).getJid();
//                    usrJid = MiscFunc.bareJid(usrJid);
//
//                    text = mainService.getString(R.string.msg_user_deactivated);
//                    text = text + ": " + MiscFunc.userFromJid(usrJid);
//
//                    mainService.showInfoNotificationAboutDevice(this, time, text);
//                    putToEventHistory(time, text);
//
//                    if (mainService.getCurrentUser().equals(usrJid)) { // you're deactivated
//
//                        setTextForAlertDialog(text);
//                        mainService.notifyAlarmDeviceChanged(getJid());
//                        setIsSubscribed(false);
//                        mainService.notifyAlarmDeviceChanged(getJid());
//
//                    } else { // other user deactivated
//                        mainService.removeFromRoster(usrJid);
//                    }
//                }
//
//                break;
//
//            case DataSrvMsg.CODE_DEVICE_CONFIG_CHANGED:
//
//                sendMessage(CMD_PANEL);
//
//                break;
//
//            default:
//
//                break;
//        }
//    }
//
//
//    private void procDevUsr(Chat chat, Message message, long time, DataDevUser data) {
//
//        AbstractData payload = data.getPayload();
//
//        if (null == payload) {
//            return;
//        }
//
//        if (!(payload instanceof DataIntOxCmd)) {
//            return;
//        }
//
//        procIntOxCmd(chat, message, time, (DataIntOxCmd) payload,
//                new AlarmDeviceUser(mainService, getJabConn(), getTel(), data));
//    }
//
//
//    private void procIntOxCmd(Chat chat, Message message, long time, DataIntOxCmd data, AlarmDeviceUser user) {
//
//        Integer num = data.getZone();
//
//        String zone;
//
//        if (null == num) {
//
//            TreeSet<Integer> nums = new TreeSet<Integer>();
//
//            for (AlarmDeviceZone z : getZones()) {
//
//                if (data.getCode() == IntOxProto.CMD_Arm) {
//
//                    if (!(z.getIsArmed() == null || z.getIsArmed() == false)) {
//                        continue;
//                    }
//
//                } else if (data.getCode() == IntOxProto.CMD_Disarm) {
//
//                    if (z.getIsArmed() == null || z.getIsArmed() == false) {
//                        continue;
//                    }
//                }
//
//                nums.add(z.getNum());
//            }
//
//            zone = zoneNumsFromTreeSet(nums);
//
//        } else {
//            zone = num.toString();
//        }
//
//        String info = mainService.getString(R.string.DEV_UserLetter) +
//                ((null == user) ? "" : user.getUserNum()) + ": ";
//
//        switch (data.getCode()) {
//
//            case IntOxProto.CMD_Arm:
//
//                info += mainService.getString(R.string.CMD_Arm);
//                info += (": " + zone);
//
//                break;
//
//            case IntOxProto.CMD_Disarm:
//
//                info += mainService.getString(R.string.CMD_Disarm);
//                info += (": " + zone);
//
//                break;
//
//            case IntOxProto.CMD_GetState:
//
//                info += mainService.getString(R.string.CMD_GetState);
//
//                break;
//        }
//
//        setLastEventText(info);
//        putToEventHistory(time, info);
//
//        mainService.showTickNotificationAboutDevice(this, time, info);
//        mainService.notifyAlarmDeviceChanged(getJid());
//    }


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


//    private void procIntOxMsg(Chat chat, Message message, long time, DataIntOxMsg data) {
//
//        TreeSet<Integer> zoneNum = data.getZoneAsTreeSet();
//        String zone = data.getZone();
//        Integer userNum = data.getUser();
//
//        AlarmDeviceZone alarmDeviceZone;
//        String info = "";
//        Uri sound = mainService.getTickSoundUri();
//        Integer type = null;
//
//
//        switch (data.getCode()) {
//
//            case IntOxProto.MSG_DisarmFailure:
//
//                info = mainService.getString(R.string.MSG_DisarmFailure);
//                sound = mainService.getInfoSoundUri();
//                type = AppDb.EventHistoryTable.EVENT_TYPE_WARNING;
//
//                break;
//
//            case IntOxProto.MSG_ArmFailure:
//
//                info = mainService.getString(R.string.MSG_ArmFailure);
//                sound = mainService.getInfoSoundUri();
//                type = AppDb.EventHistoryTable.EVENT_TYPE_WARNING;
//
//                break;
//
//            case IntOxProto.MSG_Alarm:
//
//                info = mainService.getString(R.string.MSG_Alarm);
//                sound = mainService.getAlarmSoundUri();
//                type = AppDb.EventHistoryTable.EVENT_TYPE_ALARM;
//
//                for (int num : zoneNum) {
//                    alarmDeviceZone = getZoneByNumOrCreateNew(num);
//                    alarmDeviceZone.setIsFired(true);
//                }
//
//                break;
//
//            case IntOxProto.MSG_Armed:
//            case IntOxProto.MSG_AutoArmed:
//            case IntOxProto.MSG_GrpAutoArmed:
//
//                info = mainService.getString(R.string.MSG_Armed);
//
//                for (int num : zoneNum) {
//                    alarmDeviceZone = getZoneByNumOrCreateNew(num);
//                    alarmDeviceZone.setState(AlarmDeviceZone.ST_OK); // FIXME: check this
//                    alarmDeviceZone.setIsArmed(true);
//                    alarmDeviceZone.setIsFired(false);
//                }
//
//                break;
//
//            case IntOxProto.MSG_Disarmed:
//            case IntOxProto.MSG_AutoDisarmed:
//            case IntOxProto.MSG_GrpAutoDisarmed:
//
//                info = mainService.getString(R.string.MSG_Disarmed);
//
//                for (int num : zoneNum) {
//                    alarmDeviceZone = getZoneByNumOrCreateNew(num);
//                    alarmDeviceZone.setIsArmed(false);
//                    alarmDeviceZone.setIsFired(false);
//                }
//
//                break;
//
//            case IntOxProto.MSG_AlarmShort:
//
//                info = mainService.getString(R.string.MSG_AlarmShort);
//                sound = mainService.getAlarmSoundUri();
//                type = AppDb.EventHistoryTable.EVENT_TYPE_ALARM;
//
//                for (int num : zoneNum) {
//                    alarmDeviceZone = getZoneByNumOrCreateNew(num);
//                    alarmDeviceZone.setState(AlarmDeviceZone.ST_SHORT);
//                    alarmDeviceZone.setIsFired(true);
//                }
//
//                break;
//
//            case IntOxProto.MSG_AlarmTorn:
//
//                info = mainService.getString(R.string.MSG_AlarmTorn);
//                sound = mainService.getAlarmSoundUri();
//                type = AppDb.EventHistoryTable.EVENT_TYPE_ALARM;
//
//                for (int num : zoneNum) {
//                    alarmDeviceZone = getZoneByNumOrCreateNew(num);
//                    alarmDeviceZone.setState(AlarmDeviceZone.ST_TORN);
//                    alarmDeviceZone.setIsFired(true);
//                }
//
//                break;
//
//            case IntOxProto.MSG_StateOk:
//            case IntOxProto.MSG_GrpStateOk:
//
//                info = mainService.getString(R.string.MSG_StateOk);
//
//                for (int num : zoneNum) {
//                    alarmDeviceZone = getZoneByNumOrCreateNew(num);
//                    alarmDeviceZone.setState(AlarmDeviceZone.ST_OK);
//                }
//
//                break;
//
//            case IntOxProto.MSG_Short:
//            case IntOxProto.MSG_GrpShort:
//
//                info = mainService.getString(R.string.MSG_Short);
//
//                for (int num : zoneNum) {
//                    alarmDeviceZone = getZoneByNumOrCreateNew(num);
//                    alarmDeviceZone.setState(AlarmDeviceZone.ST_SHORT);
//                }
//
//                break;
//
//            case IntOxProto.MSG_Torn:
//            case IntOxProto.MSG_GrpTorn:
//
//                info = mainService.getString(R.string.MSG_Torn);
//
//                for (int num : zoneNum) {
//                    alarmDeviceZone = getZoneByNumOrCreateNew(num);
//                    alarmDeviceZone.setState(AlarmDeviceZone.ST_TORN);
//                }
//
//                break;
//
//            case IntOxProto.MSG_NoInfo:
//
//                break;
//
//            case IntOxProto.MSG_Burglary:
//
//                info = mainService.getString(R.string.MSG_Burglary);
//                sound = mainService.getAlarmSoundUri();
//                type = AppDb.EventHistoryTable.EVENT_TYPE_ALARM;
//
//                break;
//
//            case IntOxProto.MSG_Fault:
//
//                info = mainService.getString(R.string.MSG_Fault);
//                sound = mainService.getInfoSoundUri();
//                type = AppDb.EventHistoryTable.EVENT_TYPE_WARNING;
//
//                if (null == zoneNum || zoneNum.isEmpty()) {
//
//                    setIsDevFailure(true);
//
//                    break;
//                }
//
//                for (int num : zoneNum) {
//                    alarmDeviceZone = getZoneByNumOrCreateNew(num);
//                    alarmDeviceZone.setIsZoneFailure(true);
//                }
//
//                break;
//
//            case IntOxProto.MSG_NoFault:
//
//                info = mainService.getString(R.string.MSG_NoFault);
//
//                if (null == zoneNum || zoneNum.isEmpty()) {
//
//                    setIsDevFailure(false);
//
//                    break;
//                }
//
//                for (int num : zoneNum) {
//                    alarmDeviceZone = getZoneByNumOrCreateNew(num);
//                    alarmDeviceZone.setIsZoneFailure(false);
//                }
//
//                break;
//
//            case IntOxProto.MSG_CaseOpened:
//
//                info = mainService.getString(R.string.MSG_CaseOpened);
//
//                if (null == zoneNum || zoneNum.isEmpty()) {
//                    setIsTamperOpened(true);
//                } else {
//                    for (int num : zoneNum) {
//                        alarmDeviceZone = getZoneByNumOrCreateNew(num);
//                        alarmDeviceZone.setIsTamperOpened(true);
//                    }
//                }
//
//                if (isInAlarm()) {
//                    sound = mainService.getAlarmSoundUri();
//                    type = AppDb.EventHistoryTable.EVENT_TYPE_ALARM;
//                } else {
//                    sound = mainService.getInfoSoundUri();
//                    type = AppDb.EventHistoryTable.EVENT_TYPE_WARNING;
//                }
//
//                break;
//
//            case IntOxProto.MSG_BadAC:
//
//                info = mainService.getString(R.string.MSG_BadAC);
//                sound = mainService.getInfoSoundUri();
//                type = AppDb.EventHistoryTable.EVENT_TYPE_WARNING;
//
//                setIsPowerLost(true);
//
//                break;
//
//            case IntOxProto.MSG_BadDC:
//
//                info = mainService.getString(R.string.MSG_BadDC);
//                sound = mainService.getInfoSoundUri();
//                type = AppDb.EventHistoryTable.EVENT_TYPE_WARNING;
//
//                if (null == zoneNum || zoneNum.isEmpty()) {
//                    setIsBatteryLow(true);
//                    break;
//                }
//
//                for (int num : zoneNum) {
//                    alarmDeviceZone = getZoneByNumOrCreateNew(num);
//                    alarmDeviceZone.setIsBatteryLow(true);
//                }
//
//                break;
//
//            case IntOxProto.MSG_GoodAC:
//
//                info = mainService.getString(R.string.MSG_GoodAC);
//                setIsPowerLost(false);
//
//                break;
//
//            case IntOxProto.MSG_GoodDC:
//
//                info = mainService.getString(R.string.MSG_GoodDC);
//
//                if (null == zoneNum || zoneNum.isEmpty()) {
//
//                    setIsBatteryLow(false);
//
//                    break;
//                }
//
//                for (int num : zoneNum) {
//                    alarmDeviceZone = getZoneByNumOrCreateNew(num);
//                    alarmDeviceZone.setIsBatteryLow(false);
//                }
//
//                break;
//
//            case IntOxProto.MSG_CaseClosed:
//
//                info = mainService.getString(R.string.MSG_CaseClosed);
//
//                if (null == zoneNum || zoneNum.isEmpty()) {
//
//                    setIsTamperOpened(false);
//
//                    break;
//                }
//
//                for (int num : zoneNum) {
//                    alarmDeviceZone = getZoneByNumOrCreateNew(num);
//                    alarmDeviceZone.setIsTamperOpened(false);
//                }
//
//                break;
//
//            case IntOxProto.MSG_Crash:
//
//                info = mainService.getString(R.string.MSG_Crash);
//                sound = mainService.getInfoSoundUri();
//                type = AppDb.EventHistoryTable.EVENT_TYPE_WARNING;
//
//                break;
//
//            case IntOxProto.MSG_LinkOff:
//
//                info = mainService.getString(R.string.MSG_LinkOff);
//                sound = mainService.getInfoSoundUri();
//                type = AppDb.EventHistoryTable.EVENT_TYPE_WARNING;
//
//                for (int num : zoneNum) {
//                    alarmDeviceZone = getZoneByNumOrCreateNew(num);
//                    alarmDeviceZone.setIsLinkLost(true);
//                }
//
//                break;
//
//            case IntOxProto.MSG_LinkOn:
//
//                info = mainService.getString(R.string.MSG_LinkOn);
//
//                for (int num : zoneNum) {
//                    alarmDeviceZone = getZoneByNumOrCreateNew(num);
//                    alarmDeviceZone.setIsLinkLost(false);
//                }
//
//                break;
//
//            case IntOxProto.MSG_SetProg:
//
//                info = mainService.getString(R.string.MSG_SetProg);
//                sound = mainService.getInfoSoundUri();
//
//                break;
//
//            case IntOxProto.MSG_Version:
//
//                info = mainService.getString(R.string.MSG_Version);
//
//                if (!(null == zoneNum || zoneNum.isEmpty() || null == userNum)) {
//                    int major = zoneNum.iterator().next();
//                    int minor = userNum;
//                    info = info + ": " + major + "." + minor;
//                }
//
//                break;
//
//            case IntOxProto.MSG_DeviceOn:
//
//                info = mainService.getString(R.string.MSG_DeviceOn);
//                sound = mainService.getInfoSoundUri();
//
//                break;
//
//            case IntOxProto.MSG_DeviceOff:
//
//                info = mainService.getString(R.string.MSG_DeviceOff);
//                sound = mainService.getInfoSoundUri();
//
//                break;
//        }
//
//        if (!(TextUtils.isEmpty(zone) || data.getCode() == IntOxProto.MSG_Version)) {
//            info += (": " + zone);
//        }
//
//        if (!(null == userNum || userNum <= 0 || userNum > 16)) {
//            switch (data.getCode()) {
//                case IntOxProto.MSG_Armed:
//                case IntOxProto.MSG_Disarmed:
//                case IntOxProto.MSG_GrpAutoArmed:
//                case IntOxProto.MSG_GrpAutoDisarmed:
//                case IntOxProto.MSG_Burglary:
//                    info += ("; " + getContext().getString(R.string.DEV_UserLetter) + userNum);
//                    break;
//            }
//        }
//
//        setLastEventText(info);
//        putToEventHistory(time, info, type);
//
//        mainService.showNotificationAboutDevice(this, time, info, sound);
//        mainService.notifyAlarmDeviceChanged(getJid());
//    }
//
//
//    private void procMuzDev(Chat chat, Message message, long time, DataMuzDev data) {
//
//        setIsOnline(data.getIsOnline());
//        setIsTamperOpened(data.getIsTamperOpened());
//        setIsBatteryLow(data.getIsBatteryLow());
//        setIsPowerLost(data.getIsPowerLost());
//        setIsDevFailure(data.getIsDevFailure());
//
//        SortedSet<AlarmDeviceZone> tmpZones = new TreeSet<AlarmDeviceZone>();
//
//        for (AlarmDeviceZone z : getZones()) {
//            tmpZones.add(z);
//        }
//
//        getZones().clear();
//
//        if (!(data.getZones() == null || data.getZones().getZones() == null)) {
//
//            for (DataDevZone z : data.getZones().getZones()) {
//
//                AlarmDeviceZone zone = new AlarmDeviceZone(mainService, getJabConn(), getTel(), z.getNum());
//
//                zone.setState(z.getState());
//                zone.setIsArmed(z.getIsArmed());
//                zone.setIsTamperOpened(z.getIsTamperOpened());
//                zone.setIsBatteryLow(z.getIsBatteryLow());
//                zone.setIsFired(z.getIsFired());
//                zone.setIsLinkLost(z.getIsLinkLost());
//                zone.setIsZoneFailure(z.getIsZoneFailure());
//                zone.setCustomName(z.getCustomName());
//
//                getZones().add(zone);
//            }
//        }
//
//        for (AlarmDeviceZone z1 : tmpZones) {
//            boolean found = false;
//            for (AlarmDeviceZone z2 : getZones()) {
//                if (z2.getNum() == z1.getNum()) {
//                    found = true;
//                    break;
//                }
//            }
//            if (!found) {
//                z1.remove();
//            }
//        }
//        tmpZones.clear();
//
//        mainService.notifyAlarmDeviceChanged(getJid());
//    }
//
//
//    private void procDevUsrList(Chat chat, Message message, long time, final DataDevUsersList data) {
//
//        Log.d(TAG, "procDevUsrList()");
//
//        new Runnable() {
//            @Override
//            public void run() {
//                ContentValues values = new ContentValues();
//
//                values.put(AppDb.AlarmDeviceUserTable.COLUMN_TO_DELETE, 1);
//
//                String where = AppDb.AlarmDeviceUserTable.COLUMN_ACCOUNT + " = ? AND " +
//                        AppDb.AlarmDeviceUserTable.COLUMN_DEV_GID + " = ?";
//
//                String whereArgs[] = { CommonVar.getAccount(), getTel() };
//
//                getContentResolver().update(AlarmDeviceUserProvider.CONTENT_URI, values, where, whereArgs);
//
//                for (DataDevUser du : data.getList()) {
//
//                    for (AlarmDeviceUser user : users) {
//
//                        if (user.getUserNum() == du.getNum() && user.getUserName().equals(du.getJid())) {
//
//                            //user.setToDelete(false);
//
//                            values = new ContentValues();
//                            values.put(AppDb.AlarmDeviceUserTable.COLUMN_TO_DELETE, 0);
//
//                            String sel = where + " AND " + AppDb.AlarmDeviceUserTable.COLUMN_NUM + " = ?";
//
//                            String args[] = { CommonVar.getAccount(), getTel(), String.valueOf(user.getUserNum()) };
//
//                            getContentResolver().update(AlarmDeviceUserProvider.CONTENT_URI, values, sel, args);
//
//                            break;
//                        }
//                    }
//                }
//
//                where = where + " AND " + AppDb.AlarmDeviceUserTable.COLUMN_TO_DELETE + " = 1";
//
//                getContentResolver().delete(AlarmDeviceUserProvider.CONTENT_URI, where ,whereArgs);
//
//                for (AlarmDeviceUser user : users) {
//                    user.clear();
//                }
//
//                users.clear();
//
//                for (DataDevUser du : data.getList()) {
//
//                    String name = null;
//
//                    RosterEntry entry = mainService.getRosterEntry(du.getJid());
//
//                    if (null != entry) {
//                        name = entry.getName();
//                    }
//
//                    if (TextUtils.isEmpty(name) || name.equals(du.getJid())) {
//                        name = MiscFunc.userFromJid(du.getJid());
//                    }
//
//                    AlarmDeviceUser user =  new AlarmDeviceUser(mainService, getJabConn(),
//                            getTel(), du.getNum(), du.getJid(), name);
//
//                    users.add(user);
//                    Log.d(TAG, "user online: " + user.getIsOnline());
//                }
//
//            }
//        }.run();
//    }


    @Override
    public int compareTo(Object another) {
        return this.getTel().compareToIgnoreCase(((AlarmDevice) another).getTel());
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
//        return getLockImgResourceId(getIsSubscribed(), isArmed(), isInAlarm());
        return getLockImgResourceId(true, isArmed(), isInAlarm());
    }


//    public static int getOnlineImgResourceId(Boolean isOnline) {
//
//        int res;
//
//        if (null == isOnline) {
//            res = R.drawable.ic_action_question;
//        } else if (isOnline) {
//            res = R.drawable.ic_action_link_ok;
//        } else {
//            res = R.drawable.ic_action_link_lost;
//        }
//
//        return res;
//    }
//
//
//    public int getOnlineImgResourceId() {
//        return getOnlineImgResourceId(getIsOnline());
//    }


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
