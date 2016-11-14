package wang.imallen.allenretrofit.config;

import wang.imallen.allenretrofit.helper.ProductInfoHelper;

/**
 * Created by allen on 16-9-4.
 */
public final class DouBanConfig {

    private DouBanConfig() {
    }

    public static String DEVICE_ID;
    public static String CLIENT;
    public static final String BASE_URL = "https://api.douban.com/";
    public static final String DOUBAN_URL = "https://www.douban.com/";
    public static final String API_KEY = "0efa25b90aae36592cee712b200aa30b";
    public static final String CLIENT_SECRET = "1709fbfcf5ae3ea0";
    public static final String APP_NAME = "radio_whaley";
    public static final String ALT = "json";
    public static final String FORMATS = "aac,mp3";
    public static final String VERSION = "1";

    public static final String TYPE_FIRST_TIME="n";
    public static final String TYPE_SKIP="s";
    public static final String TYPE_PLAY_COMPLISHED="e";
    public static final String TYPE_FAVOR="r";
    public static final String TYPE_DISLIKE="u";
    //不再播放
    public static final String TYPE_BAN="b";


    static {
        DEVICE_ID = ProductInfoHelper.getProductSerial();
        CLIENT = "s:mobile|y:android|f:" + VERSION + "|m:Douban|d:" + DEVICE_ID;
    }
}

