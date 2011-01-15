/*
 * Apps.java
 *
 * Created on Dec 30, 2010, 10:46:26 AM
 */
package vck;

import java.awt.Component;
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
import java.util.Scanner;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
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
    static boolean useSystem, useData, useLib, useModem, wipeDalvik;
    static Apps instance;
    static String urlBase = "http://rbirg.com/vibrant/";
    static Repo selectedRepo = null;
    static String systemPrefix = "system/app/";

    public Apps() {
        if (instance == null) {
            instance = this;
        }
        setLocationRelativeTo(null);
        initComponents();
        Apps.getInstance().zipProgress.setVisible(false);
        initializeRepo();
        this.optionsChangeSystemLocButton.setEnabled(false);
        this.statusLabel.setText("");
    }

    public static Apps getInstance() {
        if (instance == null) {
            instance = new Apps();
        }
        return instance;
    }

    private void initializeRepo() {
        try {
            VCKTools.download(new DownloadFile("http://rbirg.com/vibrant/kitchen/repo/masterlist.txt", "kitchen/repo/masterlist.txt"));
            File masterlist = new File("kitchen/repo/masterlist.txt");
            Scanner s = null;
            DefaultListModel m = new DefaultListModel();
            try {
                s = new Scanner(masterlist);
                s.useDelimiter("\\s*;\\s*");
                while (s.hasNext()) {
                    String devName = s.next();
                    String repoUrl = s.next();
                    String upkeeper = s.next();
                    repos.add(new Repo(devName, repoUrl, upkeeper));
                    m.addElement(repos.get(repos.size() - 1));
                }
                repoList.setModel(m);
            } catch (FileNotFoundException ex) {
            }
            //all repos have been added, now add to the lists!

            for (Repo repo : repos) {
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
            this.writeConsoleMessage("Couldn't download the repo list from the server. \n ***Please restart the program.*****");
            fnf.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

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

        for (Component c : utilitiesPanel.getComponents()) {
            try {
                ((JCheckBox) c).setSelected(false);
            } catch (ClassCastException cce) {
            }
        }

        for (Component c : customFlashPanel.getComponents()) {
            try {
                ((JCheckBox) c).setSelected(false);
            } catch (ClassCastException cce) {
            }
        }

        for (Component c : launchersPanel.getComponents()) {
            try {
                ((JCheckBox) c).setSelected(false);
            } catch (ClassCastException cce) {
            }
        }

        for (Component c : vibrantApps.getComponents()) {
            try {
                ((JCheckBox) c).setSelected(false);
            } catch (ClassCastException cce) {
            }
        }

        for (Component c : systemPanel.getComponents()) {
            try {
                ((JCheckBox) c).setSelected(false);
            } catch (Exception e) {
                try {
                    ((JComboBox) c).setSelectedIndex(0);
                } catch (Exception e1) {
                }
            }
        }

        files.clear();
        filesToDownload.clear();
        //initializeRepo();
        //((DefaultListModel) repoItems.getModel()).clear();
        updateRepoItemList();
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
        generateZipButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        utilitiesPanel = new javax.swing.JPanel();
        utilitiesTitaniumButton = new javax.swing.JCheckBox();
        utilitiesQuickBootButton = new javax.swing.JCheckBox();
        utilitiesRomManagerButton = new javax.swing.JCheckBox();
        utilitiesSpareParts = new javax.swing.JCheckBox();
        utilitiesSgsTools = new javax.swing.JCheckBox();
        utilitiesWirelessAdb = new javax.swing.JCheckBox();
        customFlashPanel = new javax.swing.JPanel();
        utilitiesSystem = new javax.swing.JCheckBox();
        utilitiesData = new javax.swing.JCheckBox();
        wipeDalvikCacheToggle = new javax.swing.JCheckBox();
        launchersPanel = new javax.swing.JPanel();
        launchersLauncherPro = new javax.swing.JCheckBox();
        launchersAdw = new javax.swing.JCheckBox();
        launchersGingerbread = new javax.swing.JCheckBox();
        launcherTouchWiz = new javax.swing.JCheckBox();
        launchersZeam = new javax.swing.JCheckBox();
        miscGingerbreadkb = new javax.swing.JCheckBox();
        miscDockHome = new javax.swing.JCheckBox();
        miscCarHome = new javax.swing.JCheckBox();
        vibrantApps = new javax.swing.JPanel();
        vibrantAllShare = new javax.swing.JCheckBox();
        vibrantAvatar = new javax.swing.JCheckBox();
        vibrantMediahub = new javax.swing.JCheckBox();
        vibrantWriteandGo = new javax.swing.JCheckBox();
        vibrantGogo = new javax.swing.JCheckBox();
        vibrantLayar = new javax.swing.JCheckBox();
        vibrantMiniDiary = new javax.swing.JCheckBox();
        vibrantKindle = new javax.swing.JCheckBox();
        vibrantAmazon = new javax.swing.JCheckBox();
        vibrantMemo = new javax.swing.JCheckBox();
        vibrantWifiCalling = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        VibrantVisualVoicemail = new javax.swing.JCheckBox();
        vibrantThinkFree = new javax.swing.JCheckBox();
        vibrantTelenav = new javax.swing.JCheckBox();
        vibrantSlacker = new javax.swing.JCheckBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        vibrantTmoTV = new javax.swing.JCheckBox();
        systemPanel = new javax.swing.JPanel();
        systemMms = new javax.swing.JComboBox();
        systemEmail = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        systemModem = new javax.swing.JComboBox();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        systemEmail1 = new javax.swing.JComboBox();
        repoPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        repoList = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        repoItems = new javax.swing.JList();
        jLabel9 = new javax.swing.JLabel();
        addSelectedFilesButton = new javax.swing.JButton();
        optionsTab = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        optionsSystemAppsLocationField = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        optionsChangeSystemLocButton = new javax.swing.JButton();
        zipName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        zipProgress = new javax.swing.JProgressBar();
        jScrollPane1 = new javax.swing.JScrollPane();
        console = new javax.swing.JTextArea();
        jButton2 = new javax.swing.JButton();
        statusLabel = new javax.swing.JLabel();

        jFileChooser1.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Vibrant Customization Kitchen");
        setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
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

        generateZipButton.setText("Generate ZIP");
        generateZipButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateZipButtonActionPerformed(evt);
            }
        });

        jTabbedPane1.setFocusable(false);

        utilitiesTitaniumButton.setText("Titanium Backup");
        utilitiesTitaniumButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                utilitiesTitaniumButtonActionPerformed(evt);
            }
        });

        utilitiesQuickBootButton.setText("Quick Boot");
        utilitiesQuickBootButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                utilitiesQuickBootButtonActionPerformed(evt);
            }
        });

        utilitiesRomManagerButton.setText("ROM Manager");
        utilitiesRomManagerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                utilitiesRomManagerButtonActionPerformed(evt);
            }
        });

        utilitiesSpareParts.setText("Spare Parts");
        utilitiesSpareParts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                utilitiesSparePartsActionPerformed(evt);
            }
        });

        utilitiesSgsTools.setText("SGS Tools");
        utilitiesSgsTools.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                utilitiesSgsToolsActionPerformed(evt);
            }
        });

        utilitiesWirelessAdb.setText("Wireless ADB");
        utilitiesWirelessAdb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                utilitiesWirelessAdbActionPerformed(evt);
            }
        });

        customFlashPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Custom Flashable"));

        utilitiesSystem.setText("system");
        utilitiesSystem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                utilitiesSystemActionPerformed(evt);
            }
        });

        utilitiesData.setText("data");
        utilitiesData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                utilitiesDataActionPerformed(evt);
            }
        });

        wipeDalvikCacheToggle.setText("wipe Dalvik");
        wipeDalvikCacheToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wipeDalvikCacheToggleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout customFlashPanelLayout = new javax.swing.GroupLayout(customFlashPanel);
        customFlashPanel.setLayout(customFlashPanelLayout);
        customFlashPanelLayout.setHorizontalGroup(
            customFlashPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customFlashPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(customFlashPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(utilitiesSystem)
                    .addComponent(utilitiesData)
                    .addComponent(wipeDalvikCacheToggle))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        customFlashPanelLayout.setVerticalGroup(
            customFlashPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customFlashPanelLayout.createSequentialGroup()
                .addComponent(utilitiesSystem)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(utilitiesData)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addComponent(wipeDalvikCacheToggle)
                .addContainerGap())
        );

        javax.swing.GroupLayout utilitiesPanelLayout = new javax.swing.GroupLayout(utilitiesPanel);
        utilitiesPanel.setLayout(utilitiesPanelLayout);
        utilitiesPanelLayout.setHorizontalGroup(
            utilitiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(utilitiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(utilitiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(utilitiesTitaniumButton)
                    .addComponent(utilitiesQuickBootButton)
                    .addComponent(utilitiesRomManagerButton)
                    .addComponent(utilitiesSpareParts)
                    .addComponent(utilitiesSgsTools)
                    .addComponent(utilitiesWirelessAdb))
                .addGap(110, 110, 110)
                .addComponent(customFlashPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18))
        );
        utilitiesPanelLayout.setVerticalGroup(
            utilitiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(utilitiesPanelLayout.createSequentialGroup()
                .addGroup(utilitiesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(utilitiesPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(utilitiesTitaniumButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(utilitiesQuickBootButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(utilitiesRomManagerButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(utilitiesSpareParts)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(utilitiesSgsTools)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(utilitiesWirelessAdb))
                    .addGroup(utilitiesPanelLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(customFlashPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Utilities", utilitiesPanel);

        launchersLauncherPro.setText("LauncherPro 8.2");
        launchersLauncherPro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                launchersLauncherProActionPerformed(evt);
            }
        });

        launchersAdw.setText("ADW Launcher");
        launchersAdw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                launchersAdwActionPerformed(evt);
            }
        });

        launchersGingerbread.setText("Gingerbread Launcher");
        launchersGingerbread.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                launchersGingerbreadActionPerformed(evt);
            }
        });

        launcherTouchWiz.setText("TouchWiz Launcher");
        launcherTouchWiz.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                launcherTouchWizActionPerformed(evt);
            }
        });

        launchersZeam.setText("Zeam");
        launchersZeam.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                launchersZeamActionPerformed(evt);
            }
        });

        miscGingerbreadkb.setText("Gingerbread Keyboard");
        miscGingerbreadkb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miscGingerbreadkbActionPerformed(evt);
            }
        });

        miscDockHome.setText("Dock Home");
        miscDockHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miscDockHomeActionPerformed(evt);
            }
        });

        miscCarHome.setText("Car Home");
        miscCarHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miscCarHomeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout launchersPanelLayout = new javax.swing.GroupLayout(launchersPanel);
        launchersPanel.setLayout(launchersPanelLayout);
        launchersPanelLayout.setHorizontalGroup(
            launchersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(launchersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(launchersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(launchersPanelLayout.createSequentialGroup()
                        .addGroup(launchersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(launchersLauncherPro)
                            .addComponent(launchersAdw)
                            .addComponent(launchersGingerbread))
                        .addGap(8, 8, 8)
                        .addGroup(launchersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(miscCarHome)
                            .addComponent(miscDockHome)
                            .addComponent(miscGingerbreadkb)))
                    .addComponent(launcherTouchWiz)
                    .addComponent(launchersZeam))
                .addContainerGap())
        );
        launchersPanelLayout.setVerticalGroup(
            launchersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(launchersPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(launchersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(launchersPanelLayout.createSequentialGroup()
                        .addComponent(miscCarHome)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(miscDockHome)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(miscGingerbreadkb))
                    .addGroup(launchersPanelLayout.createSequentialGroup()
                        .addComponent(launchersLauncherPro)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(launchersAdw)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(launchersGingerbread)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(launcherTouchWiz)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(launchersZeam)
                .addContainerGap(41, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Launchers & Misc", launchersPanel);

        vibrantApps.setEnabled(false);

        vibrantAllShare.setText("AllShare");
        vibrantAllShare.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vibrantAllShareActionPerformed(evt);
            }
        });

        vibrantAvatar.setText("Avatar");
        vibrantAvatar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vibrantAvatarActionPerformed(evt);
            }
        });

        vibrantMediahub.setText("Mediahub");
        vibrantMediahub.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vibrantMediahubActionPerformed(evt);
            }
        });

        vibrantWriteandGo.setText("Write and Go");
        vibrantWriteandGo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vibrantWriteandGoActionPerformed(evt);
            }
        });

        vibrantGogo.setText("GoGo");
        vibrantGogo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vibrantGogoActionPerformed(evt);
            }
        });

        vibrantLayar.setText("Layar");
        vibrantLayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vibrantLayarActionPerformed(evt);
            }
        });

        vibrantMiniDiary.setText("Mini Diary");
        vibrantMiniDiary.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vibrantMiniDiaryActionPerformed(evt);
            }
        });

        vibrantKindle.setText("Kindle");
        vibrantKindle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vibrantKindleActionPerformed(evt);
            }
        });

        vibrantAmazon.setText("Amazon");
        vibrantAmazon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vibrantAmazonActionPerformed(evt);
            }
        });

        vibrantMemo.setText("Memo");
        vibrantMemo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vibrantMemoActionPerformed(evt);
            }
        });

        vibrantWifiCalling.setText("Wifi Calling");
        vibrantWifiCalling.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vibrantWifiCallingActionPerformed(evt);
            }
        });

        jCheckBox2.setText("Audio Postcard");

        VibrantVisualVoicemail.setText("Visual Voicemail");
        VibrantVisualVoicemail.setEnabled(false);
        VibrantVisualVoicemail.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                VibrantVisualVoicemailActionPerformed(evt);
            }
        });

        vibrantThinkFree.setText("Think Free");
        vibrantThinkFree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vibrantThinkFreeActionPerformed(evt);
            }
        });

        vibrantTelenav.setText("Telenav");
        vibrantTelenav.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vibrantTelenavActionPerformed(evt);
            }
        });

        vibrantSlacker.setText("Slacker");
        vibrantSlacker.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vibrantSlackerActionPerformed(evt);
            }
        });

        jCheckBox5.setText("My Account");
        jCheckBox5.setEnabled(false);

        vibrantTmoTV.setText("T-Mobile TV");
        vibrantTmoTV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vibrantTmoTVActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout vibrantAppsLayout = new javax.swing.GroupLayout(vibrantApps);
        vibrantApps.setLayout(vibrantAppsLayout);
        vibrantAppsLayout.setHorizontalGroup(
            vibrantAppsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(vibrantAppsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(vibrantAppsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(vibrantAvatar)
                    .addComponent(vibrantAllShare)
                    .addComponent(vibrantAmazon)
                    .addComponent(jCheckBox2)
                    .addComponent(vibrantGogo)
                    .addComponent(vibrantKindle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(vibrantAppsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(vibrantAppsLayout.createSequentialGroup()
                        .addGroup(vibrantAppsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(vibrantAppsLayout.createSequentialGroup()
                                .addGroup(vibrantAppsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(vibrantMiniDiary)
                                    .addComponent(vibrantLayar)
                                    .addComponent(vibrantMediahub)
                                    .addComponent(vibrantMemo))
                                .addGap(10, 10, 10))
                            .addComponent(jCheckBox5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(vibrantAppsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(vibrantTelenav)
                            .addComponent(vibrantTmoTV)
                            .addComponent(vibrantWriteandGo)
                            .addComponent(vibrantWifiCalling)
                            .addComponent(VibrantVisualVoicemail)
                            .addComponent(vibrantThinkFree)))
                    .addComponent(vibrantSlacker))
                .addContainerGap(57, Short.MAX_VALUE))
        );
        vibrantAppsLayout.setVerticalGroup(
            vibrantAppsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(vibrantAppsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(vibrantAppsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(vibrantAllShare)
                    .addComponent(vibrantLayar)
                    .addComponent(vibrantTelenav))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(vibrantAppsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(vibrantAppsLayout.createSequentialGroup()
                        .addComponent(vibrantThinkFree)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(vibrantTmoTV)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(VibrantVisualVoicemail)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(vibrantWriteandGo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(vibrantWifiCalling))
                    .addGroup(vibrantAppsLayout.createSequentialGroup()
                        .addGroup(vibrantAppsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(vibrantAmazon)
                            .addComponent(vibrantMediahub))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(vibrantAppsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBox2)
                            .addComponent(vibrantMemo))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(vibrantAppsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(vibrantAvatar)
                            .addComponent(vibrantMiniDiary))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(vibrantAppsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(vibrantGogo)
                            .addComponent(jCheckBox5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(vibrantAppsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(vibrantKindle)
                            .addComponent(vibrantSlacker))))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Vibrant Apps", vibrantApps);

        systemMms.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Don't Modify", "AOSP Mms", "TouchWiz Mms" }));
        systemMms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                systemMmsActionPerformed(evt);
            }
        });

        systemEmail.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Don't Modify", "AOSP Email", "TouchWiz Email" }));
        systemEmail.setEnabled(false);

        jLabel5.setText("Mms");

        jLabel6.setText("Email");

        systemModem.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Don't Modify", "JL5", "JL4", "JL1", "JK6" }));

        jLabel7.setText("Modem");

        jLabel8.setText("Calendar");

        systemEmail1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Don't Modify", "AOSP Calendar", "TouchWiz Calendar" }));
        systemEmail1.setEnabled(false);

        javax.swing.GroupLayout systemPanelLayout = new javax.swing.GroupLayout(systemPanel);
        systemPanel.setLayout(systemPanelLayout);
        systemPanelLayout.setHorizontalGroup(
            systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(systemPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(systemMms, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(systemModem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addGap(18, 18, 18)
                .addGroup(systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(systemPanelLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addGroup(systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(systemEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel8)
                    .addComponent(systemEmail1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(100, Short.MAX_VALUE))
        );
        systemPanelLayout.setVerticalGroup(
            systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(systemPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(systemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(systemPanelLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(systemMms, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(systemModem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(systemPanelLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(systemEmail, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(systemEmail1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(66, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("System", systemPanel);

        repoList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Dev 1", "Dev 2", "Dev 3", "Dev 4" };
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

        jLabel3.setText("Repositories");

        repoItems.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Empty :(" };
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

        javax.swing.GroupLayout repoPanelLayout = new javax.swing.GroupLayout(repoPanel);
        repoPanel.setLayout(repoPanelLayout);
        repoPanelLayout.setHorizontalGroup(
            repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, repoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(addSelectedFilesButton)
                    .addGroup(repoPanelLayout.createSequentialGroup()
                        .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE))))
                .addContainerGap())
        );
        repoPanelLayout.setVerticalGroup(
            repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(repoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(repoPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(addSelectedFilesButton)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Repo", repoPanel);

        jLabel10.setText("Location of system apps");

        optionsSystemAppsLocationField.setText("system/app");
        optionsSystemAppsLocationField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionsSystemAppsLocationFieldActionPerformed(evt);
            }
        });
        optionsSystemAppsLocationField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                optionsSystemAppsLocationFieldKeyTyped(evt);
            }
        });

        jLabel11.setText("note: no slash in front or end");

        optionsChangeSystemLocButton.setText("Change");
        optionsChangeSystemLocButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionsChangeSystemLocButtonActionPerformed(evt);
            }
        });

        zipName.setText("flash");

        jLabel4.setText(".zip");

        jLabel12.setText("generated zip name");

        javax.swing.GroupLayout optionsTabLayout = new javax.swing.GroupLayout(optionsTab);
        optionsTab.setLayout(optionsTabLayout);
        optionsTabLayout.setHorizontalGroup(
            optionsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(optionsTabLayout.createSequentialGroup()
                        .addGroup(optionsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addGroup(optionsTabLayout.createSequentialGroup()
                                .addComponent(zipName, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4)))
                        .addGap(207, 207, 207))
                    .addGroup(optionsTabLayout.createSequentialGroup()
                        .addGroup(optionsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(optionsTabLayout.createSequentialGroup()
                                .addGroup(optionsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(optionsSystemAppsLocationField, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel10, javax.swing.GroupLayout.Alignment.LEADING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(optionsChangeSystemLocButton))
                            .addComponent(jLabel11))
                        .addContainerGap(139, Short.MAX_VALUE))))
        );
        optionsTabLayout.setVerticalGroup(
            optionsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(optionsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zipName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addGap(18, 18, 18)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(optionsTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(optionsSystemAppsLocationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(optionsChangeSystemLocButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel11)
                .addContainerGap(31, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Options", optionsTab);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel1.setText("VCK v0.8");

        jLabel2.setText("by Roman");

        zipProgress.setFocusable(false);

        console.setColumns(20);
        console.setEditable(false);
        console.setFont(new java.awt.Font("Monospaced", 0, 12));
        console.setLineWrap(true);
        console.setRows(5);
        console.setWrapStyleWord(true);
        jScrollPane1.setViewportView(console);

        jButton2.setText("Start Over");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        statusLabel.setText("Status");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel2))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(generateZipButton, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                                .addGap(6, 6, 6))
                            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, 115, Short.MAX_VALUE)
                            .addComponent(zipProgress, javax.swing.GroupLayout.Alignment.TRAILING, 0, 0, Short.MAX_VALUE)
                            .addComponent(statusLabel))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addGap(30, 30, 30)
                        .addComponent(jButton2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(statusLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zipProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(generateZipButton))
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void processMms() {
        if (systemMms.getSelectedItem().equals("AOSP Mms")) {
            String url = "http://www.rbirg.com/vibrant/kitchen/system/app/Mms-AOSP.apk";
            String source = "kitchen/system/app/Mms-AOSP.apk";
            String target = systemPrefix + "Mms.apk";
            System.out.println("adding aosp mms");
            addApp(url, source, target);
        } else if (systemMms.getSelectedItem().equals("TouchWiz Mms")) {
            String url = "http://www.rbirg.com/vibrant/kitchen/system/app/Mms-TW.apk";
            String source = "kitchen/system/app/Mms-TW.apk";
            String target = systemPrefix + "Mms.apk";
            addApp(url, source, target);
        } else {
            removeApp("system/app/Mms.apk");
        }

    }

    private void processModem() {
        if (systemModem.getSelectedItem().equals("JL5")) {
            String url = "http://www.rbirg.com/vibrant/kitchen/updates/JL5.bin";
            String source = "kitchen/updates/JL5.bin";
            String target = "updates/modem.bin";
            addApp(url, source, target);
            useModem = true;
        } else if (systemModem.getSelectedItem().equals("JL4")) {
            String url = "http://www.rbirg.com/vibrant/kitchen/updates/JL4.bin";
            String source = "kitchen/updates/JL4.bin";
            String target = "updates/modem.bin";
            addApp(url, source, target);
            useModem = true;
        } else if (systemModem.getSelectedItem().equals("JL1")) {
            String url = "http://www.rbirg.com/vibrant/kitchen/updates/JL1.bin";
            String source = "kitchen/updates/JL1.bin";
            String target = "updates/modem.bin";
            addApp(url, source, target);
            useModem = true;
        } else if (systemModem.getSelectedItem().equals("JL4")) {
            String url = "http://www.rbirg.com/vibrant/kitchen/updates/JK6.bin";
            String source = "kitchen/updates/JK6.bin";
            String target = "updates/modem.bin";
            addApp(url, source, target);
            useModem = true;
        } else if (systemModem.getSelectedItem().equals("JK2")) {
            String url = "http://www.rbirg.com/vibrant/kitchen/updates/JK2.bin";
            String source = "kitchen/updates/JK2.bin";
            String target = "updates/modem.bin";
            addApp(url, source, target);
            useModem = true;
        } else {
            removeApp("update/modem.bin");
        }
        if (useModem) {
            addApp("http://www.rbirg.com/vibrant/kitchen/updates/redbend_ua", "kitchen/updates/redbend_ua", "updates/redbend_ua");
        }

    }

    private void processCalendar() {
        if (systemEmail.getSelectedItem().equals("TouchWiz Email")) {
            String url = "http://www.rbirg.com/vibrant/kitchen/system/app/Calendar-TW.apk";
            String source = "kitchen/system/app/Calendar-TW.apk";
            String target = systemPrefix + "Calendar.apk";

            addApp(url, source, target);
            addApp(urlBase + "/kitchen/system/app/CalendarProvider-TW.apk", "/kitchen/system/app/CalendarProvider-TW.apk", "system/app/CalendarProvider.apk");

        } else if (systemEmail.getSelectedItem().equals("AOSP Email")) {
            String url = "http://www.rbirg.com/vibrant/kitchen/system/app/Calendar-AOSP.apk";
            String source = "kitchen/system/app/Calendar-AOSP.apk";
            String target = systemPrefix + "Calendar.apk";

            addApp(url, source, target);
            addApp(urlBase + "/kitchen/system/app/CalendarProvider-AOSP.apk", "/kitchen/system/app/CalendarProvider-AOSP.apk", "system/app/CalendarProvider.apk");
        } else {

            removeApp("system/app/Mms.apk");
        }
    }

    private void generateZipButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateZipButtonActionPerformed
        //after files are downloaded they need to be zipped, so this has to be called from the zip thread
        //downloadFiles();
        processMms();
        processModem();

        if (useSystem || useData || useModem || wipeDalvik) {
            for (DownloadFile f : files) {
                Zip.getInstance().addToQueue(f);
                //System.out.println("adding file to queue");
            }

            Apps.getInstance().zipProgress.setVisible(true);
            //System.out.println("sources size before zip creation: " + sources.size());
            Zip z = new Zip(Apps.files, Apps.getInstance().zipName.getText());
            z.addPropertyChangeListener(
                    new PropertyChangeListener() {

                        public void propertyChange(PropertyChangeEvent evt) {
                            if ("progress".equals(evt.getPropertyName())) {
                                Apps.getInstance().zipProgress.setValue((Integer) evt.getNewValue());
                            }
                        }
                    });

            z.execute();
        }

        for(DownloadFile f : filesToDownload) {
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
        File folder = new File(f.getSource());
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
        cleanUp();
        VCKTools.createSums();
        //Download1.getInstance().execute();
        createDirectories();
    }//GEN-LAST:event_formComponentShown

    private void wipeDalvikCacheToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wipeDalvikCacheToggleActionPerformed
        if (((JCheckBox) evt.getSource()).isSelected()) {
            wipeDalvik = true;
        } else {
            wipeDalvik = false;
        }
    }//GEN-LAST:event_wipeDalvikCacheToggleActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cleanUp();
    }//GEN-LAST:event_formWindowClosing

    private void systemMmsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_systemMmsActionPerformed
}//GEN-LAST:event_systemMmsActionPerformed

    private void vibrantTmoTVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vibrantTmoTVActionPerformed
        String target = systemPrefix + "com.mobitv.client.mobitv.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_vibrantTmoTVActionPerformed

    private void vibrantSlackerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vibrantSlackerActionPerformed
        String target = systemPrefix + "slackerradio.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_vibrantSlackerActionPerformed

    private void vibrantTelenavActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vibrantTelenavActionPerformed
        String target = systemPrefix + "Telenav.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_vibrantTelenavActionPerformed

    private void vibrantThinkFreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vibrantThinkFreeActionPerformed
        String target = systemPrefix + "thinkdroid.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_vibrantThinkFreeActionPerformed

    private void VibrantVisualVoicemailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_VibrantVisualVoicemailActionPerformed
}//GEN-LAST:event_VibrantVisualVoicemailActionPerformed

    private void vibrantWifiCallingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vibrantWifiCallingActionPerformed
        String target = systemPrefix + "WiFi-Calling.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
            addApp(urlBase + "kitchen/system/lib/libganril.so", "kitchen/system/lib/libganril.so", "system/lib/libganril.so");
            addApp(urlBase + "kitchen/system/lib/libkineto.so", "kitchen/system/lib/libkineto.so", "system/lib/libkineto.so");
            addApp(urlBase + "kitchen/system/lib/librilswitch.so", "kitchen/system/lib/librilswitch.so", "system/lib/librilswitch.so");
        } else {
            removeApp(target);
            removeApp("kitchen/system/lib/libganril.so");
            removeApp("kitchen/system/lib/libkineto.so");
            removeApp("kitchen/system/lib/librilswitch.so");
        }
}//GEN-LAST:event_vibrantWifiCallingActionPerformed

    private void vibrantMemoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vibrantMemoActionPerformed
        String target = systemPrefix + "Memo.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_vibrantMemoActionPerformed

    private void vibrantAmazonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vibrantAmazonActionPerformed
        String target = systemPrefix + "AmazonMp3.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_vibrantAmazonActionPerformed

    private void vibrantKindleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vibrantKindleActionPerformed
        String target = systemPrefix + "KindleStub.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_vibrantKindleActionPerformed

    private void vibrantMiniDiaryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vibrantMiniDiaryActionPerformed
        String target = systemPrefix + "MiniDiary.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_vibrantMiniDiaryActionPerformed

    private void vibrantLayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vibrantLayarActionPerformed
        String target = systemPrefix + "Layar-samsung.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_vibrantLayarActionPerformed

    private void vibrantGogoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vibrantGogoActionPerformed
        String target = systemPrefix + "GoGo.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_vibrantGogoActionPerformed

    private void vibrantWriteandGoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vibrantWriteandGoActionPerformed
        String target = systemPrefix + "WriteandGo.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_vibrantWriteandGoActionPerformed

    private void vibrantMediahubActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vibrantMediahubActionPerformed
        String target = systemPrefix + "MediaHub.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_vibrantMediahubActionPerformed

    private void vibrantAvatarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vibrantAvatarActionPerformed
        String target = systemPrefix + "Avatar.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_vibrantAvatarActionPerformed

    private void vibrantAllShareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vibrantAllShareActionPerformed
        String target = systemPrefix + "Dlna.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_vibrantAllShareActionPerformed

    private void miscCarHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miscCarHomeActionPerformed
        String target = "data/app/CarHomeGoogle.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
            addApp(urlBase + "kitchen/data/app/CarHomeLauncher.apk", "kitchen/data/app/CarHomeLauncher.apk", "data/app/CarHomeLauncher.apk");
        } else {
            removeApp(target);
            removeApp("kitchen/data/app/CarHomeLauncher.apk");
        }
}//GEN-LAST:event_miscCarHomeActionPerformed

    private void miscDockHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miscDockHomeActionPerformed
        String target = "data/app/CradleMain.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_miscDockHomeActionPerformed

    private void miscGingerbreadkbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miscGingerbreadkbActionPerformed
        String target = systemPrefix + "ime-mtm-stock-gingerbread.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
            addApp(url, "kitchen/system/lib/libjni_latinime.so", "system/lib/libjni_latinime.so");
        } else {
            removeApp(target);
            removeApp("kitchen/system/lib/libjni_latinime.so");
        }
}//GEN-LAST:event_miscGingerbreadkbActionPerformed

    private void launchersZeamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_launchersZeamActionPerformed
        String target = "data/app/org.zeam.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_launchersZeamActionPerformed

    private void launcherTouchWizActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_launcherTouchWizActionPerformed
        String target = systemPrefix + "TouchWiz30Launcher.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
            addApp(urlBase + "kitchen/system/app/SamsungWidget_CalendarClock.apk", "kitchen/system/app/SamsungWidget_CalendarClock.apk", "system/app/SamsungWidget_CalendarClock.apk");
            addApp(urlBase + "kitchen/system/app/SamsungWidget_FeedAndUpdate.apk", "kitchen/system/app/SamsungWidget_FeedAndUpdate.apk", "system/app/SamsungWidget_FeedAndUpdate.apk");
            addApp(urlBase + "kitchen/system/app/SamsungWidget_ProgramMonitor.apk", "kitchen/system/app/SamsungWidget_ProgramMonitor.apk", "system/app/SamsungWidget_ProgramMonitor.apk");
            addApp(urlBase + "kitchen/system/app/SamsungWidget_StockClock.apk", "kitchen/system/app/SamsungWidget_StockClock.apk", "system/app/SamsungWidget_StockClock.apk");
        } else {
            removeApp(target);
            removeApp("kitchen/system/app/SamsungWidget_CalendarClock.apk");
            removeApp("kitchen/system/app/SamsungWidget_FeedAndUpdate.apk");
            removeApp("kitchen/system/app/SamsungWidget_ProgramMonitor.apk");
            removeApp("kitchen/system/app/SamsungWidget_StockClock.apk");
        }
}//GEN-LAST:event_launcherTouchWizActionPerformed

    private void launchersGingerbreadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_launchersGingerbreadActionPerformed
        String target = "data/app/Launcher2.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_launchersGingerbreadActionPerformed

    private void launchersAdwActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_launchersAdwActionPerformed
        String target = "data/app/org.adw.launcher.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_launchersAdwActionPerformed

    private void launchersLauncherProActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_launchersLauncherProActionPerformed
        String target = "data/app/LauncherPro-0.8.2.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_launchersLauncherProActionPerformed

    private void utilitiesSgsToolsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_utilitiesSgsToolsActionPerformed
        String target = "data/app/de.Fr4gg0r.SGS.Tools.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_utilitiesSgsToolsActionPerformed

    private void utilitiesSparePartsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_utilitiesSparePartsActionPerformed
        String target = "data/app/com.androidapps.spareparts.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_utilitiesSparePartsActionPerformed

    private void utilitiesRomManagerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_utilitiesRomManagerButtonActionPerformed
        String target = "data/app/RomManager.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_utilitiesRomManagerButtonActionPerformed

    private void utilitiesQuickBootButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_utilitiesQuickBootButtonActionPerformed
        String target = "data/app/Quickboot.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_utilitiesQuickBootButtonActionPerformed

    private void utilitiesTitaniumButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_utilitiesTitaniumButtonActionPerformed
        String target = "data/app/TitaniumBackup.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
}//GEN-LAST:event_utilitiesTitaniumButtonActionPerformed

    private void utilitiesWirelessAdbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_utilitiesWirelessAdbActionPerformed
        String target = "data/app/siir.es.adbWireless.apk";
        String source = "kitchen/" + target;
        String url = urlBase + source;

        if (((JCheckBox) evt.getSource()).isSelected()) {
            addApp(url, source, target);
        } else {
            removeApp(target);
        }
    }//GEN-LAST:event_utilitiesWirelessAdbActionPerformed

    private void utilitiesSystemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_utilitiesSystemActionPerformed
        if (((JCheckBox) evt.getSource()).isSelected()) {
            this.useSystem = true;
        } else {
            this.useSystem = false;
        }
    }//GEN-LAST:event_utilitiesSystemActionPerformed

    private void utilitiesDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_utilitiesDataActionPerformed
        if (((JCheckBox) evt.getSource()).isSelected()) {
            this.useData = true;
        } else {
            this.useData = false;
        }
    }//GEN-LAST:event_utilitiesDataActionPerformed

    private void repoListPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_repoListPropertyChange
    }//GEN-LAST:event_repoListPropertyChange

    private void repoListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_repoListMouseClicked
        updateRepoItemList();
    }//GEN-LAST:event_repoListMouseClicked

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
                    if (fName.getType().equals("rom")) {
                        filesToDownload.add(f);
                    } else {
                        addApp(f);
                    }
                }
            }
            selectedRepo.model.removeElement(i);
        }
    }//GEN-LAST:event_addSelectedFilesButtonActionPerformed

    private void updateRepoItemList() {
        //grab the repo associated with the one selected
        DefaultListModel m = null;
        try {
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
        } catch (ArrayIndexOutOfBoundsException ex) {
            //((DefaultListModel) repoItems.getModel()).clear();
            selectedRepo = null;
        } finally {

            //repoItems.setSelectedIndices();
            //System.out.println("Selected indices: " + selectedIndices);
            repaint();
        }
    }

    private void repoItemsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_repoItemsMouseClicked
