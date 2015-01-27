package ua.integral.sos.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * Created by aledin on 24.12.14.
 */
public class AboutDialog extends DialogPreference {

    public AboutDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPersistent(false);
        setDialogLayoutResource(R.layout.dialog_about);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        TextView appVer = (TextView) view.findViewById(R.id.text_view_app_version);
        try {
            PackageInfo packageInfo = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
            appVer.setText(packageInfo.versionName);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setNegativeButton(null, null);
    }
}
