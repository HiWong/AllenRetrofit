package retrofit3.annotation.bean.parameter;

/**
 * Created by allen on 16-9-2.
 */
public class QueryMapBean implements ParaAnnotationBean {

    private boolean encoded = false;

    @Override
    public int annotationType() {
        return QUERYMAP;
    }

    public boolean isEncoded() {
        return encoded;
    }

    public void setEncoded(boolean encoded) {
        this.encoded = encoded;
    }
}
