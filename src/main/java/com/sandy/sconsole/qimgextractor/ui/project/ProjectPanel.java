package com.sandy.sconsole.qimgextractor.ui.project;

import com.sandy.sconsole.qimgextractor.qid.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.MainFrame;
import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.SubImgInfo;
import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.SubImgListener;
import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.ImgExtractorPanel;
import com.sandy.sconsole.qimgextractor.ui.core.tabbedpane.CloseableTabbedPane;
import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.savedialog.ImgSaveDialog;
import com.sandy.sconsole.qimgextractor.ui.project.tree.ProjectTreePanel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

import static com.sandy.sconsole.qimgextractor.util.AppUtil.*;

@Slf4j
public class ProjectPanel extends JPanel implements SubImgListener {
    
    private final MainFrame mainFrame ;
    private final ImgSaveDialog saveDialog ;
    
    private CloseableTabbedPane tabPane;
    private ProjectTreePanel    pageTree ;
    
    private QuestionImage nextImgName = null ;
    
    @Getter
    private final ProjectModel projectModel ;

    public ProjectPanel( MainFrame mainFrame, ProjectModel model ) {
        
        this.projectModel = model ;
        this.mainFrame = mainFrame ;
        this.saveDialog = new ImgSaveDialog( model ) ;
        
        QuestionImage lastSavedImg = model.getContext().getLastSavedImg() ;
        if( lastSavedImg != null ) {
            this.nextImgName = lastSavedImg.nextQuestion() ;
        }
        
        setUpUI() ;
        new Thread( this::loadPageImages ).start() ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() );
        
        tabPane = new CloseableTabbedPane() ;
        tabPane.addChangeListener( e -> tabSelectionChanged() ) ;
        
        pageTree = new ProjectTreePanel( this ) ;
        
        add( tabPane, BorderLayout.CENTER ) ;
        add( pageTree, BorderLayout.WEST ) ;
    }
    
    private void loadPageImages() {
        
        List<PageImage> pageImages = projectModel.getPageImages() ;
        for( int i=0; i<pageImages.size(); i++ ) {
            PageImage pageImg = pageImages.get( i ) ;
            File file = pageImg.getImgFile() ;
            
            mainFrame.logStausMsg( "Loading (" + i + "/" + pageImages.size() + ") " + file.getName() + "..." ) ;

            ImgExtractorPanel imgPanel = new ImgExtractorPanel( this ) ;
            imgPanel.setImage( file,
                               pageImg.getSubImgInfoList(),
                               SwingUtils.getScreenWidth() - ProjectTreePanel.PREFERRED_WIDTH - 10 ) ;
            
            SwingUtilities.invokeLater( () -> tabPane.addTab( file.getName(), imgPanel ) ) ;
        }
        mainFrame.clearStatusMsg() ;
    }
    
    public void destroy() {
        SwingUtilities.invokeLater( () -> {
            int numberOfTabs = tabPane.getTabCount() ;
            for( int i=numberOfTabs-1; i>=0; i-- ) {
                ImgExtractorPanel panel = ( ImgExtractorPanel )tabPane.getTabComponentAt( i ) ;
                if( panel != null ) {
                    panel.destroy() ;
                    tabPane.removeTabAt( i ) ;
                }
            }
        } ) ;
    }
    
    private void tabSelectionChanged() {
        ImgExtractorPanel selectedPanel = ( ImgExtractorPanel )tabPane.getSelectedComponent() ;
        projectModel.getContext()
                    .setSelectedPageImageFile( selectedPanel.getCurImgFile() ) ;
    }
    
    @Override
    public String subImageSelected( File imgSrcFile, BufferedImage image,
                                    Rectangle subImgBounds, int selectionModifier ) {
        
        String processingId = null ;
        File selectedFile ;
        File destDir ;
        
        saveDialog.updateRecommendedFileName() ;
        selectedFile = saveDialog.getSelectedFile() ;
        
        if( selectedFile == null || selectionModifier == MouseEvent.BUTTON3 ) {
            int userChoice = saveDialog.showSaveDialog( this ) ;
            if( userChoice == JOptionPane.OK_OPTION ) {
                selectedFile = saveDialog.getSelectedFile() ;
                destDir = selectedFile.getParentFile() ;
            }
            else {
                return null ;
            }
        }
        else {
            destDir = selectedFile.getParentFile() ;
        }
        
        try {
            // 1. Append .png if user has not specified. Input brevity allowed.
            String fileName = selectedFile.getName();
            if( !fileName.endsWith( ".png" ) ) {
                fileName += ".png";
            }
            
            // 2. Prepend the source id to make the file name complete
            // and save the image.
            String imgFileName = fileName;
            if( !fileName.startsWith( projectModel.getProjectName() ) ) {
                imgFileName = getFQFileName( projectModel.getProjectName(), extractPageNumber( imgSrcFile ), fileName );
            }
            File newImgFile = new File( destDir, imgFileName );
            
            // 3. Parse the file to see if it meets the file name criteria.
            // If not, then an exception will be thrown.
            QuestionImage qImg = new QuestionImage( newImgFile );
            
            // 4. Save the image, and other housekeeping tasks.
            ImageIO.write( image, "png", newImgFile );
            projectModel.getContext().setLastSavedImage( qImg );
            
            processingId = qImg.getShortFileNameWithoutExtension();
            mainFrame.logStausMsg( "Saved " + selectedFile.getName() );
            
            nextImgName = qImg.nextQuestion();
        }
        catch( Exception e ) {
            log.error( "Error saving image.", e );
            showErrorMsg( "Error saving image.", e );
        }

        return processingId ;
    }
    
    @Override
    public void selectedRegionsUpdated( File imgFile, List<SubImgInfo> selectedRegionsInfo ) {
        PageImage pageImg = projectModel.getPageImage( imgFile ) ;
        assert pageImg != null ;
        pageImg.selectedRegionsUpdated( selectedRegionsInfo ) ;
    }
    
    @Override
    public void processCommandKey( int keyCode ) {
    }
    
    @Override
    public void selectionStarted() {
        ImgExtractorPanel imgPanel = ( ImgExtractorPanel )tabPane.getSelectedComponent() ;
        if( nextImgName != null ) {
            imgPanel.setCurSelTagName( nextImgName.getShortFileNameWithoutExtension() ) ;
        }
    }
    
    @Override
    public void selectionEnded() {
        ImgExtractorPanel imgPanel = ( ImgExtractorPanel )tabPane.getSelectedComponent() ;
        imgPanel.clearCurSelTagName() ;
    }
}
