package wang.imallen.allenretrofit.api;



import java.io.IOException;
import java.io.Serializable;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit3.Converter;
import retrofit3.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit3.annotation.config.CallAdapterFactories;
import retrofit3.annotation.config.CallFactory;
import retrofit3.annotation.config.ConverterFactories;
import retrofit3.Callback;
import retrofit3.Response;
import retrofit3.annotation.config.BaseConfig;
import retrofit3.annotation.config.Executor;
import retrofit3.annotation.http.FormUrlEncoded;
import retrofit3.annotation.http.GET;
import retrofit3.annotation.http.Query;
import retrofit3.call.adapter.CallAdapter;
import retrofit3.converter.gson.GsonConverterFactory;
import rx.Observable;
import wang.imallen.allenretrofit.bean.AppResponse;
import wang.imallen.allenretrofit.config.DeviceConfig;
import wang.imallen.allenretrofit.config.ServiceAction;

/**
 * Created by allen on 16-8-27.
 */
@BaseConfig(baseUrl="http://www.baidu.com",validateEagerly = false)
public interface DeviceApi {

    @CallFactory
    Call.Factory callFactory=new OkHttpClient();

    @CallAdapterFactories
    CallAdapter.Factory[]callAdapterFactories={RxJavaCallAdapterFactory.create()};

    @ConverterFactories
    Converter.Factory[]converterFactories={GsonConverterFactory.create()};

    @Executor
    java.util.concurrent.Executor executor=java.util.concurrent.Executors.newFixedThreadPool(3);

    //String getDeviceName();
    //int getDeviceWeight();

    @FormUrlEncoded
    @GET(DeviceConfig.RECOMMANDAPPURL + ServiceAction.RECOMAND)
    Observable<AppResponse> getRecommandAppList(
            @Query(value="page",encoded=false) String page, @Query("code") String code, @Query("version") String version);

    @FormUrlEncoded
    @GET("/device/list")
    retrofit3.call.Call<AppResponse>getDeviceList(@Query("type")String type);

    @FormUrlEncoded
    @GET("/device/name")
    CustomCall<AppResponse>getDeviceNames(@Query("type")String type);



}


class CustomCall<T> extends TopCall<T>{


}

class TopCall<T> implements retrofit3.call.Call<T>,Serializable{
    @Override
    public void cancel() {

    }

    @Override
    public Response<T> execute() throws IOException {
        return null;
    }

    @Override
    public void enqueue(Callback<T> callback) {

    }

    @Override
    public boolean isExecuted() {
        return false;
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public retrofit3.call.Call<T> clone() {
        return null;
    }

    @Override
    public Request request() {
        return null;
    }
}
