package retrofit3.call.adapter;

import java.lang.annotation.Annotation;

import retrofit3.Retrofit;
import retrofit3.call.Call;

/**
 * Created by allen on 16-8-28.
 */
public interface CallAdapter<T> {

    Class responseType();

    <R> T adapt(Call<R> call);

    abstract class Factory{
        //public abstract CallAdapter<?>get(Class returnType, Annotation[]annotations, Retrofit retrofit);
        //public abstract CallAdapter<?>get(Class rawReturnType,Class responseType,Annotation[]annotations,Retrofit retrofit);
        //public abstract CallAdapter<?>get(Class rawReturnType,Class responseType,Class[]responseTypeArguments,Annotation[]annotations,Retrofit retrofit);
        public abstract CallAdapter<?>get(Class rawReturnType,Class[]returnTypeArguments,Class responseType,
                                          Class[]responseTypeArguments,Annotation[]annotations,Retrofit retrofit);
    }

}
