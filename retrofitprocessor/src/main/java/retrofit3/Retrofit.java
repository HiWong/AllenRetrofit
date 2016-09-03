/*
 * Copyright (C) 2012 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package retrofit3;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.lang.model.type.TypeMirror;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit3.annotation.bean.ApiBean;
import retrofit3.annotation.bean.MethodAnnotationBean;
import retrofit3.annotation.bean.RawMethodAnnotationBean;
import retrofit3.annotation.bean.parameter.ParaAnnotationBean;
import retrofit3.call.OkHttpCall;
import retrofit3.call.adapter.CallAdapter;
import retrofit3.converter.builtin.BuiltInConverters;

import static java.util.Collections.unmodifiableList;
import static retrofit3.Utils.checkNotNull;

/**
 * Retrofit adapts a Java interface to HTTP calls by using annotations on the declared methods to
 * define how requests are made. Create instances using {@linkplain Builder
 * the builder} and pass your interface to create() to generate an implementation.
 * <p/>
 * For example,
 * <pre>{@code
 * Retrofit retrofit = new Retrofit.Builder()
 *     .baseUrl("https://api.example.com/")
 *     .addConverterFactory(GsonConverterFactory.create())
 *     .build();
 *
 * MyApi api = retrofit.create(MyApi.class);
 * Response<User> user = api.getUser().execute();
 * }</pre>
 *
 * @author Bob Lee (bob@squareup.com)
 * @author Jake Wharton (jw@squareup.com)
 */
public final class Retrofit {
    //private final Map<Method, ServiceMethod> serviceMethodCache = new LinkedHashMap<>();
    private final Map<String,ServiceMethod>serivceMethodCache=new HashMap<>();
    private final ReadWriteLock methodCacheLock=new ReentrantReadWriteLock();

    private final okhttp3.Call.Factory callFactory;
    private final HttpUrl baseUrl;
    private final List<Converter.Factory> converterFactories;
    private final List<CallAdapter.Factory> adapterFactories;
    private final Executor callbackExecutor;
    private final boolean validateEagerly;

    Retrofit(okhttp3.Call.Factory callFactory, HttpUrl baseUrl,
             List<Converter.Factory> converterFactories, List<CallAdapter.Factory> adapterFactories,
             Executor callbackExecutor, boolean validateEagerly) {
        this.callFactory = callFactory;
        this.baseUrl = baseUrl;
        this.converterFactories = unmodifiableList(converterFactories); // Defensive copy at call site.
        this.adapterFactories = unmodifiableList(adapterFactories); // Defensive copy at call site.
        this.callbackExecutor = callbackExecutor;
        this.validateEagerly = validateEagerly;
    }

    public ServiceMethod getServiceMethod(String methodDeclaration){
        methodCacheLock.readLock().lock();
        try{
            return serivceMethodCache.get(methodDeclaration);
        }finally {
            methodCacheLock.readLock().unlock();
        }
    }

