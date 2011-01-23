/*
 * Apps.java
 *
 * Created on Dec 30, 2010, 10:46:26 AM
 */
package vck;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.UIManager;

/**
 *
 * @author roman
 */
public class Apps extends javax.swing.JFrame {

    //static ArrayList<String> sources = new ArrayList<String>();
    static ArrayList<DownloadFile> files = new ArrayList<DownloadFile>();
    static ArrayList<Repo> repos = new ArrayList<Repo>();
    static ArrayList<DownloadFile> filesToDownload = new ArrayList<DownloadFile>();
    static ArrayList<Phone> phones = new ArrayList<Phone>();
    public static boolean useSystem, useData, useLib, useModem, wipeDalvik;
    static Apps instance;
    static String urlBase = "http://rbirg.com/vibrant/";
    static Repo selectedRepo = null;
    static Phone selectedPhone;
    static String systemPrefix = "system/app/";
    static Zip zipInstance;
    static Options optionsFrame;

    public Apps() {
        if (instance == null) {
            instance = this;
        }
        initComponents();
        initApp();
        optionsFrame = new Options();
        optionsFrame.setVisible(false);
    }

    public static Apps getInstance() {
        if (instance == null) {
            instance = new Apps();
        }
        return instance;
    }

    public static void initApp() {
        Apps.getInstance().setLocationRelativeTo(null);
        Apps.getInstance().zipProgress.setVisible(false);
//        Apps.getInstance().initializeRepo();
        Options.getInstance().optionsChangeSystemLocButton.setEnabled(false);
        Apps.getInstance().statusLabel.setText("");


        Apps.getInstance().cleanUp();
        //VCKTools.createSums();
        //Download1.getInstance().execute();
        Apps.getInstance().createDirectories();
        Apps.getInstance().getPhones();
    }

    private void getPhones() {
        try {
            DownloadFile phoneListDLF = new DownloadFile("http://rbirg.com/android/phonelist.txt", "kitchen/repo/phones.txt");
            int i = 0;
            while (!VCKTools.fileExists(phoneListDLF)) {
                //System.out.println(i);
                VCKTools.download(phoneListDLF);
                i++;
                if (i == 5) {
                    break;
                }

            }
            File masterlist = new File("kitchen/repo/phones.txt");
            Scanner s = null;
            DefaultComboBoxModel m = new DefaultComboBoxModel();
            try {
                s = new Scanner(masterlist);
                s.useDelimiter("\\s*;\\s*");
                while (s.hasNext()) {
                    String name = s.next();
                    String url = s.next();
                    //String upkeeper = s.next();
                    if (!name.startsWith("#")) {
                        phones.add(new Phone(name, url));
                        //phones..add(new Repo(devName, repoUrl, upkeeper));
                        m.addElement(phones.get(phones.size() - 1));
                    }
                }
                //phoneList.setmod
                phoneList.setModel(m);
                repaint();
                if (!phones.isEmpty()) {
                    initializeRepo(phones.get(0));
                    selectedPhone = phones.get(0);
                }
            } catch (FileNotFoundException ex) {
            }
            //all repos have been added, now add to the lists!
        } catch (FileNotFoundException fnf) {
            this.writeConsoleMessage("Couldn't download the phone list from the server. \n ***Please restart the program.*****");
            //fnf.printStackTrace();
        } catch (IOException ex) {
            //ex.printStackTrace();
        }
    }

    private void initializeRepo(Phone phone) {
        try {
            VCKTools.download(new DownloadFile(phone.getRepoListURL(), "kitchen/repo/" + phone.getName() + ".txt"));
            File masterlist = new File("kitchen/repo/" + phone.getName() + ".txt");
            Scanner s = null;
            DefaultListModel m = new DefaultListModel();
            try {
                s = new Scanner(masterlist);
                s.useDelimiter("\\s*;\\s*");
                while (s.hasNext()) {
                    String devName = s.next();
                    String repoUrl = s.next();
                    String upkeeper = s.next();
                    if (!devName.startsWith("#")) {
                        phone.getRepos().add(new Repo(devName, repoUrl, upkeeper));
                        m.addElement(phone.getRepos().get(phone.getRepos().size() - 1));
                    }
                }
                repoList.setModel(m);
            } catch (FileNotFoundException ex) {
            }
            //all repos have been added, now add to the lists!

            for (Repo repo : phone.getRepos()) {
                repo.model.addElement(repo);

                if (selectedRepo == null || selectedRepo.getFiles().isEmpty()) {
                    repo.model.addElement("No files found");
                } else {
                    for (DownloadFile i : selectedRepo.getFiles()) {
                        repo.model.addElement(i);
                    }
                }
            }
            repoList.repaint();
        } catch (FileNotFoundException fnf) {
            this.writeConsoleMessage("Couldn't download the repo list from the server or it is empty. \n ***Please reselect the phone.*****");
            repoList.setModel(new DefaultListModel());
            repoItems.setModel(new DefaultListModel());
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            this.statusLabel.setText("");
        }

    }

