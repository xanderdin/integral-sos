package ua.integral.sos.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.*;

public class MainActivity extends AbstractAppActivity
        implements ListView.OnItemClickListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        AlarmDevice.AlarmDeviceListener {

    private final static int LOADER_ID_DEVICES = 1;

    private ListView deviceListView;
    private SimpleCursorAdapter deviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle(getString(R.string.title_activity_main));
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        deviceListView = (ListView) findViewById(R.id.device_list);
        deviceListView.setEmptyView(findViewById(android.R.id.empty));

        String from[] = {
                AppDb.AlarmDeviceTable.COLUMN_ID, // for text_device_info
                AppDb.AlarmDeviceTable.COLUMN_ID, // for text_device_name
                AppDb.AlarmDeviceTable.COLUMN_ID, // for image_lock_icon
                AppDb.AlarmDeviceTable.COLUMN_IS_BATTERY_LOW,
                AppDb.AlarmDeviceTable.COLUMN_IS_POWER_LOST,
                AppDb.AlarmDeviceTable.COLUMN_IS_TAMPER_OPENED,
                AppDb.AlarmDeviceTable.COLUMN_IS_DEV_FAILURE,
                AppDb.AlarmDeviceTable.COLUMN_MONEY_LEFT,
        };

        int to[] = {
                R.id.text_device_info,
                R.id.text_device_name,
                R.id.image_lock_icon,
                R.id.image_battery_indicator,
                R.id.image_power_indicator,
                R.id.image_tamper_indicator,
                R.id.image_failure_indicator,
                R.id.image_money_indicator,
        };

        deviceListAdapter = new SimpleCursorAdapter(
                this,
                R.layout.device_list_item,
                null,
                from,
                to,
                0);

        deviceListAdapter.setViewBinder(deviceListViewBinder);

        ((AdapterView<ListAdapter>) deviceListView).setAdapter(deviceListAdapter);

        initLoaders();

        // Set OnItemClickListener so we can be notified on item clicks
        deviceListView.setOnItemClickListener(this);

        registerForContextMenu(deviceListView);
    }

    private void initLoaders() {
        getSupportLoaderManager().initLoader(LOADER_ID_DEVICES, null, this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.device_list:
                displayDeviceDetail(id);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void refreshData() {
        deviceListAdapter.notifyDataSetChanged();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        CursorLoader cursorLoader = null;

        String projection[];
        String selection;
        String selectionArgs[];
        String sortOrder;

        switch (id) {
            case LOADER_ID_DEVICES:

                projection = new String[] {
                        AppDb.AlarmDeviceTable.COLUMN_ID,
                        AppDb.AlarmDeviceTable.COLUMN_IS_BATTERY_LOW,
                        AppDb.AlarmDeviceTable.COLUMN_IS_POWER_LOST,
                        AppDb.AlarmDeviceTable.COLUMN_IS_TAMPER_OPENED,
                        AppDb.AlarmDeviceTable.COLUMN_IS_DEV_FAILURE,
                        AppDb.AlarmDeviceTable.COLUMN_MONEY_LEFT,
                };

                sortOrder = AppDb.AlarmDeviceTable.COLUMN_SORT_ORDER;

                cursorLoader = new CursorLoader(this, AlarmDeviceProvider.CONTENT_URI,
                        projection, null, null, sortOrder);

                break;
        }

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_ID_DEVICES:
                deviceListAdapter.swapCursor(data);
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_ID_DEVICES:
                deviceListAdapter.swapCursor(null);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        for (AlarmDevice alarmDevice: AlarmDeviceList.getList().values()) {
            alarmDevice.removeListener(this);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        notifyDeviceListAdapterChanged();
        for (AlarmDevice alarmDevice: AlarmDeviceList.getList().values()) {
            alarmDevice.addListener(this);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add_device:
                startNewDeviceActivity();
                break;
            default:
                break;
        }
    }

    private void notifyDeviceListAdapterChanged() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initLoaders();
                refreshData();
            }
        });
    }

    private void displayDeviceDetail(long rowId) {

        AlarmDevice alarmDevice = AlarmDeviceList.getAlarmDeviceByRowId(rowId);

        if (null == alarmDevice) {
            return;
        }

        alarmDevice.cancelNotification();

        Intent intent = new Intent(this, DeviceDetailActivity.class);
        intent.putExtra(CommonDef.EXTRA_SELECTED_ID, alarmDevice.getRowId());

        startActivity(intent);
    }

    private final SimpleCursorAdapter.ViewBinder deviceListViewBinder = new SimpleCursorAdapter.ViewBinder() {

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

            Boolean val;
            AlarmDevice alarmDevice;
            int idx;
            long rowId;

            switch (view.getId()) {

                case R.id.text_device_name:

                    alarmDevice = AlarmDeviceList.getAlarmDeviceByRowId(cursor.getLong(columnIndex));

                    if (null == alarmDevice) {
                        break;
                    }

                    ((TextView) view).setText(alarmDevice.getDevName());
                    view.setSelected(true);

                    return true;

                case R.id.text_device_info:

                    alarmDevice = AlarmDeviceList.getAlarmDeviceByRowId(cursor.getLong(columnIndex));

                    if (null == alarmDevice) {
                        break;
                    }

                    if (alarmDevice.isLastEventTextChanged()) {
                        view.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_twice));
                    }

                    String text = TextUtils.isEmpty(alarmDevice.getLastEventText())
                            ? alarmDevice.getFirstDevTel() : alarmDevice.getLastEventText();

                    ((TextView) view).setText(text);
                    view.setSelected(true);

                    return true;

                case R.id.image_lock_icon:

                    alarmDevice = AlarmDeviceList.getAlarmDeviceByRowId(cursor.getLong(columnIndex));

                    if (null == alarmDevice) {
                        return true;
                    }

                    ((ImageView) view).setImageResource(alarmDevice.getLockImgResourceId());

                    if (alarmDevice.isInAlarm()) {
                        view.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake));
                    } else {
                        view.clearAnimation();
                    }

                    return true;

                case R.id.image_money_indicator:

                    Double money = (cursor.isNull(columnIndex)) ? null : cursor.getDouble(columnIndex);

                    ((ImageView) view).setImageResource(AlarmDevice.getMoneyImgResourceId(money,
                            CommonVar.getLowMoneyThreshold()));

                    return true;

                case R.id.image_battery_indicator:

                    val = (cursor.isNull(columnIndex)) ? null : (cursor.getInt(columnIndex) != 0);

                    ((ImageView) view).setImageResource(AlarmDevice.getBatteryImgResourceId(val));

                    return true;

                case R.id.image_power_indicator:

                    val = (cursor.isNull(columnIndex)) ? null : (cursor.getInt(columnIndex) != 0);

                    ((ImageView) view).setImageResource(AlarmDevice.getPowerImgResourceId(val));

                    return true;

                case R.id.image_tamper_indicator:

                    val = (cursor.isNull(columnIndex)) ? null : (cursor.getInt(columnIndex) != 0);

                    ((ImageView) view).setImageResource(AlarmDevice.getTamperImgResourceId(val));

                    return true;

                case R.id.image_failure_indicator:

                    val = (cursor.isNull(columnIndex)) ? false : (cursor.getInt(columnIndex) != 0);

                    if (val) {
                        view.setVisibility(View.VISIBLE);
                        view.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.blink));
                    } else {
                        view.clearAnimation();
                        view.setVisibility(View.GONE);
                    }

                    return true;
            }

            return false;
        }
    };

    @Override
    public void onDataChanged() {
        refreshData();
    }
}
