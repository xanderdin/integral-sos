package ua.integral.sos.app;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

/**
 * Created by aledin on 26.01.15.
 */
public class MiscProviderFunc {


    public static void setProviderBooleanValue(final Context ctx, final Uri uri, final String col, final Boolean val) {
        setProviderValue(ctx, uri, col, val);
    }


    public static void setProviderStringValue(final Context ctx, final Uri uri, final String col, final String val) {
        setProviderValue(ctx, uri, col, val);
    }


    public static void setProviderIntegerValue(final Context ctx, final Uri uri, final String col, final Integer val) {
        setProviderValue(ctx, uri, col, val);
    }


    public static void setProviderDoubleValue(final Context ctx, final Uri uri, final String col, final Double val) {
        setProviderValue(ctx, uri, col, val);
    }


    private static void setProviderValue(final Context ctx, final Uri uri, final String col, final Object val) {

        final ContentValues values = new ContentValues();

        if (null == val) {

            values.putNull(col);

        } else if (val instanceof String) {

            values.put(col, (String) val);

        } else if (val instanceof Boolean) {

            values.put(col, ((Boolean) val) ? 1 : 0);

        } else if (val instanceof Integer) {

            values.put(col, (Integer) val);

        } else if (val instanceof Long) {

            values.put(col, (Long) val);

        } else if (val instanceof Double) {

            values.put(col, (Double) val);

        } else {

            // do nothing
            return;
        }

        new Runnable() {
            @Override
            public void run() {
                ctx.getContentResolver().update(uri, values, null, null);
            }
        }.run();
    }


    public static void removeProviderRecord(final Context ctx, final Uri uri) {
        new Runnable() {
            @Override
            public void run() {
                ctx.getContentResolver().delete(uri, null, null);
            }
        }.run();
    }
}
