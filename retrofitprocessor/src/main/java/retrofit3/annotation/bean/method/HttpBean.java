package retrofit3.annotation.bean.method;

/**
 * Created by allen on 16-9-2.
 */
public class HttpBean implements MethodAnnotationBean {

    private String path = "";
    private boolean hasBody = false;

    @Override
    public int annotationType() {
        return HTTP;
    }

    public boolean isHasBody() {
        return hasBody;
    }

    public void setHasBody(boolean hasBody) {
        this.hasBody = hasBody;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
