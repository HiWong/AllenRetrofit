# AllenRetrofit
Improved Type-safe HTTP client based on Retrofit for Android and Java by HiWong.

Undoubtedly,Retrofit is an excellent type-safe http client. Yet there are still some details need to be improved. The first shortingcoming 
is that Retrofit parsing http request info by reflection instead of annotation, which has been proved to be more effective cause it 
parsing http info in build-time other than run-time. 

Another is that developers have to write a Client class for every http interface. That's really frustrating cause most of which are 
repeated work.

Based on that 2 blind sides, I created my own http client i.e AllenRetrofit based on static proxy instead of dynamic proxy. As a result, we can use annotation instead of reflection to parse http request info. 

One simple example is as below.

First, we define a http api which also contains @CallFactory,@CallAdapterFactories,@ConverterFactories:

```java
package wang.imallen.allenretrofit.api;

import java.io.IOException;
import java.io.Serializable;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit3.Callback;
import retrofit3.Converter;
import retrofit3.Response;
import retrofit3.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit3.annotation.config.BaseConfig;
import retrofit3.annotation.config.CallAdapterFactories;
import retrofit3.annotation.config.CallFactory;
import retrofit3.annotation.config.ConverterFactories;
import retrofit3.annotation.http.FormUrlEncoded;
import retrofit3.annotation.http.GET;
import retrofit3.annotation.http.HTTP;
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
@BaseConfig(baseUrl = "http://www.baidu.com", validateEagerly = false)
public interface DeviceApi {

    @CallFactory Call.Factory callFactory = new OkHttpClient();

    @CallAdapterFactories CallAdapter.Factory[] callAdapterFactories = {
            RxJavaCallAdapterFactory.create()
    };

    @ConverterFactories Converter.Factory[] converterFactories = { GsonConverterFactory.create() };

    @GET(DeviceConfig.RECOMMANDAPPURL
         + ServiceAction.RECOMAND) Observable<AppResponse> getRecommandAppList(
            @Query("page") String page, @Query("code") String code,
            @Query("version") String version);

    @FormUrlEncoded @GET("/device/list") retrofit3.call.Call<AppResponse> getDeviceList(
            @Query("type") String type);

    @FormUrlEncoded @GET("/device/name") CustomCall<AppResponse> getDeviceNames(
            @Query("type") String type, @Query("name1") String name1, @Query("name2") String name2,
            @Query("name3") String name3, @Query("name4") String name4,
            @Query("name5") String name5, @Query("name6") String name6,
            @Query("name7") String name7, @Query("name8") String name8,
            @Query("name9") String name9, @Query("name10") String name10);

    @Headers("Cache-Control: max-age=86400") @HTTP(method = "GET", path = "/device/name",
            hasBody = false) Observable<AppResponse> getDeviceNames(@Query("type") String type,
            @Query("name1") String name1, @Query("name2") String name2,
            @Query("name3") String name3, @Query("name4") String name4,
            @Query("name5") String name5, @Query("name6") String name6,
            @Query("name7") String name7, @Query("name8") String name8,
            @Query("name9") String name9, @Query("name10") String name10,
            @Query("name11") String name11, @Query("name12") String name12,
            @Query("name13") String name13, @Query("name14") String name14,
            @Query("name15") String name15, @Query("name16") String name16,
            @Query("name17") String name17, @Query("name18") String name18,
            @Query("name19") String name19, @Query("name20") String name20,
            @Query("name21") String name21, @Query("name22") String name22,
            @Query("name23") String name23, @Query("name24") String name24,
            @Query("name25") String name25, @Query("name26") String name26,
            @Query("name27") String name27, @Query("name28") String name28,
            @Query("name29") String name29);
}

```

Then static proxy and RetrofitManager are generated after built:

```java
package wang.imallen.allenretrofit.api;

import java.lang.Class;

public final class RetrofitManager {
  private static volatile RetrofitManager instance;

  private RetrofitManager() {
  }

  public static RetrofitManager getInstance() {
    if(null==instance) {
      synchronized(RetrofitManager.class) {
        if(null==instance) {
          instance=new RetrofitManager();
        }
      }
    }
    return instance;
  }

  public <T> T getProxy(Class<T> clazz) {
    if(wang.imallen.allenretrofit.api.DoubanApi.class==clazz) {
      return (T)DoubanApiProxy.getInstance();
    }
    if(wang.imallen.allenretrofit.api.DeviceApi.class==clazz) {
      return (T)DeviceApiProxy.getInstance();
    }
    throw new RuntimeException("No matched api for this class, have you ever declared it?");
  }
}

```

