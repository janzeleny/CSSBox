/*
 * SimpleBrowser.java
 * Copyright (c) 2005-2007 Radek Burget
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 */

package org.fit.cssbox.demo;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.*;
import org.w3c.dom.*;
import org.w3c.tidy.*;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.layout.BrowserCanvas;

/**
 * An example of using CSSBox for the HTML page rendering and display.
 * It parses the style sheets and creates a box tree describing the
 * final layout. As the HTML parser, jTidy is used.
 * 
 * @author  burgetr
 */
public class SimpleBrowser extends javax.swing.JFrame 
{
	private static final long serialVersionUID = -1336331141597077348L;
	
	/** The swing canvas for displaying the rendered document */
    private javax.swing.JPanel browserCanvas;
    
    /** Scroll pane for the canvas */ 
    private javax.swing.JScrollPane documentScroll;
    
    /** Root DOM Element of the document body */
	private Element docroot;
	
	/** The CSS analyzer of the DOM tree */
    private DOMAnalyzer decoder;

    
    /** 
     * Creates a new application window and displays the rendered document
     * @param root The root DOM element of the document body
     * @param baseurl The base URL of the document used for completing the relative paths
     * @param decoder The CSS analyzer that provides the effective style of the elements 
     */
    public SimpleBrowser(Element root, URL baseurl, DOMAnalyzer decoder)
    {
        docroot = root;
        this.decoder = decoder;
        initComponents(baseurl);
    }
    
    /**
     * Creates and initializes the GUI components
     * @param baseurl The base URL of the document used for completing the relative paths
     */
    private void initComponents(URL baseurl) 
    {
        documentScroll = new javax.swing.JScrollPane();
        
        //Create the browser canvas
        browserCanvas = new BrowserCanvas(docroot, decoder, new java.awt.Dimension(1000, 600), baseurl);

        //A simple mouse listener that displays the coordinates clicked
        browserCanvas.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e)
            {
                System.out.println("Click: " + e.getX() + ":" + e.getY());
            }
            public void mousePressed(MouseEvent e) { }
            public void mouseReleased(MouseEvent e) { }
            public void mouseEntered(MouseEvent e) { }
            public void mouseExited(MouseEvent e) { }
        });
        
        getContentPane().setLayout(new java.awt.GridLayout(1, 0));

        setTitle("CSSBox Browser");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        documentScroll.setViewportView(browserCanvas);
        getContentPane().add(documentScroll);
        pack();
    }
    
    /** 
     * Exit the Application 
     */
    private void exitForm(java.awt.event.WindowEvent evt)
    {
        System.exit(0);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) 
    {
    	if (args.length != 1)
    	{
    		System.err.println("Usage: SimpleBrowser <url>");
    		System.exit(0);
    	}
    	
        try {
        	//Open the network connection 
        	URL url = new URL(args[0]);
        	URLConnection con = url.openConnection();
            InputStream is = con.getInputStream();
            
            //Parse the input document using jTidy
            Tidy tidy = new Tidy();
            tidy.setTrimEmptyElements(false);
            tidy.setAsciiChars(false);
            tidy.setInputEncoding("iso-8859-2");
            tidy.setXHTML(true);
            Document doc = tidy.parseDOM(is, null);
            
            //Create the CSS analyzer
            DOMAnalyzer da = new DOMAnalyzer(doc, url);
            da.attributesToStyles(); //convert the HTML presentation attributes to inline styles
            da.addStyleSheet(null, CSSNorm.stdStyleSheet()); //use the standard style sheet
            da.addStyleSheet(null, CSSNorm.userStyleSheet()); //use the additional style sheet
            da.getStyleSheets(); //load the author style sheets
            
            //Display the result
            SimpleBrowser test = new SimpleBrowser(da.getRoot(), url, da);
            test.setSize(1275, 750);
            test.setVisible(true);
            
            is.close();
            
        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
            e.printStackTrace();
        }
    }
    
}
