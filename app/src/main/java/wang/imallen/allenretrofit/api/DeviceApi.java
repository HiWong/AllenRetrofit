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
import retrofit3.annotation.http.HTTP;
import retrofit3.annotation.http.Header;
import retrofit3.annotation.http.Headers;
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

    //@Executor
    //java.util.concurrent.Executor executor=java.util.concurrent.Executors.newFixedThreadPool(3);

    //String getDeviceName();
    //int getDeviceWeight();

    //@FormUrlEncoded
    @GET(DeviceConfig.RECOMMANDAPPURL + ServiceAction.RECOMAND)
    Observable<AppResponse> getRecommandAppList(
            @Query("page") String page, @Query("code") String code, @Query("version") String version);

    @FormUrlEncoded
    @GET("/device/list")
    retrofit3.call.Call<AppResponse>getDeviceList(@Query("type")String type);


    @FormUrlEncoded
    @GET("/device/name")
    CustomCall<AppResponse>getDeviceNames(@Query("type")String type,@Query("name1")String name1,@Query("name2")String name2,
                                          @Query("name3")String name3,@Query("name4")String name4,@Query("name5")String name5,
                                          @Query("name6")String name6,@Query("name7")String name7,@Query("name8")String name8,
                                          @Query("name9")String name9,@Query("name10")String name10);

    @Headers("Cache-Control: max-age=86400")
    //@GET("/device/name")
    @HTTP(method="GET",path="/device/name",hasBody = false)
    Observable<AppResponse>getDeviceNames(@Query("type")String type,@Query("name1")String name1,@Query("name2")String name2,
                                          @Query("name3")String name3,@Query("name4")String name4,@Query("name5")String name5,
                                          @Query("name6")String name6,@Query("name7")String name7,@Query("name8")String name8,
                                          @Query("name9")String name9,@Query("name10")String name10,@Query("name11")String name11,
                                          @Query("name12")String name12,@Query("name13")String name13,@Query("name14")String name14,
                                          @Query("name15")String name15,@Query("name16")String name16,@Query("name17")String name17,
                                          @Query("name18")String name18,@Query("name19")String name19,@Query("name20")String name20,
                                          @Query("name21")String name21,@Query("name22")String name22,@Query("name23")String name23,
                                         @Query("name24")String name24,@Query("name25")String name25,@Query("name26")String name26,
                                        @Query("name27")String name27,@Query("name28")String name28,@Query("name29")String name29);


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