DeviceApiProxy is as below:

```java
package wang.imallen.allenretrofit.api;

import java.lang.Override;
import java.lang.String;
import retrofit3.Retrofit;
import retrofit3.ServiceMethod;
import retrofit3.annotation.bean.RawMethodAnnotationBean;
import retrofit3.annotation.bean.parameter.ParaAnnotationBean;
import retrofit3.annotation.bean.parameter.QueryBean;
import retrofit3.call.Call;
import rx.Observable;
import wang.imallen.allenretrofit.bean.AppResponse;

public class DeviceApiProxy implements DeviceApi {
  private static volatile DeviceApiProxy instance;

  private final Retrofit retrofit;

  private DeviceApiProxy() {
    Retrofit.Builder retrofitBuilder=new Retrofit.Builder();
    retrofitBuilder.baseUrl("http://www.baidu.com");
    retrofitBuilder.validateEagerly(false);
    retrofitBuilder.setApiName("DeviceApi");
    retrofitBuilder.callFactory(callFactory);
    for(int i=0;i<callAdapterFactories.length;++i) {
      retrofitBuilder.addCallAdapterFactory(callAdapterFactories[i]);
    }
    for(int i=0;i<converterFactories.length;++i) {
      retrofitBuilder.addConverterFactory(converterFactories[i]);
    }
    this.retrofit=retrofitBuilder.build();
  }

  public static DeviceApiProxy getInstance() {
    if(null==instance) {
      synchronized(DeviceApiProxy.class) {
        if(null==instance) {
          instance=new DeviceApiProxy();
        }
      }
    }
    return instance;
  }

  @Override
  public Observable<AppResponse> getDeviceNames(String type, String name1, String name2, String name3, String name4, String name5, String name6, String name7, String name8, String name9, String name10, String name11, String name12, String name13, String name14, String name15, String name16, String name17, String name18, String name19, String name20, String name21, String name22, String name23, String name24, String name25, String name26, String name27, String name28, String name29) {
    ServiceMethod serviceMethod=retrofit.getServiceMethod("getDeviceNames(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)");
    Object[]args=new Object[30];
    args[0]=type;
    args[1]=name1;
    args[2]=name2;
    args[3]=name3;
    args[4]=name4;
    args[5]=name5;
    args[6]=name6;
    args[7]=name7;
    args[8]=name8;
    args[9]=name9;
    args[10]=name10;
    args[11]=name11;
    args[12]=name12;
    args[13]=name13;
    args[14]=name14;
    args[15]=name15;
    args[16]=name16;
    args[17]=name17;
    args[18]=name18;
    args[19]=name19;
    args[20]=name20;
    args[21]=name21;
    args[22]=name22;
    args[23]=name23;
    args[24]=name24;
    args[25]=name25;
    args[26]=name26;
    args[27]=name27;
    args[28]=name28;
    args[29]=name29;
    if(serviceMethod!=null) {
      return retrofit.adapt(serviceMethod,args);
    } else {
      Class[]returnTypeArguments=new Class[1];
      returnTypeArguments[0]=wang.imallen.allenretrofit.bean.AppResponse.class;
      Class[]responseTypeArguments=null;
      RawMethodAnnotationBean rawBean=new RawMethodAnnotationBean();
      rawBean.setHttpMethod("GET");
      rawBean.setHasBody(false);
      rawBean.setFormEncoded(false);
      rawBean.setMultipart(false);
      rawBean.setRelativeUrl("/device/name");
      rawBean.setStreaming(false);
      String[]headersValue=new String[1];
      headersValue[0]=new String("Cache-Control: max-age=86400");
      rawBean.setHeadersValue(headersValue);
      ParaAnnotationBean[][]paraAnnotationBeansArray=new ParaAnnotationBean[30][];
      paraAnnotationBeansArray[0]=new ParaAnnotationBean[1];
      QueryBean tempBean00=new QueryBean("type",false);
      paraAnnotationBeansArray[0][0]=tempBean00;
      paraAnnotationBeansArray[1]=new ParaAnnotationBean[1];
      QueryBean tempBean10=new QueryBean("name1",false);
      paraAnnotationBeansArray[1][0]=tempBean10;
      paraAnnotationBeansArray[2]=new ParaAnnotationBean[1];
      QueryBean tempBean20=new QueryBean("name2",false);
      paraAnnotationBeansArray[2][0]=tempBean20;
      paraAnnotationBeansArray[3]=new ParaAnnotationBean[1];
      QueryBean tempBean30=new QueryBean("name3",false);
      paraAnnotationBeansArray[3][0]=tempBean30;
      paraAnnotationBeansArray[4]=new ParaAnnotationBean[1];
      QueryBean tempBean40=new QueryBean("name4",false);
      paraAnnotationBeansArray[4][0]=tempBean40;
      paraAnnotationBeansArray[5]=new ParaAnnotationBean[1];
      QueryBean tempBean50=new QueryBean("name5",false);
      paraAnnotationBeansArray[5][0]=tempBean50;
      paraAnnotationBeansArray[6]=new ParaAnnotationBean[1];
      QueryBean tempBean60=new QueryBean("name6",false);
      paraAnnotationBeansArray[6][0]=tempBean60;
      paraAnnotationBeansArray[7]=new ParaAnnotationBean[1];
      QueryBean tempBean70=new QueryBean("name7",false);
      paraAnnotationBeansArray[7][0]=tempBean70;
      paraAnnotationBeansArray[8]=new ParaAnnotationBean[1];
      QueryBean tempBean80=new QueryBean("name8",false);
      paraAnnotationBeansArray[8][0]=tempBean80;
      paraAnnotationBeansArray[9]=new ParaAnnotationBean[1];
      QueryBean tempBean90=new QueryBean("name9",false);
      paraAnnotationBeansArray[9][0]=tempBean90;
      paraAnnotationBeansArray[10]=new ParaAnnotationBean[1];
      QueryBean tempBean100=new QueryBean("name10",false);
      paraAnnotationBeansArray[10][0]=tempBean100;
      paraAnnotationBeansArray[11]=new ParaAnnotationBean[1];
      QueryBean tempBean110=new QueryBean("name11",false);
      paraAnnotationBeansArray[11][0]=tempBean110;
      paraAnnotationBeansArray[12]=new ParaAnnotationBean[1];
      QueryBean tempBean120=new QueryBean("name12",false);
      paraAnnotationBeansArray[12][0]=tempBean120;
      paraAnnotationBeansArray[13]=new ParaAnnotationBean[1];
      QueryBean tempBean130=new QueryBean("name13",false);
      paraAnnotationBeansArray[13][0]=tempBean130;
      paraAnnotationBeansArray[14]=new ParaAnnotationBean[1];
      QueryBean tempBean140=new QueryBean("name14",false);
      paraAnnotationBeansArray[14][0]=tempBean140;
      paraAnnotationBeansArray[15]=new ParaAnnotationBean[1];
      QueryBean tempBean150=new QueryBean("name15",false);
      paraAnnotationBeansArray[15][0]=tempBean150;
      paraAnnotationBeansArray[16]=new ParaAnnotationBean[1];
      QueryBean tempBean160=new QueryBean("name16",false);
      paraAnnotationBeansArray[16][0]=tempBean160;
      paraAnnotationBeansArray[17]=new ParaAnnotationBean[1];
      QueryBean tempBean170=new QueryBean("name17",false);
      paraAnnotationBeansArray[17][0]=tempBean170;
      paraAnnotationBeansArray[18]=new ParaAnnotationBean[1];
      QueryBean tempBean180=new QueryBean("name18",false);
      paraAnnotationBeansArray[18][0]=tempBean180;
      paraAnnotationBeansArray[19]=new ParaAnnotationBean[1];
      QueryBean tempBean190=new QueryBean("name19",false);
      paraAnnotationBeansArray[19][0]=tempBean190;
      paraAnnotationBeansArray[20]=new ParaAnnotationBean[1];
      QueryBean tempBean200=new QueryBean("name20",false);
      paraAnnotationBeansArray[20][0]=tempBean200;
      paraAnnotationBeansArray[21]=new ParaAnnotationBean[1];
      QueryBean tempBean210=new QueryBean("name21",false);
      paraAnnotationBeansArray[21][0]=tempBean210;
      paraAnnotationBeansArray[22]=new ParaAnnotationBean[1];
      QueryBean tempBean220=new QueryBean("name22",false);
      paraAnnotationBeansArray[22][0]=tempBean220;
      paraAnnotationBeansArray[23]=new ParaAnnotationBean[1];
      QueryBean tempBean230=new QueryBean("name23",false);
      paraAnnotationBeansArray[23][0]=tempBean230;
      paraAnnotationBeansArray[24]=new ParaAnnotationBean[1];
      QueryBean tempBean240=new QueryBean("name24",false);
      paraAnnotationBeansArray[24][0]=tempBean240;
      paraAnnotationBeansArray[25]=new ParaAnnotationBean[1];
      QueryBean tempBean250=new QueryBean("name25",false);
      paraAnnotationBeansArray[25][0]=tempBean250;
      paraAnnotationBeansArray[26]=new ParaAnnotationBean[1];
      QueryBean tempBean260=new QueryBean("name26",false);
      paraAnnotationBeansArray[26][0]=tempBean260;
      paraAnnotationBeansArray[27]=new ParaAnnotationBean[1];
      QueryBean tempBean270=new QueryBean("name27",false);
      paraAnnotationBeansArray[27][0]=tempBean270;
      paraAnnotationBeansArray[28]=new ParaAnnotationBean[1];
      QueryBean tempBean280=new QueryBean("name28",false);
      paraAnnotationBeansArray[28][0]=tempBean280;
      paraAnnotationBeansArray[29]=new ParaAnnotationBean[1];
      QueryBean tempBean290=new QueryBean("name29",false);
      paraAnnotationBeansArray[29][0]=tempBean290;
      Class[]parameterTypes=new Class[30];
      parameterTypes[0]=java.lang.String.class;
      parameterTypes[1]=java.lang.String.class;
      parameterTypes[2]=java.lang.String.class;
      parameterTypes[3]=java.lang.String.class;
      parameterTypes[4]=java.lang.String.class;
      parameterTypes[5]=java.lang.String.class;
      parameterTypes[6]=java.lang.String.class;
      parameterTypes[7]=java.lang.String.class;
      parameterTypes[8]=java.lang.String.class;
      parameterTypes[9]=java.lang.String.class;
      parameterTypes[10]=java.lang.String.class;
      parameterTypes[11]=java.lang.String.class;
      parameterTypes[12]=java.lang.String.class;
      parameterTypes[13]=java.lang.String.class;
      parameterTypes[14]=java.lang.String.class;
      parameterTypes[15]=java.lang.String.class;
      parameterTypes[16]=java.lang.String.class;
      parameterTypes[17]=java.lang.String.class;
      parameterTypes[18]=java.lang.String.class;
      parameterTypes[19]=java.lang.String.class;
      parameterTypes[20]=java.lang.String.class;
      parameterTypes[21]=java.lang.String.class;
      parameterTypes[22]=java.lang.String.class;
      parameterTypes[23]=java.lang.String.class;
      parameterTypes[24]=java.lang.String.class;
      parameterTypes[25]=java.lang.String.class;
      parameterTypes[26]=java.lang.String.class;
      parameterTypes[27]=java.lang.String.class;
      parameterTypes[28]=java.lang.String.class;
      parameterTypes[29]=java.lang.String.class;
      Class[][]parameterTypeArguments=null;
      return retrofit.adapt("getDeviceNames(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)",rx.Observable.class,returnTypeArguments,wang.imallen.allenretrofit.bean.AppResponse.class,responseTypeArguments,rawBean,paraAnnotationBeansArray,parameterTypes,parameterTypeArguments,args);
    }
  }

  @Override
  public Call<AppResponse> getDeviceList(String type) {
    ServiceMethod serviceMethod=retrofit.getServiceMethod("getDeviceList(java.lang.String)");
    Object[]args=new Object[1];
    args[0]=type;
    if(serviceMethod!=null) {
      return retrofit.adapt(serviceMethod,args);
    } else {
      Class[]returnTypeArguments=new Class[1];
      returnTypeArguments[0]=wang.imallen.allenretrofit.bean.AppResponse.class;
      Class[]responseTypeArguments=null;
      RawMethodAnnotationBean rawBean=new RawMethodAnnotationBean();
      rawBean.setHttpMethod("GET");
      rawBean.setHasBody(false);
      rawBean.setFormEncoded(true);
      rawBean.setMultipart(false);
      rawBean.setRelativeUrl("/device/list");
      rawBean.setStreaming(false);
      ParaAnnotationBean[][]paraAnnotationBeansArray=new ParaAnnotationBean[1][];
      paraAnnotationBeansArray[0]=new ParaAnnotationBean[1];
      QueryBean tempBean00=new QueryBean("type",false);
      paraAnnotationBeansArray[0][0]=tempBean00;
      Class[]parameterTypes=new Class[1];
      parameterTypes[0]=java.lang.String.class;
      Class[][]parameterTypeArguments=null;
      return retrofit.adapt("getDeviceList(java.lang.String)",retrofit3.call.Call.class,returnTypeArguments,wang.imallen.allenretrofit.bean.AppResponse.class,responseTypeArguments,rawBean,paraAnnotationBeansArray,parameterTypes,parameterTypeArguments,args);
    }
  }

  @Override
  public Observable<AppResponse> getRecommandAppList(String page, String code, String version) {
    ServiceMethod serviceMethod=retrofit.getServiceMethod("getRecommandAppList(java.lang.String,java.lang.String,java.lang.String)");
    Object[]args=new Object[3];
    args[0]=page;
    args[1]=code;
    args[2]=version;
    if(serviceMethod!=null) {
      return retrofit.adapt(serviceMethod,args);
    } else {
      Class[]returnTypeArguments=new Class[1];
      returnTypeArguments[0]=wang.imallen.allenretrofit.bean.AppResponse.class;
      Class[]responseTypeArguments=null;
      RawMethodAnnotationBean rawBean=new RawMethodAnnotationBean();
      rawBean.setHttpMethod("GET");
      rawBean.setHasBody(false);
      rawBean.setFormEncoded(false);
      rawBean.setMultipart(false);
      rawBean.setRelativeUrl("http://appstore.aginomoto.com/api/mobile/position");
      rawBean.setStreaming(false);
      ParaAnnotationBean[][]paraAnnotationBeansArray=new ParaAnnotationBean[3][];
      paraAnnotationBeansArray[0]=new ParaAnnotationBean[1];
      QueryBean tempBean00=new QueryBean("page",false);
      paraAnnotationBeansArray[0][0]=tempBean00;
      paraAnnotationBeansArray[1]=new ParaAnnotationBean[1];
      QueryBean tempBean10=new QueryBean("code",false);
      paraAnnotationBeansArray[1][0]=tempBean10;
      paraAnnotationBeansArray[2]=new ParaAnnotationBean[1];
      QueryBean tempBean20=new QueryBean("version",false);
      paraAnnotationBeansArray[2][0]=tempBean20;
      Class[]parameterTypes=new Class[3];
      parameterTypes[0]=java.lang.String.class;
      parameterTypes[1]=java.lang.String.class;
      parameterTypes[2]=java.lang.String.class;
      Class[][]parameterTypeArguments=null;
      return retrofit.adapt("getRecommandAppList(java.lang.String,java.lang.String,java.lang.String)",rx.Observable.class,returnTypeArguments,wang.imallen.allenretrofit.bean.AppResponse.class,responseTypeArguments,rawBean,paraAnnotationBeansArray,parameterTypes,parameterTypeArguments,args);
    }
  }

  @Override
  public CustomCall<AppResponse> getDeviceNames(String type, String name1, String name2, String name3, String name4, String name5, String name6, String name7, String name8, String name9, String name10) {
    ServiceMethod serviceMethod=retrofit.getServiceMethod("getDeviceNames(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)");
    Object[]args=new Object[11];
    args[0]=type;
    args[1]=name1;
    args[2]=name2;
    args[3]=name3;
    args[4]=name4;
    args[5]=name5;
    args[6]=name6;
    args[7]=name7;
    args[8]=name8;
    args[9]=name9;
    args[10]=name10;
    if(serviceMethod!=null) {
      return retrofit.adapt(serviceMethod,args);
    } else {
      Class[]returnTypeArguments=new Class[1];
      returnTypeArguments[0]=wang.imallen.allenretrofit.bean.AppResponse.class;
      Class[]responseTypeArguments=null;
      RawMethodAnnotationBean rawBean=new RawMethodAnnotationBean();
      rawBean.setHttpMethod("GET");
      rawBean.setHasBody(false);
      rawBean.setFormEncoded(true);
      rawBean.setMultipart(false);
      rawBean.setRelativeUrl("/device/name");
      rawBean.setStreaming(false);
      ParaAnnotationBean[][]paraAnnotationBeansArray=new ParaAnnotationBean[11][];
      paraAnnotationBeansArray[0]=new ParaAnnotationBean[1];
      QueryBean tempBean00=new QueryBean("type",false);
      paraAnnotationBeansArray[0][0]=tempBean00;
      paraAnnotationBeansArray[1]=new ParaAnnotationBean[1];
      QueryBean tempBean10=new QueryBean("name1",false);
      paraAnnotationBeansArray[1][0]=tempBean10;
      paraAnnotationBeansArray[2]=new ParaAnnotationBean[1];
      QueryBean tempBean20=new QueryBean("name2",false);
      paraAnnotationBeansArray[2][0]=tempBean20;
      paraAnnotationBeansArray[3]=new ParaAnnotationBean[1];
      QueryBean tempBean30=new QueryBean("name3",false);
      paraAnnotationBeansArray[3][0]=tempBean30;
      paraAnnotationBeansArray[4]=new ParaAnnotationBean[1];
      QueryBean tempBean40=new QueryBean("name4",false);
      paraAnnotationBeansArray[4][0]=tempBean40;
      paraAnnotationBeansArray[5]=new ParaAnnotationBean[1];
      QueryBean tempBean50=new QueryBean("name5",false);
      paraAnnotationBeansArray[5][0]=tempBean50;
      paraAnnotationBeansArray[6]=new ParaAnnotationBean[1];
      QueryBean tempBean60=new QueryBean("name6",false);
      paraAnnotationBeansArray[6][0]=tempBean60;
      paraAnnotationBeansArray[7]=new ParaAnnotationBean[1];
      QueryBean tempBean70=new QueryBean("name7",false);
      paraAnnotationBeansArray[7][0]=tempBean70;
      paraAnnotationBeansArray[8]=new ParaAnnotationBean[1];
      QueryBean tempBean80=new QueryBean("name8",false);
      paraAnnotationBeansArray[8][0]=tempBean80;
      paraAnnotationBeansArray[9]=new ParaAnnotationBean[1];
      QueryBean tempBean90=new QueryBean("name9",false);
      paraAnnotationBeansArray[9][0]=tempBean90;
      paraAnnotationBeansArray[10]=new ParaAnnotationBean[1];
      QueryBean tempBean100=new QueryBean("name10",false);
      paraAnnotationBeansArray[10][0]=tempBean100;
      Class[]parameterTypes=new Class[11];
      parameterTypes[0]=java.lang.String.class;
      parameterTypes[1]=java.lang.String.class;
      parameterTypes[2]=java.lang.String.class;
      parameterTypes[3]=java.lang.String.class;
      parameterTypes[4]=java.lang.String.class;
      parameterTypes[5]=java.lang.String.class;
      parameterTypes[6]=java.lang.String.class;
      parameterTypes[7]=java.lang.String.class;
      parameterTypes[8]=java.lang.String.class;
      parameterTypes[9]=java.lang.String.class;
      parameterTypes[10]=java.lang.String.class;
      Class[][]parameterTypeArguments=null;
      return retrofit.adapt("getDeviceNames(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)",wang.imallen.allenretrofit.api.CustomCall.class,returnTypeArguments,wang.imallen.allenretrofit.bean.AppResponse.class,responseTypeArguments,rawBean,paraAnnotationBeansArray,parameterTypes,parameterTypeArguments,args);
    }
  }
}

```

As you can see, unit tests and demos have not be accomplished. And that's why I haven't submitted it to jcenter or maven.

I will add them soon. 

License
=======

    Copyright 2016 HiWong(bettarwang@gmail.com).

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
