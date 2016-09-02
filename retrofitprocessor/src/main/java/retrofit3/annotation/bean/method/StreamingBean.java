package retrofit3.annotation.bean.method;

/**
 * Created by allen on 16-9-2.
 */
public class StreamingBean implements MethodAnnotationBean {

    @Override
    public int annotationType() {
        return STREAMING;
    }

}
