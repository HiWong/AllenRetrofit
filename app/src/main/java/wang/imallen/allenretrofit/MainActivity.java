package wang.imallen.allenretrofit;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.annotation.Annotation;

import retrofit3.RetrofitManager;
import retrofit3.annotation.bean.MethodBean;
import wang.imallen.allenretrofit.api.DeviceApi;
import wang.imallen.allenretrofit.api.DeviceApiProxy;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Handler handler=new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                test();
            }
        },3000);



    }

    private void test(){
        DeviceApi apiProxy = DeviceApiProxy.getInstance();
        /*
        Annotation[] methodAnnotations = RetrofitManager.getInstance().getMethodAnnotations(DeviceApi.class.getCanonicalName(),
                "getRecommandAppList(java.lang.String,java.lang.String,java.lang.String)");
        */
        MethodBean methodBean=RetrofitManager.getInstance().getMethodBean(DeviceApi.class.getCanonicalName(),
                "getRecommandAppList(java.lang.String,java.lang.String,java.lang.String)");

        if(methodBean!=null){
            Annotation[]methodAnnotations=methodBean.getMethodAnnotations();
        }

        Log.d(TAG, "just for test");
    }
}
