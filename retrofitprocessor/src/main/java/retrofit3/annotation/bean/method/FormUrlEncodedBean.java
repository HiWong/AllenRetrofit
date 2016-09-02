package retrofit3.annotation.bean.method;

/**
 * Created by allen on 16-9-2.
 */
public class FormUrlEncodedBean implements CustomMethodAnnotationBean {

    @Override
    public int annotationType() {
        return FORMURLENCODED;
    }
}
