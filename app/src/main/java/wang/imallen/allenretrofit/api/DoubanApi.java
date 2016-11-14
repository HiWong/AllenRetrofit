package wang.imallen.allenretrofit.api;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import retrofit3.Converter;
import retrofit3.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit3.annotation.config.BaseConfig;
import retrofit3.annotation.config.CallAdapterFactories;
import retrofit3.annotation.config.CallFactory;
import retrofit3.annotation.config.ConverterFactories;
import retrofit3.annotation.http.GET;
import retrofit3.annotation.http.Header;
import retrofit3.annotation.http.Query;
import retrofit3.call.adapter.CallAdapter;
import retrofit3.converter.gson.GsonConverterFactory;
import rx.Observable;
import wang.imallen.allenretrofit.bean.douban.DouBanPlayList;

/**
 * Created by allen on 16-9-4.
 */
@BaseConfig(baseUrl="https://api.douban.com/",validateEagerly = false)
public interface DoubanApi {

    @CallFactory
    Call.Factory callFactory=new OkHttpClient();

    @CallAdapterFactories
    CallAdapter.Factory[]callAdapterFactories={RxJavaCallAdapterFactory.create()};

    @ConverterFactories
    Converter.Factory[]converterFactories={GsonConverterFactory.create()};

    @GET("v2/fm/playlist")
    Observable<DouBanPlayList> getPlayList(@Header("Authorization") String accessToken, @Query("alt") String alt,
                                           @Query("apikey") String apikey, @Query("app_name") String app_name,
                                           @Query("channel") String channel, @Query("client") String client,
                                           @Query("formats") String formats, @Query("pt") String pt, @Query("type") String type,
                                           @Query("udid") String udid, @Query("version") String version, @Query("sid") String sid);

}
