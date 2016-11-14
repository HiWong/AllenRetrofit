package wang.imallen.allenretrofit.helper;

import android.util.Log;

/**
 * Created by allen on 16-9-4.
 */
public class ProductInfoHelper {

    private static final String TAG=ProductInfoHelper.class.getSimpleName();

    public static String getProductSerial() {
        //有可能会返回unknown
        if (android.os.Build.VERSION.SDK_INT >= 9) {
            String serial = android.os.Build.SERIAL;
            if (serial == null || serial.equalsIgnoreCase("<NULL>")) {
                serial = "";
            }
            Log.d(TAG, "product serial:" + serial);
            return serial;
        } else {
            return "unknow";
        }
    }

}
