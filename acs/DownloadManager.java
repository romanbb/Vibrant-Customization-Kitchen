/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package acs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingWorker;

/**
 *
 * @author roman
 */
public class DownloadManager extends SwingWorker<List<DownloadFile>, String> {

    static LinkedList<DownloadFile> dlq;
    boolean keepGoing = true;
    static boolean isDownloading;
    static DownloadManager instance;

    public DownloadManager() {
        if (instance == null) {
            instance = this;
        }
        dlq = new LinkedList<DownloadFile>();
    }

    public static DownloadManager getInstance() {
        if (instance == null) {
            instance = new DownloadManager();
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
        //while (keepGoing) {
        Apps.getInstance().disableButton();
        while (!dlq.isEmpty()) {
            //Apps.getInstance().writeConsoleMessage("downloading started....");
            Apps.getInstance().disableButton();
            DownloadFile wf = dlq.poll();
            //createProperDirectory(wf);
//            Apps.getInstance().writeConsoleMessage("Downloading " + wf.getFriendlyname());

            if (!VCKTools.fileExists(wf)) {
                try {

                    if (!VCKTools.download(wf)) {
                        Apps.getInstance().writeConsoleMessage("download failed");
                        //Apps.getInstance().removeApp(wf.getTarget());
                    }
                } catch (FileNotFoundException fof) {
                    Apps.getInstance().writeConsoleMessage(wf.getSource() + " was not found on the web server");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

//            } else {
            //VCKTools.createSums();
        }
        if (dlq.isEmpty()) {
            Apps.getInstance().writeConsoleMessage("downloading finished!");
            //Apps.getInstance().initApp();
            Apps.getInstance().enableButton();
            Apps.getInstance().statusLabel.setText("");
            //Apps.getInstance().generateZipButton.setEnabled(true);
        }

        //}
        return dlq;

    }

   

    public void addToQueue(DownloadFile f) {
//        Apps.getInstance().generateZipButton.setEnabled(false);
        dlq.add(f);
    }
}
