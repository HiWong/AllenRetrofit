package wang.imallen.allenretrofit.bean.douban;

import java.io.Serializable;
import java.util.List;

/**
 * Created by allen on 16-9-4.
 */
public class DouBanPlayList implements Serializable {

    private int r;
    private int version_max;
    private int is_show_quick_start;
    private List<SongBean> song;

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getVersion_max() {
        return version_max;
    }

    public void setVersion_max(int version_max) {
        this.version_max = version_max;
    }

    public int getIs_show_quick_start() {
        return is_show_quick_start;
    }

    public void setIs_show_quick_start(int is_show_quick_start) {
        this.is_show_quick_start = is_show_quick_start;
    }

    public List<SongBean> getSong() {
        return song;
    }

    public void setSong(List<SongBean> song) {
        this.song = song;
    }

    public static class SongBean implements Serializable {
        private String album;
        private int status;
        private String picture;
        private String ssid;
        private String artist;
        private String url;
        private String title;
        private int length;
        private int like;
        private String subtype;
        private String public_time;
        private String sid;
        private String aid;
        private String file_ext;
        private String sha256;
        private String kbps;
        private String albumtitle;
        private String alert_msg;
        private List<SingersBean> singers;

        public String getAlbum() {
            return album;
        }

        public void setAlbum(String album) {
            this.album = album;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getPicture() {
            return picture;
        }

        public void setPicture(String picture) {
            this.picture = picture;
        }

        public String getSsid() {
            return ssid;
        }

        public void setSsid(String ssid) {
            this.ssid = ssid;
        }

        public String getArtist() {
            return artist;
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public int getLike() {
            return like;
        }

        public void setLike(int like) {
            this.like = like;
        }

        public String getSubtype() {
            return subtype;
        }

        public void setSubtype(String subtype) {
            this.subtype = subtype;
        }

        public String getPublic_time() {
            return public_time;
        }

        public void setPublic_time(String public_time) {
            this.public_time = public_time;
        }

        public String getSid() {
            return sid;
        }

        public void setSid(String sid) {
            this.sid = sid;
        }

        public String getAid() {
            return aid;
        }

        public void setAid(String aid) {
            this.aid = aid;
        }

        public String getFile_ext() {
            return file_ext;
        }

        public void setFile_ext(String file_ext) {
            this.file_ext = file_ext;
        }

        public String getSha256() {
            return sha256;
        }

        public void setSha256(String sha256) {
            this.sha256 = sha256;
        }

        public String getKbps() {
            return kbps;
        }

        public void setKbps(String kbps) {
            this.kbps = kbps;
        }

        public String getAlbumtitle() {
            return albumtitle;
        }

        public void setAlbumtitle(String albumtitle) {
            this.albumtitle = albumtitle;
        }

        public String getAlert_msg() {
            return alert_msg;
        }

        public void setAlert_msg(String alert_msg) {
            this.alert_msg = alert_msg;
        }

        public List<SingersBean> getSingers() {
            return singers;
        }

        public void setSingers(List<SingersBean> singers) {
            this.singers = singers;
        }

        public static class SingersBean implements Serializable {
            private String name;
            private String name_usual;
            private String avatar;
            private int related_site_id;
            private boolean is_site_artist;
            private String id;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getName_usual() {
                return name_usual;
            }

            public void setName_usual(String name_usual) {
                this.name_usual = name_usual;
            }

            public String getAvatar() {
                return avatar;
            }

            public void setAvatar(String avatar) {
                this.avatar = avatar;
            }

            public int getRelated_site_id() {
                return related_site_id;
            }

            public void setRelated_site_id(int related_site_id) {
                this.related_site_id = related_site_id;
            }

            public boolean isIs_site_artist() {
                return is_site_artist;
            }

            public void setIs_site_artist(boolean is_site_artist) {
                this.is_site_artist = is_site_artist;
            }

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            @Override public String toString() {
                return "SingersBean{" +
                        "name='" + name + '\'' +
                        ", name_usual='" + name_usual + '\'' +
                        ", avatar='" + avatar + '\'' +
                        ", related_site_id=" + related_site_id +
                        ", is_site_artist=" + is_site_artist +
                        ", id='" + id + '\'' +
                        '}';
            }
        }

        @Override public String toString() {
            return "SongBean{" +
                    "album='" + album + '\'' +
                    ", status=" + status +
                    ", picture='" + picture + '\'' +
                    ", ssid='" + ssid + '\'' +
                    ", artist='" + artist + '\'' +
                    ", url='" + url + '\'' +
                    ", title='" + title + '\'' +
                    ", length=" + length +
                    ", like=" + like +
                    ", subtype='" + subtype + '\'' +
                    ", public_time='" + public_time + '\'' +
                    ", sid='" + sid + '\'' +
                    ", aid='" + aid + '\'' +
                    ", file_ext='" + file_ext + '\'' +
                    ", sha256='" + sha256 + '\'' +
                    ", kbps='" + kbps + '\'' +
                    ", albumtitle='" + albumtitle + '\'' +
                    ", alert_msg='" + alert_msg + '\'' +
                    ", singers=" + singers +
                    '}';
        }
    }

    @Override public String toString() {
        return "DouBanPlayList{" +
                "r=" + r +
                ", version_max=" + version_max +
                ", is_show_quick_start=" + is_show_quick_start +
                ", song=" + song +
                '}';
    }
}
