package retrofit3.annotation.bean;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.Messager;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Created by allen on 16-8-31.
 */
public class MethodBean implements Serializable{

    static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
    static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");
    static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);




    private String methodDeclaration;
    //如setVolume这样
    private String methodName;
    //private Annotation[]methodAnnotations;
    //注意有的方法是没有参数的
    private Annotation[][]parameterAnnotationsArray;
    private List<String>parameterNames=new ArrayList<>();
    private String[]parameterTypeNames;
    private String[][]parameterTypeArgumentsNameArray;

    private List<TypeMirror> parameterTypeMirrors=new ArrayList<>();

    private TypeMirror returnTypeMirror;

    //private Annotation[]methodAnnotations;

    private String rawReturnTypeName;
    private String[]returnTypeArgumentsName;
    private String responseTypeName;
    private String[]responseTypeArgumentsName;

    private String httpMethod;
    private boolean hasBody;
    private boolean isFormEncoded;
    private boolean isMultipart;
    private String relativeUrl;
    private Set<String>relativeUrlParamNames;
    private String[]headersValue;

    /*
    public Annotation[] getMethodAnnotations() {
        return methodAnnotations;
    }

    public void setMethodAnnotations(Annotation[] methodAnnotations) {
        this.methodAnnotations = methodAnnotations;
    }
    */


    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String[][] getParameterTypeArgumentsNameArray() {
        return parameterTypeArgumentsNameArray;
    }

    public void setParameterTypeArgumentsNameArray(String[][] parameterTypeArgumentsNameArray) {
        this.parameterTypeArgumentsNameArray = parameterTypeArgumentsNameArray;
    }

    public String[] getParameterTypeNames() {
        return parameterTypeNames;
    }

    public void setParameterTypeNames(String[] parameterTypeNames) {
        this.parameterTypeNames = parameterTypeNames;
    }

    public String getRawReturnTypeName() {
        return rawReturnTypeName;
    }

    public void setRawReturnTypeName(String rawReturnTypeName) {
        this.rawReturnTypeName = rawReturnTypeName;
    }

    public String[] getResponseTypeArgumentsName() {
        return responseTypeArgumentsName;
    }

    public void setResponseTypeArgumentsName(String[] responseTypeArgumentsName) {
        this.responseTypeArgumentsName = responseTypeArgumentsName;
    }

    public String getResponseTypeName() {
        return responseTypeName;
    }

    public void setResponseTypeName(String responseTypeName) {
        this.responseTypeName = responseTypeName;
    }

    public String[] getReturnTypeArgumentsName() {
        return returnTypeArgumentsName;
    }

    public void setReturnTypeArgumentsName(String[] returnTypeArgumentsName) {
        this.returnTypeArgumentsName = returnTypeArgumentsName;
    }

    public Annotation[][] getParameterAnnotationsArray() {
        return parameterAnnotationsArray;
    }

    public void setParameterAnnotationsArray(Annotation[][] parameterAnnotationsArray) {
        this.parameterAnnotationsArray = parameterAnnotationsArray;
    }

    public TypeMirror getReturnTypeMirror() {
        return returnTypeMirror;
    }

    public void setReturnTypeMirror(TypeMirror returnTypeMirror) {
        this.returnTypeMirror = returnTypeMirror;
    }

    public List<TypeMirror> getParameterTypeMirrors() {
        return parameterTypeMirrors;
    }

    public void addParameterTypeMirror(TypeMirror mirror){
        parameterTypeMirrors.add(mirror);
    }

    public List<String> getParameterNames() {
        return parameterNames;
    }

    public void addParameterName(String name){
        parameterNames.add(name);
    }

    public String getMethodDeclaration() {
        return methodDeclaration;
    }

    public void setMethodDeclaration(String methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }

    public boolean isHasBody() {
        return hasBody;
    }

    public void setHasBody(boolean hasBody) {
        this.hasBody = hasBody;
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

    public void setParameterNames(List<String> parameterNames) {
        this.parameterNames = parameterNames;
    }

    public void setParameterTypeMirrors(List<TypeMirror> parameterTypeMirrors) {
        this.parameterTypeMirrors = parameterTypeMirrors;
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

    public String[] getHeadersValue() {
        return headersValue;
    }

    public void setHeadersValue(String[] headersValue) {
        this.headersValue = headersValue;
    }

    public void parseHttpMethodAndPath(String httpMethod, String value, boolean hasBody, Messager messager){
        if (this.httpMethod != null) {
            methodError(messager,"Only one HTTP method is allowed. Found: %s and %s.",
                    this.httpMethod, httpMethod);
            return;
        }
        this.httpMethod = httpMethod;
        this.hasBody = hasBody;

        if (value.isEmpty()) {
            return;
        }

        //Get the relative URL path and existing query string,if present
        int question = value.indexOf('?');
        if (question != -1 && question < value.length() - 1) {
            //Ensure the query string does not have any named parameters
            String queryParams = value.substring(question + 1);
            Matcher queryParamMatcher = PARAM_URL_REGEX.matcher(queryParams);
            if (queryParamMatcher.find()) {
               methodError(messager,"URL query string \"%s\" must not have replace block. "
                        + "For dynamic query parameters user @Query.", queryParams);
            }
        }

        this.relativeUrl = value;
        this.relativeUrlParamNames = parsePathParameters(value);
    }

    /**
     * 解析路径参数,如"adat/sk/{cityId}.html"中的cityId就是路径参数
     * Gets the set of unique path parameters used in the given URI. If a parameter is used twice
     * in the URI, it will only show up once in the set.
     */
    static Set<String> parsePathParameters(String path) {
        Matcher m = PARAM_URL_REGEX.matcher(path);
        Set<String> patterns = new LinkedHashSet<>();
        while (m.find()) {
            patterns.add(m.group(1));
        }
        return patterns;
    }

    private void methodError(Messager messager,String message,Object...args){
        message=String.format(message,args);
        messager.printMessage(Diagnostic.Kind.ERROR,message);
    }


}
