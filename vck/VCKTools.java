/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vck;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author roman
 */
public class VCKTools {

    public static byte[] createChecksum(String filename) throws
            Exception {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();
        return complete.digest();
    }

    // see this How-to for a faster way to convert
    // a byte array to a HEX string
    public static String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static void createSums() {
        ArrayList<String> filesFound = new ArrayList<String>();
        filesFound.addAll(recursiveFileSearch(new File("kitchen/")));
        ArrayList<String> filesToSkip = new ArrayList<String>();


        for (String s : filesFound) {
            if (s.endsWith(".md5")) {
                filesToSkip.add(s.substring(0, s.length() - 4));
                filesToSkip.add(s);
            }
            if(s.endsWith("update-script")) {
                filesToSkip.add(s);
            }

        }


        for (String s : filesFound) {
            if (!filesToSkip.contains(s)) {
                FileWriter fout = null;
                try {
                    File f = new File(s);
                    File fmd5 = new File(s + ".md5");
                    fout = new FileWriter(fmd5);
                    PrintWriter out = new PrintWriter(fout);
                    out.print(getMD5Checksum(s) + " " + f.getName());

                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        fout.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public static boolean download(DownloadFile f) throws IOException, FileNotFoundException {
        OutputStream out = null;
        URLConnection conn = null;
        InputStream in = null;


        // Get the URL
        URL url = new URL(f.getUrl());
        // Open an output stream to the destination file on our local filesystem
        out = new BufferedOutputStream(new FileOutputStream(f.getSource()));
        conn = url.openConnection();
        in = conn.getInputStream();

        // Get the data
        byte[] buffer = new byte[1024];
        int numRead;
        while ((numRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, numRead);
        }
        // file downloaded
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (FileNotFoundException fof) {
            Apps.getInstance().writeConsoleMessage(f.getSource() + " was not found on the web server");
            return false;
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return true;
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();


            for (int i = 0; i
                    < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));


                if (!success) {
                    return false;


                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();


    }

    public static List<String> recursiveFileSearch(File dir) {
        //File systemFolder = new File("");
        List<String> list = new ArrayList<String>();


        if (dir.isDirectory()) {
            for (File f : dir.listFiles()) {
                if (!f.isDirectory()) {
                    list.add(f.getPath());


                } else {
                    list.addAll(recursiveFileSearch(f));


                }
            }
        }
        return list;

    }

    public static String readFile(String filename) throws IOException {
        String lineSep = System.getProperty("line.separator");
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String nextLine = "";
        StringBuffer sb = new StringBuffer();
        while ((nextLine = br.readLine()) != null) {
            sb.append(nextLine);
            //
            // note:
            //   BufferedReader strips the EOL character
            //   so we add a new one!
            //
            sb.append(lineSep);
        }
        return sb.toString();
    }
}
