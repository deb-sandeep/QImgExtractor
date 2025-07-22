package com.sandy.sconsole.qimgextractor.ui.project;

import com.sandy.sconsole.qimgextractor.ui.MainFrame;
import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.ExtractedImgInfo;
import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.ExtractedImgListener;
import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.ImgExtractorPanel;
import com.sandy.sconsole.qimgextractor.ui.core.tabbedpane.CloseableTabbedPane;
import com.sandy.sconsole.qimgextractor.ui.project.savedialog.ImgSaveDialog;
import com.sandy.sconsole.qimgextractor.ui.project.tree.ProjectPageTree;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ProjectPanel extends JPanel implements ExtractedImgListener {
    
    private final MainFrame mainFrame;
    private final File projectDir ;
    private final File pagesDir ;
    private final File extractedImgDir ;
    private final ImgSaveDialog saveDialog ;
    
    private CloseableTabbedPane tabbedPane ;
    private ProjectPageTree pageTree ;
    
    public ProjectPanel( MainFrame mainFrame, File projectDir ) {
        this.mainFrame = mainFrame ;
        this.projectDir = projectDir ;
        this.pagesDir = new File( projectDir, "pages" ) ;
        this.extractedImgDir = new File( projectDir, "question-images" ) ;
        if( !extractedImgDir.exists() ) {
            if( extractedImgDir.mkdirs() ) {
                log.info( "Created extracted images directory." ) ;
            }
        }
        this.saveDialog = new ImgSaveDialog( this.extractedImgDir, this.projectDir.getName() ) ;
        
        setUpUI() ;
        new Thread( this::loadPageImages ).start() ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() );
        tabbedPane = new CloseableTabbedPane() ;
        pageTree = new ProjectPageTree( this ) ;
        add( tabbedPane, BorderLayout.CENTER ) ;
        add( pageTree, BorderLayout.WEST ) ;
    }
    
    private void loadPageImages() {
        File[] files = pagesDir.listFiles( f -> f.getName().endsWith( ".png" ) ) ;
        assert files != null;
        for( int i=0; i<files.length; i++ ) {
            File file = files[i] ;
            mainFrame.logStausMsg( "Loading (" + i + "/" + files.length + ") " + file.getName() + "..." ) ;
            List<ExtractedImgInfo> imgInfoList = getExtractedImgInfoList( file ) ;
            ImgExtractorPanel imgPanel = new ImgExtractorPanel( this ) ;
            
            imgPanel.setImage( file, imgInfoList, SwingUtils.getScreenWidth() - ProjectPageTree.PREFERRED_WIDTH - 10 ) ;
            SwingUtilities.invokeLater( () -> tabbedPane.addTab( file.getName(), imgPanel ) ) ;
        }
        mainFrame.clearStatusMsg() ;
    }
    
    private List<ExtractedImgInfo> getExtractedImgInfoList( File imgFile ) {
        List<ExtractedImgInfo> imgInfoList = new ArrayList<>() ;
        return imgInfoList ;
    }
    
    public void destroy() {
        SwingUtilities.invokeLater( () -> {
            int numberOfTabs = tabbedPane.getTabCount() ;
            for( int i=numberOfTabs-1; i>=0; i-- ) {
                ImgExtractorPanel panel = ( ImgExtractorPanel )tabbedPane.getTabComponentAt( i ) ;
                if( panel != null ) {
                    panel.destroy() ;
                    tabbedPane.removeTabAt( i ) ;
                }
            }
        } ) ;
    }
    
    @Override
    public String subImageSelected( File imgSrcFile, BufferedImage image, Rectangle subImgBounds, int selectionModifier ) {
        
        saveDialog.updateRecommendedFileName() ;
        int userChoice = saveDialog.showSaveDialog( this ) ;
        if( userChoice == JOptionPane.OK_OPTION ) {
            File destFile = saveDialog.getSelectedFile() ;
            if( destFile != null ) {
                try {
                    ImageIO.write( image, "png", destFile ) ;
                    mainFrame.logStausMsg( "Saved " + destFile.getName() ) ;
                }
                catch( Exception e ) {
                    log.error( "Error saving image.", e ) ;
                    mainFrame.logStausMsg( "Error saving image." ) ;
                }
            }
        }
        return "" ;
    }
    
    @Override
    public void selectedRegionsUpdated( List<ExtractedImgInfo> selectedRegionsInfo, File imgFile ) {
    }
    
    @Override
    public void processCommandKey( int keyCode ) {
    }
}
