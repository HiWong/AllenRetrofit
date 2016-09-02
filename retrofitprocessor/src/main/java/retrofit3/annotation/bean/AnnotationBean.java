package retrofit3.annotation.bean;

/**
 * Created by allen on 16-9-1.
 */
public interface AnnotationBean {

    public static final int BODY = 0x1;
    public static final int DELETE = 0x2;
    public static final int FIELD = 0x3;
    public static final int FIELDMAP = 0x4;
    public static final int FORMURLENCODED = 0x5;
    public static final int GET = 0x6;
    public static final int HEAD = 0x7;
    public static final int HEADER = 0x8;
    public static final int HEADERS = 0x9;
    public static final int HTTP = 0xA;
    public static final int MULTIPART = 0xB;
    public static final int OPTIONS = 0xC;
    public static final int PART = 0xD;
    public static final int PARTMAP = 0xE;
    public static final int PATCH = 0xF;
    public static final int PATH = 0x10;
    public static final int POST = 0x11;
    public static final int PUT = 0x12;
    public static final int QUERY = 0x13;
    public static final int QUERYMAP = 0x14;
    public static final int STREAMING = 0x15;
    public static final int URL = 0x16;

    int annotationType();

}
