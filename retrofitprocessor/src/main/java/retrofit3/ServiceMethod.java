package retrofit3;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit3.annotation.bean.ApiBean;
import retrofit3.annotation.bean.MethodAnnotationBean;
import retrofit3.annotation.bean.MethodBean;
import retrofit3.annotation.bean.RawMethodAnnotationBean;
import retrofit3.annotation.bean.parameter.BodyBean;
import retrofit3.annotation.bean.parameter.FieldBean;
import retrofit3.annotation.bean.parameter.FieldMapBean;
import retrofit3.annotation.bean.parameter.HeaderBean;
import retrofit3.annotation.bean.parameter.ParaAnnotationBean;
import retrofit3.annotation.bean.parameter.PartBean;
import retrofit3.annotation.bean.parameter.PartMapBean;
import retrofit3.annotation.bean.parameter.PathBean;
import retrofit3.annotation.bean.parameter.QueryBean;
import retrofit3.annotation.bean.parameter.QueryMapBean;
import retrofit3.annotation.bean.parameter.UrlBean;
import retrofit3.annotation.http.Body;
import retrofit3.annotation.http.DELETE;
import retrofit3.annotation.http.Field;
import retrofit3.annotation.http.FieldMap;
import retrofit3.annotation.http.FormUrlEncoded;
import retrofit3.annotation.http.GET;
import retrofit3.annotation.http.HEAD;
import retrofit3.annotation.http.HTTP;
import retrofit3.annotation.http.Header;
import retrofit3.annotation.http.Multipart;
import retrofit3.annotation.http.OPTIONS;
import retrofit3.annotation.http.PATCH;
import retrofit3.annotation.http.POST;
import retrofit3.annotation.http.PUT;
import retrofit3.annotation.http.Part;
import retrofit3.annotation.http.PartMap;
import retrofit3.annotation.http.Path;
import retrofit3.annotation.http.Query;
import retrofit3.annotation.http.QueryMap;
import retrofit3.annotation.http.Url;
import retrofit3.call.adapter.CallAdapter;

/**
 * Created by allen on 16-8-28.
 */
public final class ServiceMethod<T> {
    // Upper and lower characters, digits, underscores, and hyphens, starting with a character.
    static final String PARAM = "[a-zA-Z][a-zA-Z0-9_-]*";
    static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");
    static final Pattern PARAM_NAME_REGEX = Pattern.compile(PARAM);
    //static String apiName;

    final okhttp3.Call.Factory callFactory;
    final CallAdapter<?> callAdapter;

    private final HttpUrl baseUrl;
    private final Converter<ResponseBody, T> responseConverter;
    private final String httpMethod;
    private final String relativeUrl;
    private final Headers headers;
    private final MediaType contentType;
    private final boolean hasBody;
    private final boolean isFormEncoded;
    private final boolean isMultipart;
    private final ParameterHandler<?>[] parameterHandlers;

    ServiceMethod(Builder<T> builder) {
        this.callFactory = builder.retrofit.callFactory();
        this.callAdapter = builder.callAdapter;
        //actually baseUrl should be static variable of ServiceMethod
        this.baseUrl = builder.retrofit.baseUrl();
        this.responseConverter = builder.responseConverter;
        this.httpMethod = builder.methodAnnotationBean.getHttpMethod();
        this.relativeUrl = builder.methodAnnotationBean.getRelativeUrl();
        this.headers = builder.methodAnnotationBean.getHeaders();
        this.contentType = builder.methodAnnotationBean.getContentType();
        this.hasBody = builder.methodAnnotationBean.isHasBody();
        this.isFormEncoded = builder.methodAnnotationBean.isFormEncoded();
        this.isMultipart = builder.methodAnnotationBean.isMultipart();
        this.parameterHandlers = builder.parameterHandlers;
    }

