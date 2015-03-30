package ua.integral.sos.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by aledin on 26.01.15.
 */
public class AppDb extends SQLiteOpenHelper {

    private final static String TAG = "AppDb";

    private final static String DB_NAME = "app.db";

    private final static int DB_VERSION = 1;


    // AlarmDevice table contract
    public final static class AlarmDeviceTable implements BaseColumns {

        // This class cannot be instantiated
        private AlarmDeviceTable() {}

        public final static String TABLE_NAME = "alarmDevice";

        public final static String AFTER_DELETE_TRIGGER_NAME = "alarmDeviceAfterDelete";

        public final static String COLUMN_ID                  = "_id";
        public final static String COLUMN_CONTACT_LOOKUP_KEY  = "contactLookupKey";
        public final static String COLUMN_SORT_ORDER          = "sortOrder";
        public final static String COLUMN_IS_ENABLED          = "isEnabled";
        public final static String COLUMN_IS_TAMPER_OPENED    = "isTamperOpened";
        public final static String COLUMN_IS_BATTERY_LOW      = "isBatteryLow";
        public final static String COLUMN_IS_POWER_LOST       = "isPowerLost";
        public final static String COLUMN_IS_DEV_FAILURE      = "isDevFailure";
        public final static String COLUMN_IS_DEVICE_OFF       = "isDeviceOff";
        public final static String COLUMN_MONEY_LEFT          = "moneyLeft";

        public final static String COLUMNS[] = {
                COLUMN_ID,
                COLUMN_CONTACT_LOOKUP_KEY,
                COLUMN_SORT_ORDER,
                COLUMN_IS_ENABLED,
                COLUMN_IS_TAMPER_OPENED,
                COLUMN_IS_BATTERY_LOW,
                COLUMN_IS_POWER_LOST,
                COLUMN_IS_DEV_FAILURE,
                COLUMN_IS_DEVICE_OFF,
                COLUMN_MONEY_LEFT,
        };
    }


    // AlarmDeviceZone table contract
    public final static class AlarmDeviceZoneTable implements BaseColumns {

        // This class cannot be instantiated
        private AlarmDeviceZoneTable() {}

        public final static String TABLE_NAME = "alarmDeviceZone";

        public final static String TABLE_INDEX_01_NAME = "alarmDeviceZoneIdx01";

        public final static String COLUMN_ID                  = "_id";
        public final static String COLUMN_DEV_ID              = "devId";
        public final static String COLUMN_ZONE_NUM            = "zoneNum";
        public final static String COLUMN_ZONE_TYPE           = "zoneType";
        public final static String COLUMN_ZONE_NAME           = "zoneName";
        public final static String COLUMN_SORT_ORDER          = "sortOrder";
        public final static String COLUMN_STATE               = "state";
        public final static String COLUMN_IS_ARMED            = "isArmed";
        public final static String COLUMN_IS_FIRED            = "isFired";
        public final static String COLUMN_IS_TAMPER_OPENED    = "isTamperOpened";
        public final static String COLUMN_IS_BATTERY_LOW      = "isBatteryLow";
        public final static String COLUMN_IS_POWER_LOST       = "isPowerLost";
        public final static String COLUMN_IS_LINK_LOST        = "isLinkLost";
        public final static String COLUMN_IS_ZONE_FAILURE     = "isZoneFailure";

        public final static String COLUMNS[] = {
                COLUMN_ID,
                COLUMN_DEV_ID,
                COLUMN_ZONE_NUM,
                COLUMN_ZONE_TYPE,
                COLUMN_ZONE_NAME,
                COLUMN_SORT_ORDER,
                COLUMN_STATE,
                COLUMN_IS_ARMED,
                COLUMN_IS_FIRED,
                COLUMN_IS_TAMPER_OPENED,
                COLUMN_IS_BATTERY_LOW,
                COLUMN_IS_POWER_LOST,
                COLUMN_IS_LINK_LOST,
                COLUMN_IS_ZONE_FAILURE,
        };
    }


    // History table contract
    public final static class EventHistoryTable implements BaseColumns {

        // This class cannot be instantiated
        private EventHistoryTable() {}

        public final static String TABLE_NAME = "eventHistory";

        public final static String COLUMN_ID            = "_id";
        public final static String COLUMN_DATE_TIME     = "dateTime";
        public final static String COLUMN_DEV_ID        = "devId";
        public final static String COLUMN_EVENT_TYPE    = "eventType";
        public final static String COLUMN_EVENT_TEXT    = "eventText";

        public final static String COLUMNS[] = {
                COLUMN_ID,
                COLUMN_DATE_TIME,
                COLUMN_DEV_ID,
                COLUMN_EVENT_TYPE,
                COLUMN_EVENT_TEXT,
        };

        public final static int EVENT_TYPE_ALARM = 1;
        public final static int EVENT_TYPE_WARNING = 2;
    }


