package retrofit3.adapter.rxjava;

import java.lang.reflect.Type;

import retrofit3.call.Call;
import retrofit3.call.adapter.CallAdapter;
import rx.Observable;
import rx.Single;


final class SingleHelper {
    static CallAdapter<Single<?>> makeSingle(final CallAdapter<Observable<?>> callAdapter) {
        return new CallAdapter<Single<?>>() {
            @Override public Class responseType() {
                return callAdapter.responseType();
            }

            @Override public <R> Single<?> adapt(Call<R> call) {
                Observable<?> observable = callAdapter.adapt(call);
                return observable.toSingle();
            }
        };
    }
}
