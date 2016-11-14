package wang.imallen.allenretrofit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wang.imallen.allenretrofit.api.DeviceApi;
import wang.imallen.allenretrofit.api.DeviceApiProxy;
import wang.imallen.allenretrofit.api.DoubanApi;
import wang.imallen.allenretrofit.api.RetrofitManager;
import wang.imallen.allenretrofit.bean.AppResponse;
import wang.imallen.allenretrofit.bean.douban.DouBanPlayList;
import wang.imallen.allenretrofit.config.DouBanConfig;
import wang.imallen.allenretrofit.helper.DouBanPlayHelper;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = MainActivity.class.getSimpleName();

    private void initView(){
        findViewById(R.id.startBtn).setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

    }

    private void loadDoubanPlayList(){

        String id="1001";
        String title="八零";

        String pt, type, sid;
        if (DouBanPlayHelper.isFirst()) {
            type = "n";
            pt = DouBanPlayHelper.getNormalPt();
            sid = DouBanPlayHelper.getNormalSid();
        } else {
            type = "s";
            pt = DouBanPlayHelper.getPt();
            sid = DouBanPlayHelper.getSid();
        }
        requestPlayList(String.valueOf(id), pt, type, sid, title);
    }

    private void requestPlayList(String channel, String pt, String type, String sid,
                                 final String categoryName) {
        Log.d(TAG,"start of requestPlayList");
        Log.d("Retrofit","start of requestPlayList");
        RetrofitManager.getInstance().getProxy(DoubanApi.class)
                .getPlayList(null, DouBanConfig.ALT, DouBanConfig.API_KEY, DouBanConfig.APP_NAME, channel,
                        DouBanConfig.CLIENT, DouBanConfig.FORMATS, pt, type, DouBanConfig.DEVICE_ID,
                        DouBanConfig.VERSION, sid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<DouBanPlayList>() {

                    @Override
                    public void onCompleted() {
                       Log.d(TAG,"requestPlayList-->onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                       Log.d(TAG,"requestPlayList-->onError");
                    }

                    @Override
                    public void onNext(DouBanPlayList douBanPlayList) {

                        Log.d(TAG,"requestPlayList-->onNdex");

                    }
                });
    }

    private void httpRequest(){
        Log.d(TAG,"start of http request");
        Log.d("Retrofit","start of http request");
        //DeviceApiProxy.getInstance().getRecommandAppList(String.valueOf(1),"p_mobile","5")
       // RetrofitManager.getInstance().getProxy(DeviceApi.class)
                //.getRecommandAppList(String.valueOf(1),"p_mobile","5")
        DeviceApiProxy.getInstance().getRecommandAppList(String.valueOf(1),"p_mobile","5")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<AppResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG,"onError,err msg:"+e.getMessage());

                    }

                    @Override
                    public void onNext(AppResponse appResponse) {
                        Log.d(TAG,"onNext");
                        if(null!=appResponse){
                            Log.d(TAG,"appResponse:"+appResponse.toString());
                        }
                    }
                });
    }

    private void getDeviceNames(){
        Log.d(TAG,"start of getDeviceNames()");
        Log.d("Retrofit","start of getDeviceNames()");
        Observable<AppResponse>observable=RetrofitManager.getInstance().getProxy(DeviceApi.class)
                .getDeviceNames("new_device","Alice","Bob","Bob1",
                        "Bob2","Bob3","Bob4","Bob5","Bob6","Bob7","Bob8","Bob9","Bob10","Bob11","Bob12",
                        "Bob13","Bob14","Bob15","Bob16","Bob17","Bob18","Bob19","Bob20","Bob21","Bob22","Bob23",
                        "Bob24","Bob25","Bob26","Bob27");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startBtn:
                //httpRequest();
                //loadDoubanPlayList();
                getDeviceNames();
                break;
        }
    }
}
