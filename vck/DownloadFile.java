/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vck;

/**
 *
 * @author roman
 */
public class DownloadFile {

    String url, source, target;
    public String friendlyname;
    public String type;

    public DownloadFile(String url, String s, String t) {
        this.url = url;
        source = s;
        target = t;
    }

    public DownloadFile(String u, String src, String tar, String name) {
        this(u, src, tar);
        friendlyname = name;
    }
    
    public DownloadFile(String u, String src, String tar, String name, String type) {
        this(u, src, tar, name);
        this.type = type;
    }


    public DownloadFile(String u, String src) {
        this.url = u;
        this.source = src;

    }

    public String getUrl() {
        return url;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public String getFriendlyname() {

        if (friendlyname != null) {
            return friendlyname;
        } else {
            return target;
        }
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return this.getFriendlyname();
    }
}
