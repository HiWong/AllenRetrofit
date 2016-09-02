package retrofit3;

import android.text.TextUtils;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import retrofit3.annotation.bean.ApiBean;
import retrofit3.annotation.bean.MethodBean;
import retrofit3.annotation.config.BaseConfig;
import retrofit3.annotation.config.CallAdapterFactories;
import retrofit3.annotation.config.CallFactory;
import retrofit3.annotation.config.ConverterFactories;
import retrofit3.annotation.config.Executor;
import retrofit3.annotation.config.Platform;
import retrofit3.annotation.http.Body;
import retrofit3.annotation.http.DELETE;
import retrofit3.annotation.http.Field;
import retrofit3.annotation.http.FieldMap;
import retrofit3.annotation.http.FormUrlEncoded;
import retrofit3.annotation.http.GET;
import retrofit3.annotation.http.HEAD;
import retrofit3.annotation.http.HTTP;
import retrofit3.annotation.http.Header;
import retrofit3.annotation.http.Headers;
import retrofit3.annotation.http.Multipart;
import retrofit3.annotation.http.OPTIONS;
import retrofit3.annotation.http.PATCH;
import retrofit3.annotation.http.POST;
import retrofit3.annotation.http.PUT;
import retrofit3.annotation.http.Part;
import retrofit3.annotation.http.PartMap;
import retrofit3.annotation.http.Path;
import retrofit3.annotation.http.Query;
import retrofit3.annotation.http.QueryMap;
import retrofit3.annotation.http.Streaming;
import retrofit3.annotation.http.Url;

