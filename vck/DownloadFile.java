/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vck;

import java.io.File;

/**
 *
 * @author roman
 */
public class DownloadFile {

    String url, source, target;
    public String friendlyname;
    public String type;
    public String fileName;

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

    public void setTarget(String s) {
        File file = new File(this.getSource());
        String path = file.getPath(); //gets the file path
        int lastFolderIndex = path.lastIndexOf("\\"); //finds the last occurance of \ for the folder
        path = path.substring(0, lastFolderIndex); //the path without the file name
        new File(path).mkdirs(); //makes proper directories for the new file
        target = s + "\\" + file.getName(); //sets the new source
        
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

    public void setFriendlyname(String s) {
        friendlyname = s;
    }

    public String getType() {
        if (type == null) {
            return "";
        } else {
            return type;
        }
    }

    public String toString() {
        return this.getFriendlyname();
    }
}
