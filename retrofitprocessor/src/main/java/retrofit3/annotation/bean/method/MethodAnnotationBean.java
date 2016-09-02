package retrofit3.annotation.bean.method;


import retrofit3.annotation.bean.AnnotationBean;

/**
 * Created by allen on 16-9-2.
 */
public interface MethodAnnotationBean extends AnnotationBean {

    int DELETE = 0x1;
    int GET = 0x2;
    int HEAD = 0x3;
    int PATCH = 0x4;
    int POST = 0x5;
    int PUT = 0x6;
    int OPTIONS = 0x7;
    int HTTP = 0x8;
    int HEADERS = 0x9;
    int MULTIPART = 0xA;
    int FORMURLENCODED = 0xB;
    int STREAMING=0xC;

    //int annotationType();
}