/**
 * Created by allen on 16-8-27.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@AutoService(Processor.class)
public class RetrofitProcessor extends AbstractProcessor {


    private static final String CANONICALNAME="canonicalName";

    private Messager messager;

    //key is qualified name of api
    private Map<String,ApiBean>apiBeanMap=new HashMap<>();

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        messager = processingEnv.getMessager();
        super.init(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {

        Set<String> annotationTypes = new LinkedHashSet<>();
        annotationTypes.add(Body.class.getCanonicalName());
        annotationTypes.add(DELETE.class.getCanonicalName());
        annotationTypes.add(Field.class.getCanonicalName());
        annotationTypes.add(FieldMap.class.getCanonicalName());
        annotationTypes.add(FormUrlEncoded.class.getCanonicalName());
        annotationTypes.add(GET.class.getCanonicalName());
        annotationTypes.add(HEAD.class.getCanonicalName());
        annotationTypes.add(Header.class.getCanonicalName());
        annotationTypes.add(Headers.class.getCanonicalName());
        annotationTypes.add(HTTP.class.getCanonicalName());
        annotationTypes.add(Multipart.class.getCanonicalName());
        annotationTypes.add(OPTIONS.class.getCanonicalName());
        annotationTypes.add(Part.class.getCanonicalName());
        annotationTypes.add(PartMap.class.getCanonicalName());
        annotationTypes.add(PATCH.class.getCanonicalName());
        annotationTypes.add(Path.class.getCanonicalName());
        annotationTypes.add(POST.class.getCanonicalName());
        annotationTypes.add(PUT.class.getCanonicalName());
        annotationTypes.add(Query.class.getCanonicalName());
        annotationTypes.add(QueryMap.class.getCanonicalName());
        annotationTypes.add(Streaming.class.getCanonicalName());
        annotationTypes.add(Url.class.getCanonicalName());

        annotationTypes.add(BaseConfig.class.getCanonicalName());
        annotationTypes.add(CallAdapterFactories.class.getCanonicalName());
        annotationTypes.add(CallFactory.class.getCanonicalName());
        annotationTypes.add(ConverterFactories.class.getCanonicalName());
        annotationTypes.add(Executor.class.getCanonicalName());
        annotationTypes.add(Platform.class.getCanonicalName());

        return annotationTypes;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            //createApiProxy(annotations, roundEnv, messager);
            createNewApiProxy(roundEnv,messager);
            parseOtherConfigs(roundEnv,messager);
            writeJavaFile();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            messager.printMessage(Diagnostic.Kind.ERROR, "Unpected error in RetrofitProcessor:" + ex);
        }

        return true;
    }


    private void writeJavaFile(){

        if(apiBeanMap.isEmpty()){
            return;
        }

         boolean isFirst=true;

        TypeSpec.Builder retrofitManagerBuilder=null;
        String retrofitManagerPackageName=null;
        //proxyMethodSpecBuilder对应RetrofitManager中的getProxy()方法
        MethodSpec.Builder proxyMethodSpecBuilder=getProxyMethodSpec();
        for(Map.Entry<String,ApiBean>entry:apiBeanMap.entrySet()){
            ApiBean bean=entry.getValue();

            //first,we need to generate RetrofitManager
            if(isFirst){
                isFirst=false;
                retrofitManagerPackageName=bean.getPackageName();
                retrofitManagerBuilder=getRetrofitManagerBuilder();
            }

            //RetrofitManager.getInstance().addMethodBeanMap(bean);
            writeSingleJavaFile(bean,proxyMethodSpecBuilder);
            //RetrofitManager.getInstance().addProxy(bean.getPackageName()+"."+bean.getApiName());
        }
        if(null!=retrofitManagerBuilder){
            TypeSpec retrofitManagerTypeSpec=retrofitManagerBuilder.addMethod(proxyMethodSpecBuilder.build()).build();
            JavaFile javaFile=JavaFile.builder(retrofitManagerPackageName,retrofitManagerTypeSpec).build();
            try{
                javaFile.writeTo(processingEnv.getFiler());
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }

    }

    private MethodSpec.Builder getProxyMethodSpec(){

        TypeVariableName typeVariableName=TypeVariableName.get("T",Object.class);

        MethodSpec.Builder methodBuilder=MethodSpec.methodBuilder("getProxy")
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(typeVariableName)
                .returns(typeVariableName)
                .addParameter(String.class,CANONICALNAME);
        return methodBuilder;
    }

    private TypeSpec.Builder getRetrofitManagerBuilder(){
        //也许可以通过processingEnv或roundEnv获取packageName
        TypeSpec.Builder retrofitManagerSpec=TypeSpec.classBuilder("RetrofitManager")
                .addModifiers(Modifier.PUBLIC,Modifier.FINAL);
        //要针对各个ApiProxy设置不同的init方法,如initDeviceApiProxy()是用于初始化DeviceApiProxy的
        return retrofitManagerSpec;
    }


    private boolean isEmpty(String str){
        return null==str||"".equals(str);
    }

    private void writeSingleJavaFile(ApiBean bean,MethodSpec.Builder proxyMethodSpecBuilder){
        if(bean==null){
            return;
        }

        //we cannot use TextUtils who belongs to android sdk
        if(isEmpty(bean.getBaseUrl())){
            messager.printMessage(Diagnostic.Kind.ERROR,"base usrl must be inited");
            return;
        }

       MethodSpec.Builder constructMethodBuilder=MethodSpec.constructorBuilder()
               .addModifiers(Modifier.PRIVATE)
               //.addStatement("this.$N=$S","baseUrl",bean.getBaseUrl())
               //.addStatement("this.$N=$L","validateEagerly",bean.isValidateEagerly())
               .addStatement("Retrofit.Builder retrofitBuilder=new $T()",Retrofit.Builder.class);

        constructMethodBuilder.addStatement("retrofitBuilder.baseUrl($S)",bean.getBaseUrl());
        constructMethodBuilder.addStatement("retrofitBuilder.validateEagerly($L)",bean.isValidateEagerly());


        if(!isEmpty(bean.getCallFactoryFieldName())){
            constructMethodBuilder.addStatement("retrofitBuilder.callFactory($N)",bean.getCallFactoryFieldName());
        }
        if(!isEmpty(bean.getExecutorFieldName())){
            constructMethodBuilder.addStatement("retrofitBuilder.callbackExecutor($N)",bean.getExecutorFieldName());
        }
        if(!isEmpty(bean.getCallAdapterFactoriesFieldName())){
            constructMethodBuilder.beginControlFlow("for(int i=0;i<$N.length;++i)",bean.getCallAdapterFactoriesFieldName())
                    .addStatement("retrofitBuilder.addCallAdapterFactory($N[i])",bean.getCallAdapterFactoriesFieldName())
                    .endControlFlow();
        }

        if(!isEmpty(bean.getConverterFactoriesFieldName())){
            constructMethodBuilder.beginControlFlow("for(int i=0;i<$N.length;++i)",bean.getConverterFactoriesFieldName())
                    .addStatement("retrofitBuilder.addConverterFactory($N[i])",bean.getConverterFactoriesFieldName())
                    .endControlFlow();
        }

        constructMethodBuilder.addStatement("this.retrofit=retrofitBuilder.build()");

        MethodSpec constructMethod=constructMethodBuilder.build();

        FieldSpec instanceFieldSpec=FieldSpec.builder(ClassName.get(bean.getPackageName(),bean.getApiName()+"Proxy"),"instance")
                .addModifiers(Modifier.PRIVATE,Modifier.STATIC,Modifier.VOLATILE)
                .initializer("new $L()",bean.getApiName()+"Proxy")
                .build();

        TypeSpec.Builder apiProxyTypeBuilder=TypeSpec.classBuilder(bean.getApiName()+"Proxy")
                .addSuperinterface(ClassName.get(bean.getPackageName(),bean.getApiName()))
                //.addSuperinterface(ParameterizedTypeName.get)
                .addModifiers(Modifier.PUBLIC)
                //.addField(String.class,"baseUrl",Modifier.PRIVATE,Modifier.FINAL)
                //.addField(Boolean.class,"validateEagerly",Modifier.PRIVATE,Modifier.FINAL)
                .addField(Retrofit.class,"retrofit",Modifier.PRIVATE,Modifier.FINAL)
                .addField(instanceFieldSpec)
                //.addField(ClassName.get(bean.getPackageName(),bean.getApiName()+"Proxy"),"instance",Modifier.PRIVATE,Modifier.STATIC,Modifier.VOLATILE)
                //.addField(String.class,"packageName",Modifier.PRIVATE,Modifier.FINAL)
                //.addField(String.class,"apiName",Modifier.PRIVATE,Modifier.FINAL)
                .addMethod(constructMethod)
                .addMethod(getInstanceMethodSpec(bean));

        //List<MethodBean>methodBeanList=bean.getMethodBeanList();
        for(Map.Entry<String,MethodBean>entry:bean.getMethodBeanMap().entrySet()){
            MethodBean methodBean=entry.getValue();
            MethodSpec methodSpec=createSingleMethodSpec(bean,methodBean);
            apiProxyTypeBuilder.addMethod(methodSpec);
        }

        TypeSpec apiProxyType=apiProxyTypeBuilder.build();


        JavaFile javaFile=JavaFile.builder(bean.getPackageName(),apiProxyType).build();
        try{
            javaFile.writeTo(processingEnv.getFiler());
        }catch(IOException ex){
            ex.printStackTrace();
        }

        String canonicalName=bean.getPackageName()+"."+bean.getApiName();
        //At last,we need to add judge
        proxyMethodSpecBuilder.beginControlFlow("if($S.equals("+CANONICALNAME+")",canonicalName)
                .addStatement("return $T.getInstance()",ClassName.get(bean.getPackageName(),bean.getApiName()+"Proxy"));


    }

    public MethodSpec getInstanceMethodSpec(ApiBean apiBean){
        MethodSpec instanceMethodSpec=MethodSpec.methodBuilder("getInstance")
                .addModifiers(Modifier.PUBLIC,Modifier.STATIC)
                .returns(ClassName.get(apiBean.getPackageName(),apiBean.getApiName()+"Proxy"))
                .addStatement("return instance")
                .build();
        return instanceMethodSpec;
    }
    /*
    public MethodSpec getInstanceMethodSpec(ApiBean apiBean){
        MethodSpec instanceMethodSpec=MethodSpec.methodBuilder("getInstance()")
                .addModifiers(Modifier.PUBLIC,Modifier.STATIC)
                .returns(ClassName.get(apiBean.getPackageName(),apiBean.getApiName()+"Proxy"))
                .beginControlFlow("if(null==instance)")
                .addStatement("synchronized($L.class)")
    }
    */

    private TypeName getTypeName(TypeMirror mirror){
        return TypeName.get(mirror);
    }

    private MethodSpec createSingleMethodSpec(ApiBean apiBean,MethodBean methodBean){

        MethodSpec.Builder methodBuilder=MethodSpec.methodBuilder(methodBean.getMethodName())
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(methodBean.getReturnTypeMirror()))
                .addAnnotation(Override.class);

        for(int i=0;i<methodBean.getParameterTypeMirrors().size();++i){
            methodBuilder.addParameter(getTypeName(methodBean.getParameterTypeMirrors().get(i)),
                    methodBean.getParameterNames().get(i));
        }

        //methodBuilder.addStatement("if(retrofit.getServiceMethod($S)!=null)");


        methodBuilder.addStatement("$T serviceMethod=retrofit.getServiceMethod($S)",ServiceMethod.class,
                methodBean.getMethodDeclaration());
        //init args, but actually we could init parameterTypes here
        List<String>parameterNames=methodBean.getParameterNames();
         if(null==parameterNames||parameterNames.size()==0){
             methodBuilder.addStatement("Object[]args=null");
         }else{
             methodBuilder.addStatement("Object[]args=new Object[$L]",parameterNames.size());
             for(int i=0;i<parameterNames.size();++i){
                 methodBuilder.addStatement("args[$L]=$S",i,parameterNames.get(i));
             }
         }

        //add if...else control
        methodBuilder.beginControlFlow("if(serviceMethod!=null)")
                .addStatement("return retrofit.adapt(serviceMethod,args)")
                .nextControlFlow("else");


        //init returnTypeArguments
        String[]returnTypeArgumentsName=methodBean.getReturnTypeArgumentsName();
        if(returnTypeArgumentsName==null||returnTypeArgumentsName.length==0){
            methodBuilder.addStatement("Class[]returnTypeArguments=null");
        }else{
            methodBuilder.addStatement("Class[]returnTypeArguments=new Class[$L]",returnTypeArgumentsName.length);
            for(int i=0;i<returnTypeArgumentsName.length;++i){
                methodBuilder.addStatement("returnTypeArguments[$L]=$L.class",i,returnTypeArgumentsName[i]);
            }
        }
        //init responseTypeArguments
        String[]responseTypeArguments=methodBean.getResponseTypeArgumentsName();
        if(responseTypeArguments==null||responseTypeArguments.length==0){
            methodBuilder.addStatement("Class[]responseTypeArguments=null");
        }else{
            methodBuilder.addStatement("Class[]responseTypeArguments=new Class[$L]",responseTypeArguments.length);
            for(int i=0;i<responseTypeArguments.length;++i){
                methodBuilder.addStatement("responseTypeArguments[$L]=$S",i,responseTypeArguments[i]);
            }
        }
        //init methodAnnotations
        /*
        Annotation[]methodAnnotations=methodBean.getMethodAnnotations();
        if(null==methodAnnotations||methodAnnotations.length==0){
            methodBuilder.addStatement("Annotation[]methodAnnotations=null");
        }else{
            methodBuilder.addStatement("Annotation[]methodAnnotations=new $T[$L]",Annotation.class,methodAnnotations.length);
            for(int i=0;i<methodAnnotations.length;++i){
                methodBuilder.addStatement("methodAnnotations[$L]=$L",i,methodAnnotations[i]);
            }
        }
        */
        methodBuilder.addStatement("$T[]methodAnnotations=$T.getInstance().getMethodAnnotations($S," +
                "$S)",Annotation.class,RetrofitManager.class,apiBean.getPackageName()+"."+apiBean.getApiName(),methodBean.getMethodDeclaration());



        //init parameterAnnotationsArray
        /*
        Annotation[][]parameterAnnotaionsArray=methodBean.getParameterAnnotationsArray();
        if(null==parameterAnnotaionsArray||parameterAnnotaionsArray.length==0){
            methodBuilder.addStatement("Annotation[][]parameterAnnotationsArray=null");
        }else{
            methodBuilder.addStatement("Annotation[][]parameterAnnotationsArray=new Annotation[$L][]",parameterAnnotaionsArray.length);
            for(int i=0;i<parameterAnnotaionsArray.length;++i){
                if(parameterAnnotaionsArray[i]==null||parameterAnnotaionsArray[i].length==0){
                    methodBuilder.addStatement("parameterAnnotationsArray[$L]=null",i);
                }else{
                    for(int j=0;j<parameterAnnotaionsArray[i].length;++j){
                        methodBuilder.addStatement("parameterAnnotationsArray[$L][$L]=$L",i,j,parameterAnnotaionsArray[i][j]);
                    }
                }
            }
        }
        */
        methodBuilder.addStatement("Annotation[][]parameterAnnotationsArray=RetrofitManager.getInstance().getParameterAnnotationsArray($S," +
                "$S)",apiBean.getPackageName()+"."+apiBean.getApiName(),methodBean.getMethodDeclaration());


        //init parameterTypes
        String[]parameterTypeNames=methodBean.getParameterTypeNames();
        if(null==parameterTypeNames||parameterTypeNames.length==0){
            methodBuilder.addStatement("Class[]parameterTypes=null");
        }else{
            methodBuilder.addStatement("Class[]parameterTypes=new Class[$L]",parameterTypeNames.length);
            for(int i=0;i<parameterTypeNames.length;++i){
                methodBuilder.addStatement("parameterTypes[$L]=$L.class",i,parameterTypeNames[i]);
            }
        }
        //init parameterTypeArguments
        String[][]parameterTypeArgumentsNameArray=methodBean.getParameterTypeArgumentsNameArray();
        if(null==parameterTypeArgumentsNameArray||parameterTypeArgumentsNameArray.length==0){
            methodBuilder.addStatement("Class[][]parameterTypeArguments=null");
        }else{
            methodBuilder.addStatement("Class[][]parameterTypeArguments=new Class[$L][]",parameterTypeArgumentsNameArray.length);
            for(int i=0;i<parameterTypeArgumentsNameArray.length;++i){
                if(parameterTypeArgumentsNameArray[i]==null||parameterTypeArgumentsNameArray[i].length==0){
                    methodBuilder.addStatement("parameterTypeArguments[$L]=null",i);
                }else{
                    for(int j=0;j<parameterTypeArgumentsNameArray[i].length;++j){
                        methodBuilder.addStatement("parameterTypeArguments[$L][$L]=$L",i,j,parameterTypeArgumentsNameArray[i][j]);
                    }
                }
            }
        }

        methodBuilder.addStatement("return retrofit.adapt($S,$S,$L.class,returnTypeArguments,$L.class," +
                "responseTypeArguments,methodAnnotations,parameterAnnotationsArray,parameterTypes,"+
                "parameterTypeArguments,args)",apiBean.getApiName(),methodBean.getMethodDeclaration(),
                methodBean.getRawReturnTypeName(),methodBean.getResponseTypeName());

        methodBuilder.endControlFlow();

       return methodBuilder.build();
    }



    private void createNewApiProxy(RoundEnvironment roundEnv,Messager messager){
        for(Element element:roundEnv.getElementsAnnotatedWith(BaseConfig.class)){
            ElementKind kind=element.getKind();
            //目前只支持接口,后面要支持抽象类
            if(kind!=ElementKind.INTERFACE){
                messager.printMessage(Diagnostic.Kind.ERROR,"@BaseConfig can only be annotated interfaces");
                return;
            }
            TypeElement apiElement=(TypeElement)element;
            parseApiElement(apiElement);
        }
    }

    private void parseOtherConfigs(RoundEnvironment roundEnv,Messager messager){
        parseCallFactory(roundEnv,messager);
        parseCallAdapterFactories(roundEnv,messager);
        parseCallbackExecutor(roundEnv,messager);
        parseConverterFactories(roundEnv,messager);
    }
    private void parseConverterFactories(RoundEnvironment roundEnv,Messager messager){
        for(Element element:roundEnv.getElementsAnnotatedWith(ConverterFactories.class)){
            ElementKind kind=element.getKind();
            if(kind!=ElementKind.FIELD){
                messager.printMessage(Diagnostic.Kind.ERROR,"@CallFactory can only be annotated fields");
                return;
            }
            VariableElement fieldElement=(VariableElement)element;
            parseVariableElement(fieldElement);
            String fieldName=fieldElement.getSimpleName().toString();
            TypeElement apiElement=(TypeElement)fieldElement.getEnclosingElement();
            ApiBean apiBean=apiBeanMap.get(apiElement.getQualifiedName().toString());
            if(null!=apiBean){
                apiBean.setConverterFactoriesFieldName(fieldName);
            }
        }
    }

    private void parseCallbackExecutor(RoundEnvironment roundEnv,Messager messager){
        for(Element element:roundEnv.getElementsAnnotatedWith(Executor.class)){
            ElementKind kind=element.getKind();
            if(kind!=ElementKind.FIELD){
                messager.printMessage(Diagnostic.Kind.ERROR,"@CallFactory can only be annotated fields");
                return;
            }
            VariableElement fieldElement=(VariableElement)element;
            parseVariableElement(fieldElement);
            String fieldName=fieldElement.getSimpleName().toString();
            Object constantValue=fieldElement.getConstantValue();
            TypeElement apiElement=(TypeElement)fieldElement.getEnclosingElement();
            ApiBean apiBean=apiBeanMap.get(apiElement.getQualifiedName().toString());
            if(null!=apiBean){
                apiBean.setExecutorFieldName(fieldName);
            }
        }
    }

    private void parseCallAdapterFactories(RoundEnvironment roundEnv,Messager messager){
        for(Element element:roundEnv.getElementsAnnotatedWith(CallAdapterFactories.class)){
            ElementKind kind=element.getKind();
            if(kind!=ElementKind.FIELD){
                messager.printMessage(Diagnostic.Kind.ERROR,"@CallFactory can only be annotated fields");
                return;
            }
            VariableElement fieldElement=(VariableElement)element;
            parseVariableElement(fieldElement);
            String fieldName=fieldElement.getSimpleName().toString();
            Object constantValue=fieldElement.getConstantValue();
            TypeElement apiElement=(TypeElement)fieldElement.getEnclosingElement();
            ApiBean apiBean=apiBeanMap.get(apiElement.getQualifiedName().toString());
            if(null!=apiBean){
                apiBean.setCallAdapterFactoriesFieldName(fieldName);
            }
        }

    }

    private void parseCallFactory(RoundEnvironment roundEnv,Messager messager){
        //类似这样:
        //@CallFactory
        //Call.Factory callFactory=new OkHttpClient();
        for(Element element:roundEnv.getElementsAnnotatedWith(CallFactory.class)){

            ElementKind kind=element.getKind();
            if(kind!=ElementKind.FIELD){
                messager.printMessage(Diagnostic.Kind.ERROR,"@CallFactory can only be annotated fields");
                return;
            }
            VariableElement fieldElement=(VariableElement)element;
            parseVariableElement(fieldElement);
            //fieldName是类似DeviceApi中callFactory这样的
            String fieldName=fieldElement.getSimpleName().toString();
            //Object constantValue=fieldElement.getConstantValue();
            TypeElement apiElement=(TypeElement)fieldElement.getEnclosingElement();
            ApiBean apiBean=apiBeanMap.get(apiElement.getQualifiedName().toString());
            if(null!=apiBean){
                apiBean.setCallFactoryFieldName(fieldName);
            }
        }
    }


    private void parseApiElement(TypeElement apiElement){
        List<? extends Element>enclosedElements=apiElement.getEnclosedElements();

        //apiName类似"DeviceApi"
        String apiName=apiElement.getSimpleName().toString();
        //packageName类似wang.imallen.allenretrofit.api
        String packageName=getPackageName(processingEnv.getElementUtils(),apiElement);


        ApiBean apiBean=new ApiBean();
        apiBean.setApiName(apiName);
        apiBean.setPackageName(packageName);
        apiBeanMap.put(apiElement.getQualifiedName().toString(),apiBean);

        //////////first,parse values of @BaseConfig
        parseBaseConfig(apiElement,apiBean);
        //注意enclosedElements包含所有的内部元素,如DeviceApi中的callFactory,callAdapterFactories,converterFactories,executor,
        // getDeviceName(),getDeviceWeight(),getRecommandAppList(java.lang.String,java.lang.String,java.lang.String),getDeviceList(java.lang.String),getDeviceNames(java.lang.String)
        for(Element element:enclosedElements){

            if(element instanceof ExecutableElement){
                ExecutableElement methodElement=(ExecutableElement)element;
                parseSingleMethodElement(methodElement);
            }else if(element instanceof VariableElement){
                //fields

            }

        }
    }

    private void parseSingleMethodElement(ExecutableElement methodElement){
        MethodBean methodBean=new MethodBean();
        //methodDeclaration是类似"getRecommandAppList(java.lang.String,java.lang.String,java.lang.String)"这样
        methodBean.setMethodDeclaration(methodElement.toString());
        methodBean.setMethodName(methodElement.getSimpleName().toString());

        //then we need to parse method annotations now
        parseMethodParameters(methodElement,methodBean);

        //因为parseMethodAnnotations()中涉及到responseType,故该方法只能放在parseMethodParameters()之后
        parseMethodAnnotations(methodElement,methodBean);
        if(TextUtils.isEmpty(methodBean.getHttpMethod())){
            return;
        }

        TypeElement apiElement=(TypeElement)methodElement.getEnclosingElement();
        ApiBean apiBean=apiBeanMap.get(apiElement.getQualifiedName().toString());
        if(apiBean!=null){
            apiBean.addMethodBean(methodBean);
        }

    }

    private void parseMethodParameters(ExecutableElement methodElement,MethodBean methodBean){
        List<? extends VariableElement>parameterElements=methodElement.getParameters();
        //有的方法是没有参数的
        if(parameterElements.size()==0){
            return;
        }

        //returnTypeMirror是类似rx.Observable<wang.imallen.allenretrofit.bean.AppResponse>这样,而rawReturnTypeName是类似"rx.Observable"这样,而returnTypeArgument是{wang.imallen.allenretrofit.bean.AppResponse}这样
        TypeMirror returnTypeMirror=methodElement.getReturnType();
        methodBean.setReturnTypeMirror(returnTypeMirror);
        String rawReturnTypeName=getRawType(returnTypeMirror);
        methodBean.setRawReturnTypeName(rawReturnTypeName);
        List<? extends TypeMirror>returnTypeArguments=((DeclaredType)returnTypeMirror).getTypeArguments();
        if(returnTypeArguments!=null&&returnTypeArguments.size()>0){
            String[]returnTypeArgumentsName=new String[returnTypeArguments.size()];
            for(int i=0;i<returnTypeArguments.size();++i){
                returnTypeArgumentsName[i]=getRawType(returnTypeArguments.get(i));
            }
            methodBean.setReturnTypeArgumentsName(returnTypeArgumentsName);
            methodBean.setResponseTypeName(returnTypeArgumentsName[0]);
            TypeMirror responseTypeMirror=returnTypeArguments.get(0);
            List<? extends TypeMirror>responseTypeArguments=((DeclaredType)responseTypeMirror).getTypeArguments();
            if(responseTypeArguments!=null&&responseTypeArguments.size()>0){
                String[]responseTypeArgumentsName=new String[responseTypeArguments.size()];
                for(int i=0;i<responseTypeArguments.size();++i){
                    responseTypeArgumentsName[i]=getRawType(responseTypeArguments.get(i));
                }
                methodBean.setResponseTypeArgumentsName(responseTypeArgumentsName);
            }
        }


        int parameterSize=parameterElements.size();
        String[]parameterTypeNames=new String[parameterSize];
        String[][]parameterTypeArgumentsNameArray=new String[parameterSize][];
        Annotation[][]parameterAnnotationsArray=new Annotation[parameterSize][];

        for(int i=0;i<parameterSize;++i){
            VariableElement parameterElement=parameterElements.get(i);

            parseParameterAnnotations(parameterElement,methodBean,parameterAnnotationsArray,i);
            TypeMirror typeMirror=parameterElement.asType();

            methodBean.addParameterTypeMirror(typeMirror);
            methodBean.addParameterName(parameterElement.getSimpleName().toString());

            //parameterTypeNames[i]=typeMirror.toString();
            parameterTypeNames[i]=getRawType(typeMirror);
            //now we get all the type arguments such as {T1,T2,T3,...} from Observable<T1,T2,T3,...>
            DeclaredType declaredType=(DeclaredType)typeMirror;
            List<? extends TypeMirror>typeArguments=declaredType.getTypeArguments();
            if(typeArguments.size()>0){
                parameterTypeArgumentsNameArray[i]=new String[typeArguments.size()];
                for(int j=0;j<parameterTypeArgumentsNameArray[i].length;++j){
                    //parameterTypeArgumentsArray[i][j]=typeArguments.get(j).toString();
                    parameterTypeArgumentsNameArray[i][j]=getRawType(typeArguments.get(j));
                }

            }
        }
        methodBean.setParameterTypeNames(parameterTypeNames);
        methodBean.setParameterTypeArgumentsNameArray(parameterTypeArgumentsNameArray);
        methodBean.setParameterAnnotationsArray(parameterAnnotationsArray);


    }

    private void parseParameterAnnotations(VariableElement variableElement,MethodBean methodBean,Annotation[][]parameterAnnotationsArray,int index){
        //Annotation[][]parameterAnnotationsArray=new Annotation[parameterSize][];
        int i=0;
        //Annotation[]parameterAnnotations=parameterAnnotationsArray[index];

        Class[]annotationClassArray={Url.class,Path.class,Query.class,QueryMap.class,Header.class,Field.class,
        FieldMap.class,Part.class,PartMap.class,Body.class};
        List<Annotation>annotationList=new ArrayList<>();

        for(Class annotationClass:annotationClassArray){
            Annotation[]annotations=variableElement.getAnnotationsByType(annotationClass);
            if(annotations!=null&&annotations.length>0){
                annotationList.add(annotations[0]);
            }
        }
        Annotation[]annotationArray=new Annotation[annotationList.size()];
        annotationList.toArray(annotationArray);
        parameterAnnotationsArray[index]=annotationArray;
        //methodBean.setParameterAnnotationsArray(parameterAnnotationsArray);
    }

    private void parseMethodAnnotations(ExecutableElement methodElement,MethodBean methodBean){
        //List<? extends AnnotationMirror>annotationMirrors=methodElement.getAnnotationMirrors();
        //Annotation[]methodAnnotations=new Annotation[methodElement.getAnnotationMirrors().size()];
        //List<Annotation>methodAnnotations=new ArrayList<>();
        if(methodElement.getAnnotation(DELETE.class)!=null){
            //methodAnnotations.add(methodElement.getAnnotation(DELETE.class));
            DELETE delete=methodElement.getAnnotation(DELETE.class);
            methodBean.parseHttpMethodAndPath("DELETE",delete.value(),false,messager);
        }
        if(methodElement.getAnnotation(GET.class)!=null){
            //methodAnnotations.add(methodElement.getAnnotation(GET.class));
            GET get=methodElement.getAnnotation(GET.class);
            methodBean.parseHttpMethodAndPath("GET",get.value(),false,messager);
        }
        if(methodElement.getAnnotation(HEAD.class)!=null){
            //methodAnnotations.add(methodElement.getAnnotation(HEAD.class));
            HEAD head=methodElement.getAnnotation(HEAD.class);
            methodBean.parseHttpMethodAndPath("HEAD",head.value(),false,messager);
            if (!"java.lang.Void".equals(methodBean.getResponseTypeName())) {
                messager.printMessage(Diagnostic.Kind.ERROR,"HEAD method must use Void as response type.");
            }
        }
        if(methodElement.getAnnotation(PATCH.class)!=null){
            //methodAnnotations.add(methodElement.getAnnotation(PATCH.class));
            PATCH patch=methodElement.getAnnotation(PATCH.class);
            methodBean.parseHttpMethodAndPath("PATCH",patch.value(),true,messager);
        }
        if(methodElement.getAnnotation(POST.class)!=null){
            //methodAnnotations.add(methodElement.getAnnotation(POST.class));
            POST post=methodElement.getAnnotation(POST.class);
            methodBean.parseHttpMethodAndPath("POST",post.value(),true,messager);
        }
        if(methodElement.getAnnotation(PUT.class)!=null){
            //methodAnnotations.add(methodElement.getAnnotation(PUT.class));
            PUT put=methodElement.getAnnotation(PUT.class);
            methodBean.parseHttpMethodAndPath("PUT",put.value(),true,messager);
        }
        if(methodElement.getAnnotation(OPTIONS.class)!=null){
            //methodAnnotations.add(methodElement.getAnnotation(OPTIONS.class));
            OPTIONS options=methodElement.getAnnotation(OPTIONS.class);
            methodBean.parseHttpMethodAndPath("OPTIONS",options.value(),false,messager);
        }
        if(methodElement.getAnnotation(HTTP.class)!=null){
            //methodAnnotations.add(methodElement.getAnnotation(HTTP.class));
            HTTP http=methodElement.getAnnotation(HTTP.class);
            methodBean.parseHttpMethodAndPath(http.method(),http.path(),http.hasBody(),messager);
        }
        if(methodElement.getAnnotation(Headers.class)!=null){
            //methodAnnotations.add(methodElement.getAnnotation(Headers.class));
            Headers headers=methodElement.getAnnotation(Headers.class);
            String[]headersToParse= headers.value();
            if(headersToParse.length==0){
                messager.printMessage(Diagnostic.Kind.ERROR,"@Headers annotation is empty.");
                return;
            }
            methodBean.setHeadersValue(headersToParse);
        }
        if(methodElement.getAnnotation(Multipart.class)!=null){
            //methodAnnotations.add(methodElement.getAnnotation(Multipart.class));
            if(methodBean.isFormEncoded()){
                messager.printMessage(Diagnostic.Kind.ERROR,"Only one encoding annotation is allowed.");
                return;
            }
            methodBean.setMultipart(true);
        }
        if(methodElement.getAnnotation(FormUrlEncoded.class)!=null){
            //methodAnnotations.add(methodElement.getAnnotation(FormUrlEncoded.class));
            if(methodBean.isMultipart()){
                messager.printMessage(Diagnostic.Kind.ERROR,"Only one encoding annotations is allowed.");
                return;
            }
            methodBean.setFormEncoded(true);
        }
        /*
        if(methodAnnotations.size()<1){
            return;
        }
        Annotation[]annotationArray=new Annotation[methodAnnotations.size()];
        methodBean.setMethodAnnotations(methodAnnotations.toArray(annotationArray));
        */
    }


    private void parseBaseConfig(TypeElement apiElement, ApiBean apiBean){
        BaseConfig config=apiElement.getAnnotation(BaseConfig.class);
        if(null==config){
            messager.printMessage(Diagnostic.Kind.ERROR,"Cannot get BaseConfig from api element");
            return;
        }
        apiBean.setBaseUrl(config.baseUrl());
        apiBean.setValidateEagerly(config.validateEagerly());
    }


    private void createApiProxy(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv, Messager messager) {
        //typeElement是注解类型,诸如Field,GET,POST,Query这样的
        for (TypeElement typeElement : annotations) {

            parseTypeElement(typeElement);

            parseGeneralAnnotation(typeElement,roundEnv);
        }
    }


    private void generateJavaFile(TypeElement typeElement){
        String packageName=getPackageName(processingEnv.getElementUtils(),typeElement);

        MethodSpec constructorMethod=MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class,"greeting")
                .addStatement("this.$N=$N","greeting","greeting")
                .build();

        TypeSpec apiProxy=TypeSpec.classBuilder(typeElement.getSimpleName()+"Proxy")
                .addModifiers(Modifier.PUBLIC)
                .addField(String.class,"greeting",Modifier.PRIVATE,Modifier.FINAL)
                .addMethod(constructorMethod)
                .build();

        JavaFile javaFile= JavaFile.builder(packageName,apiProxy).build();
        try{
            javaFile.writeTo(processingEnv.getFiler());
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private String getPackageName(Elements elementUtils,TypeElement type){
        PackageElement pkg=elementUtils.getPackageOf(type);
        if(pkg.isUnnamed()){
            return null;
        }
        return pkg.getQualifiedName().toString();
    }

    private String getOriginalType(TypeElement typeElement){
        if(null==typeElement){
            return null;
        }
        Types typeUtils=processingEnv.getTypeUtils();
        while(true){
            //typeElement为retrofit3.Call时,superClass为none,从而temp为null
            //typeElement为wang.imallen.allenretrofit.api.CustomCall时,superClass为wang.imallen.allenretrofit.api.TopCall<T>,
            // 但是需要注意的是此后temp为wang.imallen.allenretrofit.api.TopCall而不是wang.imallen.allenretrofit.api.TopCall<T>
            //而在typeElement为wang.imallen.allenretrofit.api.TopCall时,superClass为java.lang.Object而不是retrofit3.Call
            TypeMirror superClass=typeElement.getSuperclass();
            //typeElement为retrofit3.Call时,interfaces为{java.lang.Cloneable},typeElement为wang.imallen.allenretrofit.api.TopCall时,interfaces为{retrofit3.Call<T>,java.io.Serializable}
            List<? extends TypeMirror>interfaces=typeElement.getInterfaces();
            if(superClass==null){
                break;
            }else{
                TypeElement temp=(TypeElement)typeUtils.asElement(superClass);
                if(temp==null||"java.lang.Object".equals(temp.getQualifiedName().toString())){
                    break;
                }else{
                    typeElement=temp;
                }
            }
        }

        return typeElement.getQualifiedName().toString();
    }



    private void parseTypeElement(TypeElement typeElement) {
        Element enclosingElement = typeElement.getEnclosingElement();
        Name simpleName = typeElement.getSimpleName();
        //enclosedElements由getDeviceName(),getDeviceWeight(),getRecommandAppList(java.lang.String,java.lang.String,java.lang.String)组成
        List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
        List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
        NestingKind nestingKind = typeElement.getNestingKind();
        Name qualifiedName = typeElement.getQualifiedName();
        //this is a very important info
        TypeMirror typeMirror = typeElement.getSuperclass();
        List<? extends TypeParameterElement> typeParameterElements = typeElement.getTypeParameters();

        /*
        try {
            ClassLoader classLoader=RetrofitProcessor.class.getClassLoader();
            classLoader.loadClass(typeElement.getQualifiedName().toString());
            Class<?> clazz = Class.forName(typeElement.getQualifiedName().toString());
            Method[] methods = clazz.getMethods();
            Method[] declaredMethods = clazz.getDeclaredMethods();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        */

    }

    /**
     * 注意:variableElement可能有多个注解
     *
     * @param variableElement
     */
    private void parseVariableElement(VariableElement variableElement) {
        //variableSimpleName是如"ip","appleName"这样的字符串
        String variableSimpleName = variableElement.getSimpleName().toString();
        //typeMirror是像java.lang.String这样的类型
        TypeMirror typeMirror = variableElement.asType();
        //typeMirrorStr是如"java.lang.String"这样的字符串
        String typeMirrorStr = typeMirror.toString();

        //所以typeMirrorStr加上variableSimpleName就可以组成java.lang.String appleName;这样的
        if (variableElement.getAnnotation(Body.class) != null) {
            Body body = variableElement.getAnnotation(Body.class);

        } else if (variableElement.getAnnotation(Field.class) != null) {
            Field field = variableElement.getAnnotation(Field.class);
            String value = field.value();
            boolean encoded = field.encoded();
        } else if (variableElement.getAnnotation(FieldMap.class) != null) {
            FieldMap fieldMap = variableElement.getAnnotation(FieldMap.class);
            boolean encoded = fieldMap.encoded();
        } else if (variableElement.getAnnotation(Header.class) != null) {
            Header header = variableElement.getAnnotation(Header.class);
            String value = header.value();
        } else if (variableElement.getAnnotation(Part.class) != null) {
            Part part = variableElement.getAnnotation(Part.class);
            String value = part.value();
            String encoding = part.encoding();
        } else if (variableElement.getAnnotation(PartMap.class) != null) {
            PartMap partMap = variableElement.getAnnotation(PartMap.class);
            String encoding = partMap.encoding();
        } else if (variableElement.getAnnotation(Path.class) != null) {
            Path path = variableElement.getAnnotation(Path.class);
            String value = path.value();
            boolean encoded = path.encoded();
        } else if (variableElement.getAnnotation(Query.class) != null) {
            Query query = variableElement.getAnnotation(Query.class);
            boolean encoded = query.encoded();
            String value = query.value();
        } else if (variableElement.getAnnotation(QueryMap.class) != null) {
            QueryMap queryMap = variableElement.getAnnotation(QueryMap.class);
            boolean encoded = queryMap.encoded();
        } else if (variableElement.getAnnotation(Url.class) != null) {
            Url url = variableElement.getAnnotation(Url.class);

        }
    }

    private String getOriginalType(TypeMirror typeMirror){
        if(null==typeMirror){
            return null;
        }
        //typeMirror为rx.Observable<wang.imallen.allenretrofit.bean.AppResponse>时,typeElement为rx.Observable
        TypeElement typeElement=(TypeElement)processingEnv.getTypeUtils().asElement(typeMirror);
        return getOriginalType(typeElement);
    }

    private String getRawType(TypeMirror typeMirror){
        if(null==typeMirror){
            return null;
        }
        TypeElement typeElement=(TypeElement)processingEnv.getTypeUtils().asElement(typeMirror);
        return typeElement.getQualifiedName().toString();
    }


    //methodElment-->setVolume(java.lang.String,java.lang.String,java.lang.String)
    //注意:methodElement可能有多个注解
    private void parseMethodElement(ExecutableElement methodElement) {
        //simpleName类似setVolume
        System.out.println("simpleName:" + methodElement.getSimpleName());
        List<? extends VariableElement> parameters = methodElement.getParameters();
        List<? extends TypeParameterElement> typeParameters = methodElement.getTypeParameters();

        //typeMirror-->retrofit2.Call<okhttp3.ResponseBody>,rx.Observable<wang.imallen.allenretrofit.bean.AppResponse>,retrofit3.Call<wang.imallen.allenretrofit.bean.AppResponse>
        //要注意类似wang.imallen.allenretrofit.api.CustomCall<wang.imallen.allenretrofit.bean.AppResponse>这样的
        TypeMirror typeMirror = methodElement.getReturnType();
        //rawType-->"rx.Observable","retrofit3.Call"
        //String rawType= getOriginalType(typeMirror);
        //typeMirror是rxObservable<wang.imallen.allenretrofit.bean.AppResponse>时,rawType是rx.Observable
        String rawType=getRawType(typeMirror);

        //className类似"retrofit2.Call<okhttp3.ResponseBody>"这样
        String className = typeMirror.toString();
        try {
            Class<?> clazz2 = Class.forName("retrofit2.Call");
            //declaredType是像retrofit2.Call<java.lang.String>这样的
            DeclaredType declaredType = (DeclaredType) typeMirror;
            //typeArguments就是当declaredType是retrofit2.Call<java.lang.String>时,typeArguments为{java.lang.String}这种.
            //即declaredType为Call<Foo1,Foo2,Foo3...>时,typeArguments为{Foo1,Foo2,Foo3}这样
            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();

            Class<?> clazz = Class.forName(className);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        //returnTypeStr-->"retrofit2.Call<okhttp3.ResponseBody>"
        String returnTypeStr = typeMirror.toString();
        //enlosingElement-->core.AppleApi
        Element enclosingElement = methodElement.getEnclosingElement();
        //enclosedElements-->null
        List<? extends Element> enclosedElements = methodElement.getEnclosedElements();

        AnnotationValue defaultValue = methodElement.getDefaultValue();
        List<? extends TypeMirror> thrownTypes = methodElement.getThrownTypes();

        parseAnnotationsOfMethodElement(methodElement);


    }

    /**
     * 实际上只要考虑那几种可以作用于方法的Annotation即可
     * @param type
     * @param methodElement
     */
    private void parseAnnotationByType(DeclaredType type,ExecutableElement methodElement){
        if(null==type){
            return;
        }
        if(DELETE.class.getCanonicalName().equals(type.toString())){
            DELETE delete=methodElement.getAnnotation(DELETE.class);
        }else if(FormUrlEncoded.class.getCanonicalName().equals(type.toString())){
            FormUrlEncoded formUrlEncoded=methodElement.getAnnotation(FormUrlEncoded.class);
        }else if(GET.class.getCanonicalName().equals(type.toString())){
            GET get=methodElement.getAnnotation(GET.class);
        }else if(POST.class.getCanonicalName().equals(type.toString())){
            POST post=methodElement.getAnnotation(POST.class);
        }
    }

    private void parseAnnotationsOfMethodElement(ExecutableElement methodElement){
        //annotationMirrors是类似{retrofit3.annotation.http.FormUrlEncoded,retrofit3.annotation.http.GET("http://appstore.aginomoto.com/api/mobile/position")}这样的
        List<? extends AnnotationMirror> annotationMirrors = methodElement.getAnnotationMirrors();
        for(AnnotationMirror mirror:annotationMirrors){
            //name是类似"@retrofit3.annotation.http.FormUrlEncoded","@retrofit3.annotation.http.GET("/device/list")"
            String name=mirror.toString();
            //type是类似retrofit3.annotation.http.FormUrlEncoded,retrofit3.annotation.http.GET这样
            DeclaredType type=mirror.getAnnotationType();

            //由于FormUrlEncoded中无方法,故values此时为空;如果为GET,则values为{"value()"->""/device/list""}这样
            Map<? extends ExecutableElement,? extends AnnotationValue>values=mirror.getElementValues();
            System.out.println("name:"+name+",type:"+type.toString());
        }

        if (methodElement.getAnnotation(DELETE.class) != null) {

        } else if (methodElement.getAnnotation(FormUrlEncoded.class) != null) {
            FormUrlEncoded formUrlEncoded = methodElement.getAnnotation(FormUrlEncoded.class);


        } else if (methodElement.getAnnotation(GET.class) != null) {

        } else if (methodElement.getAnnotation(HEAD.class) != null) {

        } else if (methodElement.getAnnotation(Headers.class) != null) {

        } else if (methodElement.getAnnotation(HTTP.class) != null) {

        } else if (methodElement.getAnnotation(Multipart.class) != null) {

        } else if (methodElement.getAnnotation(OPTIONS.class) != null) {

        } else if (methodElement.getAnnotation(PATCH.class) != null) {

        } else if (methodElement.getAnnotation(POST.class) != null) {

        } else if (methodElement.getAnnotation(PUT.class) != null) {

        } else if (methodElement.getAnnotation(Streaming.class) != null) {

        }
    }


    private void parseGeneralAnnotation(TypeElement typeElement,RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(typeElement);
        //element是注解附着的元素,诸如AppleApi,cityId,app_name这样的
        for (Element element : elements) {
            //simpleName是诸如"cityId","converterFactories"这样的
            String simpleName = element.getSimpleName().toString();
            ElementKind kind = element.getKind();
            if (kind == ElementKind.CLASS) {

                TypeElement classElement = (TypeElement) element;

                parseTypeElement(classElement);

                //classesToSkip.add((TypeElement)element);
            } else if (kind == ElementKind.METHOD) {
                messager.printMessage(Diagnostic.Kind.NOTE, "found an ElementKind.METHOD element");
                ExecutableElement methodElement = (ExecutableElement) element;
                TypeElement classElement = (TypeElement) element.getEnclosingElement();
                parseMethodElement(methodElement);


            } else if (kind == ElementKind.PARAMETER) {
                messager.printMessage(Diagnostic.Kind.NOTE, "found an ElementKind.PARAMETER element");
                //variableElement是注解附着的参数元素,如@Field("id")String cityId中的cityId,和@Field("apple_name")String appleName中的appleName
                VariableElement variableElement = (VariableElement) element;
                parseVariableElement(variableElement);
                //methodElement是诸如getWeatherDetail(java.lang.String)这样的
                //要考虑到重载函数的情况
                ExecutableElement methodElement = (ExecutableElement) (variableElement.getEnclosingElement());
                //classElement是诸如core.MyProcessorTest这样的,此时getQualifiedName就是core.MyProcessorTest;
                TypeElement classElement = (TypeElement) methodElement.getEnclosingElement();
                System.out.println("classElement.getQualifiedName:" + classElement.getQualifiedName());

                parseMethodElement(methodElement);
                parseTypeElement(classElement);
            } else if (kind == ElementKind.FIELD) {
                //enclosingElement是core.DeviceApi这样
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                //name是converterFactories这样
                String name = element.getSimpleName().toString();
                //elementType是retrofit2.Converter.Factory[]这样
                TypeMirror elementType = element.asType();
                if (element instanceof VariableElement) {
                    System.out.println("element instanceof VariableElement");
                } else if (element instanceof TypeElement) {
                    System.out.println("element instanceof TypeElement");
                } else if (element instanceof TypeParameterElement) {
                    System.out.println("element instanceof TypeParameterElement");
                }


            }

        }
    }


}
