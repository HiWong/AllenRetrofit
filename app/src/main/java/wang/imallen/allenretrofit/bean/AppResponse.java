package wang.imallen.allenretrofit.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by allen on 16-8-27.
 */
public class AppResponse extends BaseHttpResponse implements Serializable {

    private String name;
    private int count;
    private String code;
    private String page;
    private int pagesize;
    private List<AppItem> positionitems;

    /**
     * @return 主题名字
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    /**
     * @return 推荐位代码
     */
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return 页码
     */
    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    /**
     * @return 每页大小
     */
    public int getPagesize() {
        return pagesize;
    }

    public void setPagesize(int pagesize) {
        this.pagesize = pagesize;
    }

    /**
     * @return the list 应用列表
     */
    public List<AppItem> getItems() {
        return positionitems;
    }

    /**
     * @param positionitems the list to set
     */
    public void setList(List<AppItem> positionitems) {
        this.positionitems = positionitems;
    }

    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();
        sb.append("name:");
        sb.append(name);
        sb.append(",count:");
        sb.append(count);
        sb.append(",code:");
        sb.append(code);
        sb.append(",page:");
        sb.append(page);
        sb.append(",pageSize:");
        sb.append(pagesize);
        return sb.toString();
    }

}