//        updateRepoItemList();
    }//GEN-LAST:event_repoItemsMouseClicked

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        unselectAll();

    }//GEN-LAST:event_jButton2ActionPerformed

    private void optionsSystemAppsLocationFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsSystemAppsLocationFieldActionPerformed
        this.optionsChangeSystemLocButton.setEnabled(true);
    }//GEN-LAST:event_optionsSystemAppsLocationFieldActionPerformed

    private void optionsSystemAppsLocationFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_optionsSystemAppsLocationFieldKeyTyped
        this.optionsChangeSystemLocButton.setEnabled(true);
    }//GEN-LAST:event_optionsSystemAppsLocationFieldKeyTyped

    private void optionsChangeSystemLocButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsChangeSystemLocButtonActionPerformed
        unselectAll();
        systemPrefix = this.optionsSystemAppsLocationField.getText() + "/";
        this.optionsChangeSystemLocButton.setEnabled(false);
        this.writeConsoleMessage("System URL Changed; please make your selections again.");
    }//GEN-LAST:event_optionsChangeSystemLocButtonActionPerformed

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
    private javax.swing.JCheckBox VibrantVisualVoicemail;
    private javax.swing.JButton addSelectedFilesButton;
    private javax.swing.JTextArea console;
    private javax.swing.JPanel customFlashPanel;
    public javax.swing.JButton generateZipButton;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JCheckBox launcherTouchWiz;
    private javax.swing.JCheckBox launchersAdw;
    private javax.swing.JCheckBox launchersGingerbread;
    private javax.swing.JCheckBox launchersLauncherPro;
    private javax.swing.JPanel launchersPanel;
    private javax.swing.JCheckBox launchersZeam;
    private javax.swing.JCheckBox miscCarHome;
    private javax.swing.JCheckBox miscDockHome;
    private javax.swing.JCheckBox miscGingerbreadkb;
    private javax.swing.JButton optionsChangeSystemLocButton;
    private javax.swing.JTextField optionsSystemAppsLocationField;
    private javax.swing.JPanel optionsTab;
    private javax.swing.JList repoItems;
    private javax.swing.JList repoList;
    private javax.swing.JPanel repoPanel;
    public javax.swing.JLabel statusLabel;
    private javax.swing.JComboBox systemEmail;
    private javax.swing.JComboBox systemEmail1;
    private javax.swing.JComboBox systemMms;
    private javax.swing.JComboBox systemModem;
    private javax.swing.JPanel systemPanel;
    private javax.swing.JCheckBox utilitiesData;
    private javax.swing.JPanel utilitiesPanel;
    private javax.swing.JCheckBox utilitiesQuickBootButton;
    private javax.swing.JCheckBox utilitiesRomManagerButton;
    private javax.swing.JCheckBox utilitiesSgsTools;
    private javax.swing.JCheckBox utilitiesSpareParts;
    private javax.swing.JCheckBox utilitiesSystem;
    private javax.swing.JCheckBox utilitiesTitaniumButton;
    private javax.swing.JCheckBox utilitiesWirelessAdb;
    private javax.swing.JCheckBox vibrantAllShare;
    private javax.swing.JCheckBox vibrantAmazon;
    private javax.swing.JPanel vibrantApps;
    private javax.swing.JCheckBox vibrantAvatar;
    private javax.swing.JCheckBox vibrantGogo;
    private javax.swing.JCheckBox vibrantKindle;
    private javax.swing.JCheckBox vibrantLayar;
    private javax.swing.JCheckBox vibrantMediahub;
    private javax.swing.JCheckBox vibrantMemo;
    private javax.swing.JCheckBox vibrantMiniDiary;
    private javax.swing.JCheckBox vibrantSlacker;
    private javax.swing.JCheckBox vibrantTelenav;
    private javax.swing.JCheckBox vibrantThinkFree;
    private javax.swing.JCheckBox vibrantTmoTV;
    private javax.swing.JCheckBox vibrantWifiCalling;
    private javax.swing.JCheckBox vibrantWriteandGo;
    private javax.swing.JCheckBox wipeDalvikCacheToggle;
    public static javax.swing.JTextField zipName;
    public javax.swing.JProgressBar zipProgress;
    // End of variables declaration//GEN-END:variables
}