    public void enableButton() {
        this.generateZipButton.setEnabled(true);
    }

    public void disableButton() {
        this.generateZipButton.setEnabled(false);
    }

    private void createDirectories() {
        String prefix = "kitchen/";

        new File(prefix + "system/app").mkdirs();
        new File(prefix + "data/app").mkdirs();
        new File(prefix + "system/lib").mkdirs();
        new File(prefix + "updates").mkdirs();
        new File(prefix + "META-INF/com/google/android").mkdirs();
        new File(prefix + "repo").mkdirs();

        addApp(urlBase + "kitchen/META-INF/CERT.RSA", "kitchen/META-INF/CERT.RSA", "META-INF/CERT.RSA");
        addApp(urlBase + "kitchen/META-INF/CERT.SF", "kitchen/META-INF/CERT.SF", "META-INF/CERT.SF");
        addApp(urlBase + "kitchen/META-INF/MANIFEST.MF", "kitchen/META-INF/MANIFEST.MF", "META-INF/MANIFEST.MF");
    }

    public void unselectAll() {

//        for (Component c : utilitiesPanel.getComponents()) {
//            try {
//                ((JCheckBox) c).setSelected(false);
//            } catch (ClassCastException cce) {
//            }
//        }

//        for (Component c : customFlashPanel.getComponents()) {
//            try {
//                ((JCheckBox) c).setSelected(false);
//            } catch (ClassCastException cce) {
//            }
//        }
//
//        for (Component c : launchersPanel.getComponents()) {
//            try {
//                ((JCheckBox) c).setSelected(false);
//            } catch (ClassCastException cce) {
//            }
//        }

//        for (Component c : vibrantApps.getComponents()) {
//            try {
//                ((JCheckBox) c).setSelected(false);
//            } catch (ClassCastException cce) {
//            }
//        }

//        for (Component c : systemPanel.getComponents()) {
//            try {
//                ((JCheckBox) c).setSelected(false);
//            } catch (Exception e) {
//                try {
//                    ((JComboBox) c).setSelectedIndex(0);
//                } catch (Exception e1) {
//                }
//            }
//        }

        files.clear();
        filesToDownload.clear();
        //initializeRepo();
        //((DefaultListModel) repoItems.getModel()).clear();
        //updateRepoItemList();
        //this.console.setText("");

        repaint();


    }

    public void cleanUp() {
        String prefix = "kitchen/";
        new File("kitchen/META-INF/com/google/android/").delete();
        removeApp("system/app/Mms.apk");
        files.clear();


        //removeApp("META-INF/com/google/android/update-script");
        //new File("kitchen/temp.md5").delete();

//        deleteDir(new File(prefix + "system"));
//        deleteDir(new File(prefix + "data"));
//        deleteDir(new File(prefix + "META-INF"));
    }

