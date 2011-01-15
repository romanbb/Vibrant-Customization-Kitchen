/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vck;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;

public class URLLabel extends Label {

    private java.applet.Applet applet;
    private URL url;
    private String target = "";
    private Color unvisitedURL = Color.blue;
    private Color visitedURL = Color.green;

    public URLLabel(Applet applet, String url, String text) {
        this(applet, url, text, "_self");
    }

    public URLLabel(Applet applet, String url, String text, String target) {
        super(text);
        setForeground(unvisitedURL);
        try {
            this.applet = applet;
            this.url = new URL(url);
            this.target = target;
            addMouseListener(new Clicked());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void paint(Graphics g) {
        Rectangle r;
        super.paint(g);
        r = g.getClipBounds();
        g.drawLine(0,
                r.height - this.getFontMetrics(this.getFont()).getDescent(),
                this.getFontMetrics(this.getFont()).stringWidth(this.getText()),
                r.height - this.getFontMetrics(this.getFont()).getDescent());
    }

    public void setUnvisitedURLColor(Color c) {
        unvisitedURL = c;
    }

    public void setVisitedURLColor(Color c) {
        visitedURL = c;
    }

    class Clicked extends MouseAdapter {

        public void mouseClicked(MouseEvent me) {
            setForeground(visitedURL);
            applet.getAppletContext().showDocument(url, target);
        }
    }
}
