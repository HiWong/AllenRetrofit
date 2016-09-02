package retrofit3.annotation.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by allen on 16-8-31.
 */
public class ApiBean implements Serializable {

    private String packageName;
    private String apiName;

    private String baseUrl;
    private boolean validateEagerly;

    private String callFactoryFieldName;
    private String callAdapterFactoriesFieldName;
    private String executorFieldName;
    private String converterFactoriesFieldName;

    //private List<MethodBean> methodBeanList=new ArrayList<>();
    private Map<String, MethodBean> methodBeanMap = new HashMap<>();

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getCallAdapterFactoriesFieldName() {
        return callAdapterFactoriesFieldName;
    }

    public void setCallAdapterFactoriesFieldName(String callAdapterFactoriesFieldName) {
        this.callAdapterFactoriesFieldName = callAdapterFactoriesFieldName;
    }

    public String getCallFactoryFieldName() {
        return callFactoryFieldName;
    }

    public void setCallFactoryFieldName(String callFactoryFieldName) {
        this.callFactoryFieldName = callFactoryFieldName;
    }

    public boolean isValidateEagerly() {
        return validateEagerly;
    }

    public void setValidateEagerly(boolean validateEagerly) {
        this.validateEagerly = validateEagerly;
    }

    public String getExecutorFieldName() {
        return executorFieldName;
    }

    public void setExecutorFieldName(String executorFieldName) {
        this.executorFieldName = executorFieldName;
    }

    public String getConverterFactoriesFieldName() {
        return converterFactoriesFieldName;
    }

    public void setConverterFactoriesFieldName(String converterFactoriesFieldName) {
        this.converterFactoriesFieldName = converterFactoriesFieldName;
    }

    public void addMethodBean(MethodBean bean) {
        methodBeanMap.put(bean.getMethodDeclaration(), bean);
    }

    public Map<String, MethodBean> getMethodBeanMap() {
        return methodBeanMap;
    }
}
