package retrofit3.call.adapter.impl;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.concurrent.Executor;

import okhttp3.Request;
import retrofit3.Callback;
import retrofit3.Response;
import retrofit3.Retrofit;
import retrofit3.call.Call;
import retrofit3.call.adapter.CallAdapter;

/**
 * Created by allen on 16-8-28.
 */
public final class ExecutorCallAdapterFactory extends CallAdapter.Factory{

    final Executor callbackExecutor;

    public ExecutorCallAdapterFactory(Executor callbackExecutor){
        this.callbackExecutor=callbackExecutor;
    }

    @Override
    public CallAdapter<?> get(Class rawReturnType, Class[]returnTypeArguments,final Class responseType,
                              Class[]responseTypeArguments, Annotation[] annotations, Retrofit retrofit) {
        if(rawReturnType!= Call.class){
            return null;
        }
        return new CallAdapter<Call<?>>(){
            @Override
            public Class responseType() {
                return responseType;
            }

            @Override
            public <R> Call<?> adapt(Call<R> call) {
                return new ExecutorCallbackCall<>(callbackExecutor,call);
            }
        };
    }

    static final class ExecutorCallbackCall<T> implements Call<T> {
        final Executor callbackExecutor;
        final Call<T> delegate;

        ExecutorCallbackCall(Executor callbackExecutor, Call<T> delegate) {
            this.callbackExecutor = callbackExecutor;
            this.delegate = delegate;
        }

        @Override public void enqueue(final Callback<T> callback) {
            if (callback == null) throw new NullPointerException("callback == null");

            delegate.enqueue(new Callback<T>() {
                @Override public void onResponse(final Call<T> call, final Response<T> response) {
                    callbackExecutor.execute(new Runnable() {
                        @Override public void run() {
                            if (delegate.isCanceled()) {
                                // Emulate OkHttp's behavior of throwing/delivering an IOException on cancellation.
                                callback.onFailure(call, new IOException("Canceled"));
                            } else {
                                callback.onResponse(call, response);
                            }
                        }
                    });
                }

                @Override public void onFailure(final Call<T> call, final Throwable t) {
                    callbackExecutor.execute(new Runnable() {
                        @Override public void run() {
                            callback.onFailure(call, t);
                        }
                    });
                }
            });
        }

        @Override public boolean isExecuted() {
            return delegate.isExecuted();
        }

        @Override public Response<T> execute() throws IOException {
            return delegate.execute();
        }

        @Override public void cancel() {
            delegate.cancel();
        }

        @Override public boolean isCanceled() {
            return delegate.isCanceled();
        }

        @SuppressWarnings("CloneDoesntCallSuperClone") // Performing deep clone.
        @Override public Call<T> clone() {
            return new ExecutorCallbackCall<>(callbackExecutor, delegate.clone());
        }

        @Override public Request request() {
            return delegate.request();
        }
    }

}
