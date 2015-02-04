package ua.integral.sos.app;

import android.app.Application;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * Created by aledin on 22.12.14.
 */
public class App extends Application implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate() {
        super.onCreate();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        String val = sp.getString(CommonDef.PREF_KEY_MAX_HISTORY_RECORDS,
                String.valueOf(CommonDef.PREF_VAL_MAX_HISTORY_RECORDS));
        if (TextUtils.isEmpty(val)) {
            val = "0";
        }
        CommonVar.setMaxHistoryRecords(Integer.valueOf(val));

        initSoundDefaultPreferences(sp);
        initSounds();

        initAlarmDeviceList();
    }

    private static void initSoundDefaultPreferences(SharedPreferences sp) {

        // The following values could not be initialized
        // from R.xml.preferences, so let's set them here.
        if (!sp.contains(CommonDef.PREF_KEY_URI_SOUND_ALARM)) {
            sp.edit()
                    .putString(CommonDef.PREF_KEY_URI_SOUND_ALARM, CommonDef.PREF_VAL_DEFAULT_URI_SOUND_ALARM)
                    .commit();
        }
        if (!sp.contains(CommonDef.PREF_KEY_URI_SOUND_INFO)) {
            sp.edit()
                    .putString(CommonDef.PREF_KEY_URI_SOUND_INFO, CommonDef.PREF_VAL_DEFAULT_URI_SOUND_INFO)
                    .commit();
        }
        if (!sp.contains(CommonDef.PREF_KEY_URI_SOUND_TICK)) {
            sp.edit()
                    .putString(CommonDef.PREF_KEY_URI_SOUND_TICK, CommonDef.PREF_VAL_DEFAULT_URI_SOUND_TICK)
                    .commit();
        }

    }

    private void initSounds() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        CommonVar.setAlarmSoundUri(Uri.parse(sp.getString(
                CommonDef.PREF_KEY_URI_SOUND_ALARM, CommonDef.PREF_VAL_DEFAULT_URI_SOUND_ALARM)));
        CommonVar.setInfoSoundUri(Uri.parse(sp.getString(
                CommonDef.PREF_KEY_URI_SOUND_INFO, CommonDef.PREF_VAL_DEFAULT_URI_SOUND_INFO)));
        CommonVar.setTickSoundUri(Uri.parse(sp.getString(
                CommonDef.PREF_KEY_URI_SOUND_TICK, CommonDef.PREF_VAL_DEFAULT_URI_SOUND_TICK)));
    }

    private void initAlarmDeviceList() {
        String[] projection = {
                AppDb.AlarmDeviceTable.COLUMN_CONTACT_LOOKUP_KEY,
        };
        String selection = null;
        String[] selectionArgs = null;
        String sortOrder = AppDb.AlarmDeviceTable.COLUMN_SORT_ORDER;
        Cursor cursor = getContentResolver().query(AlarmDeviceProvider.CONTENT_URI,
                projection, selection, selectionArgs, sortOrder);
        while (cursor.moveToNext()) {
            AlarmDeviceList.put(new AlarmDevice(getApplicationContext(), cursor.getString(0)));
        }
        cursor.close();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (CommonDef.PREF_KEY_URI_SOUND_ALARM.equals(key) ||
                CommonDef.PREF_KEY_URI_SOUND_INFO.equals(key) ||
                CommonDef.PREF_KEY_URI_SOUND_TICK.equals(key)) {
            initSounds();
        } else if (CommonDef.PREF_KEY_MAX_HISTORY_RECORDS.equals(key)) {
            String val = sharedPreferences.getString(key, String.valueOf(CommonDef.PREF_VAL_MAX_HISTORY_RECORDS));
            if (TextUtils.isEmpty(val)) {
                val = "0";
            }
            CommonVar.setMaxHistoryRecords(Integer.valueOf(val));
        }
    }
}
