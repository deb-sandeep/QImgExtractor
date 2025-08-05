package com.sandy.sconsole.qimgextractor.ui.project;

import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.MainFrame;
import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.imgpanel.SubImgInfo;
import com.sandy.sconsole.qimgextractor.ui.project.imgpanel.SubImgListener;
import com.sandy.sconsole.qimgextractor.ui.project.imgpanel.ImgExtractorPanel;
import com.sandy.sconsole.qimgextractor.ui.core.tabbedpane.CloseableTabbedPane;
import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.savedialog.ImgSaveDialog;
import com.sandy.sconsole.qimgextractor.ui.project.tree.ProjectTreePanel;
import com.sandy.sconsole.qimgextractor.util.AppUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sandy.sconsole.qimgextractor.util.AppUtil.*;
import static javax.swing.SwingUtilities.invokeLater;

@Slf4j
public class ProjectPanel extends JPanel implements SubImgListener {
    
    private final MainFrame mainFrame ;
    private final ImgSaveDialog saveDialog ;
    
    private CloseableTabbedPane tabPane;
    private ProjectTreePanel pageTree ;
    
    @Getter
    private final ProjectModel projectModel ;
    
    private final Map<PageImage, ImgExtractorPanel> panelMap = new HashMap<>();
    
    private transient QuestionImage nextImgName = null ;

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
        tabPane.addTabCloseListener( (tabIndex, component ) -> tabClosing( component ) ) ;
        
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
            loadPageImg( pageImg ) ;
        }
        mainFrame.clearStatusMsg() ;
        
        QuestionImage lastSavedQImg = projectModel.getContext().getLastSavedImg() ;
        if( lastSavedQImg != null ) {
            activatePageImg( lastSavedQImg.getPageImg() ) ;
        }
    }
    
    public void destroy() {
        invokeLater( () -> {
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
    
    private void loadPageImg( PageImage pageImg ) {
        
        File file = pageImg.getImgFile() ;
        
        int initialDisplayWidth = SwingUtils.getScreenWidth() - ProjectTreePanel.PREFERRED_WIDTH - 10 ;
        ImgExtractorPanel imgPanel = new ImgExtractorPanel( this ) ;
        imgPanel.setImage( pageImg, initialDisplayWidth ) ;
        
        panelMap.put( pageImg, imgPanel ) ;
        projectModel.addListener( imgPanel ) ;
        
        invokeLater( () -> tabPane.addTab( file.getName(), imgPanel ) ) ;
    }
    
    private void tabSelectionChanged() {
        ImgExtractorPanel selectedPanel = ( ImgExtractorPanel )tabPane.getSelectedComponent() ;
        if( selectedPanel != null ) {
            projectModel.getContext()
                        .setSelectedPageImg( selectedPanel.getPageImg() ) ;
        }
    }
    
    private void tabClosing( Component component ) {
        ImgExtractorPanel panel = ( ImgExtractorPanel )component ;
        panelMap.remove( panel.getPageImg() ) ;
        projectModel.removeListener( panel ) ;
    }
    
    @Override
    public String subImageSelected( File imgSrcFile, BufferedImage img,
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
            // 1. Append .png if the user has not specified. Input brevity is allowed.
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
            
            // 3. Save the image, and other housekeeping tasks.
            File newImgFile = new File( destDir, imgFileName );
            ImageIO.write( img, "png", newImgFile );
            
            // 4. Parse the file to see if it meets the file name criteria.
            // If not, then an exception will be thrown.
            // NOTE: THis is a hack - the question image being created is a
            // fake instance with the sole purpose of generating the short
            // file name without the extension - this becomes the tag name
            // which this function returns. DON'T use this qImg instance for
            // anything else.
            PageImage curPageImg = projectModel.getContext().getSelectedPageImg() ;
            QuestionImage qImg = new QuestionImage( curPageImg, newImgFile, null );
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
    public void selectedRegionAdded( PageImage pageImage, SubImgInfo newRegionInfo ) {
        
        String fqFileName = getFQFileName( projectModel.getProjectName(),
                                           AppUtil.extractPageNumber( pageImage.getImgFile() ),
                                           newRegionInfo.getTag() + ".png" ) ;
        File imgFile = new File( projectModel.getExtractedImgDir(), fqFileName ) ;
        
        QuestionImage qImg = new QuestionImage( pageImage, imgFile, newRegionInfo );
        
        pageImage.addQImg( qImg, true ) ;
        projectModel.getContext().setLastSavedImage( qImg ) ;
        projectModel.notifyListenersNewQuestionImgAdded( pageImage, qImg ) ;
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
    
    public void questionImgDeleted( SubImgInfo subImgInfo ) {
        // TODO:
    }
    
    public void questionImgTagNameChanged( QuestionImage qImg, String newTagName ) {
        projectModel.questionImgTagNameChanged( qImg, newTagName ) ;
    }
    
    public void activatePageImg( PageImage pageImg ) {
        invokeLater( () -> {
            if( panelMap.containsKey( pageImg ) ) {
                ImgExtractorPanel selPanel = ( ImgExtractorPanel )tabPane.getSelectedComponent() ;
                if( selPanel.getPageImg() != pageImg ) {
                    tabPane.setSelectedComponent( panelMap.get( pageImg ) ) ;
                }
            }
            else {
                loadPageImg( pageImg ) ;
                invokeLater( () -> tabPane.setSelectedComponent( panelMap.get( pageImg ) ) ) ;
            }
        }) ;
    }
}
