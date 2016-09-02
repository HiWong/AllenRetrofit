package retrofit3;

import java.io.IOException;
import java.lang.annotation.Annotation;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit3.annotation.bean.MethodAnnotationBean;
import retrofit3.annotation.bean.parameter.ParaAnnotationBean;

/**
 * Created by allen on 16-8-28.
 */
public interface Converter<F,T> {

    T convert(F value) throws IOException;

    abstract class Factory{
        public Converter<ResponseBody,?>responseBodyConverter(Class responseType, MethodAnnotationBean methodAnnotationBean,Retrofit retrofit){
            return null;
        }

        public Converter<?, RequestBody>requestBodyConverter(Class parameterType, ParaAnnotationBean[]paraAnnotationBeans,
                                                             MethodAnnotationBean methodAnnotationBean, Retrofit retrofit){
            return null;
        }

        public Converter<?,String>stringConverter(Class parameterType, ParaAnnotationBean[]paraAnnotationBeans, Retrofit retrofit){
            return null;
        }
    }

}
