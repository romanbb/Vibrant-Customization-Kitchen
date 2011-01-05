/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vck;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
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

    private static boolean fileExists(DownloadFile f) {
        //System.out.println("checking if exists: " + f.getSource());
        File file = new File(f.getSource());
        if (file.exists()) {
            //System.out.println(f.getSource() + " exists");
            try {
                File newMD5 = new File("kitchen/temp.md5");
                //System.out.println("created new temp md5 file");
                try {
                    VCKTools.download(new DownloadFile(f.getUrl() + ".md5", "kitchen/temp.md5", f.getTarget() + ".md5"));
                } catch (FileNotFoundException ex) {
                    Apps.getInstance().writeConsoleMessage(f.getSource() + " has no MD5 on the server, going to download it for funsies!");
                    return false;
                }

                //check against current md5
                String online = VCKTools.readFile("kitchen/temp.md5");
                String local = VCKTools.readFile(f.getSource() + ".md5");
                Apps.getInstance().writeConsoleMessage(online + " - " + local);
                if (online.equals(local)) {
                    Apps.getInstance().writeConsoleMessage(f.getSource() + " matches the web server, skipping");
                    return true;
                } else {
                    Apps.getInstance().writeConsoleMessage(f.getSource() + "MD5 mismatch");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
        return false;
    }
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

    public void start() {
        if (!keepGoing) {
            this.execute();
        }
        keepGoing = true;
    }

    public void stop() {
        keepGoing = false;
    }

    public List<DownloadFile> doInBackground() {
        while (keepGoing) {
            if (!dlq.isEmpty()) {
                //Apps.getInstance().writeConsoleMessage("downloading started....");
                Apps.getInstance().generateZipButton.setEnabled(false);
                DownloadFile wf = dlq.poll();

                if (!fileExists(wf)) {
                    download(wf);
                }

                if (dlq.isEmpty()) {
                    Apps.getInstance().writeConsoleMessage("downloading finished!");
                }
            } else {
                VCKTools.createSums();
                Apps.getInstance().generateZipButton.setEnabled(true);
            }

        }
        return dlq;

    }

    public void addToQueue(String url, String source, String target) {
        dlq.add(new DownloadFile(url, source, target));
    }

    public void addToQueue(DownloadFile f) {
        Apps.getInstance().generateZipButton.setEnabled(false);
        dlq.add(f);
    }

    public static void download(DownloadFile f) {
        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;

        try {

            // Get the URL
            URL url = new URL(f.getUrl());
            // Open an output stream to the destination file on our local filesystem
            Apps.getInstance().writeConsoleMessage("downloading " + f.getSource());
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

        }
    }
}
