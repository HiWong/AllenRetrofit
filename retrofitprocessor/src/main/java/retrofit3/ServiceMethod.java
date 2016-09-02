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
import retrofit3.annotation.bean.MethodBean;
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

    static String apiName;


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
        this.httpMethod = builder.httpMethod;
        this.relativeUrl = builder.relativeUrl;
        this.headers = builder.headers;
        this.contentType = builder.contentType;
        this.hasBody = builder.hasBody;
        this.isFormEncoded = builder.isFormEncoded;
        this.isMultipart = builder.isMultipart;
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
        final Annotation[][] parameterAnnotationsArray;
        final Class[] parameterTypes;
        final Class[][] parameterTypeArgumentsArray;

        final Class rawReturnType;
        final Class[] returnTypeArguments;
        //responseType其实就是returnTypeArguments[0],而responseTypeArguemtns就是responseType的type arguments
        //如Observalbe<CustomType<String,Integer>>,responseType-->CustomType.class,responseTypeArguements={java.lang.String.class,java.lang.Integer.class}
        final Class responseType;
        final Class[] responseTypeArguments;

        ////////////////////start of method annotations related///////////
        String httpMethod;
        boolean hasBody;
        boolean isFormEncoded;
        boolean isMultipart;
        String relativeUrl;
        Headers headers;
        MediaType contentType;
        Set<String> relativeUrlParamNames;
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

        public Builder(Retrofit retrofit, String apiName, String methodDeclaration, Class rawReturnType,
                       Class[] returnTypeArguments, Class responseType, Class[] responseTypeArguments,
                       Annotation[] methodAnnotations, Annotation[][] parameterAnnotationsArray,
                       Class[] parameterTypes, Class[][] parameterTypeArguments) {
            this.retrofit = retrofit;
            ServiceMethod.apiName = apiName;
            this.methodDeclaration = methodDeclaration;
            this.rawReturnType = rawReturnType;
            this.returnTypeArguments = returnTypeArguments;
            this.responseType = responseType;
            this.responseTypeArguments = responseTypeArguments;
            //this.methodAnnotations = methodAnnotations;
            this.parameterAnnotationsArray = parameterAnnotationsArray;
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

            for (Annotation annotation : methodAnnotations) {
                parseMethodAnnotation(annotation);
            }

            if (httpMethod == null) {
                throw methodError("HTTP method annotation is required (e.g., @GET, @POST, etc.).");
            }

            if (!hasBody) {
                if (isMultipart) {
                    throw methodError("Multipart can only be specified on HTTP methods with request body (e.g., @POST).");
                }
                if (isFormEncoded) {
                    throw methodError("FormUrlEncoded can only be specified on HTTP methods with " +
                            "request body (e.g.,@POST).");
                }
            }

            int parameterCount = parameterAnnotationsArray.length;
            parameterHandlers = new ParameterHandler<?>[parameterCount];
            for (int i = 0; i < parameterCount; ++i) {
                Class parameterType = parameterTypes[i];
                Annotation[] parameterAnnotations = parameterAnnotationsArray[i];
                if (parameterAnnotations == null) {
                    throw parameterError(i, "No Retrofit annotation found.");
                }
                parameterHandlers[i] = parseParameter(i, parameterType, parameterTypeArgumentsArray[i], parameterAnnotations);
            }

            if (relativeUrl == null && !gotUrl) {
                throw methodError("Missing either @%s URL or @Url parameter.", httpMethod);
            }
            if (!isFormEncoded && !isMultipart && !hasBody && gotBody) {
                throw methodError("Non-boddy HTTP method cannot contain @Body.");
            }
            if (isFormEncoded && !gotField) {
                throw methodError("Multipart method must contain at least one @Part.");
            }
            return new ServiceMethod<>(this);
        }

        private void parseMethodAnnotation(Annotation annotation) {
            if (annotation instanceof DELETE) {
                parseHttpMethodAndPath("DELETE", ((DELETE) annotation).value(), false);
            } else if (annotation instanceof GET) {
                parseHttpMethodAndPath("GET", ((GET) annotation).value(), false);
            } else if (annotation instanceof HEAD) {
                parseHttpMethodAndPath("HEAD", ((HEAD) annotation).value(), false);
                if (!Void.class.equals(responseType)) {
                    throw methodError("HEAD method must use Void as response type.");
                }
            } else if (annotation instanceof PATCH) {
                parseHttpMethodAndPath("PATCH", ((PATCH) annotation).value(), true);
            } else if (annotation instanceof POST) {
                parseHttpMethodAndPath("POST", ((POST) annotation).value(), true);
            } else if (annotation instanceof PUT) {
                parseHttpMethodAndPath("PUT", ((PUT) annotation).value(), true);
            } else if (annotation instanceof OPTIONS) {
                parseHttpMethodAndPath("OPTIONS", ((OPTIONS) annotation).value(), false);
            } else if (annotation instanceof HTTP) {
                HTTP http = (HTTP) annotation;
                parseHttpMethodAndPath(http.method(), http.path(), http.hasBody());
            } else if (annotation instanceof retrofit3.annotation.http.Headers) {
                String[] headersToParse = ((retrofit3.annotation.http.Headers) annotation).value();
                if (headersToParse.length == 0) {
                    throw methodError("@Headers annotation is empty.");
                }
                headers = parseHeaders(headersToParse);
            } else if (annotation instanceof Multipart) {
                if (isFormEncoded) {
                    throw methodError("Only one encoding annotation is allowed.");
                }
                isMultipart = true;
            } else if (annotation instanceof FormUrlEncoded) {
                if (isMultipart) {
                    throw methodError("Only one encoding annotation is allowed.");
                }
                isFormEncoded = true;
            }
        }

        private ParameterHandler<?> parseParameter(int i, Class parameterType, Class[] paraTypeArguments, Annotation[] annotations) {
            ParameterHandler<?> result = null;
            for (Annotation annotation : annotations) {
                ParameterHandler<?> annotationAction = parseParameterAnnotation(i, parameterType, paraTypeArguments, annotations, annotation);
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
                                                             Annotation[] annotations, Annotation annotation) {
            if (annotation instanceof Url) {
                return parseUrl(i, parameterType);
            } else if (annotation instanceof Path) {
                return parsePath(i, parameterType, annotations, annotation);
            } else if (annotation instanceof Query) {
                return parseQuery(i, parameterType, paraTypeArguments, annotations, annotation);
            } else if (annotation instanceof QueryMap) {
                return parseQueryMap(i, parameterType, paraTypeArguments, annotations, annotation);
            } else if (annotation instanceof Header) {
                return parseHeader(i, parameterType, paraTypeArguments, annotations, annotation);
            } else if (annotation instanceof Field) {
                return parseField(i, parameterType, paraTypeArguments, annotations, annotation);
            } else if (annotation instanceof FieldMap) {
                return parseFieldMap(i, parameterType, paraTypeArguments, annotations, annotation);
            } else if (annotation instanceof Part) {
                return parsePart(i, parameterType, paraTypeArguments, annotations, annotation);
            } else if (annotation instanceof PartMap) {
                return parsePartMap(i, parameterType, paraTypeArguments, annotations, annotation);
            } else if (annotation instanceof Body) {
                return parseBody(i, parameterType, paraTypeArguments, annotations, annotation);
            }

            return null;
        }

        private ParameterHandler<?> parseBody(int i, Class parameterType, Class[] paraTypeArguments,
                                              Annotation[] annotations, Annotation annotation) {
            if (isFormEncoded || isMultipart) {
                throw parameterError(i, "@Body parameters cannot be used with form or multi-part encoding.");
            }
            if (gotBody) {
                throw parameterError(i, "Multiple @Body method annotations found.");
            }

            Converter<?, RequestBody> converter;
            try {
                converter = retrofit.requestBodyConverter(parameterType, annotations, methodAnnotations);
            } catch (RuntimeException e) {
                //Wide exception reange because factories are user code.
                throw parameterError(e, i, "Unable to create @Body converter for %s", parameterType);
            }
            gotBody = true;
            return new ParameterHandler.Body<>(converter);
        }

        private ParameterHandler<?> parsePartMap(int i, Class parameterType, Class[] paraTypeArguments,
                                                 Annotation[] annotations, Annotation annotation) {
            if (!isMultipart) {
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

            Converter<?, RequestBody> valueConverter = retrofit.requestBodyConverter(valueType, annotations, methodAnnotations);

            PartMap partMap = (PartMap) annotation;
            return new ParameterHandler.PartMap<>(valueConverter, partMap.encoding());
        }

        private ParameterHandler<?> parsePart(int i, Class parameterType, Class[] paraTypeArguments,
                                              Annotation[] annotations, Annotation annotation) {
            if (!isMultipart) {
                throw parameterError(i, "@Part parameters can only be used with multipart encoding.");
            }
            Part part = (Part) annotation;
            gotPart = true;

            String partName = part.value();
            if (partName.isEmpty()) {
                if (!MultipartBody.Part.class.isAssignableFrom(parameterType)) {
                    throw parameterError(i, "@Part annotation must supply a name or use MultipartBody.Part parameter type.");
                }
                return ParameterHandler.RawPart.INSTANCE;
            } else {
                Headers headers = Headers.of("Content-Disposition", "form-data; name=\"" + partName + "\"",
                        "Content-Transfer-Encoding", part.encoding());
                if (Iterable.class.isAssignableFrom(parameterType)) {
                    if (null == paraTypeArguments || paraTypeArguments.length == 0) {
                        throw parameterError(i, parameterType.getSimpleName()
                                + " must include generic type (e.g., "
                                + parameterType.getSimpleName()
                                + "<String>)");
                    }
                    Class iterableType = paraTypeArguments[0];
                    Converter<?, RequestBody> converter = retrofit.requestBodyConverter(iterableType, annotations, methodAnnotations);
                    return new ParameterHandler.Part<>(headers, converter).iterable();
                } else if (parameterType.isArray()) {
                    Class<?> arrayComponentType = boxIfPrimitive(parameterType.getComponentType());
                    Converter<?, RequestBody> converter = retrofit.requestBodyConverter(arrayComponentType, annotations, methodAnnotations);
                    return new ParameterHandler.Part<>(headers, converter).array();
                } else if (MultipartBody.Part.class.isAssignableFrom(parameterType)) {
                    throw parameterError(i, "@Part parameters using the MultipartBody.Part must not "
                            + "include a part name in the annotation.");
                } else {
                    Converter<?, RequestBody> converter = retrofit.requestBodyConverter(parameterType, annotations, methodAnnotations);
                    return new ParameterHandler.Part<>(headers, converter);
                }
            }
        }

        private ParameterHandler<?> parseFieldMap(int i, Class parameterType, Class[] paraTypeArguments,
                                                  Annotation[] annotations, Annotation annotation) {
            if (!isFormEncoded) {
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
            Converter<?, String> valueConverter = retrofit.stringConverter(valueType, annotations);

            gotField = true;
            return new ParameterHandler.FieldMap<>(valueConverter, ((FieldMap) annotation).encoded());
        }

        private ParameterHandler<?> parseField(int i, Class parameterType, Class[] paraTypeArguments,
                                               Annotation[] annotations, Annotation annotation) {
            if (!isFormEncoded) {
                throw parameterError(i, "@Field parameters can only be used with form encoding.");
            }
            Field field = (Field) annotation;
            String name = field.value();
            boolean encoded = field.encoded();


            gotField = true;

            if (Iterable.class.isAssignableFrom(parameterType)) {
                if (null == paraTypeArguments || paraTypeArguments.length == 0) {
                    throw parameterError(i, parameterType.getSimpleName()
                            + " must include generic type (e.g.,"
                            + parameterType.getSimpleName()
                            + "<String>");
                }
                Class iterableType = paraTypeArguments[0];
                Converter<?, String> converter = retrofit.stringConverter(iterableType, annotations);
                return new ParameterHandler.Field<>(name, converter, encoded).iterable();
            } else if (parameterType.isArray()) {
                Class<?> arrayComponentType = boxIfPrimitive(parameterType.getComponentType());
                Converter<?, String> converter = retrofit.stringConverter(arrayComponentType, annotations);
                return new ParameterHandler.Field<>(name, converter, encoded).array();
            } else {
                Converter<?, String> converter = retrofit.stringConverter(parameterType, annotations);
                return new ParameterHandler.Field<>(name, converter, encoded);
            }
        }

        private ParameterHandler<?> parseHeader(int i, Class parameterType, Class[] paraTypeArguments,
                                                Annotation[] annotations, Annotation annotation) {
            Header header = (Header) annotation;
            String name = header.value();

            if (Iterable.class.isAssignableFrom(parameterType)) {
                if (null == paraTypeArguments || paraTypeArguments.length == 0) {
                    throw parameterError(i, parameterType + " must include generic type (e.g.,"
                            + parameterType.getSimpleName() + "<String>");
                }
                Class iterableType = paraTypeArguments[0];
                Converter<?, String> converter = retrofit.stringConverter(iterableType, annotations);
                return new ParameterHandler.Header<>(name, converter).iterable();
            } else if (parameterType.isArray()) {
                Class<?> arrayComponentType = boxIfPrimitive(parameterType.getComponentType());
                Converter<?, String> converter = retrofit.stringConverter(arrayComponentType, annotations);
                return new ParameterHandler.Header<>(name, converter).array();
            } else {
                Converter<?, String> converter = retrofit.stringConverter(parameterType, annotations);
                return new ParameterHandler.Header<>(name, converter);
            }
        }

        private ParameterHandler<?> parseQueryMap(int i, Class parameterType, Class[] paraTypeArguments,
                                                  Annotation[] annotations, Annotation annotation) {
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
            Converter<?, String> valueConverter = retrofit.stringConverter(valueType, annotations);
            return new ParameterHandler.QueryMap<>(valueConverter, ((QueryMap) annotation).encoded());
        }

        private ParameterHandler<?> parseQuery(int i, Class parameterType, Class[] paraTypeArguments,
                                               Annotation[] annotations, Annotation annotation) {
            Query query = (Query) annotation;
            String name = query.value();
            boolean encoded = query.encoded();
            gotQuery = true;
            if (Iterable.class.isAssignableFrom(parameterType)) {
                if (null == paraTypeArguments || paraTypeArguments.length == 0) {
                    throw parameterError(i, parameterType.getSimpleName()
                            + " must include generic type (e.g.,"
                            + parameterType.getSimpleName() + "<String>)");
                }
                Converter<?, String> converter = retrofit.stringConverter(paraTypeArguments[0], annotations);
                return new ParameterHandler.Query<>(name, converter, encoded).iterable();
            } else if (parameterType.isArray()) {
                Class<?> arrayComponentType = boxIfPrimitive(parameterType.getComponentType());
                Converter<?, String> converter = retrofit.stringConverter(arrayComponentType, annotations);
                return new ParameterHandler.Query<>(name, converter, encoded).array();
            } else {
                Converter<?, String> converter = retrofit.stringConverter(parameterType, annotations);
                return new ParameterHandler.Query<>(name, converter, encoded);
            }
        }

        private ParameterHandler<?> parsePath(int i, Class parameterType, Annotation[] annotations, Annotation annotation) {
            if (gotQuery) {
                throw parameterError(i, "A @Path parameter must not come after a @Query.");
            }
            if (gotUrl) {
                throw parameterError(i, "@Path parameters may not be used with @Url");
            }
            if (relativeUrl == null) {
                throw parameterError(i, "@Path can only be used with relative url on @%s", httpMethod);
            }
            gotPath = true;

            Path path = (Path) annotation;
            String name = path.value();
            validatePathName(i, name);

            Converter<?, String> converter = retrofit.stringConverter(parameterType, annotations);
            return new ParameterHandler.Path<>(name, converter, path.encoded());
        }

        private void validatePathName(int i, String name) {
            if (!PARAM_NAME_REGEX.matcher(name).matches()) {
                throw parameterError(i, "@Path parameter name must match %s. Found: %s",
                        PARAM_URL_REGEX.pattern(), name);
            }
            //Verify URL replacement name is actually present in the URL path.
            if (!relativeUrlParamNames.contains(name)) {
                throw parameterError(i, "URL \"%s\" does not contains \"{%s}\".", relativeUrl, name);
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
            if (relativeUrl != null) {
                throw parameterError(i, "@Url cannot be used with @%s URL", httpMethod);
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

        private Headers parseHeaders(String[] headers) {
            Headers.Builder builder = new Headers.Builder();
            for (String header : headers) {
                int colon = header.indexOf(':');
                if (colon == -1 || colon == 0 || colon == header.length() - 1) {
                    throw methodError("@Headers value must be in the form \"Name: Value\". Found:\"%s\"", header);
                }
                String headerName = header.substring(0, colon);
                String headerValue = header.substring(colon + 1).trim();
                if ("Content-Type".equalsIgnoreCase(headerName)) {
                    contentType = MediaType.parse(headerValue);
                } else {
                    builder.add(headerName, headerValue);
                }
            }
            return builder.build();
        }

        private void parseHttpMethodAndPath(String httpMethod, String value, boolean hasBody) {
            if (this.httpMethod != null) {
                throw methodError("Only one HTTP method is allowed. Found: %s and %s.",
                        this.httpMethod, httpMethod);
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
                    throw methodError("URL query string \"%s\" must not have replace block. "
                            + "For dynamic query parameters user @Query.", queryParams);
                }
            }

            this.relativeUrl = value;
            this.relativeUrlParamNames = parsePathParameters(value);
        }

        private Converter<ResponseBody, T> createResponseConverter() {
            Annotation[] annotations = methodAnnotations;
            try {
                return retrofit.responseBodyConverter(responseType, annotations);
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
                        responseTypeArguments, methodAnnotations);
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
                    + apiName
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
