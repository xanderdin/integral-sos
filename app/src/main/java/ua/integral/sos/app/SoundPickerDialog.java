package ua.integral.sos.app;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.preference.DialogPreference;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

/**
 * Created by aledin on 10.12.14.
 */
public class SoundPickerDialog extends DialogPreference implements AdapterView.OnItemClickListener {

    private final static String TAG = "SoundChooserDialog";

    private final String TAB_SUPPLIED = "supplied";
    private final String TAB_INTERNAL = "internal";
    private final String TAB_EXTERNAL = "external";

    private final Uri URI_SUPPLIED = Uri.parse("android.resource://" + getContext().getPackageName());
    private final Uri URI_INTERNAL = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
    private final Uri URI_EXTERNAL = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    private TextView selectedTitle;
    private String selectedName;
    private Uri selectedUri;

    private ArrayAdapter<String> suppliedListAdapter;
    private SimpleCursorAdapter internalListAdapter;
    private SimpleCursorAdapter externalListAdapter;

    private ListView suppliedListView;
    private ListView internalListView;
    private ListView externalListView;

    private final String suppliedSoundsTitles[];
    private final String suppliedSoundsValues[];

    private Cursor internalCursor;
    private Cursor externalCursor;

    private TabHost tabs;


    private final String mediaStoreFrom[] = {
            MediaStore.MediaColumns.TITLE,
    };

    private final int mediaStoreTo[] = {
            android.R.id.text1,
    };

    private MediaPlayer mediaPlayer;


    public SoundPickerDialog(Context context, AttributeSet attr) {
        super(context, attr);

        Log.d(TAG, "SoundPickerDialog()");

        if (CommonDef.PREF_KEY_URI_SOUND_ALARM.equals(getKey())) {
            suppliedSoundsTitles = getContext().getResources().getStringArray(R.array.alarm_sound_list_titles);
            suppliedSoundsValues = getContext().getResources().getStringArray(R.array.alarm_sound_list_values);
        } else if (CommonDef.PREF_KEY_URI_SOUND_INFO.equals(getKey())) {
            suppliedSoundsTitles = getContext().getResources().getStringArray(R.array.info_sound_list_titles);
            suppliedSoundsValues = getContext().getResources().getStringArray(R.array.info_sound_list_values);
        } else if (CommonDef.PREF_KEY_URI_SOUND_TICK.equals(getKey())) {
            suppliedSoundsTitles = getContext().getResources().getStringArray(R.array.tick_sound_list_titles);
            suppliedSoundsValues = getContext().getResources().getStringArray(R.array.tick_sound_list_values);
        } else {
            suppliedSoundsTitles = new String[]{};
            suppliedSoundsValues = new String[]{};
        }

        setDialogLayoutResource(R.layout.dialog_sound_picker);
    }


    private void releaseResources() {

        if (null != mediaPlayer) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (null != internalCursor) {
            internalCursor.close();
        }

        if (null != externalCursor) {
            externalCursor.close();
        }
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        Log.d(TAG, "onDismiss()");

        releaseResources();
    }


    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();

        Log.d(TAG, "onActivityDestroy()");

