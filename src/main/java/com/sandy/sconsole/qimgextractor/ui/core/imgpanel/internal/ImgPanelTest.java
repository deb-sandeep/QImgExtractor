package com.sandy.sconsole.qimgextractor.ui.core.imgpanel.internal;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.ExtractedImgInfo;
import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.ExtractedImgListener;
import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.ImgExtractorPanel;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

@Slf4j
public class ImgPanelTest extends JFrame implements ExtractedImgListener {
    
    private static final File imgFile = new File( "/Users/sandeep/Documents/StudyNotes/question-bank/FIITJEE/AITS-A/AITS-13-A-FT1P1/pages/AITS-13-A-FT1P1-09.png" ) ;
    
    public static void main(String[] args) {
        ImgPanelTest app = new ImgPanelTest() ;
        app.setVisible( true ) ;
        app.setImage( imgFile, app.getImgState() ) ;
    }
    
    private ImgExtractorPanel imgExtractorPanel;
    
    public ImgPanelTest() {
        setUpUI() ;
    }
    
    private void setUpUI() {
        SwingUtils.setMaximized( this ) ;
        super.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE ) ;
        setLayout( new BorderLayout() ) ;
        
        imgExtractorPanel = new ImgExtractorPanel( this ) ;
        add( imgExtractorPanel, BorderLayout.CENTER ) ;
    }
    
    public void setImage( File file, List<ExtractedImgInfo> imgInfoList ) {
        imgExtractorPanel.setImage( file, imgInfoList ) ;
    }
    
    private int nextTagNumber = 1 ;
    
    public String subImageSelected( BufferedImage subImg, Rectangle subImgBounds, int selectionModifier ) {
        try {
            ImageIO.write( subImg, "png", new File( "/Users/sandeep/temp/subimg.png" ) ) ;
        }
        catch( IOException e ) {
            log.error( "Error writing subimg." , e ) ;
        }
        return "Sub-image " + nextTagNumber++ ;
    }
    
    @Override
    public void selectedRegionsUpdated( List<ExtractedImgInfo> selectedRegionsInfo ) {
        try {
            File stateFile = new File( "/Users/sandeep/temp/subimg.state" ) ;
            ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( stateFile ) ) ;
            oos.writeObject( selectedRegionsInfo ) ;
            oos.close() ;
        }
        catch( IOException e ) {
            log.error( "Error writing state." , e ) ;
        }
    }
    
    @Override
    public void processCommandKey( int keyCode ) {
    }
    
    public List<ExtractedImgInfo> getImgState() {
        List<ExtractedImgInfo> state = null ;
        try {
            File stateFile = new File( "/Users/sandeep/temp/subimg.state" ) ;
            ObjectInputStream ois = new ObjectInputStream( new FileInputStream( stateFile ) ) ;
            state = ( List<ExtractedImgInfo> )ois.readObject() ;
            ois.close() ;
        }
        catch( Exception e ) {
            log.error( "Error reading state." , e ) ;
        }
        return state ;
    }
}
