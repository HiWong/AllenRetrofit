package retrofit3.call.adapter.impl;
import java.lang.annotation.Annotation;
import retrofit3.Retrofit;
import retrofit3.call.Call;
import retrofit3.call.adapter.CallAdapter;

/**
 * Creates call adapters for that uses the same thread for both I/O and application-level
 * callbacks. For synchronous calls this is the application thread making the request; for
 * asynchronous calls this is a thread provided by OkHttp's dispatcher.
 */
public final class DefaultCallAdapterFactory extends CallAdapter.Factory {
    public static final CallAdapter.Factory INSTANCE = new DefaultCallAdapterFactory();

    @Override
    public CallAdapter<?> get(Class rawReturnType, Class[] returnTypeArguments, final Class responseType, Class[] responseTypeArguments, Annotation[] annotations, Retrofit retrofit) {
        if (rawReturnType != Call.class) {
            return null;
        }
        return new CallAdapter<Call<?>>() {
            @Override
            public <R> Call<?> adapt(Call<R> call) {
                return call;
            }

            @Override
            public Class responseType() {
                return responseType;
            }
        };
    }
}