        releaseResources();
    }


    private void fillFromPersistentValues() {

        String val = "";

        if (CommonDef.PREF_KEY_URI_SOUND_ALARM.equals(getKey())) {
            val = getPersistedString(CommonDef.PREF_VAL_DEFAULT_URI_SOUND_ALARM);
        } else if (CommonDef.PREF_KEY_URI_SOUND_INFO.equals(getKey())) {
            val = getPersistedString(CommonDef.PREF_VAL_DEFAULT_URI_SOUND_INFO);
        } else if (CommonDef.PREF_KEY_URI_SOUND_TICK.equals(getKey())) {
            val = getPersistedString(CommonDef.PREF_VAL_DEFAULT_URI_SOUND_TICK);
        }

        if (TextUtils.isEmpty(val) || val.startsWith(URI_SUPPLIED.toString())) {

            tabs.setCurrentTabByTag(TAB_SUPPLIED);

            String sound = Uri.parse(val).getLastPathSegment();

            for (int i = 0; i < suppliedSoundsValues.length; i++) {

                if (null == sound || sound.equals(suppliedSoundsValues[i])) {
                    selectedName = suppliedSoundsTitles[i];
                    suppliedListView.setItemChecked(i, true);
                    suppliedListView.setSelection(i);
                    break;
                }
            }

        } else if (val.startsWith(URI_INTERNAL.toString())) {

            tabs.setCurrentTabByTag(TAB_INTERNAL);

            try {

                String sound = Uri.parse(val).getLastPathSegment();

                long id = Long.valueOf(sound);

                internalCursor.moveToFirst();
                internalCursor.moveToPrevious();

                while (internalCursor.moveToNext()) {

                    if (id == internalCursor.getLong(internalCursor.getColumnIndex(MediaStore.MediaColumns._ID))) {

                        selectedName = internalCursor.getString(internalCursor.getColumnIndex(MediaStore.MediaColumns.TITLE));

                        for (int i = 0; i < internalListView.getCount(); i++) {

                            if (id == internalListView.getItemIdAtPosition(i)) {
                                internalListView.setItemChecked(i, true);
                                internalListView.setSelection(i);
                                break;
                            }
                        }

                        break;
                    }
                }

            } catch (Exception e) {
//                Log.d(TAG, e.getMessage());
                e.printStackTrace();
            }

        } else if (val.startsWith(URI_EXTERNAL.toString())) {

            tabs.setCurrentTabByTag(TAB_EXTERNAL);

            try {

                String sound = Uri.parse(val).getLastPathSegment();

                long id = Long.valueOf(sound);

                externalCursor.moveToFirst();
                externalCursor.moveToPrevious();

                while (externalCursor.moveToNext()) {

                    if (id == externalCursor.getLong(externalCursor.getColumnIndex(MediaStore.MediaColumns._ID))) {

                        selectedName = externalCursor.getString(externalCursor.getColumnIndex(MediaStore.MediaColumns.TITLE));

                        for (int i = 0; i < externalListView.getCount(); i++) {

                            if (id == externalListView.getItemIdAtPosition(i)) {
                                externalListView.setItemChecked(i, true);
                                externalListView.setSelection(i);
                                break;
                            }
                        }
                    }

                    break;
                }

            } catch (Exception e) {
//                Log.d(TAG, e.getMessage());
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        Log.d(TAG, "onBindDialogView()");

        selectedTitle = (TextView) view.findViewById(R.id.text_view_selected_sound_title);

        suppliedListView = (ListView) view.findViewById(R.id.list_view_sound_supplied);
        internalListView = (ListView) view.findViewById(R.id.list_view_sound_internal);
        externalListView = (ListView) view.findViewById(R.id.list_view_sound_external);

        tabs = (TabHost) view.findViewById(R.id.tab_host_sound_picker);
        tabs.setup();

        TabHost.TabSpec tabSpec;

        tabSpec = tabs.newTabSpec(TAB_SUPPLIED);
        tabSpec.setContent(R.id.tab_sound_supplied);
        setTabSpecIndicator(tabs, tabSpec, R.string.tab_sound_supplied);
        tabs.addTab(tabSpec);

        tabSpec = tabs.newTabSpec(TAB_INTERNAL);
        tabSpec.setContent(R.id.tab_sound_internal);
        setTabSpecIndicator(tabs, tabSpec, R.string.tab_sound_internal);
        tabs.addTab(tabSpec);

        tabSpec = tabs.newTabSpec(TAB_EXTERNAL);
        tabSpec.setContent(R.id.tab_sound_external);
        setTabSpecIndicator(tabs, tabSpec, R.string.tab_sound_external);
        tabs.addTab(tabSpec);

        String projection[] = {
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.TITLE,
        };


        internalCursor = getContext().getContentResolver().query(
                URI_INTERNAL, projection, null, null, MediaStore.MediaColumns.TITLE);

        externalCursor = getContext().getContentResolver().query(
                URI_EXTERNAL, projection, null, null, MediaStore.MediaColumns.TITLE);

        int listItemResId = (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) ?
                android.R.layout.simple_list_item_1 : android.R.layout.simple_list_item_activated_1;

        suppliedListAdapter = new ArrayAdapter<String>(
                getContext(),
                //android.R.layout.simple_list_item_activated_1,
                //R.layout.sound_list_item,
                listItemResId,
                android.R.id.text1, //R.id.text_view_sound_item_title,
                suppliedSoundsTitles);

        internalListAdapter = new SimpleCursorAdapter(
                getContext(),
                //android.R.layout.simple_list_item_activated_1,
                //R.layout.sound_list_item,
                listItemResId,
                internalCursor,
                mediaStoreFrom,
                mediaStoreTo,
                0);

        externalListAdapter = new SimpleCursorAdapter(
                getContext(),
                //android.R.layout.simple_list_item_activated_1,
                //R.layout.sound_list_item,
                listItemResId,
                externalCursor,
                mediaStoreFrom,
                mediaStoreTo,
                0);

        suppliedListView.setAdapter(suppliedListAdapter);
        suppliedListView.setEmptyView(view.findViewById(R.id.supplied_sound_list_empty));
        suppliedListView.setOnItemClickListener(this);

        internalListView.setAdapter(internalListAdapter);
        internalListView.setEmptyView(view.findViewById(R.id.internal_sound_list_empty));
        internalListView.setOnItemClickListener(this);

        externalListView.setAdapter(externalListAdapter);
        externalListView.setEmptyView(view.findViewById(R.id.external_sound_list_empty));
        externalListView.setOnItemClickListener(this);

        mediaPlayer = new MediaPlayer();

        selectedName = "";
        fillFromPersistentValues();
        selectedTitle.setText(selectedName);
    }


    private void setTabSpecIndicator(TabHost tabHost, TabHost.TabSpec tabSpec, int resId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            View tabIndicator = LayoutInflater.from(getContext()).inflate(R.layout.apptheme_tab_indicator_holo,
                    tabHost.getTabWidget(), false);
            TextView title = (TextView) tabIndicator.findViewById(android.R.id.title);
            title.setText(getContext().getString(resId));
            tabSpec.setIndicator(tabIndicator);
        } else {
            tabSpec.setIndicator(getContext().getString(resId));
        }
    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {

            if (!hasKey() || null == selectedUri) {
                return;
            }

            persistString(selectedUri.toString());
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {

            case R.id.list_view_sound_supplied:

                if (position == 0) { // Silence
                    selectedUri = Uri.parse("");
                } else {
                    selectedUri = Uri.parse(URI_SUPPLIED + "/raw/" + suppliedSoundsValues[position]);
                }

                suppliedListView.setItemChecked(position, true);
                suppliedListView.setSelection(position);

                selectedName = suppliedSoundsTitles[position];
                selectedTitle.setText(selectedName);

                break;

            case R.id.list_view_sound_internal:

                selectedUri = Uri.withAppendedPath(URI_INTERNAL, "" + id);

                internalListView.setItemChecked(position, true);
                internalListView.setSelection(position);
                internalCursor.moveToPosition(position);

                selectedName = internalCursor.getString(internalCursor.getColumnIndex(MediaStore.MediaColumns.TITLE));
                selectedTitle.setText(selectedName);

                break;

            case R.id.list_view_sound_external:

                selectedUri = Uri.withAppendedPath(URI_EXTERNAL, "" + id);

                externalListView.setItemChecked(position, true);
                externalListView.setSelection(position);
                internalCursor.moveToPosition(position);

                selectedName = externalCursor.getString(externalCursor.getColumnIndex(MediaStore.MediaColumns.TITLE));
                selectedTitle.setText(selectedName);

                break;

            default:
                return;
        }

        if (TextUtils.isEmpty(selectedUri.toString())) {
            return;
        }

        if (null == mediaPlayer) {
            return;
        }

        mediaPlayer.reset();

        try {

            mediaPlayer.setDataSource(getContext(), selectedUri);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (Exception e) {
//            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
    }
}
