package retrofit3.converter.builtin;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit3.Converter;
import retrofit3.Retrofit;
import retrofit3.Utils;
import retrofit3.annotation.bean.MethodAnnotationBean;
import retrofit3.annotation.bean.parameter.ParaAnnotationBean;
import retrofit3.annotation.http.Streaming;

/**
 * Created by allen on 16-8-28.
 */
public class BuiltInConverters extends Converter.Factory{

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Class responseType, MethodAnnotationBean methodAnnotationBean, Retrofit retrofit) {
        if(responseType==ResponseBody.class){
            //if(Utils.isAnnotationPresent(methodAnnotationBean,Streaming.class)){
            if(methodAnnotationBean.isStreaming()){
                return StreamingResponseBodyConverter.INSTANCE;
            }
            return BufferingResponseBodyConverter.INSTANCE;
        }
        if(responseType==Void.class){
            return VoidResponseBodyConverter.INSTANCE;
        }
        return null;
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Class parameterType, ParaAnnotationBean[]paraAnnotationBeans,
                                                          MethodAnnotationBean methodAnnotationBean, Retrofit retrofit) {
        if(RequestBody.class.isAssignableFrom(parameterType)){
            return RequestBodyConverter.INSTANCE;
        }
        return null;
    }

    @Override
    public Converter<?, String> stringConverter(Class parameterType, ParaAnnotationBean[]paraAnnotationBeans, Retrofit retrofit) {
        if(parameterType==String.class){
            return ToStringConverter.INSTANCE;
        }
        return null;
    }


    public static final class RequestBodyConverter implements Converter<RequestBody,RequestBody>{
        public static final RequestBodyConverter INSTANCE=new RequestBodyConverter();

        @Override
        public RequestBody convert(RequestBody value) throws IOException{
            return value;
        }
    }

    public static final class VoidResponseBodyConverter implements Converter<ResponseBody,Void>{
        public static final VoidResponseBodyConverter INSTANCE=new VoidResponseBodyConverter();

        @Override
        public Void convert(ResponseBody value) throws IOException {
            value.close();
            return null;
        }
    }

    public static final class StreamingResponseBodyConverter implements Converter<ResponseBody,ResponseBody>{
        public static final StreamingResponseBodyConverter INSTANCE=new StreamingResponseBodyConverter();

        @Override
        public ResponseBody convert(ResponseBody value) throws IOException{
            return value;
        }
    }

    public static final class BufferingResponseBodyConverter implements Converter<ResponseBody,ResponseBody>{
        public static final BufferingResponseBodyConverter INSTANCE=new BufferingResponseBodyConverter();

        @Override
        public ResponseBody convert(ResponseBody value) throws IOException{
            try{
                //Buffer the entire body to avoid future I/O.
                return Utils.buffer(value);
            }finally {
                value.close();
            }
        }
    }

    public static final class ToStringConverter implements Converter<Object,String>{
        public static final ToStringConverter INSTANCE=new ToStringConverter();

        @Override
        public String convert(Object value){
            return value.toString();
        }
    }


}
