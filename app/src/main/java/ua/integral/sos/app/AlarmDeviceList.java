package ua.integral.sos.app;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aledin on 26.01.15.
 */
public class AlarmDeviceList {

    private static final AlarmDeviceList ourInstance = new AlarmDeviceList();

    private static final Map<Long,AlarmDevice> list = Collections.synchronizedMap(new HashMap<Long, AlarmDevice>());

    public static AlarmDeviceList getInstance() {
        return ourInstance;
    }


    private AlarmDeviceList() {
    }


    public static synchronized Map<Long,AlarmDevice> getList() {
        return list;
    }


    public static synchronized void clear() {

        for (AlarmDevice alarmDevice : getList().values()) {
            alarmDevice.clear();
        }

        getList().clear();
    }


    public static synchronized void put(AlarmDevice alarmDevice) {
        getList().put(alarmDevice.getRowId(), alarmDevice);
    }


//    public static synchronized AlarmDevice get(String gid) {
//        return getList().get(gid);
//    }


//    public static synchronized AlarmDevice getOrAdd(Context context, RosterEntry entry) {
//
//        String tel = AlarmDevice.getIdFromTel(entry.getUser());
//
//        AlarmDevice alarmDevice = getList().get(gid);
//
//        if (null == alarmDevice) {
//            alarmDevice = new AlarmDevice(service, conn, entry);
//            getList().put(gid, alarmDevice);
//        }
//
//        return alarmDevice;
//    }


    public static synchronized int size() {
        return getList().size();
    }


    public static synchronized void remove(AlarmDevice alarmDevice) {
        getList().remove(alarmDevice.getRowId());
        alarmDevice.remove();
    }


//    public static synchronized void update(MainService service, XMPPConnection conn, RosterEntry entry) {
//        AlarmDevice alarmDevice = getOrAdd(service, conn, entry);
//        alarmDevice.update(entry);
//    }


//    public static synchronized int getActivatedAlarmDeviceCount() {
//
//        int res = 0;
//
//        for (AlarmDevice alarmDevice : getList().values()) {
//            if (alarmDevice.getIsSubscribed()) {
//                res++;
//            }
//        }
//
//        return res;
//    }


//    public static synchronized AlarmDevice getAlarmDeviceByGid(String gid) {
//        return getList().get(gid);
//    }
//
//
//    public static synchronized AlarmDevice getAlarmDeviceByJid(String jid) {
//        for (AlarmDevice alarmDevice : getList().values()) {
//            if (jid.equals(alarmDevice.getJid())) {
//                return alarmDevice;
//            }
//        }
//        return null;
//    }


    public static synchronized AlarmDevice getAlarmDeviceByRowId(long rowId) {
//        for (AlarmDevice alarmDevice : getList().values()) {
//            if (alarmDevice.getRowId() == rowId) {
//                return alarmDevice;
//            }
//        }
//        return null;
        return getList().get(rowId);
    }

//    public static synchronized AlarmDevice getAlarmDeviceByTel(String tel) {
//        for (AlarmDevice alarmDevice : getList().values()) {
//            if (PhoneNumberUtils.compare(alarmDevice.getTel(), tel)) {
//                return alarmDevice;
//            }
//        }
//        return null;
//    }
}