    public void createUpdateScript() {
        //check whether to use data and system
//        for (DownloadFile entry : files) {
//            if (!useData && entry.getTarget().startsWith("data")) {
//                useData = true;
//                //System.out.println(entry + " setting data to true");
//                break;
//            }
//            if (!useSystem && entry.getTarget().startsWith("system")) {
//                useSystem = true;
//                //System.out.println(entry + " setting system to true");
//                break;
//            }
//        }

        //writing the script


        try {
            new File("kitchen/META-INF/com/google/android/").mkdirs();
            File f = new File("kitchen/META-INF/com/google/android/update-script");
            //if(f.exists()) f.delete();
            FileWriter outFile = new FileWriter(f);
            PrintWriter out = new PrintWriter(outFile);
            int i = 1;

            String systemDir;

            for (DownloadFile dlf : files) {
                String dir = dlf.getTarget();
                if (dir.equals("data")) {
                    useData = true;
                } else if ((dir.equals("system"))) {
                    useSystem = true;
                }
            }

            if (wipeDalvik) {
                out.println("show_progress 0." + i + " 0");
                out.println("delete_recursive DATA:dalvik-cache");
                out.println("show_progress 0." + i + " 10\n");
                i++;
            }

            if (!files.isEmpty()) {
                out.println("show_progress 0." + i + " 0");
            }
            if (useData) {
                out.println("copy_dir PACKAGE:data DATA:");
            }
            if (useSystem) {
                out.println("copy_dir PACKAGE:system SYSTEM:");
            }
            for (DownloadFile entry : files) {

                if (!useData && entry.getTarget().startsWith("data")) {
                    useData = true;
                    out.println("copy_dir PACKAGE:data DATA:");

                }
                if (!useSystem && entry.getTarget().startsWith("system")) {
                    useSystem = true;
                    out.println("copy_dir PACKAGE:system SYSTEM:");
                    break;
                }
            }

            out.println("show_progress 0." + i + " 10\n");
            i++;

            //modem
            if (useModem) {
                out.println("show_progress 0." + i + " 0");
                out.println("copy_dir PACKAGE:updates TMP:/updates");
                out.println("set_perm 0 0 755 TMP:/updates/redbend_ua");
                out.println("run_program /tmp/updates/redbend_ua restore /tmp/updates/modem.bin /dev/block/bml12");
                out.println("show_progress 0." + i + " 10");
                i++;
            }

            //out.println("show_progress 0." + i + " 0");
//            if (useSystem) {
//                out.println("copy_dir PACKAGE:system SYSTEM:");
//            }
//            if (useData) {
//                out.println("copy_dir PACKAGE:data DATA:");
//            }
            //out.println("show_progress 0." + i + " 10");
            out.close();

            files.add(new DownloadFile(null, "kitchen/META-INF/com/google/android/update-script", "META-INF/com/google/android/update-script"));
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        MmsGroup = new javax.swing.ButtonGroup();
        ModemGroup = new javax.swing.ButtonGroup();
        jFileChooser1 = new javax.swing.JFileChooser();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        repoPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        repoList = new javax.swing.JList();
        jScrollPane3 = new javax.swing.JScrollPane();
        repoItems = new javax.swing.JList();
        jLabel9 = new javax.swing.JLabel();
        addSelectedFilesButton = new javax.swing.JButton();
        phoneList = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        console = new javax.swing.JTextArea();
        generateZipButton = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        zipProgress = new javax.swing.JProgressBar();
        statusLabel = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();

        jFileChooser1.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Android Customization Suite 1.0");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        repoList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Restart program", "Please!" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        repoList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        repoList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                repoListMouseClicked(evt);
            }
        });
        repoList.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                repoListPropertyChange(evt);
            }
        });
        jScrollPane2.setViewportView(repoList);

        repoItems.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Select a repo from the left", "<<<" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        repoItems.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                repoItemsMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(repoItems);

        jLabel9.setText("Items (select multiple with CTRL)");

        addSelectedFilesButton.setText("Add Selected Files");
        addSelectedFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSelectedFilesButtonActionPerformed(evt);
            }
        });

        phoneList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Restart Program!", "File didnt' download" }));
        phoneList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneListActionPerformed(evt);
            }
        });

        jLabel3.setText("Choose your phone >>");

        console.setColumns(20);
        console.setEditable(false);
        console.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        console.setLineWrap(true);
        console.setRows(5);
        console.setWrapStyleWord(true);
        jScrollPane1.setViewportView(console);

        generateZipButton.setText("Generate ZIP");
        generateZipButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateZipButtonActionPerformed(evt);
            }
        });

        jButton2.setText("Start Over");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        zipProgress.setFocusable(false);

        statusLabel.setText("Status");

        javax.swing.GroupLayout repoPanelLayout = new javax.swing.GroupLayout(repoPanel);
        repoPanel.setLayout(repoPanelLayout);
        repoPanelLayout.setHorizontalGroup(
            repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(repoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, repoPanelLayout.createSequentialGroup()
                        .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, repoPanelLayout.createSequentialGroup()
                                    .addComponent(jLabel9)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(addSelectedFilesButton))
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 342, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(phoneList, 0, 342, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, repoPanelLayout.createSequentialGroup()
                        .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(zipProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 225, Short.MAX_VALUE)
                            .addComponent(statusLabel))
                        .addGap(18, 18, 18)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(generateZipButton, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        repoPanelLayout.setVerticalGroup(
            repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(repoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(phoneList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(addSelectedFilesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(generateZipButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(repoPanelLayout.createSequentialGroup()
                        .addComponent(statusLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(zipProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jMenu1.setText("File");

        jMenuItem2.setText("Exit");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Options");
        jMenu2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu2ActionPerformed(evt);
            }
        });

        jMenuItem1.setText("Show Options");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Help");

        jMenuItem3.setText("About");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem3);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(repoPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(repoPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

//    private void processMms() {
//        if (systemMms.getSelectedItem().equals("AOSP Mms")) {
//            String url = "http://www.rbirg.com/vibrant/kitchen/system/app/Mms-AOSP.apk";
//            String source = "kitchen/system/app/Mms-AOSP.apk";
//            String target = "system/app/Mms.apk";
//            //System.out.println("adding aosp mms");
//            addApp(url, source, target);
//        } else if (systemMms.getSelectedItem().equals("TouchWiz Mms")) {
//            String url = "http://www.rbirg.com/vibrant/kitchen/system/app/Mms-TW.apk";
//            String source = "kitchen/system/app/Mms-TW.apk";
//            String target = "system/app/Mms.apk";
//            addApp(url, source, target);
//        } else {
//            removeApp("system/app/Mms.apk");
//        }
//
//    }
    private void processModem() {
//        if (systemModem.getSelectedItem().equals("JL5")) {
//            String url = "http://www.rbirg.com/vibrant/kitchen/updates/JL5.bin";
//            String source = "kitchen/updates/JL5.bin";
//            String target = "updates/modem.bin";
//            addApp(url, source, target);
//            useModem = true;
//        } else if (systemModem.getSelectedItem().equals("JL4")) {
//            String url = "http://www.rbirg.com/vibrant/kitchen/updates/JL4.bin";
//            String source = "kitchen/updates/JL4.bin";
//            String target = "updates/modem.bin";
//            addApp(url, source, target);
//            useModem = true;
//        } else if (systemModem.getSelectedItem().equals("JL1")) {
//            String url = "http://www.rbirg.com/vibrant/kitchen/updates/JL1.bin";
//            String source = "kitchen/updates/JL1.bin";
//            String target = "updates/modem.bin";
//            addApp(url, source, target);
//            useModem = true;
//        } else if (systemModem.getSelectedItem().equals("JL4")) {
//            String url = "http://www.rbirg.com/vibrant/kitchen/updates/JK6.bin";
//            String source = "kitchen/updates/JK6.bin";
//            String target = "updates/modem.bin";
//            addApp(url, source, target);
//            useModem = true;
//        } else if (systemModem.getSelectedItem().equals("JK2")) {
//            String url = "http://www.rbirg.com/vibrant/kitchen/updates/JK2.bin";
//            String source = "kitchen/updates/JK2.bin";
//            String target = "updates/modem.bin";
//            addApp(url, source, target);
//            useModem = true;
//        } else {
//            removeApp("update/modem.bin");
//        }
//        if (useModem) {
//            addApp("http://www.rbirg.com/vibrant/kitchen/updates/redbend_ua", "kitchen/updates/redbend_ua", "updates/redbend_ua");
//        }

        for (DownloadFile f : files) {
            if (f.getType().equals("modem")) {
                useModem = true;
            }
        }
        if (useModem) {
            addApp("http://www.rbirg.com/vibrant/kitchen/updates/redbend_ua", "kitchen/updates/redbend_ua", "updates/redbend_ua");
        }
    }

//    private void processCalendar() {
//        if (systemEmail.getSelectedItem().equals("TouchWiz Email")) {
//            String url = "http://www.rbirg.com/vibrant/kitchen/system/app/Calendar-TW.apk";
//            String source = "kitchen/system/app/Calendar-TW.apk";
//            String target = "system/app/Calendar.apk";
//
//            addApp(url, source, target);
//            addApp(urlBase + "/kitchen/system/app/CalendarProvider-TW.apk", "/kitchen/system/app/CalendarProvider-TW.apk", "system/app/CalendarProvider.apk");
//
//        } else if (systemEmail.getSelectedItem().equals("AOSP Email")) {
//            String url = "http://www.rbirg.com/vibrant/kitchen/system/app/Calendar-AOSP.apk";
//            String source = "kitchen/system/app/Calendar-AOSP.apk";
//            String target = "system/app/Calendar.apk";
//
//            addApp(url, source, target);
//            addApp(urlBase + "/kitchen/system/app/CalendarProvider-AOSP.apk", "/kitchen/system/app/CalendarProvider-AOSP.apk", "system/app/CalendarProvider.apk");
//        } else {
//
//            removeApp("system/app/Mms.apk");
//        }
//    }
    private void generateZipButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateZipButtonActionPerformed
        //after files are downloaded they need to be zipped, so this has to be called from the zip thread
        //downloadFiles();
//        processMms();
//        processModem();



        if (files.size() > 3 || useSystem || useData || wipeDalvik) {
            for (DownloadFile f : files) {
                Zip.getInstance().addToQueue(f);
            }

            Apps.getInstance().zipProgress.setVisible(true);
            //System.out.println("sources size before zip creation: " + sources.size());
            if (zipInstance != null) {
                zipInstance.cancel(true);
            }
            zipInstance = new Zip(Apps.files, Options.zipName.getText());
            zipInstance.addPropertyChangeListener(
                    new PropertyChangeListener() {

                        public void propertyChange(PropertyChangeEvent evt) {
                            if ("progress".equals(evt.getPropertyName())) {
                                Apps.getInstance().zipProgress.setValue((Integer) evt.getNewValue());
                            }
                        }
                    });

            zipInstance.execute();
        }

        for (DownloadFile f : filesToDownload) {
            DownloadManager.getInstance().addToQueue(f);
        }
        DownloadManager dlm = DownloadManager.getInstance();
        dlm.execute();

    }//GEN-LAST:event_generateZipButtonActionPerformed

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    private void addApp(DownloadFile f) {
        //File folder = new File(f.getSource());
        files.add(f);
    }

    private void addApp(String s, String t) {
        addApp(null, s, t);
    }

    private void addApp(String url, String sourceLoc, String targetLoc) {
        //this.getInstance().generateZipButton.setEnabled(false);
        DownloadFile f = new DownloadFile(url, sourceLoc, targetLoc);
        File folder = new File(sourceLoc);


        files.add(f);
        //System.out.println("added file to queue");
//        if (url != null) {
//            Download1.getInstance().addToQueue(f);
//        }
    }

    public void removeApp(String target) {

        HashSet<DownloadFile> h = new HashSet<DownloadFile>(files);
        files.clear();
        files.addAll(h);

        for (int i = 0; i < files.size(); i++) {
            if (files.get(i).getTarget().equals(target)) {
                files.remove(i);
                break;
            }

        }

    }

    public void setZipProgress(int n) {
        zipProgress.setValue(n);
        //System.out.println("set to " + n);
        zipProgress.repaint();
    }

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        initApp();
    }//GEN-LAST:event_formComponentShown

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cleanUp();
    }//GEN-LAST:event_formWindowClosing

    public void makeDirs(String f) {
        File file = new File(f);
        String path = file.getPath();
        int lastFolderIndex = path.lastIndexOf("\\");
        if (lastFolderIndex >= 0) {
            path = path.substring(0, lastFolderIndex);
            new File(path).mkdirs();
        }
    }

    private void updateRepoItemList(Phone phone) {
        //grab the repo associated with the one selected
        DefaultListModel m = null;
        try {
            List<Repo> repos = phone.getRepos();
            selectedRepo = repos.get(repoList.getSelectedIndex());

            //update the right list
            m = selectedRepo.model;
            m.clear();
            //fill the list
            if (selectedRepo == null || selectedRepo.getFiles().isEmpty()) {
                m.addElement("No files");
            } else {

                for (DownloadFile i : selectedRepo.getFiles()) {
                    boolean showFile = true;


                    //check files, if it's in there, don't show
                    for (DownloadFile f : files) {
                        if (f.getFriendlyname().equals(i.getFriendlyname())) {
                            showFile = false;
                        }
                    }
                    //check filestodl, if it's in there don't show
                    for (DownloadFile f : filesToDownload) {
                        if (f.getFriendlyname().equals(i.getFriendlyname())) {
                            showFile = false;
                        }
                    }

                    Enumeration en = m.elements();
                    while (en.hasMoreElements()) {
                        DownloadFile nextElement = (DownloadFile) en.nextElement();
                        if (nextElement.getFriendlyname().equals(i.getFriendlyname())) {
                            showFile = false;
                        }
                    }

                    if (showFile) {
                        m.addElement(i);
                    }
                }
            }
            repoItems.setModel(m);
        } catch (IndexOutOfBoundsException ex) {
            //((DefaultListModel) repoItems.getModel()).clear();
            selectedRepo = null;
        } finally {

            //repoItems.setSelectedIndices();
            repaint();
        }
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed


        unselectAll();
        initApp();
        selectedRepo = null;
        repoList.setSelectedIndex(0);
//        this.updateRepoItemList();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void addSelectedFilesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSelectedFilesButtonActionPerformed
        //        for (Object i : repoItems.getSelectedValues()) {
        //            DownloadFile dlf = (DownloadFile) i;
        //            addApp(dlf);
        //            selectedRepo.model.removeElement(i);
        //            this.writeConsoleMessage("added " + dlf.friendlyname);
        //        }
        for (Object i : repoItems.getSelectedValues()) {
            DownloadFile fName = (DownloadFile) i;
            for (DownloadFile f : selectedRepo.getFiles()) {
                if (fName.getFriendlyname().equals(f.getFriendlyname())) {
                    makeDirs(f.getSource());
                    if (fName.getType().equals("rom")) {
                        filesToDownload.add(f);
                        this.writeConsoleMessage("Adding " + f + " to queue.");
                        selectedRepo.model.removeElement(i);
                    } else if (fName.getType().equals("category")) {
                    } else {
                        addApp(f);
                        this.writeConsoleMessage("Adding " + f + " to queue.");
                        selectedRepo.model.removeElement(i);
                    }
                }
            }

        }
}//GEN-LAST:event_addSelectedFilesButtonActionPerformed

    private void repoItemsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_repoItemsMouseClicked
        //        updateRepoItemList();
}//GEN-LAST:event_repoItemsMouseClicked

    private void repoListPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_repoListPropertyChange
}//GEN-LAST:event_repoListPropertyChange

    private void repoListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_repoListMouseClicked
        updateRepoItemList(selectedPhone);
}//GEN-LAST:event_repoListMouseClicked

    private void jMenu2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu2ActionPerformed
    }//GEN-LAST:event_jMenu2ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        optionsFrame.setVisible(true);
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void phoneListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phoneListActionPerformed
        ComboBoxModel m = phoneList.getModel();
        this.selectedPhone = (Phone) m.getSelectedItem();
        initializeRepo(selectedPhone);
    }//GEN-LAST:event_phoneListActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        new About().setVisible(true);
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    public void writeConsoleMessage(String s) {
        if (console.getText().equals("")) {
            console.append(s);
        } else {
            console.append("\n" + s);
            console.setCaretPosition(console.getDocument().getLength());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Look and feel failure");
        }

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new Apps().setVisible(true);
            }
        });

    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup MmsGroup;
    private javax.swing.ButtonGroup ModemGroup;
    private javax.swing.JButton addSelectedFilesButton;
    private javax.swing.JTextArea console;
    public javax.swing.JButton generateZipButton;
    private javax.swing.JButton jButton2;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JComboBox phoneList;
    private javax.swing.JList repoItems;
    private javax.swing.JList repoList;
    private javax.swing.JPanel repoPanel;
    public javax.swing.JLabel statusLabel;
    public javax.swing.JProgressBar zipProgress;
    // End of variables declaration//GEN-END:variables
}
