package retrofit3.annotation.bean.parameter;

/**
 * Created by allen on 16-9-2.
 */
public class BodyBean implements ParaAnnotationBean {

    @Override
    public int annotationType() {
        return BODY;
    }
}