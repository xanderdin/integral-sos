package ua.integral.sos.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.*;

import java.text.DateFormat;
import java.util.Date;


public class DeviceDetailActivity extends AbstractAppActivity
        implements ListView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        AlarmDevice.AlarmDeviceListener {

    private final static int LOADER_ID_ZONES = 2;
    private final static int LOADER_ID_HISTORY = 3;

    private AlarmDevice alarmDevice;

    private TextView devName;
    private TextView devInfo;
    private ImageView devLockIcon;
    private ImageView imgAttentionIndicator;
    private ImageView imgMoneyIndicator;
    private ImageView imgBatteryIndicator;
    private ImageView imgPowerIndicator;
    private ImageView imgTamperIndicator;
    private ImageView imgFailureIndicator;

    private boolean isViewReady;

    private TabHost tabHost;

    private GridView gridZones;
    private SimpleCursorAdapter zoneListAdapter;

    private ListView listHistory;
    private SimpleCursorAdapter historyListAdapter;

    private long devId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_device_detail);

        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if (null == savedInstanceState) {
            setDevId(getIntent().getLongExtra(CommonDef.EXTRA_SELECTED_ID, 0));
        } else {
        }

        devName = (TextView) findViewById(R.id.text_device_name);
        devInfo = (TextView) findViewById(R.id.text_device_info);
        devLockIcon = (ImageView) findViewById(R.id.image_lock_icon);
        imgAttentionIndicator = (ImageView) findViewById(R.id.image_attention_icon);
        imgMoneyIndicator = (ImageView) findViewById(R.id.image_money_indicator);
        imgBatteryIndicator = (ImageView) findViewById(R.id.image_battery_indicator);
        imgPowerIndicator = (ImageView) findViewById(R.id.image_power_indicator);
        imgTamperIndicator = (ImageView) findViewById(R.id.image_tamper_indicator);
        imgFailureIndicator = (ImageView) findViewById(R.id.image_failure_indicator);

        String zonesFrom[] = {
                AppDb.AlarmDeviceZoneTable.COLUMN_ID,
                AppDb.AlarmDeviceZoneTable.COLUMN_ID,
                AppDb.AlarmDeviceZoneTable.COLUMN_ZONE_NUM,
                AppDb.AlarmDeviceZoneTable.COLUMN_ZONE_NAME,
        };

        int zonesTo[] = {
                R.id.zone_lock_icon,
                R.id.zone_attention_icon,
                R.id.zone_num,
                R.id.zone_name,
        };

        zoneListAdapter = new SimpleCursorAdapter(
                this,
                R.layout.zone_list_item,
                null,
                zonesFrom,
                zonesTo,
                0);

        zoneListAdapter.setViewBinder(zoneListViewBinder);

         String historyFrom[] = {
                AppDb.EventHistoryTable.COLUMN_DATE_TIME,
                AppDb.EventHistoryTable.COLUMN_DATE_TIME,
                AppDb.EventHistoryTable.COLUMN_EVENT_TEXT
        };

        int historyTo[] = {
                R.id.text_view_date,
                R.id.text_view_time,
                R.id.text_view_content
        };

        historyListAdapter = new SimpleCursorAdapter(
                this,
                R.layout.history_list_item,
                null,
                historyFrom,
                historyTo,
                0);

        historyListAdapter.setViewBinder(historyListViewBinder);

        // tabs
        tabHost = (TabHost) findViewById(R.id.tabHost);

        tabHost.setup();

        TabHost.TabSpec tabSpec;

        tabSpec = tabHost.newTabSpec("tab_zones");
        setTabSpecIndicator(tabHost, tabSpec, R.string.tab_device_zones);
        tabSpec.setContent(R.id.tab_device_zones);

        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tab_history");
        setTabSpecIndicator(tabHost, tabSpec, R.string.tab_device_history);
        tabSpec.setContent(R.id.tab_device_history);

        tabHost.addTab(tabSpec);

        tabHost.setCurrentTab(CommonVar.getDeviceDetailSelectedTabIdx());

        gridZones = (GridView) findViewById(R.id.zone_list);
        gridZones.setEmptyView(findViewById(R.id.zone_list_empty));
        gridZones.setOnItemClickListener(this);

        listHistory = (ListView) findViewById(R.id.history_list);
        listHistory.setEmptyView(findViewById(R.id.history_list_empty));

        alarmDevice = AlarmDeviceList.getAlarmDeviceByRowId(getDevId());

        if (null == alarmDevice) {
            finish();
            return;
        }

        getSupportActionBar().setTitle(alarmDevice.getDevName());
        gridZones.setAdapter(zoneListAdapter);

        listHistory.setAdapter(historyListAdapter);

        initLoaders();

        registerForContextMenu(findViewById(R.id.layout_device_header));
        registerForContextMenu(gridZones);

        setViewReady(true);
        refreshData();
    }


    public long getDevId() {
        return devId;
    }

    public void setDevId(long devId) {
        this.devId = devId;
    }

    private void setTabSpecIndicator(TabHost tabHost, TabHost.TabSpec tabSpec, int resId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            View tabIndicator = LayoutInflater.from(this).inflate(R.layout.apptheme_tab_indicator_holo,
                    tabHost.getTabWidget(), false);
            TextView title = (TextView) tabIndicator.findViewById(android.R.id.title);
            title.setText(getString(resId));
            tabSpec.setIndicator(tabIndicator);
        } else {
            tabSpec.setIndicator(getString(resId));
        }
    }

    private synchronized void setAlarmDevice(AlarmDevice alarmDevice) {
        this.alarmDevice = alarmDevice;
    }

    private synchronized AlarmDevice getAlarmDevice() {
        return alarmDevice;
    }

    private synchronized void setViewReady(boolean val) {
        this.isViewReady = val;
    }

    private synchronized boolean isViewReady() {
        return isViewReady;
    }

    private void initLoaders() {
        getSupportLoaderManager().initLoader(LOADER_ID_ZONES, null, this);
        getSupportLoaderManager().initLoader(LOADER_ID_HISTORY, null, this);
    }

    private void notifyDeviceChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initLoaders();
                refreshData();
            }
        });
    }

    @Override
    protected void onPause() {
        getAlarmDevice().cancelNotification();
        getAlarmDevice().removeListener(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyDeviceChanged();
        getAlarmDevice().addListener(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (null == intent) {
            return;
        }

        if (getDevId() != intent.getLongExtra(CommonDef.EXTRA_SELECTED_ID, 0)) {
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        CommonVar.setDeviceDetailSelectedTabIdx(tabHost.getCurrentTab());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonVar.setDeviceDetailSelectedTabIdx(tabHost.getCurrentTab());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.device_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_call:
                callDevice(getAlarmDevice().getRowId());
                return true;
            case R.id.action_edit:
                editDevice(getAlarmDevice().getRowId());
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (null == info) { // This is from layout_device_header
            switch (item.getItemId()) {
                case R.id.action_call:
                    callDevice(getAlarmDevice().getRowId());
                    return true;
                case R.id.action_edit:
                    editDevice(getAlarmDevice().getRowId());
                    return true;
            }
        } else { // This is from zone list or user list
            int num;
            AlarmDeviceZone alarmDeviceZone;
            switch (item.getItemId()) {
                case R.id.action_edit_zone:
                    alarmDeviceZone = getAlarmDevice().getZoneByRowId(info.id);
                    if (null == alarmDeviceZone) {
                        return true;
                    }
                    num = alarmDeviceZone.getZoneNum();
                    editZone(num);
                    return true;
                case R.id.action_remove_zone:
                    alarmDeviceZone = getAlarmDevice().getZoneByRowId(info.id);
                    if (null == alarmDeviceZone) {
                        return true;
                    }
                    removeZone(alarmDeviceZone);
                    return true;
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch(parent.getId()) {
            case R.id.zone_list:
                showZoneInfo(id);
                break;
            default:
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        CursorLoader cursorLoader = null;

        String projection[];
        String selection;
        String selectionArgs[];
        String sortOrder;

        switch (id) {

            case LOADER_ID_ZONES:

                projection = new String[] {
                        AppDb.AlarmDeviceZoneTable.COLUMN_ID,
                        AppDb.AlarmDeviceZoneTable.COLUMN_ZONE_NUM,
                        AppDb.AlarmDeviceZoneTable.COLUMN_ZONE_TYPE,
                        AppDb.AlarmDeviceZoneTable.COLUMN_ZONE_NAME,
                        AppDb.AlarmDeviceZoneTable.COLUMN_STATE,
                        AppDb.AlarmDeviceZoneTable.COLUMN_IS_ARMED,
                        AppDb.AlarmDeviceZoneTable.COLUMN_IS_FIRED,
                        AppDb.AlarmDeviceZoneTable.COLUMN_IS_BATTERY_LOW,
                        AppDb.AlarmDeviceZoneTable.COLUMN_IS_POWER_LOST,
                        AppDb.AlarmDeviceZoneTable.COLUMN_IS_TAMPER_OPENED,
                        AppDb.AlarmDeviceZoneTable.COLUMN_IS_LINK_LOST,
                        AppDb.AlarmDeviceZoneTable.COLUMN_IS_ZONE_FAILURE,
                };

                selection = AppDb.AlarmDeviceZoneTable.COLUMN_DEV_ID + " = ?";

                selectionArgs = new String[] { String.valueOf(getDevId()) };

                sortOrder = AppDb.AlarmDeviceZoneTable.COLUMN_SORT_ORDER + ", " +
                        AppDb.AlarmDeviceZoneTable.COLUMN_ZONE_NUM;

                cursorLoader = new CursorLoader(
                        this,
                        AlarmDeviceZoneProvider.CONTENT_URI,
                        projection, selection, selectionArgs, sortOrder);

                break;

            case LOADER_ID_HISTORY:

                projection = new String[] {
                        AppDb.EventHistoryTable.COLUMN_ID,
                        AppDb.EventHistoryTable.COLUMN_DATE_TIME,
                        AppDb.EventHistoryTable.COLUMN_EVENT_TEXT,
                        AppDb.EventHistoryTable.COLUMN_EVENT_TYPE,
                };

                selection = AppDb.EventHistoryTable.COLUMN_DEV_ID + " = ?";

                selectionArgs = new String[] { String.valueOf(getDevId()) };

                sortOrder = AppDb.EventHistoryTable.COLUMN_DATE_TIME + ", " +
                        AppDb.EventHistoryTable.COLUMN_ID + " ASC";

                cursorLoader = new CursorLoader(
                        this,
                        EventHistoryProvider.CONTENT_URI,
                        projection, selection, selectionArgs, sortOrder);

                break;
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
            case LOADER_ID_ZONES:
                zoneListAdapter.swapCursor(data);
                break;
            case LOADER_ID_HISTORY:
                historyListAdapter.swapCursor(data);
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        switch (loader.getId()) {
            case LOADER_ID_ZONES:
                zoneListAdapter.swapCursor(null);
                break;
            case LOADER_ID_HISTORY:
                historyListAdapter.swapCursor(null);
                break;
            default:
                break;
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_device_header:
                finish();
                break;
            default:
                break;
        }
    }

    private final SimpleCursorAdapter.ViewBinder zoneListViewBinder = new SimpleCursorAdapter.ViewBinder() {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

            int idx;

            int type;
            Boolean isArmed;
            Boolean isFired;
            Boolean isInAlarm;
            Boolean isTamperOpened;
            Boolean isLinkLost;
            Boolean isBatteryLow;
            Boolean isPowerLost;
            Boolean isZoneFailure;

            switch (view.getId()) {

                case R.id.zone_lock_icon:

                    idx = cursor.getColumnIndex(AppDb.AlarmDeviceZoneTable.COLUMN_ZONE_TYPE);

                    type = cursor.getInt(idx);

                    idx = cursor.getColumnIndex(AppDb.AlarmDeviceZoneTable.COLUMN_IS_ARMED);

                    isArmed = (cursor.isNull(idx)) ? null : (cursor.getInt(idx) != 0);

                    idx = cursor.getColumnIndex(AppDb.AlarmDeviceZoneTable.COLUMN_IS_FIRED);

                    isFired = (cursor.isNull(idx)) ? null : (cursor.getInt(idx) != 0);

                    idx = cursor.getColumnIndex(AppDb.AlarmDeviceZoneTable.COLUMN_IS_TAMPER_OPENED);

                    isTamperOpened = (cursor.isNull(idx)) ? null : (cursor.getInt(idx) != 0);

                    idx = cursor.getColumnIndex(AppDb.AlarmDeviceZoneTable.COLUMN_IS_LINK_LOST);

                    isLinkLost = (cursor.isNull(idx)) ? null : (cursor.getInt(idx) != 0);

                    isInAlarm = AlarmDeviceZone.isInAlarm(isArmed, isFired, isTamperOpened, isLinkLost);

                    ((ImageView) view).setImageResource(AlarmDeviceZone.getImgResourceId(type, isArmed, isInAlarm));

                    if (isInAlarm) {
                        view.setAnimation(AnimationUtils.loadAnimation(DeviceDetailActivity.this, R.anim.shake));
                    } else {
                        view.clearAnimation();
                    }

                    return true;

                case R.id.zone_attention_icon:

                    idx = cursor.getColumnIndex(AppDb.AlarmDeviceZoneTable.COLUMN_IS_TAMPER_OPENED);

                    isTamperOpened = (cursor.isNull(idx)) ? null : (cursor.getInt(idx) != 0);

                    idx = cursor.getColumnIndex(AppDb.AlarmDeviceZoneTable.COLUMN_IS_LINK_LOST);

                    isLinkLost = (cursor.isNull(idx)) ? null : (cursor.getInt(idx) != 0);

                    idx = cursor.getColumnIndex(AppDb.AlarmDeviceZoneTable.COLUMN_IS_BATTERY_LOW);

                    isBatteryLow = (cursor.isNull(idx)) ? null : (cursor.getInt(idx) != 0);

                    idx = cursor.getColumnIndex(AppDb.AlarmDeviceZoneTable.COLUMN_IS_POWER_LOST);

                    isPowerLost = (cursor.isNull(idx)) ? null : (cursor.getInt(idx) != 0);

                    idx = cursor.getColumnIndex(AppDb.AlarmDeviceZoneTable.COLUMN_IS_ZONE_FAILURE);

                    isZoneFailure = (cursor.isNull(idx)) ? null : (cursor.getInt(idx) != 0);

                    boolean hasAttentionInfo = AlarmDeviceZone.hasAttentionInfo(isTamperOpened, isLinkLost,
                            isBatteryLow, isPowerLost, isZoneFailure);

                    if (hasAttentionInfo) {
                        view.setVisibility(View.VISIBLE);
                    } else {
                        view.setVisibility(View.GONE);
                    }

                    return true;

                case R.id.zone_num:

                    String num = cursor.getString(columnIndex);

                    ((TextView) view).setText(num);

                    return true;

                case R.id.zone_name:

                    String name = cursor.getString(columnIndex);

                    ((TextView) view).setText(name);
                    view.setSelected(true);

                    return true;
            }

            return false;
        }
    };

    private final SimpleCursorAdapter.ViewBinder historyListViewBinder = new SimpleCursorAdapter.ViewBinder() {
        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

            long seconds;
            DateFormat dateFormat;

            switch (view.getId()) {

                case R.id.text_view_date:

                    seconds = cursor.getLong(columnIndex);

                    dateFormat = DateFormat.getDateInstance();

                    ((TextView) view).setText(dateFormat.format(new Date(seconds * 1000)));

                    return true;

                case R.id.text_view_time:

                    seconds = cursor.getLong(columnIndex);

                    dateFormat = DateFormat.getTimeInstance();

                    ((TextView) view).setText(dateFormat.format(new Date(seconds * 1000)));

                    return true;

                case R.id.text_view_content:

                    ((TextView) view).setText(cursor.getString(columnIndex));

                    int eventTypeColumn = cursor.getColumnIndex(AppDb.EventHistoryTable.COLUMN_EVENT_TYPE);

                    if (cursor.isNull(eventTypeColumn)) {
                        view.setBackgroundResource(R.color.white);
                        return true;
                    }

                    int type = cursor.getInt(eventTypeColumn);

                    switch (type) {
                        case AppDb.EventHistoryTable.EVENT_TYPE_ALARM:
                            view.setBackgroundResource(R.color.light_red);
                            break;
                        case AppDb.EventHistoryTable.EVENT_TYPE_WARNING:
                            view.setBackgroundResource(R.color.light_orange);
                            break;
                        default:
                            view.setBackgroundResource(R.color.white);
                            break;
                    }

                    return true;
            }

            return false;
        }
    };

    private void showZoneInfo(long rowId) {

        final AlarmDeviceZone alarmDeviceZone = getAlarmDevice().getZoneByRowId(rowId);

        if (null == alarmDeviceZone) {
            return;
        }

        final String customName = alarmDeviceZone.getZoneName();

        LayoutInflater factory = LayoutInflater.from(this);

        final View dialogView = factory.inflate(R.layout.dialog_zone_info, null);

        final ImageView linkIndicator = (ImageView) dialogView.findViewById(R.id.image_zone_link_indicator);
        final ImageView batteryIndicator = (ImageView) dialogView.findViewById(R.id.image_zone_battery_indicator);
        final ImageView powerIndicator = (ImageView) dialogView.findViewById(R.id.image_zone_power_indicator);
        final ImageView tamperIndicator = (ImageView) dialogView.findViewById(R.id.image_zone_tamper_indicator);
        final ImageView failureIndicator = (ImageView) dialogView.findViewById(R.id.image_zone_failure_indicator);

        final TextView linkTextView = (TextView) dialogView.findViewById(R.id.text_view_zone_link_status);
        final TextView batteryTextView = (TextView) dialogView.findViewById(R.id.text_view_zone_battery_status);
        final TextView powerTextView = (TextView) dialogView.findViewById(R.id.text_view_zone_power_status);
        final TextView tamperTextView = (TextView) dialogView.findViewById(R.id.text_view_zone_tamper_status);
        final TextView failureTextView = (TextView) dialogView.findViewById(R.id.text_view_zone_failure_status);
        final TextView noInfoTextView = (TextView) dialogView.findViewById(R.id.text_view_zone_no_info);

        if (alarmDeviceZone.hasAdditionalInfo()) {
            linkIndicator.setImageResource(alarmDeviceZone.getLinkImgResourceId());
            batteryIndicator.setImageResource(alarmDeviceZone.getBatteryImgResourceId());
            powerIndicator.setImageResource(alarmDeviceZone.getPowerImgResourceId());
            tamperIndicator.setImageResource(alarmDeviceZone.getTamperImgResourceId());
            failureIndicator.setImageResource(alarmDeviceZone.getZoneFailureImgResourceId());
        } else {
            linkIndicator.setVisibility(View.GONE);
            batteryIndicator.setVisibility(View.GONE);
            powerIndicator.setVisibility(View.GONE);
            tamperIndicator.setVisibility(View.GONE);
            failureIndicator.setVisibility(View.GONE);
            linkTextView.setVisibility(View.GONE);
            batteryTextView.setVisibility(View.GONE);
            powerTextView.setVisibility(View.GONE);
            tamperTextView.setVisibility(View.GONE);
            failureTextView.setVisibility(View.GONE);
            noInfoTextView.setVisibility(View.VISIBLE);
        }

        if (null != alarmDeviceZone.getIsZoneFailure() && alarmDeviceZone.getIsZoneFailure() == true) {
            failureIndicator.setAnimation(AnimationUtils.loadAnimation(DeviceDetailActivity.this, R.anim.blink));
        } else {
            failureIndicator.clearAnimation();
        }

        AlertDialog d = new AlertDialog.Builder(this)
                /* .setIcon(R.drawable.ic_) */
                .setTitle(customName)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();

        setDialog(d);
        getDialog().show();
    }


    private void refreshData() {

        if (!isViewReady()) {
            return;
        }

        getSupportActionBar().setTitle(getAlarmDevice().getDevName());
        devName.setText(getAlarmDevice().getDevName());
        devName.setSelected(true);

        if (getAlarmDevice().isLastEventTextChanged()) {
            devInfo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_twice));
        }
        devInfo.setText(TextUtils.isEmpty(getAlarmDevice().getLastEventText())
                ? getAlarmDevice().getFirstDevTel() : getAlarmDevice().getLastEventText());
        devInfo.setSelected(true);

        devLockIcon.setImageResource(getAlarmDevice().getLockImgResourceId());

        if (!getAlarmDevice().isDeviceOff() && getAlarmDevice().isInAlarm()) {
            devLockIcon.setAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
        } else {
            devLockIcon.clearAnimation();
        }

        if (getAlarmDevice().hasZonesAttentionInfo()) {
            imgAttentionIndicator.setVisibility(View.VISIBLE);
        } else {
            imgAttentionIndicator.setVisibility(View.GONE);
        }

        imgMoneyIndicator.setImageResource(getAlarmDevice().getMoneyImgResourceId());
        imgBatteryIndicator.setImageResource(getAlarmDevice().getBatteryImgResourceId());
        imgPowerIndicator.setImageResource(getAlarmDevice().getPowerImgResourceId());
        imgTamperIndicator.setImageResource(getAlarmDevice().getTamperImgResourceId());

        boolean val = getAlarmDevice().getIsDevFailure() == null ? false : getAlarmDevice().getIsDevFailure();

        if (val) {
            imgFailureIndicator.setVisibility(View.VISIBLE);
            imgFailureIndicator.setAnimation(AnimationUtils.loadAnimation(this, R.anim.blink));
        } else {
            imgFailureIndicator.clearAnimation();
            imgFailureIndicator.setVisibility(View.GONE);
        }

        zoneListAdapter.notifyDataSetChanged();
        historyListAdapter.notifyDataSetChanged();
    }

    private void editZone(final int num) {

        final String zoneName = getAlarmDevice().getZoneByNum(num).getZoneName();

        LayoutInflater factory = LayoutInflater.from(this);

        final View dialogView = factory.inflate(R.layout.dialog_zone_edit, null);

        final EditText editCustomName = (EditText) dialogView.findViewById(R.id.edit_zone_custom_name);

        editCustomName.setText(zoneName);

        AlertDialog d = new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_action_edit)
                .setTitle(R.string.dialog_zone_edit_title)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String newCustomName = editCustomName.getText().toString();

                        if (TextUtils.equals(zoneName, newCustomName)) {
                            return;
                        }

                        getAlarmDevice().getZoneByNum(num).setZoneName(newCustomName);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();

        setDialog(d);
        getDialog().show();
    }

    private void removeZone(final AlarmDeviceZone alarmDeviceZone) {

        if (null == alarmDeviceZone.getIsArmed() || false == alarmDeviceZone.getIsArmed()) {
            AlertDialog d = new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(alarmDeviceZone.getZoneName())
                    .setMessage(R.string.dialog_remove_zone_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getAlarmDevice().removeZone(alarmDeviceZone.getZoneNum());
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .create();
            setDialog(d);
            getDialog().show();
        } else {
            showToast(R.string.msg_device_must_be_disarmed);
        }
    }

    @Override
    public void onDataChanged() {
        notifyDeviceChanged();
    }
}
