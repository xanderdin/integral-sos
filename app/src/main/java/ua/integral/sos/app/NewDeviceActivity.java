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


public class NewDeviceActivity extends ActionBarActivity {

    private final static int GET_CONTACT = 1;
    private final static int ADD_CONTACT = 2;

    private String tel;
    private String name;
    private String contactLookupKey;
    private Uri contactLookupUri;

    private TextView textViewDeviceName;
    private TextView textViewDeviceTel;
    private Button buttonAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_device);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textViewDeviceName = (TextView) findViewById(R.id.text_view_device_name);
        textViewDeviceTel = (TextView) findViewById(R.id.text_view_device_tel);
        buttonAdd = (Button) findViewById(R.id.button_add);

        buttonAdd.setEnabled(false);

        if (null != savedInstanceState) {
            setContactLookupKey(savedInstanceState.getString(CommonDef.EXTRA_CONTACT_LOOKUP_KEY));
            setContactLookupUri(savedInstanceState.getString(CommonDef.EXTRA_CONTACT_LOOKUP_URI));
            setName(savedInstanceState.getString(CommonDef.EXTRA_NAME));
            setTel(savedInstanceState.getString(CommonDef.EXTRA_TEL));
        }

        textViewDeviceName.setText(getName());
        textViewDeviceTel.setText(getTel());

        if (TextUtils.isEmpty(getTel())) {
            buttonAdd.setEnabled(false);
        } else {
            buttonAdd.setEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CommonDef.EXTRA_CONTACT_LOOKUP_KEY, getContactLookupKey());
        if (null != getContactLookupUri()) {
            outState.putString(CommonDef.EXTRA_CONTACT_LOOKUP_URI, getContactLookupUri().toString());
        }
        outState.putString(CommonDef.EXTRA_NAME, getName());
        outState.putString(CommonDef.EXTRA_TEL, getTel());
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
                setContactLookupUri(uri);
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
        };

        String selection = ContactsContract.Data.HAS_PHONE_NUMBER + " = 1";

        Cursor cursor = getContentResolver().query(uri, projection, selection, null, null);

        if (cursor.moveToNext()) {
            setContactLookupKey(cursor.getString(0));
            setName(cursor.getString(1));
            textViewDeviceName.setText(getName());
        } else {
            setName("");
            textViewDeviceName.setText("");
            setTel("");
            textViewDeviceTel.setText("");
            buttonAdd.setEnabled(false);
            Toast.makeText(this, getString(R.string.msg_contact_without_tel), Toast.LENGTH_SHORT).show();
            cursor.close();
            return;
        }

        cursor.close();

        String[] proj = {
                ContactsContract.CommonDataKinds.Phone.NUMBER,
        };

        String sel = ContactsContract.Data.LOOKUP_KEY + " = ? AND " +
                ContactsContract.Data.HAS_PHONE_NUMBER + " = 1";

        String[] selArgs = {
                getContactLookupKey(),
        };

        cursor = getContentResolver().query(ContactsContract.Data.CONTENT_URI, proj, sel, selArgs, null);

        if (cursor.moveToNext()){
            setTel(cursor.getString(0));
            textViewDeviceTel.setText(getTel());
        } else {
            setTel(null);
            textViewDeviceTel.setText("");
        }

        cursor.close();

        if (TextUtils.isEmpty(getTel())) {
            buttonAdd.setEnabled(false);
        } else {
            buttonAdd.setEnabled(true);
        }
    }

    public Uri getContactLookupUri() {
        return contactLookupUri;
    }

    public void setContactLookupUri(Uri contactLookupUri) {
        this.contactLookupUri = contactLookupUri;
    }

    public void setContactLookupUri(String uri) {
        setContactLookupUri(Uri.parse(uri));
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getContactLookupKey() {
        return contactLookupKey;
    }

    public void setContactLookupKey(String contactLookupKey) {
        this.contactLookupKey = contactLookupKey;
    }

    public String getName() {
        return TextUtils.isEmpty(name) ? getTel() : name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
