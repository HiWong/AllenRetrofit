package retrofit3.annotation.bean.parameter;

/**
 * Created by allen on 16-9-2.
 */
public class FieldMapBean implements ParaAnnotationBean {

    @Override
    public int annotationType() {
        return FIELDMAP;
    }

    private boolean encoded=false;

    public boolean isEncoded() {
        return encoded;
    }

    public void setEncoded(boolean encoded) {
        this.encoded = encoded;
    }
}
