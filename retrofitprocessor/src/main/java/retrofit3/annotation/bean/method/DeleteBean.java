package retrofit3.annotation.bean.method;

/**
 * Created by allen on 16-9-2.
 */
public class DeleteBean implements CustomMethodAnnotationBean {

    private String value = "";

    @Override
    public int annotationType() {
        return DELETE;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
