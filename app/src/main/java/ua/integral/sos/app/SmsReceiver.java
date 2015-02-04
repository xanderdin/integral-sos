package ua.integral.sos.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {

    private final SmsManager smsManager = SmsManager.getDefault();

    public SmsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");

        final Bundle bundle = intent.getExtras();

        if (null == bundle) {
            return;
        }

        try {
            final Object[] pdusObj = (Object[]) bundle.get("pdus");
            SmsMessage[] smsMessages = new SmsMessage[pdusObj.length];
            for (int i = 0; i < smsMessages.length; i++) {
                smsMessages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
            }
            String tel = smsMessages[0].getDisplayOriginatingAddress();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < smsMessages.length; i++) {
                stringBuilder.append(smsMessages[i].getMessageBody());
            }
            String body = stringBuilder.toString();
            for (AlarmDevice alarmDevice : AlarmDeviceList.getList().values()) {
                if (PhoneNumberUtils.compare(context, tel, alarmDevice.getDevTel())) {
                    alarmDevice.parseMessage(body);
                    abortBroadcast();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
