/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vck;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.String;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 *
 * @author roman
 */
public class Zip extends SwingWorker<List<DownloadFile>, String> {

    List<DownloadFile> sources;
    String name;
    static Zip instance;

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

    public List<DownloadFile> doInBackground() {
        //folders should be created when program starts, cleaned when it closes
        //file all files in /system/, /data/, and /META-INF/, add to sources
        //sources.clear();
        //sources.addAll(Apps.recursiveFileSearch(new File("META-INF")));
        //sources.addAll(Apps.recursiveFileSearch(new File("system")));
        //sources.addAll(Apps.recursiveFileSearch(new File("data")));
        Apps.createUpdateScript();//generate update script based on files in sources

        // Create a buffer for reading the files
        byte[] buf = new byte[1024];

        try {
            // Create the ZIP file
            String target = name;
            if (target.equals("")) {
                target = "flash";
            }
            target += ".zip";

            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(target));

            // Compress the files
            for (int i = 0; i < sources.size(); i++) {
                int progress = (int) (((double) (i + 1) / sources.size()) * 100);
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

            JOptionPane.showMessageDialog(null, "Please, for the love of your phone, make a Nandroid backup. \nThis is an alpha.", "Success!", 1);
            Apps.getInstance().zipProgress.setVisible(false);
        } catch (FileNotFoundException fnf) {
            JOptionPane.showMessageDialog(null, "Improper name.", "Error!", 0);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //Apps.zipProgress.setVisible(false);
            return sources;
        }
    }

    public void process() {
    }
}
