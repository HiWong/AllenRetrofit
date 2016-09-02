package retrofit3.annotation.bean.parameter;

import retrofit3.annotation.bean.AnnotationBean;

/**
 * Created by allen on 16-9-2.
 */
public interface ParaAnnotationBean extends AnnotationBean{

    int URL = 0x10;
    int PATH = 0x11;
    int QUERY = 0x12;
    int QUERYMAP = 0x13;
    int HEADER = 0x14;
    int FIELD = 0x15;
    int FIELDMAP = 0x16;
    int PART = 0x17;
    int PARTMAP = 0x18;
    int BODY = 0x19;


}
