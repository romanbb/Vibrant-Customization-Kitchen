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

    public DownloadFile(String url, String s, String t) {
        this.url = url;
        source = s;
        target = t;
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
}
