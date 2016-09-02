package retrofit3;

import java.util.regex.Matcher;

import retrofit3.annotation.bean.MethodBean;

/**
 * Created by allen on 16-9-1.
 */
public final class ProcessorHelper {

    private ProcessorHelper() {
    }

    /**
     * 返回getRecommandAppList这样
     *
     * @param methodDeclartion 如getRecommandAppList(String page,String code,String version)这样
     * @return
     */
    public static String getMethodName(String methodDeclartion) {
        if (null == methodDeclartion) {
            return null;
        }
        int index = methodDeclartion.indexOf("(");
        if (index < 1) {
            return null;
        }
        return methodDeclartion.substring(0, index - 1);
    }

}
