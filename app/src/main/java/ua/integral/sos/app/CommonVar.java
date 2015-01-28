package ua.integral.sos.app;

import android.net.Uri;

/**
 * Created by aledin on 21.12.14.
 */
public class CommonVar {

    private static final CommonVar ourInstance = new CommonVar();

    public static CommonVar getInstance() {
        return ourInstance;
    }

    private CommonVar() {
    }

    private static Uri alarmSoundUri;
    private static Uri infoSoundUri;
    private static Uri tickSoundUri;

    private static int maxHistoryRecords;

    private static int deviceDetailSelectedTabIdx;

    public synchronized static int getDeviceDetailSelectedTabIdx() {
        return deviceDetailSelectedTabIdx;
    }

    public synchronized static void setDeviceDetailSelectedTabIdx(int deviceDetailSelectedTabIdx) {
        CommonVar.deviceDetailSelectedTabIdx = deviceDetailSelectedTabIdx;
    }

    public synchronized static int getMaxHistoryRecords() {
        return maxHistoryRecords;
    }

    public synchronized static void setMaxHistoryRecords(int maxHistoryRecords) {
        CommonVar.maxHistoryRecords = maxHistoryRecords;
    }

    public synchronized static Uri getAlarmSoundUri() {
        return alarmSoundUri;
    }

    public synchronized static void setAlarmSoundUri(Uri alarmSoundUri) {
        getInstance().alarmSoundUri = alarmSoundUri;
    }

    public synchronized static Uri getInfoSoundUri() {
        return infoSoundUri;
    }

    public synchronized static void setInfoSoundUri(Uri infoSoundUri) {
        getInstance().infoSoundUri = infoSoundUri;
    }

    public synchronized static Uri getTickSoundUri() {
        return tickSoundUri;
    }

    public synchronized static void setTickSoundUri(Uri tickSoundUri) {
        getInstance().tickSoundUri = tickSoundUri;
    }
}
