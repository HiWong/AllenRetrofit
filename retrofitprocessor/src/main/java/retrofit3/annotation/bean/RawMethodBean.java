package retrofit3.annotation.bean;

import java.io.Serializable;
import java.util.Set;

/**
 * create a java bean to simplify the constructor of ServiceMethod.Builder
 * Created by allen on 16-9-2.
 */
public class RawMethodBean implements Serializable{

    private String httpMethod;
    private boolean hasBody;
    private boolean isFormEncoded;
    private boolean isMultipart;
    private String relativeUrl;
    private Set<String> relativeUrlParamNames;
    private String[]headersValue;

    public boolean isHasBody() {
        return hasBody;
    }

    public void setHasBody(boolean hasBody) {
        this.hasBody = hasBody;
    }

    public String[] getHeadersValue() {
        return headersValue;
    }

    public void setHeadersValue(String[] headersValue) {
        this.headersValue = headersValue;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public boolean isFormEncoded() {
        return isFormEncoded;
    }

    public void setFormEncoded(boolean formEncoded) {
        isFormEncoded = formEncoded;
    }

    public boolean isMultipart() {
        return isMultipart;
    }

    public void setMultipart(boolean multipart) {
        isMultipart = multipart;
    }

    public String getRelativeUrl() {
        return relativeUrl;
    }

    public void setRelativeUrl(String relativeUrl) {
        this.relativeUrl = relativeUrl;
    }

    public Set<String> getRelativeUrlParamNames() {
        return relativeUrlParamNames;
    }

    public void setRelativeUrlParamNames(Set<String> relativeUrlParamNames) {
        this.relativeUrlParamNames = relativeUrlParamNames;
    }
}
