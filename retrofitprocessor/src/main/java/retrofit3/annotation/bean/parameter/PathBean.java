package retrofit3.annotation.bean.parameter;

/**
 * Created by allen on 16-9-2.
 */
public class PathBean implements ParaAnnotationBean {

    private String value;
    private boolean encoded = false;

    public PathBean(String value,boolean encoded){
        this.value=value;
        this.encoded=encoded;
    }

    @Override
    public int annotationType() {
        return PATH;
    }

    public boolean isEncoded() {
        return encoded;
    }

    public void setEncoded(boolean encoded) {
        this.encoded = encoded;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
