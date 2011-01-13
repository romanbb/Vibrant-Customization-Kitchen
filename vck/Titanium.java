/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vck;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author roman
 */
public class Titanium {

    public String packageName;
    public String dataFileName;
    public String apkGzipFileName;
    private String apkFileName;
    private String apkMD5;
    ArrayList<String> fileList = new ArrayList<String>();
    String guiName;

    public Titanium() {
    }

    public Titanium(String packageName) {
        this.packageName = packageName;
    }

    public void getProperties() {
        FileInputStream in = null;
        try {
            File f = new File("titanium/");
            String pName = null;

            //loop once to get property file
            for (String s : f.list()) {
                String fileName = s;
                if (fileName.startsWith(getPackageName()) && fileName.endsWith(".properties")) {
                    pName = s;

                }
            }

            Properties defaultProps = new Properties();
            in = new FileInputStream("titanium/" + pName);
            defaultProps.load(in);

            //get all the properties desired
            setApkFileName(defaultProps.getProperty("market.package_name") + ".apk");
            setGuiName(defaultProps.getProperty("app_gui_label"));
            //in.close();

            //loop again to set all the proper names, etc
            for (String s : f.list()) {
                //fileList.add(s);

                String fileName = s;
                //System.out.println(fileName);

                if (fileName.startsWith(getPackageName()) && fileName.endsWith(".tar.gz")) {
                    setDataFileName(fileName);

                } else if (fileName.startsWith(getPackageName()) && fileName.endsWith(".apk.gz")) {
                    setApkGzipFileName(fileName);
                    String md5 = fileName.substring(this.getApkFileName().length() - 3, fileName.length() - 7);

                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();

        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(Titanium.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static void main(String[] args) {
        Titanium t = new Titanium("bt.android.elixir");
        t.getProperties();
        //t.unzipApk();

    }

    public boolean unzipApk() {
        try {
            //Zip.getInstance().unzipArchive(new File("titanium/" + getApkFileName()), new File("titanium/tmp"));
            Zip.unGzip("titanium/" + getApkGzipFileName(), getApkFileName());

            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean unzipData() {

        return false;
    }

    public void setGuiName(String name) {
        this.guiName = name;
    }

    public String getGuiName() {
        return this.guiName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDataFileName() {
        return dataFileName;
    }

    public void setDataFileName(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public String getApkGzipFileName() {
        return apkGzipFileName;
    }

    public void setApkGzipFileName(String apkFileName) {
        this.apkGzipFileName = apkFileName;
    }

    public String getApkFileName() {
        return apkFileName;
    }

    public void setApkFileName(String apkFileName) {
        this.apkFileName = apkFileName;
    }

    public String getApkMD5() {
        return apkMD5;
    }

    public void setApkMD5(String apkMD5) {
        this.apkMD5 = apkMD5;
    }
}
