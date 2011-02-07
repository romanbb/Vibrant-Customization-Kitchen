/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Options.java
 *
 * Created on Jan 21, 2011, 12:52:49 PM
 */
package acs;

import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author roman
 */
public class Options extends javax.swing.JFrame {

    static Options instance;

    /** Creates new form Options */
    public Options() {
        if (instance == null) {
            instance = this;
        }
        initComponents();
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Look and feel failure");
        }
        this.setLocationRelativeTo(null);
    }

    public static Options getInstance() {
        if (instance == null) {
            instance = new Options();
        }
        return instance;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        customFlashPanel = new javax.swing.JPanel();
        utilitiesSystem = new javax.swing.JCheckBox();
        utilitiesData = new javax.swing.JCheckBox();
        wipeDalvikCacheToggle = new javax.swing.JCheckBox();
        jLabel12 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        zipName = new javax.swing.JTextField();
        optionsChangeSystemLocButton = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        optionsSystemAppsLocationField = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();

        setTitle("Options");
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

        customFlashPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Empty Flashable"));
        customFlashPanel.setName("customFlashPanel"); // NOI18N

        utilitiesSystem.setText("system");
        utilitiesSystem.setName("utilitiesSystem"); // NOI18N
        utilitiesSystem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                utilitiesSystemActionPerformed(evt);
            }
        });

        utilitiesData.setText("data");
        utilitiesData.setName("utilitiesData"); // NOI18N
        utilitiesData.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                utilitiesDataActionPerformed(evt);
            }
        });

        wipeDalvikCacheToggle.setText("wipe Dalvik");
        wipeDalvikCacheToggle.setName("wipeDalvikCacheToggle"); // NOI18N
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
                .addContainerGap(35, Short.MAX_VALUE))
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

        jLabel12.setText("generated zip name");
        jLabel12.setName("jLabel12"); // NOI18N

        jLabel4.setText(".zip");
        jLabel4.setName("jLabel4"); // NOI18N

        zipName.setText("flash");
        zipName.setName("zipName"); // NOI18N

        optionsChangeSystemLocButton.setText("Change");
        optionsChangeSystemLocButton.setName("optionsChangeSystemLocButton"); // NOI18N
        optionsChangeSystemLocButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionsChangeSystemLocButtonActionPerformed(evt);
            }
        });

        jLabel11.setText("note: no slash in front or end");
        jLabel11.setName("jLabel11"); // NOI18N

        optionsSystemAppsLocationField.setText("system/app");
        optionsSystemAppsLocationField.setName("optionsSystemAppsLocationField"); // NOI18N
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

        jLabel10.setText("Location of system apps (make selections then change)");
        jLabel10.setName("jLabel10"); // NOI18N

        jCheckBox1.setText("Clockwork 3!");
        jCheckBox1.setName("jCheckBox1"); // NOI18N
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(zipName, javax.swing.GroupLayout.DEFAULT_SIZE, 102, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4)))
                        .addGap(212, 212, 212))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(optionsSystemAppsLocationField, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(optionsChangeSystemLocButton)))
                        .addGap(70, 70, 70))
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(customFlashPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jCheckBox1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(zipName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4))
                        .addGap(18, 18, 18)
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(optionsSystemAppsLocationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(optionsChangeSystemLocButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel11))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(customFlashPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void optionsSystemAppsLocationFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsSystemAppsLocationFieldActionPerformed
        this.optionsChangeSystemLocButton.setEnabled(true);
}//GEN-LAST:event_optionsSystemAppsLocationFieldActionPerformed

    private void optionsSystemAppsLocationFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_optionsSystemAppsLocationFieldKeyTyped
        this.optionsChangeSystemLocButton.setEnabled(true);
}//GEN-LAST:event_optionsSystemAppsLocationFieldKeyTyped

    private void optionsChangeSystemLocButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsChangeSystemLocButtonActionPerformed
        //unselectAll();
        //initApp();
        Apps.systemPrefix = this.optionsSystemAppsLocationField.getText();
        this.optionsChangeSystemLocButton.setEnabled(false);
        //this.writeConsoleMessage("System URL Changed; please make your selections again.");
        for (DownloadFile dlf : Apps.files) {
            if (dlf.getTarget().startsWith("system")) {
                dlf.setTarget(Apps.systemPrefix);
            }
        }
}//GEN-LAST:event_optionsChangeSystemLocButtonActionPerformed

    private void utilitiesSystemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_utilitiesSystemActionPerformed
        if (((JCheckBox) evt.getSource()).isSelected()) {
            Apps.useSystem = true;
        } else {
            Apps.useSystem = false;
        }
}//GEN-LAST:event_utilitiesSystemActionPerformed

    private void utilitiesDataActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_utilitiesDataActionPerformed
        if (((JCheckBox) evt.getSource()).isSelected()) {
            Apps.useData = true;
        } else {
            Apps.useData = false;
        }
}//GEN-LAST:event_utilitiesDataActionPerformed

    private void wipeDalvikCacheToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wipeDalvikCacheToggleActionPerformed
        if (((JCheckBox) evt.getSource()).isSelected()) {
            Apps.wipeDalvik = true;
        } else {
            Apps.wipeDalvik = false;
        }
}//GEN-LAST:event_wipeDalvikCacheToggleActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        this.setVisible(false);        // TODO add your handling code here:
    }//GEN-LAST:event_formWindowClosing

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Look and feel failure");
        }
        SwingUtilities.updateComponentTreeUI(this);
        this.pack();
    }//GEN-LAST:event_formComponentShown

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        if (((JCheckBox) evt.getSource()).isSelected()) {
            Apps.cwm3 = true;
        } else {
            Apps.cwm3 = false;
        }
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel customFlashPanel;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel4;
    public javax.swing.JButton optionsChangeSystemLocButton;
    private javax.swing.JTextField optionsSystemAppsLocationField;
    private javax.swing.JCheckBox utilitiesData;
    private javax.swing.JCheckBox utilitiesSystem;
    private javax.swing.JCheckBox wipeDalvikCacheToggle;
    public static javax.swing.JTextField zipName;
    // End of variables declaration//GEN-END:variables
}