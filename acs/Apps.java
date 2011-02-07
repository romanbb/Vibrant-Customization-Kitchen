/*
 * Apps.java
 *
 * Created on Dec 30, 2010, 10:46:26 AM
 */
package acs;

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
import javax.swing.JCheckBox;
import javax.swing.UIManager;

/**
 *
 * @author roman
 */
public class Apps extends javax.swing.JFrame {
    public boolean debug = true;
    //static ArrayList<String> sources = new ArrayList<String>();
    static ArrayList<DownloadFile> files = new ArrayList<DownloadFile>();
    static ArrayList<Repo> repos = new ArrayList<Repo>();
    static ArrayList<DownloadFile> filesToDownload = new ArrayList<DownloadFile>();
    static ArrayList<Phone> phones = new ArrayList<Phone>();
    public static boolean useSystem, useData, useLib, useModem, wipeDalvik, useSD;
    static Apps instance;
    static String urlBase = "http://rbirg.com/vibrant/";
    static Repo selectedRepo = null;
    static Phone selectedPhone;
    static String systemPrefix = "system/app/";
    static Zip zipInstance;
    static Options optionsFrame;
    static boolean cwm3 = false;
    public static DownloadManager dlm;

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
            if(Apps.getInstance().debug) {
                fnf.printStackTrace();
            }
            //fnf.printStackTrace();
        } catch (IOException ex) {
            if(Apps.getInstance().debug) {
                ex.printStackTrace();
            }
            //ex.printStackTrace();
        }
    }

    private void initializeRepo(Phone phone) {

        try {
            this.statusLabel.setText("Loading repo list for: " + phone.getName());
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
        this.statusLabel.setText("");
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
        try {
            new File("kitchen/META-INF/com/google/android/").mkdirs();
            File f = new File("kitchen/META-INF/com/google/android/update-script");
            if (cwm3) {
                f = new File("kitchen/META-INF/com/google/android/updater-script");
            }
            //if(f.exists()) f.delete();
            FileWriter outFile = new FileWriter(f);
            PrintWriter out = new PrintWriter(outFile);
            int i = 1;


            //keep!
            for (DownloadFile dlf : files) {
                String dir = dlf.getTarget();
                if (dir.startsWith("data")) {
                    useData = true;
                } else if ((dir.startsWith("system"))) {
                    useSystem = true;
                } else if ((dir.startsWith("sdcard"))) {
                    useSD = true;
                }
                if (dlf.getType().equals("modem")) {
                    useModem = true;
                }
            }

            UpdateScript script = new UpdateScript(this.selectedPhone, cwm3);
            out.print(script.createScript());
            out.close();

            if (cwm3) {

                files.add(new DownloadFile(null, "kitchen/META-INF/com/google/android/updater-script", "META-INF/com/google/android/updater-script"));
            } else {
                files.add(new DownloadFile(null, "kitchen/META-INF/com/google/android/update-script", "META-INF/com/google/android/update-script"));
            }
        } catch (IOException ex) {
            if(Apps.getInstance().debug) {
                ex.printStackTrace();
            }

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
        jLabel3 = new javax.swing.JLabel();
        zipProgress = new javax.swing.JProgressBar();
        phoneList = new javax.swing.JComboBox();
        statusLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        console = new javax.swing.JTextArea();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        repoItems = new javax.swing.JList();
        addSelectedFilesButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        repoList = new javax.swing.JList();
        jPanel3 = new javax.swing.JPanel();
        generateZipButton = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jCheckBox1 = new javax.swing.JCheckBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();

        jFileChooser1.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Android Customization Suite 1.2.2"); // NOI18N
        setMinimumSize(new java.awt.Dimension(559, 400));
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

        jLabel3.setText("Choose your phone >>");

        zipProgress.setFocusable(false);

        phoneList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Restart Program!", "File didnt' download" }));
        phoneList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                phoneListActionPerformed(evt);
            }
        });

        statusLabel.setText("Status");

        console.setColumns(20);
        console.setEditable(false);
        console.setFont(new java.awt.Font("Monospaced", 0, 12));
        console.setLineWrap(true);
        console.setRows(5);
        console.setWrapStyleWord(true);
        jScrollPane1.setViewportView(console);

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(140);
        jSplitPane1.setContinuousLayout(true);
        jSplitPane1.setDoubleBuffered(true);

        jLabel9.setFont(jLabel9.getFont().deriveFont((float)12));
        jLabel9.setText("Items (select multiple with CTRL)");

        repoItems.setFont(repoItems.getFont().deriveFont((float)13));
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

        addSelectedFilesButton.setText("Add Selected Files");
        addSelectedFilesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSelectedFilesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 78, Short.MAX_VALUE)
                        .addComponent(addSelectedFilesButton)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 93, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addSelectedFilesButton))
                .addContainerGap())
        );

        jSplitPane1.setRightComponent(jPanel1);

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        repoList.setFont(repoList.getFont().deriveFont((float)13));
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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 140, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 148, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)))
        );

        jSplitPane1.setLeftComponent(jPanel2);

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

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(generateZipButton, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(generateZipButton, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(12, Short.MAX_VALUE))
        );

        jCheckBox1.setText("Clockwork 3!");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

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
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 535, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(phoneList, 0, 418, Short.MAX_VALUE)
                        .addContainerGap())
                    .addComponent(jSplitPane1, 0, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(zipProgress, javax.swing.GroupLayout.PREFERRED_SIZE, 213, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(statusLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 160, Short.MAX_VALUE)
                                .addComponent(jCheckBox1)))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(phoneList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(statusLabel)
                            .addComponent(jCheckBox1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zipProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
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
    private void processModem(DownloadFile f) {
        if (f.getType().equals("modem")) {
            useModem = true;
            DownloadFile redbend = new DownloadFile("http://www.rbirg.com/vibrant/kitchen/updates/redbend_ua", "kitchen/updates/redbend_ua",  "updates/redbend_ua", "redbend", "app");
            Zip.getInstance().addToQueue(redbend);
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
        dlm = new DownloadManager();


        if (cwm3) {
            addApp(new DownloadFile(
                    urlBase + "kitchen/META-INF/com/google/android/update-binary",
                    "kitchen/META-INF/com/google/android/update-binary",
                    "META-INF/com/google/android/update-binary"));
        }



        if (files.size() > 3 || useSystem || useData || wipeDalvik) {
            for (DownloadFile f : files) {
                Zip.getInstance().addToQueue(f);
                this.processModem(f);
            }

            Apps.getInstance().zipProgress.setVisible(true);
            //System.out.println("sources size before zip creation: " + sources.size());
            //if it exists, start over!
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
        } else {
            //DownloadManager dlm = new DownloadManager();
            //dlm.dlq.clear();

            processDownloads();
        }




    }//GEN-LAST:event_generateZipButtonActionPerformed

    public void processDownloads() {
        for (DownloadFile f : filesToDownload) {
            dlm.addToQueue(f);
        }
        if (!filesToDownload.isEmpty() && zipInstance != null) {

            Apps.getInstance().writeConsoleMessage("\n****Starting downloads that are not part of " + Apps.zipInstance.name + ".zip****");


        }
        dlm.execute();
    }

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
        System.out.println("adding " + f);
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
            if(Apps.getInstance().debug) {
                ex.printStackTrace();
            }
            //((DefaultListModel) repoItems.getModel()).clear();
            selectedRepo = null;
        } finally {

            //repoItems.setSelectedIndices();
            repaint();
        }
    }

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        initApp();
        selectedRepo = null;
        this.initializeRepo(phones.get(0));
        this.console.setText("");
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

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        if (((JCheckBox) evt.getSource()).isSelected()) {
            Apps.cwm3 = true;
        } else {
            Apps.cwm3 = false;
        }
}//GEN-LAST:event_jCheckBox1ActionPerformed

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
    public javax.swing.JTextArea console;
    public javax.swing.JButton generateZipButton;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JComboBox phoneList;
    private javax.swing.JList repoItems;
    private javax.swing.JList repoList;
    public javax.swing.JLabel statusLabel;
    public javax.swing.JProgressBar zipProgress;
    // End of variables declaration//GEN-END:variables
}