    public Request toRequest(Object... args) throws IOException {
        RequestBuilder requestBuilder = new RequestBuilder(httpMethod, baseUrl, relativeUrl, headers,
                contentType, hasBody, isFormEncoded, isMultipart);

        @SuppressWarnings("unchecked")
        ParameterHandler<Object>[] handlers = (ParameterHandler<Object>[]) parameterHandlers;

        int argumentCount = args != null ? args.length : 0;
        if (argumentCount != handlers.length) {
            throw new IllegalArgumentException("Argument count (" + argumentCount
                    + ") doesn't match expected count (" + handlers.length + ")");
        }

        for (int i = 0; i < argumentCount; ++i) {
            handlers[i].apply(requestBuilder, args[i]);
        }

        return requestBuilder.build();
    }

    public T toResponse(ResponseBody body) throws IOException {
        return responseConverter.convert(body);
    }

    public okhttp3.Call.Factory getCallFactory() {
        return callFactory;
    }

    static final class Builder<T> {
        final Retrofit retrofit;
        //methodName是如"getRecommandAppList(java.lang.String,java.lang.String,java.lang.String)"这样的
        final String methodDeclaration;
        //final Annotation[] methodAnnotations;
        //有一组参数的Annotation,而每个参数可能有多个Annotation,所以是二维数组
        //final Annotation[][] parameterAnnotationsArray;
        final ParaAnnotationBean[][]parameterAnnotationBeansArray;

        final Class[] parameterTypes;
        final Class[][] parameterTypeArgumentsArray;

        final Class rawReturnType;
        final Class[] returnTypeArguments;
        //responseType其实就是returnTypeArguments[0],而responseTypeArguemtns就是responseType的type arguments
        //如Observalbe<CustomType<String,Integer>>,responseType-->CustomType.class,responseTypeArguements={java.lang.String.class,java.lang.Integer.class}
        final Class responseType;
        final Class[] responseTypeArguments;

        ////////////////////start of method annotations related///////////
        /*
        String httpMethod;
        boolean hasBody;
        boolean isFormEncoded;
        boolean isMultipart;
        String relativeUrl;
        Headers headers;
        MediaType contentType;
        Set<String> relativeUrlParamNames;
        */

        final MethodAnnotationBean methodAnnotationBean;
        /////////////////////end of method annotations related//////////////

        //////////////start of parameter annotations related///////////////
        boolean gotField;
        boolean gotPart;
        boolean gotBody;
        boolean gotPath;
        boolean gotQuery;
        boolean gotUrl;

        ParameterHandler<?>[] parameterHandlers;
        Converter<ResponseBody, T> responseConverter;
        CallAdapter<?> callAdapter;
        /////////////////end of parameter annotations related/////////////

        /*
        public Builder(Retrofit retrofit,ApiBean apiBean,String methodDeclaration){
            MethodBean methodBean=apiBean.getMethodBeanMap().get(methodDeclaration);
            if(null==methodBean){
                throw new RuntimeException("Cannot find MethodBean based on "+methodDeclaration);
            }
            //this(retrofit,apiBean.getApiName(),methodDeclaration,methodBean.getRawReturnTypeName())
        }
        */

