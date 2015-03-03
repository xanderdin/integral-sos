package ua.integral.sos.app;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.Toast;

/**
 * Created by aledin on 26.01.15.
 */
public class AbstractAppActivity extends ActionBarActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = "AbstractServiceActivity";

    private String name;
    private String tel;

    protected final static String VAR_NAME = "name";
    protected final static String VAR_TEL = "tel";

    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        if (null == savedInstanceState) {
            name = "";
            tel = "";
        } else {
            name = savedInstanceState.getString(VAR_NAME);
            tel = savedInstanceState.getString(VAR_TEL);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(VAR_NAME, getName());
        outState.putString(VAR_TEL, getTel());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        dismissDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_device:
                startNewDeviceActivity();
                return true;
            case R.id.action_settings:
                startSettingsActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        MenuInflater inflater = getMenuInflater();

        switch (v.getId()) {
            case R.id.device_list:
                AlarmDevice alarmDevice = AlarmDeviceList.getAlarmDeviceByRowId(info.id);
                if (null == alarmDevice) {
                    return;
                }
                inflater.inflate(R.menu.context_device, menu);
                break;
            case R.id.layout_device_header:
                inflater.inflate(R.menu.context_device_detail, menu);
                break;
            case R.id.zone_list:
                inflater.inflate(R.menu.context_zone, menu);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (null == info) {
            // not from a list
        } else {
            switch (item.getItemId()) {
                case R.id.action_call:
                    callDevice(info.id);
                    return true;
                case R.id.action_edit:
                    editDevice(info.id);
                    return true;
                case R.id.action_remove:
                    removeDeviceListItem(info.id);
                    return true;
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    protected void callDevice(final long rowId) {
        final AlarmDevice alarmDevice = AlarmDeviceList.getAlarmDeviceByRowId(rowId);
        if (null == alarmDevice) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + alarmDevice.getFirstDevTel()));
        startActivity(intent);
    }

    protected void editDevice(final long rowId) {

        final AlarmDevice alarmDevice = AlarmDeviceList.getAlarmDeviceByRowId(rowId);

        if (null == alarmDevice) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
        intent.setData(Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI,
                alarmDevice.getContactLookupKey()));
        intent.putExtra("finishActivityOnSaveCompleted", true);
        startActivity(intent);
    }

    protected void showToast(final String msg) {
        Log.d(TAG, "showToast(" + msg + ")");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AbstractAppActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected void showToast(int id) {
        showToast(getString(id));
    }

    @TargetApi(11)
    private void myInvalidateOptionsMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    invalidateOptionsMenu();
                }
            });
        }
    }

    protected void startSettingsActivity() {
        Log.d(TAG, "startSettingsActivity()");
        startActivity(new Intent(this, SettingsActivity.class));
    }

    protected void startMainActivity() {
        Log.d(TAG, "startMainActivity()");

        if (this instanceof MainActivity) {
            return;
        }

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    protected void startNewDeviceActivity() {
        Intent intent = new Intent();
        intent.setClass(this, NewDeviceActivity.class);
        startActivity(intent);
    }

    protected void showInfoDialog(final String title, final String msg) {
        Log.d(TAG, "showInfoDialog()");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title)
                .setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_alert);

        builder.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        setDialog(builder.create());
        getDialog().show();
    }

    protected void showProgressDialog(String text) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(TextUtils.isEmpty(text) ? getString(R.string.processing) : text);
        progressDialog.setCancelable(false);
        setDialog(progressDialog);
        getDialog().show();
    }

    private void removeDeviceListItem(final long rowId) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.dialog_remove_title)
                .setMessage(R.string.dialog_remove_message)
                .setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlarmDevice alarmDevice = AlarmDeviceList.getAlarmDeviceByRowId(rowId);
                if (null == alarmDevice) {
                    return;
                }
                AlarmDeviceList.remove(alarmDevice);
            }
        });

        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        setDialog(builder.create());
        getDialog().show();
    }

    protected void showInfoDialog(int titleId, int msgId) {
        showInfoDialog(getString(titleId), getString(msgId));
    }

    protected String getTel() {
        return tel;
    }

    protected void setTel(String tel) {
        this.tel = tel;
    }

    protected String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected synchronized Dialog getDialog() {
        return dialog;
    }

    protected synchronized void setDialog(Dialog dialog) {
        dismissDialog();
        this.dialog = dialog;
    }

    protected synchronized void dismissDialog() {
        if (null != getDialog()) {
            getDialog().dismiss();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }
}
