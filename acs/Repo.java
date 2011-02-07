package acs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import javax.swing.DefaultListModel;

/**
 *
 * @author roman
 */
public class Repo {

    public String name;
    protected String upkeeper;
    public String fileListURL;
    public ArrayList<DownloadFile> files = new ArrayList<DownloadFile>();
    protected File fileList;
    public DefaultListModel model;

    public Repo(String n, String fl, String uk) throws FileNotFoundException {
        name = n;
        fileListURL = fl;
        upkeeper = uk;
        model = new DefaultListModel();
        try {
            getFileList();
            processFiles();
        } catch (IOException e) {
            Apps.getInstance().writeConsoleMessage(name + " doesn't have a repo setup.");
        }
    }

    /*
     * downloads the file list from the internet for this developer
     */
    private void getFileList() throws IOException {
        //create the directory
        File workingDirectory = new File("kitchen/repo/");
        workingDirectory.mkdir();

        //now we need to download the file & assign it to a proper variable
        String filename = name + ".txt";
        VCKTools.download(new DownloadFile(fileListURL, workingDirectory + "/" + filename, null));
        fileList = new File(workingDirectory + "/" + filename);
    }

    /*
     * basically goes through the dev's file and either add's the files or whatever
     * different types for now:
     * app - a flashable
     * rom - will ignore the zipping mechanism and just download the file
     */
    private void processFiles() {
        try {
            Scanner s = new Scanner(fileList);
            s.useDelimiter("\\s*;\\s*");
            while (s.hasNext()) {
                String appname = s.next();
                String url = s.next();
                String source = s.next();
                String target = s.next();
                String type = s.next();
                

                files.add(new DownloadFile(url, source, target, appname, type));
            }
        } catch (FileNotFoundException ioex) {
            Apps.getInstance().writeConsoleMessage("Something went wrong... report bug to dev and be specific :)");
        } catch (NoSuchElementException e) {
            System.out.println("Error in phone: " + this.name);
        }
    }


    public String getName() {
        return name;


    }

    public void setName(String name) {
        this.name = name;


    }

    public String getFileListURL() {
        return fileListURL;


    }

    public void setFileListURL(String fileListURL) {
        this.fileListURL = fileListURL;


    }

    public List<DownloadFile> getFiles() {
        return files;


    }

    public String toString() {
        return name;
    }

    public static void main(String[] args) {
        for(int i = 0; i <= 100; i += 5) {
            double ratio = i / (double)100;
            int value = (int)(ratio * 28);
            System.out.println(i + " " + value);
        }
    }

}
