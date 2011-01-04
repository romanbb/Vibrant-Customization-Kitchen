/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vck;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 *
 * @author roman
 */
public class Zip extends SwingWorker<List<String>, String> {

    List<String> sources;
    String name;

    public Zip(List<String> s, String name) {
        sources = s;
        this.name = name;
    }

    public List<String> doInBackground() {
        //folders should be created when program starts, cleaned when it closes
        //file all files in /system/, /data/, and /META-INF/, add to sources
        sources.clear();
        sources.addAll(Apps.recursiveFileSearch(new File("META-INF")));
        sources.addAll(Apps.recursiveFileSearch(new File("system")));
        sources.addAll(Apps.recursiveFileSearch(new File("data")));
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
                System.out.println(progress);
                setProgress(progress);
                //Apps.setZipProgress(progress);

                FileInputStream in = new FileInputStream(sources.get(i));

                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(sources.get(i)));

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
