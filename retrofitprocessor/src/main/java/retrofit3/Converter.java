package retrofit3;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by allen on 16-8-28.
 */
public interface Converter<F,T> {

    T convert(F value) throws IOException;

    abstract class Factory{
        public Converter<ResponseBody,?>responseBodyConverter(Class responseType, Annotation[]annotations,Retrofit retrofit){
            return null;
        }

        public Converter<?, RequestBody>requestBodyConverter(Class parameterType,Annotation[]parameterAnnotations,
                                                             Annotation[]methodAnnotations,Retrofit retrofit){
            return null;
        }

        public Converter<?,String>stringConverter(Class parameterType,Annotation[]annotations,Retrofit retrofit){
            return null;
        }
    }

}
