package retrofit3.annotation.bean.method;

/**
 * Created by allen on 16-9-2.
 */
public class MultipartBean implements MethodAnnotationBean {

    @Override
    public int annotationType() {
        return MULTIPART;
    }
}
