package wang.imallen.allenretrofit;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wang.imallen.allenretrofit.api.DeviceApiProxy;
import wang.imallen.allenretrofit.bean.AppResponse;


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

    private void httpRequest(){
        Log.d(TAG,"start of http request");
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.startBtn:
                httpRequest();
                break;
        }
    }
}
