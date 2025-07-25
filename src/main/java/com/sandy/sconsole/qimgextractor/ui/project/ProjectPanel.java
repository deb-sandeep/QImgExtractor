package com.sandy.sconsole.qimgextractor.ui.project;

import com.sandy.sconsole.qimgextractor.qid.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.MainFrame;
import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.ExtractedImgInfo;
import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.ExtractedImgListener;
import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.ImgExtractorPanel;
import com.sandy.sconsole.qimgextractor.ui.core.tabbedpane.CloseableTabbedPane;
import com.sandy.sconsole.qimgextractor.ui.project.savedialog.ImgSaveDialog;
import com.sandy.sconsole.qimgextractor.ui.project.tree.ProjectPageTree;
import com.sandy.sconsole.qimgextractor.util.AppUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.sandy.sconsole.qimgextractor.QImgExtractor.getProjectContext;
import static com.sandy.sconsole.qimgextractor.util.AppUtil.showErrorMsg;

@Slf4j
public class ProjectPanel extends JPanel implements ExtractedImgListener {
    
    private final MainFrame mainFrame;
    private final ProjectContext projectContext;
    private final File pagesDir ;
    private final File extractedImgDir ;
    private final File workDir ;
    private final ImgSaveDialog saveDialog ;
    
    @Getter
    private final String srcId;
    
    private CloseableTabbedPane tabbedPane ;
    private ProjectPageTree pageTree ;
    
    private QuestionImage nextImgName = null ;
    
    public ProjectPanel( MainFrame mainFrame, ProjectContext projectContext ) {
        
        this.projectContext = getProjectContext() ;
        this.mainFrame = mainFrame ;
        
        File projectDir = projectContext.getProjectDir();
        this.srcId = projectDir.getName() ;
        this.pagesDir = new File( projectDir, "pages" ) ;
        this.extractedImgDir = new File( projectDir, "question-images" ) ;
        this.workDir = new File( projectDir, ".workspace" ) ;
        
        if( !extractedImgDir.exists() ) {
            if( extractedImgDir.mkdirs() ) {
                log.info( "Created extracted images directory." ) ;
            }
        }
        
        if( !workDir.exists() ) {
            if( workDir.mkdirs() ) {
                log.info( "Created workspace directory." ) ;
            }
        }
        
        this.saveDialog = new ImgSaveDialog( this.extractedImgDir,
                                             this.projectContext ) ;
        
        QuestionImage lastSavedImg = projectContext.getLastSavedImg() ;
        if( lastSavedImg != null ) {
            this.nextImgName = lastSavedImg.nextQuestion() ;
        }
        
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
            List<ExtractedImgInfo> imgInfoList = loadImgInfo( file ) ;
            ImgExtractorPanel imgPanel = new ImgExtractorPanel( this ) ;
            
            imgPanel.setImage( file, imgInfoList, SwingUtils.getScreenWidth() - ProjectPageTree.PREFERRED_WIDTH - 10 ) ;
            SwingUtilities.invokeLater( () -> tabbedPane.addTab( file.getName(), imgPanel ) ) ;
        }
        mainFrame.clearStatusMsg() ;
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
    public String subImageSelected( File imgSrcFile, BufferedImage image,
                                    Rectangle subImgBounds, int selectionModifier ) {
        
        String processingId = null ;
        
        saveDialog.updateRecommendedFileName() ;
        
        int userChoice = saveDialog.showSaveDialog( this ) ;
        if( userChoice == JOptionPane.OK_OPTION ) {
            
            File destFile = saveDialog.getSelectedFile() ;
            if( destFile != null ) {
                try {
                    // 1. Append .png if user has not specified. Input brevity allowed.
                    String fileName = destFile.getName() ;
                    if( !fileName.endsWith( ".png" ) ) {
                        fileName += ".png" ;
                    }
                    
                    // 2. Prepend the source id to make the file name complete
                    // and save the image.
                    String imgFileName = fileName ;
                    if( !fileName.startsWith( srcId ) ) {
                        imgFileName = srcId + "." + fileName ;
                    }
                    File newImgFile = new File( destFile.getParentFile(),
                                                imgFileName) ;

                    // 3. Parse the file to see if it meets the file name criteria.
                    // If not, then an exception will be thrown.
                    QuestionImage qImg = new QuestionImage( newImgFile ) ;

                    // 4. Save the image, and other housekeeping tasks.
                    ImageIO.write( image, "png", newImgFile ) ;
                    projectContext.setLastSavedImage( qImg );
                    
                    processingId = qImg.getShortFileNameWithoutExtension() ;
                    mainFrame.logStausMsg( "Saved " + destFile.getName() ) ;
                    
                    nextImgName = qImg.nextQuestion() ;
                }
                catch( Exception e ) {
                    log.error( "Error saving image.", e ) ;
                    showErrorMsg( "Error saving image.", e ) ;
                }
            }
        }
        return processingId ;
    }
    
    @Override
    public void selectedRegionsUpdated( List<ExtractedImgInfo> selectedRegionsInfo, File imgFile ) {
        saveImgInfo( imgFile, selectedRegionsInfo ) ;
    }
    
    @Override
    public void processCommandKey( int keyCode ) {
    }
    
    @Override
    public void selectionStarted() {
        ImgExtractorPanel imgPanel = ( ImgExtractorPanel )tabbedPane.getSelectedComponent() ;
        if( nextImgName != null ) {
            imgPanel.setCurSelTagName( nextImgName.getShortFileNameWithoutExtension() ) ;
        }
    }
    
    @Override
    public void selectionEnded() {
        ImgExtractorPanel imgPanel = ( ImgExtractorPanel )tabbedPane.getSelectedComponent() ;
        imgPanel.clearCurSelTagName() ;
    }
    
    private List<ExtractedImgInfo> loadImgInfo( File imgFile ) {
        List<ExtractedImgInfo> imgInfoList = new ArrayList<>() ;
        File imgInfoFile = getImgInfoFile( imgFile ) ;
        if( imgInfoFile.exists() ) {
            try {
                ObjectInputStream ois = new ObjectInputStream( new FileInputStream( imgInfoFile ) ) ;
                imgInfoList = ( List<ExtractedImgInfo> )ois.readObject() ;
                ois.close() ;
            }
            catch( Exception e ) {
                log.error( "Error reading image info.", e ) ;
                showErrorMsg( "Error reading image info", e ) ;
            }
        }
        return imgInfoList ;
    }
    
    private void saveImgInfo( File imgFile, List<ExtractedImgInfo> selectedRegionsInfo ) {
        File imgInfoFile = getImgInfoFile( imgFile ) ;
        try {
            ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( imgInfoFile ) ) ;
            oos.writeObject( selectedRegionsInfo ) ;
            oos.close() ;
        }
        catch( Exception e ) {
            log.error( "Error saving image info.", e ) ;
            showErrorMsg( "Error saving image info.", e ) ;
        }
    }
    
    private File getImgInfoFile( File imgFile ) {
        return new File( workDir, AppUtil.stripExtension( imgFile ) + ".regions.info" ) ;
    }
}
