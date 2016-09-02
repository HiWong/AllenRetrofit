package wang.imallen.allenretrofit.bean;

import java.io.Serializable;

/**
 * Created by allen on 16-8-27.
 */
public class AppItem implements Serializable {

    private String title;
    private int score;
    private String intro;
    private String poster;
    private String background;
    private int cornercode;
    private String packname;
    private String download;
    private long size;
    private String vercode;

    /**
     * @return the title
     * 应用标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the score
     * 评分
     */
    public int getScore() {
        return score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * @return the intro
     * 应用介绍
     */
    public String getIntro() {
        return intro;
    }

    /**
     * @param intro the intro to set
     */
    public void setIntro(String intro) {
        this.intro = intro;
    }

    /**
     * @return the poster
     */
    public String getPoster() {
        return poster;
    }

    /**
     * @param poster the poster to set
     */
    public void setPoster(String poster) {
        this.poster = poster;
    }

    /**
     * @return the background
     */
    public String getBackground() {
        return background;
    }

    /**
     * @param background the background to set
     */
    public void setBackground(String background) {
        this.background = background;
    }

    /**
     * @return the cornercode
     */
    public int getCornercode() {
        return cornercode;
    }

    /**
     * @param cornercode the cornercode to set
     */
    public void setCornercode(int cornercode) {
        this.cornercode = cornercode;
    }

    /**
     * @return 包名
     */
    public String getPackname() {
        return packname;
    }

    /**
     * @param packname the packname to set
     */
    public void setPackname(String packname) {
        this.packname = packname;
    }

    /**
     * @return the download下载url
     */
    public String getDownload() {
        return download;
    }

    /**
     * @param download the download to set
     */
    public void setDownload(String download) {
        this.download = download;
    }

    /**
     * @return the size 应用文件大小
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size the size to set
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * @return the version
     */
    public String getVercode() {
        return vercode;
    }

    /**
     * @param vercode the version to set
     */
    public void setVercode(String vercode) {
        this.vercode = vercode;
    }
}
