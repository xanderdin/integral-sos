package ua.integral.sos.app;

import java.util.Random;

/**
 * Created by aledin on 26.01.15.
 */
public class MiscFunc {

    public static long now() {
        return System.currentTimeMillis()/1000;
    }

    public static boolean isTelOk(String tel) {
        return tel.matches("^[\\+]{0,1}[0-9]{"
                + (CommonDef.TEL_MIN_LENGTH - 1)
                + ","
                + (CommonDef.TEL_MAX_LENGTH - 1)
                + "}$");
    }
}