    public void addServiceMethod(String methodDeclaration,ServiceMethod serviceMethod){
        methodCacheLock.writeLock().lock();
        try{
            serivceMethodCache.put(methodDeclaration,serviceMethod);
        }finally {
            methodCacheLock.writeLock().unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T adapt(ServiceMethod serviceMethod,Object...args){
        OkHttpCall okHttpCall=new OkHttpCall<>(serviceMethod,args);
        Object ret=serviceMethod.callAdapter.adapt(okHttpCall);
        return (T)ret;
    }

    public <T> T adapt(String apiName,String methodDeclaration,Class rawReturnType,Class[]returnTypeArguments,
                       Class responseType,Class[]responseTypeArguments,RawMethodAnnotationBean rawBean,
                       ParaAnnotationBean[][]paraAnnotationBeansArray,Class[]parameterTypes,Class[][]parameterTypeArguments,
                       Object...args){
        ServiceMethod serviceMethod=new ServiceMethod.Builder(this,apiName,methodDeclaration,rawReturnType,returnTypeArguments,
                responseType,responseTypeArguments,rawBean,paraAnnotationBeansArray,parameterTypes,parameterTypeArguments)
                .build();
        addServiceMethod(methodDeclaration,serviceMethod);
        return adapt(serviceMethod,args);
    }

    /*
    public ServiceMethod loadServiceMethod(ApiBean apiBean,String methodDeclaration){
        //actually we could use TypeMirror here.
        //so actually we do not need to use String and String array which is generated from TypeMirror and so on
        //but if we use TypeMirror directly, it would be slower than using reflection
        //So actually we have to parse the TypeMirrors,TypeElement,VariableElement and so on
        ServiceMethod serviceMethod=new ServiceMethod.Builder(this,)
    }
    */

    /**
     * The factory used to create {@linkplain okhttp3.Call OkHttp calls} for sending a HTTP requests.
     * Typically an instance of {@link OkHttpClient}.
     */
    public okhttp3.Call.Factory callFactory() {
        return callFactory;
    }

    /**
     * The API base URL.
     */
    public HttpUrl baseUrl() {
        return baseUrl;
    }

    /**
     * Returns a list of the factories tried when creating a
     * {@linkplain #callAdapter(Class, Class[], Class, Class[], MethodAnnotationBean)} call adapter}.
     */
    public List<CallAdapter.Factory> callAdapterFactories() {
        return adapterFactories;
    }

    /**
     * Returns the {@link CallAdapter} for {@code returnType} from the available {@linkplain
     * #callAdapterFactories() factories}.
     *
     * @throws IllegalArgumentException if no call adapter available for {@code type}.
     */
    public CallAdapter<?> callAdapter(Class rawReturnType, Class[] returnTypeArguments, Class responseType,
                                      Class[] responseTypeArguments, MethodAnnotationBean methodAnnotationBean) {
        return nextCallAdapter(null, rawReturnType, returnTypeArguments, responseType, responseTypeArguments, methodAnnotationBean);
    }

    /**
     * Returns the {@link CallAdapter} for {@code returnType} from the available {@linkplain
     * #callAdapterFactories() factories} except {@code skipPast}.
     *
     * @throws IllegalArgumentException if no call adapter available for {@code type}.
     */
    public CallAdapter<?> nextCallAdapter(CallAdapter.Factory skipPast, Class rawReturnType, Class[] returnTypeArguments,
                                          Class responseType, Class[] responseTypeArguments, MethodAnnotationBean methodAnnotationBean) {
        checkNotNull(rawReturnType, "returnType == null");
        checkNotNull(methodAnnotationBean, "methodAnnotationBean == null");

        int start = adapterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = adapterFactories.size(); i < count; i++) {
            CallAdapter<?> adapter = adapterFactories.get(i).get(rawReturnType, returnTypeArguments, responseType,
                    responseTypeArguments, methodAnnotationBean, this);
            if (adapter != null) {
                return adapter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate call adapter for ")
                .append(rawReturnType)
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(adapterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = adapterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(adapterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    public List<Converter.Factory> converterFactories() {
        return converterFactories;
    }

    /**
     * Returns a {@link Converter} for {@code type} to {@link RequestBody} from the available
     * {@linkplain #converterFactories() factories}.
     *
     * @throws IllegalArgumentException if no converter available for {@code type}.
     */
    public <T> Converter<T, RequestBody> requestBodyConverter(Class type,
                                                              ParaAnnotationBean[]paraAnnotationBeans, MethodAnnotationBean methodAnnotationBean) {
        return nextRequestBodyConverter(null, type, paraAnnotationBeans, methodAnnotationBean);
    }

    /**
     * Returns a {@link Converter} for {@code type} to {@link RequestBody} from the available
     * {@linkplain #converterFactories() factories} except {@code skipPast}.
     *
     * @throws IllegalArgumentException if no converter available for {@code type}.
     */
    public <T> Converter<T, RequestBody> nextRequestBodyConverter(Converter.Factory skipPast,
                                                                  Class type, ParaAnnotationBean[]paraAnnotationBeans, MethodAnnotationBean methodAnnotationBean) {
        checkNotNull(type, "type == null");
        checkNotNull(paraAnnotationBeans, "paraAnnotationBeans == null");
        checkNotNull(methodAnnotationBean, "methodAnnotationBean == null");

        int start = converterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            Converter.Factory factory = converterFactories.get(i);
            Converter<?, RequestBody> converter =
                    factory.requestBodyConverter(type, paraAnnotationBeans, methodAnnotationBean, this);
            if (converter != null) {
                //noinspection unchecked
                return (Converter<T, RequestBody>) converter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate RequestBody converter for ")
                .append(type)
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * Returns a {@link Converter} for {@link ResponseBody} to {@code type} from the available
     * {@linkplain #converterFactories() factories}.
     *
     * @throws IllegalArgumentException if no converter available for {@code type}.
     */
    public <T> Converter<ResponseBody, T> responseBodyConverter(Class responseType, MethodAnnotationBean methodAnnotationBean) {
        return nextResponseBodyConverter(null, responseType, methodAnnotationBean);
    }

    /**
     * Returns a {@link Converter} for {@link ResponseBody} to {@code type} from the available
     * {@linkplain #converterFactories() factories} except {@code skipPast}.
     *
     * @throws IllegalArgumentException if no converter available for {@code type}.
     */
    public <T> Converter<ResponseBody, T> nextResponseBodyConverter(Converter.Factory skipPast,
                                                                    Class responseType,MethodAnnotationBean methodAnnotationBean) {
        checkNotNull(responseType, "type == null");
        checkNotNull(methodAnnotationBean, "methodAnnotationBean == null");

        int start = converterFactories.indexOf(skipPast) + 1;
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            Converter<ResponseBody, ?> converter =
                    converterFactories.get(i).responseBodyConverter(responseType, methodAnnotationBean, this);
            if (converter != null) {
                //noinspection unchecked
                return (Converter<ResponseBody, T>) converter;
            }
        }

        StringBuilder builder = new StringBuilder("Could not locate ResponseBody converter for ")
                .append(responseType)
                .append(".\n");
        if (skipPast != null) {
            builder.append("  Skipped:");
            for (int i = 0; i < start; i++) {
                builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
            }
            builder.append('\n');
        }
        builder.append("  Tried:");
        for (int i = start, count = converterFactories.size(); i < count; i++) {
            builder.append("\n   * ").append(converterFactories.get(i).getClass().getName());
        }
        throw new IllegalArgumentException(builder.toString());
    }

    /**
     * Returns a {@link Converter} for {@code type} to {@link String} from the available
     * {@linkplain #converterFactories() factories}.
     * 由于GsonConverterFactory并未实现stringConverter(...),所以这里其实获得的string converter其实是BuiltInConverters提供的
     */
    public <T> Converter<T, String> stringConverter(Class type, ParaAnnotationBean[]paraAnnotationBeans) {
        checkNotNull(type, "type == null");
        checkNotNull(paraAnnotationBeans, "paraAnnotationBeans == null");

        for (int i = 0, count = converterFactories.size(); i < count; i++) {
            Converter<?, String> converter =
                    converterFactories.get(i).stringConverter(type, paraAnnotationBeans, this);
            if (converter != null) {
                //noinspection unchecked
                return (Converter<T, String>) converter;
            }
        }

        // Nothing matched. Resort to default converter which just calls toString().
        //noinspection unchecked
        return (Converter<T, String>) BuiltInConverters.ToStringConverter.INSTANCE;
    }

    /**
     * The executor used for {@link Callback} methods on a {@link retrofit3.call.Call}. This may be {@code null},
     * in which case callbacks should be made synchronously on the background thread.
     */
    public Executor callbackExecutor() {
        return callbackExecutor;
    }

    /**
     * Build a new {@link Retrofit}.
     * <p/>
     * Calling {@link #baseUrl} is required before calling {@link #build()}. All other methods
     * are optional.
     */
    public static final class Builder {
        private Platform platform;
        private okhttp3.Call.Factory callFactory;
        private HttpUrl baseUrl;
        private List<Converter.Factory> converterFactories = new ArrayList<>();
        private List<CallAdapter.Factory> adapterFactories = new ArrayList<>();
        private Executor callbackExecutor;
        private boolean validateEagerly;

        Builder(Platform platform) {
            this.platform = platform;
            // Add the built-in converter factory first. This prevents overriding its behavior but also
            // ensures correct behavior when using converters that consume all types.
            converterFactories.add(new BuiltInConverters());
        }

        public Builder() {
            this(Platform.get());
        }

        /**
         * The HTTP client used for requests.
         * <p/>
         * This is a convenience method for calling {@link #callFactory}.
         * <p/>
         * Note: This method <b>does not</b> make a defensive copy of {@code client}. Changes to its
         * settings will affect subsequent requests. Pass in a {@linkplain OkHttpClient#clone() cloned}
         * instance to prevent this if desired.
         */
        public Builder client(OkHttpClient client) {
            return callFactory(checkNotNull(client, "client == null"));
        }

        /**
         * Specify a custom call factory for creating {@link retrofit3.call.Call} instances.
         * <p/>
         * Note: Calling {@link #client} automatically sets this value.
         */
        public Builder callFactory(okhttp3.Call.Factory factory) {
            this.callFactory = checkNotNull(factory, "factory == null");
            return this;
        }

        /**
         * Set the API base URL.
         *
         * @see #baseUrl(HttpUrl)
         */
        public Builder baseUrl(String baseUrl) {
            checkNotNull(baseUrl, "baseUrl == null");
            HttpUrl httpUrl = HttpUrl.parse(baseUrl);
            if (httpUrl == null) {
                throw new IllegalArgumentException("Illegal URL: " + baseUrl);
            }
            return baseUrl(httpUrl);
        }

        /**
         * Set the API base URL.
         * <p/>
         * The specified endpoint values (such as with {@link retrofit3.annotation.http.GET @GET}) are resolved against this
         * value using {@link HttpUrl#resolve(String)}. The behavior of this matches that of an
         * {@code <a href="">} link on a website resolving on the current URL.
         * <p/>
         * <b>Base URLs should always end in {@code /}.</b>
         * <p/>
         * A trailing {@code /} ensures that endpoints values which are relative paths will correctly
         * append themselves to a base which has path components.
         * <p/>
         * <b>Correct:</b><br>
         * Base URL: http://example.com/api/<br>
         * Endpoint: foo/bar/<br>
         * Result: http://example.com/api/foo/bar/
         * <p/>
         * <b>Incorrect:</b><br>
         * Base URL: http://example.com/api<br>
         * Endpoint: foo/bar/<br>
         * Result: http://example.com/foo/bar/
         * <p/>
         * This method enforces that {@code baseUrl} has a trailing {@code /}.
         * <p/>
         * <b>Endpoint values which contain a leading {@code /} are absolute.</b>
         * <p/>
         * Absolute values retain only the host from {@code baseUrl} and ignore any specified path
         * components.
         * <p/>
         * Base URL: http://example.com/api/<br>
         * Endpoint: /foo/bar/<br>
         * Result: http://example.com/foo/bar/
         * <p/>
         * Base URL: http://example.com/<br>
         * Endpoint: /foo/bar/<br>
         * Result: http://example.com/foo/bar/
         * <p/>
         * <b>Endpoint values may be a full URL.</b>
         * <p/>
         * Values which have a host replace the host of {@code baseUrl} and values also with a scheme
         * replace the scheme of {@code baseUrl}.
         * <p/>
         * Base URL: http://example.com/<br>
         * Endpoint: https://github.com/square/retrofit/<br>
         * Result: https://github.com/square/retrofit/
         * <p/>
         * Base URL: http://example.com<br>
         * Endpoint: //github.com/square/retrofit/<br>
         * Result: http://github.com/square/retrofit/ (note the scheme stays 'http')
         */
        public Builder baseUrl(HttpUrl baseUrl) {
            checkNotNull(baseUrl, "baseUrl == null");
            List<String> pathSegments = baseUrl.pathSegments();
            if (!"".equals(pathSegments.get(pathSegments.size() - 1))) {
                throw new IllegalArgumentException("baseUrl must end in /: " + baseUrl);
            }
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Add converter factory for serialization and deserialization of objects.
         */
        public Builder addConverterFactory(Converter.Factory factory) {
            converterFactories.add(checkNotNull(factory, "factory == null"));
            return this;
        }

        /**
         * Add a call adapter factory for supporting service method return types other than {@link
         * retrofit3.call.Call}.
         */
        public Builder addCallAdapterFactory(CallAdapter.Factory factory) {
            adapterFactories.add(checkNotNull(factory, "factory == null"));
            return this;
        }

        /**
         * The executor on which {@link Callback} methods are invoked when returning {@link retrofit3.call.Call} from
         * your service method.
         * <p/>
         * Note: {@code executor} is not used for {@linkplain #addCallAdapterFactory custom method
         * return types}.
         */
        public Builder callbackExecutor(Executor executor) {
            this.callbackExecutor = checkNotNull(executor, "executor == null");
            return this;
        }

        /**
         * When calling {@link } on the resulting {@link Retrofit} instance, eagerly validate
         * the configuration of all methods in the supplied interface.
         */
        public Builder validateEagerly(boolean validateEagerly) {
            this.validateEagerly = validateEagerly;
            return this;
        }

        /**
         * Create the {@link Retrofit} instance using the configured values.
         * <p/>
         * Note: If neither {@link #client} nor {@link #callFactory} is called a default {@link
         * OkHttpClient} will be created and used.
         */
        public Retrofit build() {
            if (baseUrl == null) {
                throw new IllegalStateException("Base URL required.");
            }

            okhttp3.Call.Factory callFactory = this.callFactory;
            if (callFactory == null) {
                callFactory = new OkHttpClient();
            }

            Executor callbackExecutor = this.callbackExecutor;
            if (callbackExecutor == null) {
                callbackExecutor = platform.defaultCallbackExecutor();
            }

            // Make a defensive copy of the adapters and add the default Call adapter.
            List<CallAdapter.Factory> adapterFactories = new ArrayList<>(this.adapterFactories);
            adapterFactories.add(platform.defaultCallAdapterFactory(callbackExecutor));

            // Make a defensive copy of the converters.
            List<Converter.Factory> converterFactories = new ArrayList<>(this.converterFactories);

            return new Retrofit(callFactory, baseUrl, converterFactories, adapterFactories,
                    callbackExecutor, validateEagerly);
        }
    }
}
