package retrofit3.annotation.bean.parameter;

/**
 * Created by allen on 16-9-2.
 */
public class PartMapBean implements ParaAnnotationBean {

    private String encoding = "binary";

    public PartMapBean(String encoding){
        this.encoding=encoding;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
}