        public Builder(Retrofit retrofit,String methodDeclaration, Class rawReturnType,
                       Class[] returnTypeArguments, Class responseType, Class[] responseTypeArguments,
                       RawMethodAnnotationBean rawBean, ParaAnnotationBean[][]parameterAnnotationBeansArray,
                       Class[] parameterTypes, Class[][] parameterTypeArguments) {
            this.retrofit = retrofit;
            this.methodDeclaration = methodDeclaration;
            this.rawReturnType = rawReturnType;
            this.returnTypeArguments = returnTypeArguments;
            this.responseType = responseType;
            this.responseTypeArguments = responseTypeArguments;
            methodAnnotationBean=new MethodAnnotationBean(rawBean);
            this.parameterAnnotationBeansArray=parameterAnnotationBeansArray;
            this.parameterTypes = parameterTypes;
            this.parameterTypeArgumentsArray = parameterTypeArguments;
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

        public ServiceMethod build() {
            callAdapter = createCallAdapter();
            if (responseType == Response.class || responseType == okhttp3.Response.class) {
                throw methodError("'" + responseType.getName()
                        + " ' is not a valid response body type. Did you mean ResponseBody?");
            }
            responseConverter = createResponseConverter();

            /*
            for (Annotation annotation : methodAnnotations) {
                parseMethodAnnotation(annotation);
            }
            */
            //headers=parseHeaders(headersValue);

            if (methodAnnotationBean.getHttpMethod() == null) {
                throw methodError("HTTP method annotation is required (e.g., @GET, @POST, etc.).");
            }

            if (!methodAnnotationBean.isHasBody()) {
                if (methodAnnotationBean.isMultipart()) {
                    throw methodError("Multipart can only be specified on HTTP methods with request body (e.g., @POST).");
                }
                if (methodAnnotationBean.isFormEncoded()) {
                    throw methodError("FormUrlEncoded can only be specified on HTTP methods with " +
                            "request body (e.g.,@POST).");
                }
            }

            //int parameterCount = parameterAnnotationsArray.length;
            int parameterCount=parameterAnnotationBeansArray.length;
            parameterHandlers = new ParameterHandler<?>[parameterCount];
            for (int i = 0; i < parameterCount; ++i) {
                Class parameterType = parameterTypes[i];
                /*
                Annotation[] parameterAnnotations = parameterAnnotationsArray[i];
                if (parameterAnnotations == null) {
                    throw parameterError(i, "No Retrofit annotation found.");
                }
                */
                ParaAnnotationBean[]paraAnnotationBeans=parameterAnnotationBeansArray[i];

                Class[]parameterTypeArguments=null;
                if(null!=parameterTypeArgumentsArray){
                    parameterTypeArguments=parameterTypeArgumentsArray[i];
                }
                parameterHandlers[i] = parseParameter(i, parameterType, parameterTypeArguments, paraAnnotationBeans);
            }

            if (methodAnnotationBean.getRelativeUrl() == null && !gotUrl) {
                throw methodError("Missing either @%s URL or @Url parameter.", methodAnnotationBean.getHttpMethod());
            }
            if (!methodAnnotationBean.isFormEncoded() && !methodAnnotationBean.isMultipart() && !methodAnnotationBean.isHasBody() && gotBody) {
                throw methodError("Non-boddy HTTP method cannot contain @Body.");
            }
            if (methodAnnotationBean.isFormEncoded() && !gotField) {
                throw methodError("Multipart method must contain at least one @Part.");
            }
            if (methodAnnotationBean.isMultipart() && !gotPart) {
                throw methodError("Multipart method must contain at least one @Part.");
            }
            return new ServiceMethod<>(this);
        }

        private ParameterHandler<?> parseParameter(int i, Class parameterType, Class[] paraTypeArguments, ParaAnnotationBean[]paraAnnotationBeans) {
            ParameterHandler<?> result = null;
            for (ParaAnnotationBean bean:paraAnnotationBeans) {
                ParameterHandler<?> annotationAction = parseParameterAnnotation(i, parameterType, paraTypeArguments, paraAnnotationBeans, bean);
                if (annotationAction == null) {
                    continue;
                }

                if (result != null) {
                    throw parameterError(i, "Multiple Retrofit annotations found, only one allowed.");
                }

                result = annotationAction;
            }
            if (result == null) {
                throw parameterError(i, "No Retrofit annotation found.");
            }

            return result;
        }

        private ParameterHandler<?> parseParameterAnnotation(int i, Class parameterType, Class[] paraTypeArguments,
                                                             ParaAnnotationBean[]paraAnnotationBeans, ParaAnnotationBean bean) {
            if (bean instanceof UrlBean) {
                return parseUrl(i, parameterType);
            } else if (bean instanceof PathBean) {
                return parsePath(i, parameterType, paraAnnotationBeans, bean);
            } else if (bean instanceof QueryBean) {
                return parseQuery(i, parameterType, paraTypeArguments, paraAnnotationBeans, bean);
            } else if (bean instanceof QueryMapBean) {
                return parseQueryMap(i, parameterType, paraTypeArguments,  paraAnnotationBeans, bean);
            } else if (bean instanceof HeaderBean) {
                return parseHeader(i, parameterType, paraTypeArguments,  paraAnnotationBeans, bean);
            } else if (bean instanceof FieldBean) {
                return parseField(i, parameterType, paraTypeArguments,  paraAnnotationBeans, bean);
            } else if (bean instanceof FieldMapBean) {
                return parseFieldMap(i, parameterType, paraTypeArguments,paraAnnotationBeans, bean);
            } else if (bean instanceof PartBean) {
                return parsePart(i, parameterType, paraTypeArguments, paraAnnotationBeans, bean);
            } else if (bean instanceof PartMapBean) {
                return parsePartMap(i, parameterType, paraTypeArguments, paraAnnotationBeans, bean);
            } else if (bean instanceof BodyBean) {
                return parseBody(i, parameterType, paraTypeArguments, paraAnnotationBeans);
            }

            return null;
        }

        private ParameterHandler<?> parseBody(int i, Class parameterType, Class[] paraTypeArguments,
                                              ParaAnnotationBean[]paraAnnotationBeans) {
            if (methodAnnotationBean.isFormEncoded() || methodAnnotationBean.isMultipart()) {
                throw parameterError(i, "@Body parameters cannot be used with form or multi-part encoding.");
            }
            if (gotBody) {
                throw parameterError(i, "Multiple @Body method annotations found.");
            }

            Converter<?, RequestBody> converter;
            try {
                converter = retrofit.requestBodyConverter(parameterType, paraAnnotationBeans, methodAnnotationBean);
            } catch (RuntimeException e) {
                //Wide exception reange because factories are user code.
                throw parameterError(e, i, "Unable to create @Body converter for %s", parameterType);
            }
            gotBody = true;
            return new ParameterHandler.Body<>(converter);
        }

        private ParameterHandler<?> parsePartMap(int i, Class parameterType, Class[] paraTypeArguments,
                                                 ParaAnnotationBean[]paraAnnotationBeans, ParaAnnotationBean paraBean) {
            if (!methodAnnotationBean.isMultipart()) {
                throw parameterError(i, "@PartMap parameters can only be used with multipart encoding.");
            }
            gotPart = true;
            if (!Map.class.isAssignableFrom(parameterType)) {
                throw parameterError(i, "@PartMap parameter type must be Map.");
            }
            if (null == paraTypeArguments || paraTypeArguments.length != 2) {
                throw parameterError(i, "Map must include generic types (e.g., Map<String,String>)");
            }

            Class keyType = paraTypeArguments[0];
            if (String.class != keyType) {
                throw parameterError(i, "@PartMap keys must be of type String: " + keyType);
            }
            Class valueType = paraTypeArguments[1];
            if (MultipartBody.Part.class.isAssignableFrom(valueType)) {
                throw parameterError(i, "@PartMap values cannot be MultipartBody.Part. "
                        + "Use @Part List<Part> or a different value type instead.");
            }

            Converter<?, RequestBody> valueConverter = retrofit.requestBodyConverter(valueType, paraAnnotationBeans, methodAnnotationBean);

            //PartMap partMap = (PartMap) annotation;
            return new ParameterHandler.PartMap<>(valueConverter, ((PartMapBean)paraBean).getEncoding());
        }

        private ParameterHandler<?> parsePart(int i, Class parameterType, Class[] paraTypeArguments,
                                              ParaAnnotationBean[]paraAnnotationBeans, ParaAnnotationBean paraBean) {
            if (!methodAnnotationBean.isMultipart()) {
                throw parameterError(i, "@Part parameters can only be used with multipart encoding.");
            }
            //Part part = (Part) annotation;
            PartBean partBean=(PartBean)paraBean;
            gotPart = true;

            String partName = partBean.getValue();
            if (partName.isEmpty()) {
                if (!MultipartBody.Part.class.isAssignableFrom(parameterType)) {
                    throw parameterError(i, "@Part annotation must supply a name or use MultipartBody.Part parameter type.");
                }
                return ParameterHandler.RawPart.INSTANCE;
            } else {
                Headers headers = Headers.of("Content-Disposition", "form-data; name=\"" + partName + "\"",
                        "Content-Transfer-Encoding", partBean.getEncoding());
                if (Iterable.class.isAssignableFrom(parameterType)) {
                    if (null == paraTypeArguments || paraTypeArguments.length == 0) {
                        throw parameterError(i, parameterType.getSimpleName()
                                + " must include generic type (e.g., "
                                + parameterType.getSimpleName()
                                + "<String>)");
                    }
                    Class iterableType = paraTypeArguments[0];
                    Converter<?, RequestBody> converter = retrofit.requestBodyConverter(iterableType, paraAnnotationBeans,methodAnnotationBean);
                    return new ParameterHandler.Part<>(headers, converter).iterable();
                } else if (parameterType.isArray()) {
                    Class<?> arrayComponentType = boxIfPrimitive(parameterType.getComponentType());
                    Converter<?, RequestBody> converter = retrofit.requestBodyConverter(arrayComponentType,paraAnnotationBeans, methodAnnotationBean);
                    return new ParameterHandler.Part<>(headers, converter).array();
                } else if (MultipartBody.Part.class.isAssignableFrom(parameterType)) {
                    throw parameterError(i, "@Part parameters using the MultipartBody.Part must not "
                            + "include a part name in the annotation.");
                } else {
                    Converter<?, RequestBody> converter = retrofit.requestBodyConverter(parameterType,paraAnnotationBeans, methodAnnotationBean);
                    return new ParameterHandler.Part<>(headers, converter);
                }
            }
        }

        private ParameterHandler<?> parseFieldMap(int i, Class parameterType, Class[] paraTypeArguments,
                                                  ParaAnnotationBean[]paraAnnotationBeans, ParaAnnotationBean paraBean) {
            if (!methodAnnotationBean.isFormEncoded()) {
                throw parameterError(i, "@FieldMap parameters can only be used with form encoding.");
            }
            if (paraTypeArguments == null || paraTypeArguments.length != 2) {
                throw parameterError(i, "Map must include generic type (e.g.,Map<String,String>)");
            }
            Class keyType = paraTypeArguments[0];
            if (String.class != keyType) {
                throw parameterError(i, "@FieldMap keys must be of type String: " + keyType);
            }
            Class valueType = paraTypeArguments[1];
            Converter<?, String> valueConverter = retrofit.stringConverter(valueType, paraAnnotationBeans);

            gotField = true;
            return new ParameterHandler.FieldMap<>(valueConverter, ((FieldMapBean)paraBean).isEncoded());
        }

        private ParameterHandler<?> parseField(int i, Class parameterType, Class[] paraTypeArguments,
                                               ParaAnnotationBean[]paraAnnotationBeans, ParaAnnotationBean paraBean) {
            if (!methodAnnotationBean.isFormEncoded()) {
                throw parameterError(i, "@Field parameters can only be used with form encoding.");
            }
            //Field field = (Field) annotation;
            FieldBean fieldBean=(FieldBean)paraBean;
            String name = fieldBean.getValue();
            boolean encoded = fieldBean.isEncoded();


            gotField = true;

            if (Iterable.class.isAssignableFrom(parameterType)) {
                if (null == paraTypeArguments || paraTypeArguments.length == 0) {
                    throw parameterError(i, parameterType.getSimpleName()
                            + " must include generic type (e.g.,"
                            + parameterType.getSimpleName()
                            + "<String>");
                }
                Class iterableType = paraTypeArguments[0];
                Converter<?, String> converter = retrofit.stringConverter(iterableType, paraAnnotationBeans);
                return new ParameterHandler.Field<>(name, converter, encoded).iterable();
            } else if (parameterType.isArray()) {
                Class<?> arrayComponentType = boxIfPrimitive(parameterType.getComponentType());
                Converter<?, String> converter = retrofit.stringConverter(arrayComponentType, paraAnnotationBeans);
                return new ParameterHandler.Field<>(name, converter, encoded).array();
            } else {
                Converter<?, String> converter = retrofit.stringConverter(parameterType, paraAnnotationBeans);
                return new ParameterHandler.Field<>(name, converter, encoded);
            }
        }

        private ParameterHandler<?> parseHeader(int i, Class parameterType, Class[] paraTypeArguments,
                                                ParaAnnotationBean[]paraAnnotationBeans, ParaAnnotationBean paraBean) {
            //Header header = (Header) annotation;
            HeaderBean headerBean=(HeaderBean)paraBean;
            String name = headerBean.getValue();

            if (Iterable.class.isAssignableFrom(parameterType)) {
                if (null == paraTypeArguments || paraTypeArguments.length == 0) {
                    throw parameterError(i, parameterType + " must include generic type (e.g.,"
                            + parameterType.getSimpleName() + "<String>");
                }
                Class iterableType = paraTypeArguments[0];
                Converter<?, String> converter = retrofit.stringConverter(iterableType, paraAnnotationBeans);
                return new ParameterHandler.Header<>(name, converter).iterable();
            } else if (parameterType.isArray()) {
                Class<?> arrayComponentType = boxIfPrimitive(parameterType.getComponentType());
                Converter<?, String> converter = retrofit.stringConverter(arrayComponentType, paraAnnotationBeans);
                return new ParameterHandler.Header<>(name, converter).array();
            } else {
                Converter<?, String> converter = retrofit.stringConverter(parameterType, paraAnnotationBeans);
                return new ParameterHandler.Header<>(name, converter);
            }
        }

        private ParameterHandler<?> parseQueryMap(int i, Class parameterType, Class[] paraTypeArguments,
                                                  ParaAnnotationBean[]paraAnnotationBeans, ParaAnnotationBean paraBean) {
            if (!Map.class.isAssignableFrom(parameterType)) {
                throw parameterError(i, "@QueryMap parameter type must be Map.");
            }
            //Class mapType=Utils.getSupertype(parameterType,parameterType,Map.class);
            if (null == paraTypeArguments || paraTypeArguments.length != 2) {
                throw parameterError(i, "Map must include 2 generic types (e.g.,Map<String,String>");
            }
            Class keyType = paraTypeArguments[0];
            if (String.class != keyType) {
                throw parameterError(i, "@QueryMap keys must be of type String: " + keyType);
            }
            Class valueType = paraTypeArguments[1];
            Converter<?, String> valueConverter = retrofit.stringConverter(valueType, paraAnnotationBeans);
            return new ParameterHandler.QueryMap<>(valueConverter, ((QueryMapBean)paraBean).isEncoded());
        }

        private ParameterHandler<?> parseQuery(int i, Class parameterType, Class[] paraTypeArguments,
                                               ParaAnnotationBean[]paraAnnotationBeans, ParaAnnotationBean paraBean) {
            //Query query = (Query) annotation;
            QueryBean queryBean=(QueryBean)paraBean;
            String name = queryBean.getValue();
            boolean encoded = queryBean.isEncoded();
            gotQuery = true;
            if (Iterable.class.isAssignableFrom(parameterType)) {
                if (null == paraTypeArguments || paraTypeArguments.length == 0) {
                    throw parameterError(i, parameterType.getSimpleName()
                            + " must include generic type (e.g.,"
                            + parameterType.getSimpleName() + "<String>)");
                }
                Converter<?, String> converter = retrofit.stringConverter(paraTypeArguments[0], paraAnnotationBeans);
                return new ParameterHandler.Query<>(name, converter, encoded).iterable();
            } else if (parameterType.isArray()) {
                Class<?> arrayComponentType = boxIfPrimitive(parameterType.getComponentType());
                Converter<?, String> converter = retrofit.stringConverter(arrayComponentType, paraAnnotationBeans);
                return new ParameterHandler.Query<>(name, converter, encoded).array();
            } else {
                Converter<?, String> converter = retrofit.stringConverter(parameterType, paraAnnotationBeans);
                return new ParameterHandler.Query<>(name, converter, encoded);
            }
        }

        private ParameterHandler<?> parsePath(int i, Class parameterType, ParaAnnotationBean[]paraAnnotationBeans, ParaAnnotationBean paraBean) {
            if (gotQuery) {
                throw parameterError(i, "A @Path parameter must not come after a @Query.");
            }
            if (gotUrl) {
                throw parameterError(i, "@Path parameters may not be used with @Url");
            }
            if (methodAnnotationBean.getRelativeUrl() == null) {
                throw parameterError(i, "@Path can only be used with relative url on @%s", methodAnnotationBean.getHttpMethod());
            }
            gotPath = true;

            //Path path = (Path) annotation;
            String name = ((PathBean)paraBean).getValue();
            validatePathName(i, name);

            Converter<?, String> converter = retrofit.stringConverter(parameterType,paraAnnotationBeans);
            return new ParameterHandler.Path<>(name, converter, ((PathBean) paraBean).isEncoded());
        }

        private void validatePathName(int i, String name) {
            if (!PARAM_NAME_REGEX.matcher(name).matches()) {
                throw parameterError(i, "@Path parameter name must match %s. Found: %s",
                        PARAM_URL_REGEX.pattern(), name);
            }
            //Verify URL replacement name is actually present in the URL path.
            if (!methodAnnotationBean.getRelativeUrlParamNames().contains(name)) {
                throw parameterError(i, "URL \"%s\" does not contains \"{%s}\".", methodAnnotationBean.getRelativeUrl(), name);
            }
        }

        private ParameterHandler<?> parseUrl(int i, Class parameterType) {
            if (gotUrl) {
                throw parameterError(i, "Multiple @Url method annotations found.");
            }
            if (gotPath) {
                throw parameterError(i, "@Path parameters may not be used with @Url");
            }
            if (gotQuery) {
                throw parameterError(i, "A @Url parameter must not come after a @Query");
            }
            if (methodAnnotationBean.getRelativeUrl()!= null) {
                throw parameterError(i, "@Url cannot be used with @%s URL", methodAnnotationBean.getHttpMethod());
            }

            gotUrl = true;
            if (parameterType == HttpUrl.class || parameterType == String.class
                    || parameterType == URI.class
                    || "android.net.Uri".equals(parameterType.getName())) {
                return new ParameterHandler.RelativeUrl();
            } else {
                throw parameterError(i, "@Url must be okhttp3.HttpUrl,String,java.net.URI, or android.net.Uri type.");
            }
        }


        private Converter<ResponseBody, T> createResponseConverter() {
            //Annotation[] annotations = methodAnnotations;
            try {
                return retrofit.responseBodyConverter(responseType, methodAnnotationBean);
            } catch (RuntimeException e) {
                throw methodError(e, "Unable to create converter for %s", responseType);
            }
        }

        private CallAdapter<?> createCallAdapter() {
            if (rawReturnType == void.class) {
                throw methodError("Service methods cannot return void.");
            }
            try {
                return retrofit.callAdapter(rawReturnType, returnTypeArguments, responseType,
                        responseTypeArguments, methodAnnotationBean);
            } catch (RuntimeException e) { //Wide exception range because factories are user code.
                throw methodError(e, "Unable ito create call adapter for %s", rawReturnType);
            }
        }

        private RuntimeException methodError(String message, Object... args) {
            return methodError(null, message, args);
        }

        private RuntimeException methodError(Throwable cause, String message, Object... args) {
            message = String.format(message, args);
            return new IllegalArgumentException(message
                    + "\n    for method "
                    + retrofit.getApiName()
                    + "."
                    + methodDeclaration, cause);
        }

        private RuntimeException parameterError(
                Throwable cause, int p, String message, Object... args) {
            return methodError(cause, message + " (parameter #" + (p + 1) + ")", args);
        }

        private RuntimeException parameterError(int p, String message, Object... args) {
            return methodError(message + " (parameter #" + (p + 1) + ")", args);
        }

        static Class<?> boxIfPrimitive(Class<?> type) {
            if (boolean.class == type) {
                return Boolean.class;
            } else if (byte.class == type) {
                return Byte.class;
            } else if (char.class == type) {
                return Character.class;
            } else if (double.class == type) {
                return Double.class;
            } else if (float.class == type) {
                return Float.class;
            } else if (int.class == type) {
                return Integer.class;
            } else if (long.class == type) {
                return Long.class;
            } else if (short.class == type) {
                return Short.class;
            }
            return type;
        }

    }




}
