package retrofit3;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import retrofit3.annotation.bean.ApiBean;
import retrofit3.annotation.bean.MethodBean;

/**
 * Created by allen on 16-8-27.
 */
public final class RetrofitManager {

    private static volatile RetrofitManager instance;

    //key is the canonicalName of api
    //private Map<String,Object> apiProxyCache=new HashMap<>();

    private Map<String,Map<String,MethodBean>>methodBeanData=new HashMap<>();

    public void addMethodBeanMap(ApiBean apiBean){
        String canonicalName=apiBean.getPackageName()+"."+apiBean.getApiName();
        methodBeanData.put(canonicalName,apiBean.getMethodBeanMap());
    }

    public MethodBean getMethodBean(String canonicalName,String methodDeclaration){
        return methodBeanData.get(canonicalName).get(methodDeclaration);
    }

    public Annotation[]getMethodAnnotations(String canonicalName,String methodDeclaration){
        return getMethodBean(canonicalName,methodDeclaration).getMethodAnnotations();
    }

    public Annotation[][]getParameterAnnotationsArray(String canonicalName,String methodDeclaration){
        return getMethodBean(canonicalName,methodDeclaration).getParameterAnnotationsArray();
    }

    private RetrofitManager(){}

    public static RetrofitManager getInstance(){
        if(null==instance){
            synchronized (RetrofitManager.class){
                if(null==instance){
                    instance=new RetrofitManager();
                }
            }
        }
        return instance;
    }



    public <T> T getProxy(String canonicalName){
        if("wang.imallen.allenretrofit.api.DeviceApi".equals(canonicalName)){
            return wang.imallen.allenretrofit.api.DeviceApiProxy.getInstance();
        }
        if("wang.imallen.allenretrofit.api.WeatherApi".equals(canonicalName)){
            return wang.imallen.allenretrofit.api.WeatherApiProxy.getInstance();
        }

        return null;
    }

    /*
    public <T> T getApiProxy(String apiCanonicalName){
        Object obj=apiProxyCache.get(apiCanonicalName);

    }
    */


}

