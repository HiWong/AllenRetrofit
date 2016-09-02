package retrofit3.annotation.bean.parameter;

/**
 * Created by allen on 16-9-2.
 */
public class PartMapBean implements ParaAnnotationBean {

    private String encoding = "binary";

    @Override
    public int annotationType() {
        return PARTMAP;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
