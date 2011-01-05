/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vck;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingWorker;

/**
 *
 * @author roman
 */
public class Download extends SwingWorker<List<DownloadFile>, String> {

    static LinkedList<DownloadFile> dlq = new LinkedList<DownloadFile>();
    boolean keepGoing = true;
    static boolean isDownloading;
    static Download instance;

    public Download() {
        if (instance == null) {
            instance = this;
        }
    }

    public static Download getInstance() {
        if (instance == null) {
            instance = new Download();
        }
        return instance;
    }


    public List<DownloadFile> doInBackground() {
        while (!dlq.isEmpty()) {
            //try {
            DownloadFile wf = dlq.poll();

            Apps.getInstance().setStatus("Downloading " + wf.getSource());
            System.out.println("downloading " + wf.getUrl());
            download(wf);

            Apps.getInstance().setStatus("Done downloading");
            //}
        }
        return dlq;
    }

    public void addToQueue(String url, String source, String target) {
        dlq.add(new DownloadFile(url, source, target));
    }

    public static void download(DownloadFile f) {
        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;
        try {
            // Get the URL
            URL url = new URL(f.getUrl());
            // Open an output stream to the destination file on our local filesystem
            System.out.println("downloading from " + f.getUrl() + " to " + f.getSource());
            out = new BufferedOutputStream(new FileOutputStream(f.getSource()));
            conn = url.openConnection();
            in = conn.getInputStream();

            // Get the data
            isDownloading = true;
            byte[] buffer = new byte[1024];
            int numRead;
            while ((numRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, numRead);
            }
            // file downloaded

        } catch (Exception exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            isDownloading = false;
        }
    }

    public static void main(String[] args) {
        //download("http://rbirg.com/vibrant/vck2.PNG", "vck.png");
    }
}
