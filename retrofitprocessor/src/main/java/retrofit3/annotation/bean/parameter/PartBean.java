package retrofit3.annotation.bean.parameter;

/**
 * Created by allen on 16-9-2.
 */
public class PartBean implements ParaAnnotationBean {

    private String value = "";
    private String encoding = "binary";

    public PartBean(String value,String encoding){
        this.value=value;
        this.encoding=encoding;
    }

    @Override
    public int annotationType() {
        return PART;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
