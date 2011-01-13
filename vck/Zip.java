/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vck;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.String;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.apache.commons.io.IOUtils;



/**
 *
 * @author roman
 */
public class Zip extends SwingWorker<List<DownloadFile>, String> {

    List<DownloadFile> sources;
    String name;
    static Zip instance;
    static LinkedList<DownloadFile> dlq = new LinkedList<DownloadFile>();

    public Zip() {
        //this(new ArrayList<String>(), "");
    }

    public Zip(List<DownloadFile> s, String name) {
        if (instance == null) {
            instance = this;
        }
        sources = s;
        this.name = name;
    }

    public static Zip getInstance() {
        if (instance == null) {
            instance = new Zip();
        }
        return instance;
    }

    private static boolean fileExists(DownloadFile f) {
        //System.out.println("checking if exists: " + f.getSource());
        File file = new File(f.getSource());
        if (file.exists()) {
            //System.out.println(f.getSource() + " exists");
            try {
                File newMD5 = new File("kitchen/temp.md5");
                //newMD5.createNewFile();
                //System.out.println("created new temp md5 file");
                try {
                    VCKTools.download(new DownloadFile(f.getUrl() + ".md5", "kitchen/temp.md5", f.getTarget() + ".md5"));
                } catch (FileNotFoundException ex) {
                    Apps.getInstance().writeConsoleMessage(f.getTarget() + " has no MD5 on the server, going to download it for funsies!");
                    return false;
                }
                //check against current md5
                String online = VCKTools.readFile("kitchen/temp.md5");
                String local = VCKTools.readFile(f.getSource() + ".md5");
                if (online.equals(local)) {
                    Apps.getInstance().writeConsoleMessage(f.getTarget() + " matches the web server, skipping");
                    return true;
                } else {
                    Apps.getInstance().writeConsoleMessage(f.getTarget() + " MD5 mismatch");
                    Apps.getInstance().writeConsoleMessage("MD5 of online file: " + online);
                    Apps.getInstance().writeConsoleMessage("MD5 of local file: " + local);
                    new File(f.getSource()).delete();
                    new File(f.getSource() + ".md5").delete();
                    return false;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
        return false;
    }

    public List<DownloadFile> doInBackground() {
        //int j = 0;
        while (!dlq.isEmpty()) {
            //Apps.getInstance().writeConsoleMessage("downloading started....");
            //Apps.getInstance().generateZipButton.setEnabled(false);
            DownloadFile wf = dlq.poll();

            if (!fileExists(wf)) {
                try {
                    Apps.getInstance().writeConsoleMessage("downloading  " + wf.getTarget());
                    if (!VCKTools.download(wf)) {
                        Apps.getInstance().writeConsoleMessage(wf.getTarget() + " download failed");
                        Apps.getInstance().removeApp(wf.getTarget());
                    } else {
                    }
                } catch (FileNotFoundException fof) {
                    Apps.getInstance().writeConsoleMessage(wf.getTarget() + " was not found on the web server");
                    Apps.getInstance().removeApp(wf.getTarget());
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
            //int progress1 = (int) (((double) (j + 1) / dlq.size()) * 100);
            ////progress1-=50;
            //j++;
            //setProgress(progress1);
            if (dlq.isEmpty()) {
                Apps.getInstance().writeConsoleMessage("downloading finished!");
                // Apps.getInstance().generateZipButton.setEnabled(true);
                VCKTools.createSums();
            }
        }


        Apps.getInstance().createUpdateScript();//generate update script based on files in sources


        // Create a buffer for reading the files
        byte[] buf = new byte[1024];

        try {
            // Create the ZIP file
            String target = name;
            if (target.equals("")) {
                target = "flash";
            }
            target += ".zip";

            File f = new File(target);
            if (f.exists()) {
                f.delete();
            }
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(target));

            // Compress the files
            for (int i = 0; i < sources.size(); i++) {
                int progress = (int) (((double) (i + 1) / sources.size()) * 100);
                progress = (progress / 2) + 50;
                setProgress(progress);
                //Apps.setZipProgress(progress);


                //System.out.println("addin source to zip: " +  sources.get(i).substring(8));
                FileInputStream in = new FileInputStream(sources.get(i).getSource()); //file location

                // Add ZIP entry to output stream. Change the output of the zip file here
                out.putNextEntry(new ZipEntry(sources.get(i).getTarget()));

                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                // Complete the entry
                out.closeEntry();
                in.close();
            }

            // Complete the ZIP file
            out.close();
            Apps.getInstance().cleanUp();
            Apps.getInstance().writeConsoleMessage("zip generated!");
            Apps.getInstance().writeConsoleMessage("for best practices, try opening your zip to make sure it isn't somehow corrupt :)");
            JOptionPane.showMessageDialog(null, "Please, for the love of your phone, make a Nandroid backup.", "Success!", 1);
            Apps.getInstance().zipProgress.setVisible(false);
        } catch (FileNotFoundException fnf) {
            //JOptionPane.showMessageDialog(null, "Improper name.", "Error!", 0);
            fnf.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //Apps.zipProgress.setVisible(false);
            Apps.getInstance().removeApp("kitchen/META-INF/com/google/android/update-script");
            return sources;
        }
    }

    public void addToQueue(String url, String source, String target) {
        dlq.add(new DownloadFile(url, source, target));
    }

    public void addToQueue(DownloadFile f) {
        //Apps.getInstance().generateZipButton.setEnabled(false);
        dlq.add(f);
    }

    public void processDownload() {
        if (!dlq.isEmpty()) {
            //Apps.getInstance().writeConsoleMessage("downloading started....");
            //Apps.getInstance().generateZipButton.setEnabled(false);
            DownloadFile wf = dlq.poll();

            if (!fileExists(wf)) {
                try {
                    if (!VCKTools.download(wf)) {
                        System.out.println("download failed");
                        Apps.getInstance().removeApp(wf.getTarget());
                    }
                } catch (FileNotFoundException fof) {
                    Apps.getInstance().writeConsoleMessage(wf.getSource() + " was not found on the web server");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            if (dlq.isEmpty()) {
                Apps.getInstance().writeConsoleMessage("downloading finished!");
                // Apps.getInstance().generateZipButton.setEnabled(true);
            }
        } else {
            VCKTools.createSums();


        }
    }

    public void process() {
        processDownload();
    }

    public void unzipArchive(File archive, File outputDir) {
        try {
            ZipFile zipfile = new ZipFile(archive);
            for (Enumeration e = zipfile.entries(); e.hasMoreElements();) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                unzipEntry(zipfile, entry, outputDir);
            }
        } catch (Exception e) {
            System.out.println("Error while extracting file " + archive + ", " + e);
            e.printStackTrace();
        }
    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry, File outputDir) throws IOException {

        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        System.out.println("Extracting: " + entry);
        BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            IOUtils.copy(inputStream, outputStream);
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

    private void createDir(File dir) {
        System.out.println("Creating dir " + dir.getName());
        if (!dir.mkdirs()) {
            throw new RuntimeException("Can not create dir " + dir);
        }
    }

    public static void unGzip(String zipIn, String fileOut) throws IOException {
        File f = new File(zipIn );
        GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(f));

        OutputStream out = new FileOutputStream(new File(fileOut));

        byte[] buf = new byte[102400]; //size can be changed according to programmer's need.
        int len;
        while ((len = gzipInputStream.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }
}
