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


    public static synchronized int size() {
        return getList().size();
    }


    public static synchronized void remove(AlarmDevice alarmDevice) {
        getList().remove(alarmDevice.getRowId());
        alarmDevice.remove();
    }


    public static synchronized AlarmDevice getAlarmDeviceByRowId(long rowId) {
        return getList().get(rowId);
    }
}
