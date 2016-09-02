package retrofit3.annotation.bean;

import java.io.Serializable;
import java.util.Set;

import okhttp3.Headers;
import okhttp3.MediaType;

/**
 * This java bean will be used in ServiceMethod and Retrofit to reduce variables of methods
 * Created by allen on 16-9-2.
 */
public class MethodAnnotationBean implements Serializable{

    private String httpMethod;
    private boolean hasBody;
    private boolean isFormEncoded;
    private boolean isMultipart;
    private String relativeUrl;
    Headers headers;
    MediaType contentType;
    private Set<String> relativeUrlParamNames;
    //private String[]headersValue;
    //flag of @Streaming
    private boolean streaming =false;

    public boolean isHasBody() {
        return hasBody;
    }

    public void setHasBody(boolean hasBody) {
        this.hasBody = hasBody;
    }

    public MediaType getContentType() {
        return contentType;
    }

    public void setContentType(MediaType contentType) {
        this.contentType = contentType;
    }

    public Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Headers headers) {
        this.headers = headers;
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

    public boolean isStreaming() {
        return streaming;
    }

    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
    }
}
