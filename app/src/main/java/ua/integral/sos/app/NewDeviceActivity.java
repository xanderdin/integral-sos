package ua.integral.sos.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class NewDeviceActivity extends ActionBarActivity {

    private String tel;
    private String name;

    private EditText editTel;
    private EditText editName;
    private Button buttonAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_device);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editTel = (EditText) findViewById(R.id.edit_tel);
        editName = (EditText) findViewById(R.id.edit_name);
        buttonAdd = (Button) findViewById(R.id.button_add);

        buttonAdd.setEnabled(false);

        if (null == savedInstanceState) {
            setTel(getIntent().getStringExtra(CommonDef.EXTRA_TEL));
            setName(getIntent().getStringExtra(CommonDef.EXTRA_NAME));
        }

        editTel.setText(getTel());
        editName.setText(getName());

        if (TextUtils.isEmpty(getTel())) {
            buttonAdd.setEnabled(false);
        } else {
            buttonAdd.setEnabled(true);
        }

        InputWatcher watcher = new InputWatcher();

        editTel.addTextChangedListener(watcher);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CommonDef.EXTRA_TEL, getTel());
        outState.putString(CommonDef.EXTRA_NAME, getName());
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_add:
                setTel(editTel.getText().toString().trim());
                setName(editName.getText().toString().trim());
                AlarmDeviceList.put(new AlarmDevice(this, getTel(), getName()));
                finish();
                break;
        }
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private class InputWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            buttonAdd.setEnabled(false);

            String gid = editTel.getText().toString().trim();

            if (gid.length() < CommonDef.TEL_MIN_LENGTH)
                return;

            if (!MiscFunc.isTelOk(gid)) {
                editTel.setError(getString(R.string.err_bad_value));
                return;
            } else {
                editTel.setError(null);
            }

            buttonAdd.setEnabled(true);
        }
    }
}
