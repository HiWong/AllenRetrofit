package retrofit3.annotation.bean.parameter;

/**
 * Created by allen on 16-9-2.
 */
public class HeaderBean implements ParaAnnotationBean {

    private String value;

    public HeaderBean(String value){
        this.value=value;
    }

    @Override
    public int annotationType() {
        return HEADER;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
