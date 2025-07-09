package com.sandy;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.ExtractedImgInfo;
import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.ExtractedImgListener;
import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.ImgExtractorPanel;
import com.sandy.sconsole.qimgextractor.ui.core.tabbedpane.CloseableTabbedPane;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ImgPanelTest extends JFrame implements ExtractedImgListener {
    
    private static final File imgDir = new File( "/Users/sandeep/temp/imgs" ) ;
    private static final File stateDir = new File( "/Users/sandeep/temp/imgstate" ) ;
    
    public static void main(String[] args) {
        ImgPanelTest app = new ImgPanelTest() ;
        app.setVisible( true ) ;
        app.setImages() ;
    }
    
    private CloseableTabbedPane tabbedPane ;
    
    public ImgPanelTest() {
        setUpUI() ;
    }
    
    private void setUpUI() {
        SwingUtils.setMaximized( this ) ;
        super.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE ) ;
        setLayout( new BorderLayout() ) ;
        
        tabbedPane = new CloseableTabbedPane() ;
        add( tabbedPane, BorderLayout.CENTER ) ;
    }
    
    public void setImages() {
        Arrays.stream( imgDir.listFiles( f -> f.getName().endsWith(".png") ) ).toList().forEach( file -> {
            File stateFile = new File( stateDir, file.getName() + ".state" ) ;
            List<ExtractedImgInfo> infos = getImgState( stateFile ) ;
            setImage( file, infos ) ;
        }) ;
    }
    
    public void setImage( File file, List<ExtractedImgInfo> imgInfoList ) {
        ImgExtractorPanel imgPanel = new ImgExtractorPanel( this ) ;
        imgPanel.setImage( file, imgInfoList, tabbedPane.getWidth() - 50 ) ;
        tabbedPane.addTab( file.getName(), imgPanel ) ;
        tabbedPane.raiseAlert( imgPanel );
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
    public void selectedRegionsUpdated( List<ExtractedImgInfo> selectedRegionsInfo, File imgFile ) {
        try {
            File stateFile = new File( stateDir, imgFile.getName() + ".state" ) ;
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
    
    public List<ExtractedImgInfo> getImgState( File stateFile ) {
        List<ExtractedImgInfo> state = null ;
        try {
            if( stateFile.exists() ) {
                ObjectInputStream ois = new ObjectInputStream( new FileInputStream( stateFile ) ) ;
                state = ( List<ExtractedImgInfo> )ois.readObject() ;
                ois.close() ;
            }
            else {
                state = new ArrayList<ExtractedImgInfo>() ;
            }
        }
        catch( Exception e ) {
            log.error( "Error reading state." , e ) ;
        }
        return state ;
    }
}
