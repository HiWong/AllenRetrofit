package wang.imallen.allenretrofit.helper;

/**
 * Created by allen on 16-9-4.
 */
public final class DouBanPlayHelper {

    private static final String NORMAL_PT = "0.0";
    private static final String NORMAL_SID = "";
    private static boolean isFirst = true;
    private static String PT = NORMAL_PT;
    private static String SID = NORMAL_SID;

    private DouBanPlayHelper() {
    }

    public static boolean isFirst() {
        return isFirst;
    }

    public static void setFirst(boolean first) {
        isFirst = first;
    }

    public static void setLastPlay(String playTime, String id) {
        PT = playTime;
        SID = id;
    }

    public static String getPt() {
        return PT;
    }

    public static String getSid() {
        return SID;
    }

    public static String getNormalPt() {
        return NORMAL_PT;
    }

    public static String getNormalSid() {
        return NORMAL_SID;
    }
}

