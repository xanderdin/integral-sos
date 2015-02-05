package ua.integral.sos.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class NewDeviceActivity extends ActionBarActivity {

    private final static int GET_CONTACT = 1;
    private final static int ADD_CONTACT = 2;

    private String contactLookupKey;

    private String devName;
    private String devTel;

    private TextView textViewDevName;
    private TextView textViewDevTel;
    private Button buttonAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_device);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textViewDevName = (TextView) findViewById(R.id.text_view_device_name);
        textViewDevTel = (TextView) findViewById(R.id.text_view_device_tel);
        buttonAdd = (Button) findViewById(R.id.button_add);

        buttonAdd.setEnabled(false);

        if (null != savedInstanceState) {
            setContactLookupKey(savedInstanceState.getString(CommonDef.EXTRA_CONTACT_LOOKUP_KEY));
            setDevName(savedInstanceState.getString(CommonDef.EXTRA_NAME));
            setDevTel(savedInstanceState.getString(CommonDef.EXTRA_TEL));
        }

        textViewDevName.setText(getDevName());
        textViewDevTel.setText(getDevTel());

        if (TextUtils.isEmpty(getDevTel())) {
            buttonAdd.setEnabled(false);
        } else {
            buttonAdd.setEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CommonDef.EXTRA_CONTACT_LOOKUP_KEY, getContactLookupKey());
        outState.putString(CommonDef.EXTRA_NAME, getDevName());
        outState.putString(CommonDef.EXTRA_TEL, getDevTel());
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add:
                AlarmDeviceList.put(new AlarmDevice(this, getContactLookupKey()));
                finish();
                break;
            case R.id.button_get_contact:
                getContact();
                break;
            case R.id.button_add_contact:
                addContact();
                break;
        }
    }

    private void getContact() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, GET_CONTACT);
    }

    private void addContact() {
        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
        intent.putExtra("finishActivityOnSaveCompleted", true);
        startActivityForResult(intent, ADD_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        if (null == data) {
            return;
        }

        Uri uri = data.getData();

        if (null == uri) {
            return;
        }

        switch (requestCode) {
            case ADD_CONTACT:
            case GET_CONTACT:
                getContactData(uri);
                break;
        }
    }

    private void getContactData(Uri uri) {

        @SuppressLint("InlinedApi")
        String[] projection = {
                ContactsContract.Contacts.LOOKUP_KEY,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
                        ? ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
                        : ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts._ID,
        };

        String selection = ContactsContract.Data.HAS_PHONE_NUMBER + " = 1";

        Cursor cursor = getContentResolver().query(uri, projection, selection, null, null);

        if (cursor.moveToNext()) {
            setContactLookupKey(cursor.getString(0));
            setDevName(cursor.getString(1));
            ArrayList<String> tels = getContactTels(cursor.getLong(2));
            setDevTel(tels.isEmpty() ? "" : tels.get(0));
        } else {
            setDevName("");
            setDevTel("");
        }

        cursor.close();

        textViewDevName.setText(getDevName());
        textViewDevTel.setText(getDevTel());

        if (TextUtils.isEmpty(getDevTel())) {
            Toast.makeText(this, getString(R.string.msg_contact_without_tel), Toast.LENGTH_SHORT).show();
            buttonAdd.setEnabled(false);
        } else {
            buttonAdd.setEnabled(true);
        }
    }

    private ArrayList<String> getContactTels(long contactId) {

        ArrayList<String> res = new ArrayList<>();

        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER };

        String selection = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";

        String[] selectionArgs = {
                String.valueOf(contactId),
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
        };

        String orderBy = ContactsContract.Data._ID + " ASC";

        Cursor cursor = getContentResolver().query(
                ContactsContract.Data.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                orderBy);

        while (cursor.moveToNext()) {
            res.add(cursor.getString(0));
        }

        return res;
    }

    public String getDevTel() {
        return devTel;
    }

    public void setDevTel(String devTel) {
        this.devTel = devTel;
    }

    public String getContactLookupKey() {
        return contactLookupKey;
    }

    public void setContactLookupKey(String contactLookupKey) {
        this.contactLookupKey = contactLookupKey;
    }

    public String getDevName() {
        return TextUtils.isEmpty(devName) ? getDevTel() : devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }
}
