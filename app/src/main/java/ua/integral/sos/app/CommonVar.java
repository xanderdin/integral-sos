package ua.integral.sos.app;

/**
 * Created by aledin on 21.12.14.
 */
public class CommonVar {

    private static final CommonVar ourInstance = new CommonVar();

    public static CommonVar getInstance() {
        return ourInstance;
    }

    private CommonVar() {
    }

    private static int maxHistoryRecords;

    private static int deviceDetailSelectedTabIdx;

    public synchronized static int getDeviceDetailSelectedTabIdx() {
        return deviceDetailSelectedTabIdx;
    }

    public synchronized static void setDeviceDetailSelectedTabIdx(int deviceDetailSelectedTabIdx) {
        CommonVar.deviceDetailSelectedTabIdx = deviceDetailSelectedTabIdx;
    }

    public synchronized static int getMaxHistoryRecords() {
        return maxHistoryRecords;
    }

    public synchronized static void setMaxHistoryRecords(int maxHistoryRecords) {
        CommonVar.maxHistoryRecords = maxHistoryRecords;
    }
}
