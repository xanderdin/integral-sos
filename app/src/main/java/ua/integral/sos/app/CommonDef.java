package ua.integral.sos.app;

/**
 * Created by aledin on 26.01.15.
 */
public class CommonDef {

    public final static String PREF_KEY_MAX_HISTORY_RECORDS = "max_history_records";
    public final static int    PREF_VAL_MAX_HISTORY_RECORDS = 1000;

    public final static String PREF_KEY_LOW_MONEY_THRESHOLD = "low_money_threshold";
    public final static double PREF_VAL_LOW_MONEY_THRESHOLD = 10.0;

    public final static String PREF_KEY_URI_SOUND_ALARM  = "uri_sound_alarm";
    public final static String PREF_KEY_URI_SOUND_INFO   = "uri_sound_info";
    public final static String PREF_KEY_URI_SOUND_TICK   = "uri_sound_tick";

    public final static String PREF_VAL_DEFAULT_URI_SOUND_ALARM =
            "android.resource://my.pocket.tomtit/raw/alarm_sound_01";
    public final static String PREF_VAL_DEFAULT_URI_SOUND_INFO =
            "android.resource://my.pocket.tomtit/raw/info_sound_01";
    public final static String PREF_VAL_DEFAULT_URI_SOUND_TICK =
            "android.resource://my.pocket.tomtit/raw/tick_sound_01";

    public final static String EXTRA_TEL = "tel";
    public final static String EXTRA_NAME = "name";
    public final static String EXTRA_SELECTED_ID = "selectedId";
    public final static String EXTRA_CONTACT_LOOKUP_KEY = "contactLookupKey";

    public final static int NOTIFICATION_TYPE_TICK      = 0;
    public final static int NOTIFICATION_TYPE_INFO      = 1;
    public final static int NOTIFICATION_TYPE_ALARM     = 2;
}