    private final static String ALARM_DEVICE_TABLE_CREATE = "CREATE TABLE " +
            AlarmDeviceTable.TABLE_NAME                 + " (" +
            AlarmDeviceTable.COLUMN_ID                  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            AlarmDeviceTable.COLUMN_CONTACT_LOOKUP_KEY  + " TEXT NOT NULL COLLATE NOCASE UNIQUE, " +
            AlarmDeviceTable.COLUMN_SORT_ORDER          + " INTEGER, " +
            AlarmDeviceTable.COLUMN_IS_ENABLED          + " INTEGER, " +
            AlarmDeviceTable.COLUMN_IS_BATTERY_LOW      + " INTEGER, " +
            AlarmDeviceTable.COLUMN_IS_POWER_LOST       + " INTEGER, " +
            AlarmDeviceTable.COLUMN_IS_TAMPER_OPENED    + " INTEGER, " +
            AlarmDeviceTable.COLUMN_IS_DEV_FAILURE      + " INTEGER, " +
            AlarmDeviceTable.COLUMN_IS_DEVICE_OFF       + " INTEGER, " +
            AlarmDeviceTable.COLUMN_MONEY_LEFT          + " REAL" +
                                                        "); ";


    private final static String ALARM_DEVICE_TABLE_AFTER_DELETE_TRIGGER_CREATE =
            "CREATE TRIGGER "      + AlarmDeviceTable.AFTER_DELETE_TRIGGER_NAME +
            " AFTER DELETE ON "    + AlarmDeviceTable.TABLE_NAME +
            " FOR EACH ROW BEGIN " +
            " DELETE FROM "        + AlarmDeviceZoneTable.TABLE_NAME +
            " WHERE "              + AlarmDeviceZoneTable.COLUMN_DEV_ID +
            " = OLD."              + AlarmDeviceTable.COLUMN_ID + ";" +
            " DELETE FROM "        + EventHistoryTable.TABLE_NAME +
            " WHERE "              + EventHistoryTable.COLUMN_DEV_ID +
            " = OLD."              + AlarmDeviceTable.COLUMN_ID + ";" +
            " END; ";


    private final static String ALARM_DEVICE_ZONE_TABLE_CREATE = "CREATE TABLE " +
            AlarmDeviceZoneTable.TABLE_NAME                + " (" +
            AlarmDeviceZoneTable.COLUMN_ID                 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            AlarmDeviceZoneTable.COLUMN_DEV_ID             + " INTEGER NOT NULL, " +
            AlarmDeviceZoneTable.COLUMN_ZONE_NUM           + " INTEGER NOT NULL, " +
            AlarmDeviceZoneTable.COLUMN_ZONE_TYPE          + " INTEGER NOT NULL DEFAULT " + AlarmDeviceZone.TYPE_DETECTOR + "," +
            AlarmDeviceZoneTable.COLUMN_ZONE_NAME          + " TEXT, " +
            AlarmDeviceZoneTable.COLUMN_SORT_ORDER         + " INTEGER, " +
            AlarmDeviceZoneTable.COLUMN_STATE              + " INTEGER, " +
            AlarmDeviceZoneTable.COLUMN_IS_ARMED           + " INTEGER, " +
            AlarmDeviceZoneTable.COLUMN_IS_FIRED           + " INTEGER, " +
            AlarmDeviceZoneTable.COLUMN_IS_TAMPER_OPENED   + " INTEGER, " +
            AlarmDeviceZoneTable.COLUMN_IS_BATTERY_LOW     + " INTEGER, " +
            AlarmDeviceZoneTable.COLUMN_IS_POWER_LOST      + " INTEGER, " +
            AlarmDeviceZoneTable.COLUMN_IS_LINK_LOST       + " INTEGER, " +
            AlarmDeviceZoneTable.COLUMN_IS_ZONE_FAILURE    + " INTEGER" +
                                                             "); ";


    private final static String ALARM_DEVICE_ZONE_TABLE_INDEX_01_CREATE = "CREATE UNIQUE INDEX " +
            AlarmDeviceZoneTable.TABLE_INDEX_01_NAME  + " ON " +
            AlarmDeviceZoneTable.TABLE_NAME           + " (" +
            AlarmDeviceZoneTable.COLUMN_DEV_ID        + ", " +
            AlarmDeviceZoneTable.COLUMN_ZONE_NUM      + ");";


    private final static String EVENT_HISTORY_TABLE_CREATE = "CREATE TABLE " +
            EventHistoryTable.TABLE_NAME          + " (" +
            EventHistoryTable.COLUMN_ID           + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            EventHistoryTable.COLUMN_DATE_TIME    + " INTEGER, " +
            EventHistoryTable.COLUMN_DEV_ID       + " INTEGER, " +
            EventHistoryTable.COLUMN_EVENT_TYPE   + " INTEGER, " +
            EventHistoryTable.COLUMN_EVENT_TEXT   + " TEXT" +
                                                    "); ";

    public AppDb(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        Log.d(TAG, "AppDb()");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ALARM_DEVICE_TABLE_CREATE);
        db.execSQL(ALARM_DEVICE_ZONE_TABLE_CREATE);
        db.execSQL(ALARM_DEVICE_ZONE_TABLE_INDEX_01_CREATE);
        db.execSQL(EVENT_HISTORY_TABLE_CREATE);
        db.execSQL(ALARM_DEVICE_TABLE_AFTER_DELETE_TRIGGER_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
