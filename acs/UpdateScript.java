/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package acs;

/**
 *
 * @author roman
 */
public class UpdateScript {

    String script = "";
    Phone phone;
    boolean cwm3;
    int i = 1;

    public UpdateScript(Phone phone) {
        this.phone = phone;
        cwm3 = false;
    }

    public UpdateScript(Phone phone, boolean cwm3) {
        this.phone = phone;
        this.cwm3 = cwm3;
    }

    public String createScript() {
        if (!cwm3) {
            if (!Apps.getInstance().files.isEmpty()) {
                script += "show_progress 0." + i + " 0\n";
            }
            if (Apps.getInstance().wipeDalvik) {
                script += "show_progress 0." + i + " 0\n";
                script += "delete_recursive DATA:dalvik-cache\n";
                script += "show_progress 0." + i + " 10\n";
                i++;
            }
            if (Apps.getInstance().useData) {
                script += "copy_dir PACKAGE:data DATA:\n";
            }
            if (Apps.getInstance().useSystem) {
                script += "copy_dir PACKAGE:system SYSTEM:\n";
            }
            if (Apps.getInstance().useSD) {
                script += "copy_dir PACKAGE:sdcard SDCARD:\n";
            }

            script += "show_progress 0." + i + " 10\n";
            i++;

            //modem
            if (Apps.getInstance().useModem) {
                script += "show_progress 0." + i + " 0\n";
                script += "copy_dir PACKAGE:updates TMP:/updates\n";
                script += "set_perm 0 0 755 TMP:/updates/redbend_ua\n";
                script += "run_program /tmp/updates/redbend_ua restore /tmp/updates/modem.bin /dev/block/bml12\n";

                script += "show_progress 0." + i + " 10\n";
                i++;
            }
            return script;

            /**#################################################*/
            /** cwm3 script                                    */
            /*##################################################*/
        } else {
            script += "show_progress(1.000000, 0);\n";
            script += mount() + "\n";

            if (Apps.getInstance().wipeDalvik) {
                //script += "show_progress(0." + i + ", 0);\n";
                script += "delete_recursive(\"/data/dalvik-cache\")\n";
                script += "set_progress(." + i + "000000);\n";
                i++;
            }

            if (!Apps.getInstance().files.isEmpty()) {
                //script += "show_progress(0." + i + ", 0);\n";
            }
            if (Apps.getInstance().useData) {
                script += "package_extract_dir(\"data\", \"/data\");\n";
            }
            if (Apps.getInstance().useSystem) {
                script += "package_extract_dir(\"system\", \"/system\");\n";
            }
            if (Apps.getInstance().useSD) {
                script += "package_extract_dir(\"sdcard\", \"/sdcard\");\n";
            }
            if (!Apps.getInstance().files.isEmpty()) {
                script += "set_progress(." + i + "000000);\n";
            }
            i++;

            if (Apps.getInstance().useModem) {
                //script += "show_progress 0." + i + " 0\n";
                script += addModem() + "\n";
                script += "set_progress(." + i + "000000);\n";
                i++;
            }
            script += unmount();
            return script;
        }
    }

    public String mount() {
        String name = phone.getName();
        if (name.equals("Samsung Epic 4G")) {
            return "run_program(\"/sbin/busybox\", \"mount\", \"/system\");\n"
                    + "run_program(\"/sbin/busybox\", \"mount\", \"/data\");\n"
                    + "run_program(\"/sbin/busybox\", \"mount\", \"/cache\");\n";
        } else if (name.equals("Samsung Vibrant")) {
            return "run_program(\"/sbin/busybox\", \"mount\", \"/system\");\n"
                    + "run_program(\"/sbin/busybox\", \"mount\", \"/data\");\n"
                    + "run_program(\"/sbin/busybox\", \"mount\", \"/cache\");\n";
        } else {
            return "";
        }
    }

    public String unmount() {
        String name = phone.getName();
        if (name.equals("Samsung Epc 4G")) {
            return "run_program(\"/sbin/unmount\", \"/dev/block/stl9\", \"/system\");\n"
                    + "run_program(\"/sbin/unmount\", \"/dev/block/stl10\", \"/data\");\n"
                    + "run_program(\"/sbin/unmount\", \"/dev/block/stl11\", \"/cache\")\n";
        }
        return "unmount(\"/system\");\n"
                + "unmount(\"/data\");\n"
                + "unmount(\"/cache\");";
    }

    public String addModem() {
        String name = phone.getName();
        if (name.equals("Samsung Vibrant")) {
            return "package_extract_dir(\"updates\", \tmp/updates\");\n"
                    + "set_perm(0, 0, 755, \"/tmp/updates/redbend_ua\");\n"
                    + "run_program(\"/tmp/updates/redbend_ua\", \"restore\", \"/tmp/updates/modem.bin\", \"/dev/block/bml12\");";
        } else {
            return "";
        }
    }
}
