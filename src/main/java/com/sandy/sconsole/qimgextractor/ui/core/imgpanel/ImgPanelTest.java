package com.sandy.sconsole.qimgextractor.ui.core.imgpanel;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ImgPanelTest extends JFrame {
    
    private static final File imgFile = new File( "/Users/sandeep/Documents/StudyNotes/question-bank/FIITJEE/AITS-A/AITS-13-A-FT1P1/pages/AITS-13-A-FT1P1-09.png" ) ;
    
    public static void main(String[] args) {
        ImgPanelTest app = new ImgPanelTest() ;
        app.setVisible( true ) ;
        app.setImage( imgFile ) ;
    }
    
    private ImagePanel imagePanel ;
    
    public ImgPanelTest() {
        setUpUI() ;
    }
    
    private void setUpUI() {
        SwingUtils.setMaximized( this ) ;
        super.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE ) ;
        setLayout( new BorderLayout() ) ;
        
        imagePanel = new ImagePanel() ;
        add( imagePanel, BorderLayout.CENTER ) ;
    }
    
    public void setImage( File file ) {
        imagePanel.setImage( file ) ;
    }
}
