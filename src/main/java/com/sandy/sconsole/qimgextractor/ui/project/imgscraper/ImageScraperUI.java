package com.sandy.sconsole.qimgextractor.ui.project.imgscraper;

import com.sandy.sconsole.qimgextractor.ui.MainFrame;
import com.sandy.sconsole.qimgextractor.ui.core.tabbedpane.CloseableTabbedPane;
import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.ImgCanvasListener;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.ImgExtractorPanel;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.SelectedRegionMetadata;
import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.state.ProjectContext;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.savedialog.ImgSaveDialog;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.tree.PageQuestionTreePanel;
import com.sandy.sconsole.qimgextractor.util.AppUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.sandy.sconsole.qimgextractor.util.AppUtil.*;
import static com.sandy.sconsole.qimgextractor.util.AppUtil.getFQFileName;
import static javax.swing.SwingUtilities.invokeLater;

@Slf4j
public class ImageScraperUI extends JPanel
    implements ImgCanvasListener {

    private final MainFrame mainFrame ; // Injected
    
    @Getter
    private final ProjectPanel projectPanel ; // Injected

    @Getter
    private final ProjectModel projectModel ; // Injected
    
    private final ImgSaveDialog saveDialog ;
    private final CloseableTabbedPane tabPane;
    
    private final Map<PageImage, ImgExtractorPanel> panelMap = new HashMap<>() ;
    private final ExecutorService executor = Executors.newFixedThreadPool( 1 ) ;
    
    public ImageScraperUI( ProjectPanel projectPanel ) {
        
        this.projectPanel = projectPanel ;
        this.projectModel = projectPanel.getProjectModel() ;
        this.mainFrame = projectPanel.getMainFrame() ;
        this.saveDialog = new ImgSaveDialog( projectModel ) ;
        this.tabPane = new CloseableTabbedPane() ;
        
        setUpUI() ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() ) ;
        
        tabPane.addChangeListener( e -> tabSelectionChanged() ) ;
        tabPane.addTabCloseListener( (tabIndex, component ) -> tabClosing( component ) ) ;
        
        add( tabPane, BorderLayout.CENTER ) ;
        add( new PageQuestionTreePanel( this ), BorderLayout.WEST ) ;
    }
    
    public void loadPageImages() {
        new SwingWorker<Void, PageImage>() {
            @Override
            protected Void doInBackground() {
                List<PageImage> pageImages = projectModel.getPageImages() ;
                
                for( int i=0; i<pageImages.size(); i++ ) {
                    PageImage pageImg = pageImages.get( i ) ;
                    File file = pageImg.getImgFile() ;
                    mainFrame.logStausMsg( "Loading (" + i + "/" + pageImages.size() + ") " + file.getName() + "..." ) ;
                    
                    if( pageImg.getState().isVisible() ) {
                        loadPageImg( pageImg ) ;
                    }
                }
                return null ;
            }
            
            @Override
            protected void done() {
                QuestionImage lastSavedQImg = projectModel.getContext().getLastSavedImg() ;
                PageImage lastSelectedPageImg = projectModel.getSelectedPageImg() ;
                
                if( lastSelectedPageImg != null ) {
                    activatePageImg( lastSelectedPageImg ) ;
                }
                else if ( lastSavedQImg != null ) {
                    activatePageImg( lastSavedQImg.getPageImg() ) ;
                }
                mainFrame.logStausMsg( "Page images loaded." ) ;
            }
        }.execute() ;
    }
    
    public void destroy() {
        int numberOfTabs = tabPane.getTabCount() ;
        for( int i=numberOfTabs-1; i>=0; i-- ) {
            ImgExtractorPanel panel = ( ImgExtractorPanel )tabPane.getTabComponentAt( i ) ;
            if( panel != null ) {
                panel.destroy() ;
                tabPane.removeTabAt( i ) ;
            }
        }
        executor.shutdown() ;
    }
    
    private synchronized void loadPageImg( PageImage pageImg ) {
        
        File file = pageImg.getImgFile() ;
        
        int initialDisplayWidth = 1100 ;
        ImgExtractorPanel imgPanel = new ImgExtractorPanel( this ) ;
        imgPanel.setImage( pageImg, initialDisplayWidth ) ;
        
        panelMap.put( pageImg, imgPanel ) ;
        projectModel.addListener( imgPanel ) ;
        
        pageImg.getState().setVisible( true ) ;
        projectModel.savePageState() ;
        
        invokeLater( () -> tabPane.addTab( file.getName(), imgPanel ) ) ;
    }
    
    private void tabSelectionChanged() {
        ImgExtractorPanel selectedPanel = ( ImgExtractorPanel )tabPane.getSelectedComponent() ;
        if( selectedPanel != null ) {
            PageImage selectedPageImg = selectedPanel.getPageImg() ;
            projectModel.setSelectedPageImg( selectedPageImg ) ;
            selectedPanel.requestFocus() ;
        }
    }
    
    private void tabClosing( Component component ) {
        log.debug( "Tab closing event detected." ) ;
        ImgExtractorPanel panel = ( ImgExtractorPanel )component ;
        panelMap.remove( panel.getPageImg() ) ;
        projectModel.removeListener( panel ) ;
        projectModel.setPageImgClosed( panel.getPageImg() ) ;
    }
    
    @Override
    public String subImageSelected( File imgSrcFile, BufferedImage img,
                                    Rectangle subImgBounds, int selectionEndAction ) {
        
        String processingId = null ;
        File selectedFile ;
        File destDir ;
        
        saveDialog.updateRecommendedFileName() ;
        selectedFile = saveDialog.getSelectedFile() ;
        
        if( selectedFile == null || selectionEndAction == MouseEvent.BUTTON3 ) {
            log.debug( "  Recommended file name is null or user has initiated save dialog with right click." ) ;
            int userChoice = saveDialog.showSaveDialog( this ) ;
            
            if( userChoice == JOptionPane.OK_OPTION ) {
                selectedFile = saveDialog.getSelectedFile() ;
                destDir = selectedFile.getParentFile() ;
                log.debug( "  User accepted file name. {}", selectedFile.getName() ) ;
            }
            else {
                log.debug( "  User has cancelled the save dialog." ) ;
                return null ;
            }
        }
        else {
            destDir = selectedFile.getParentFile() ;
            log.debug( "  Using recommended file name. {}", selectedFile.getName() ) ;
        }
        
        try {
            log.debug( "  Transforming to fully qualified file name." ) ;
            // 1. Append .png if the user has not specified. Input brevity is allowed.
            String fileName = selectedFile.getName();
            if( !fileName.endsWith( ".png" ) ) {
                fileName += ".png";
                log.debug( "    Appending .png to file name." ) ;
            }
            
            // 2. Prepend the source id to make the file name complete
            // and save the image.
            String imgFileName = fileName;
            if( !fileName.startsWith( projectModel.getProjectName() ) ) {
                imgFileName = getFQFileName( projectModel.getProjectName(), extractPageNumber( imgSrcFile ), fileName ) ;
                log.debug( "    Prepending source id '{}' to file name.", projectModel.getProjectName() ) ;
            }
            
            // 3. Parse the file to see if it meets the file name criteria.
            // If not, then an exception will be thrown.
            // NOTE: THis is a hack - the question image being created is a
            // fake instance with the sole purpose of generating the short
            // file name without the extension - this becomes the tag name
            // which this function returns. DON'T use this qImg instance for
            // anything else.
            log.debug( "  Parsing file name to see if it meets the file name criteria." ) ;
            PageImage curPageImg = projectModel.getContext().getSelectedPageImg() ;
            File newImgFile = new File( destDir, imgFileName ) ;
            QuestionImage qImg = new QuestionImage( curPageImg, newImgFile, null ) ;
            
            // 4. If the file name is valid, check if the user has turned off part mode
            // while naming the file. If so, update the context accordingly.
            ProjectContext ctx = projectModel.getContext() ;
            if( ctx.isPartSelectionModeEnabled() ) {
                if( !qImg.isPart() ) ctx.setPartSelectionModeEnabled( false ) ;
            }
            else {
                if( qImg.isPart() ) ctx.setPartSelectionModeEnabled( true ) ;
            }
            
            // 5. Save the image, and other housekeeping tasks.
            log.debug( "    File name is valid. Saving image to file '{}'.", newImgFile.getName() ) ;
            ImageIO.write( img, "png", newImgFile ) ;
            processingId = qImg.getShortFileNameWithoutExtension() ;
            
            // 6. Reset the force next question flag
            projectModel.getContext().setForceNextImgFlag( false ) ;
            projectModel.getState().setImgCuttingWip( true ) ;
            
            mainFrame.logStausMsg( "   Saved image file : " + selectedFile.getName() );
        }
        catch( Exception e ) {
            log.error( "Error saving image.", e );
            showErrorMsg( "Error saving image.", e );
        }
        
        return processingId ;
    }
    
    @Override
    public void selectedRegionAdded( PageImage pageImage, SelectedRegionMetadata regionMeta ) {
        
        log.debug( "  Updating the project model with the new question image added." ) ;
        String fqFileName = getFQFileName( projectModel.getProjectName(),
                AppUtil.extractPageNumber( pageImage.getImgFile() ),
                regionMeta.getTag() + ".png" ) ;
        File imgFile = new File( projectModel.getExtractedImgDir(), fqFileName ) ;
        
        QuestionImage qImg = new QuestionImage( pageImage, imgFile, regionMeta ) ;
        
        pageImage.addQuestionImg( qImg, true ) ;
        projectModel.getContext().setLastSavedImage( qImg ) ;
        
        log.debug( "  Notifying the model listeners." ) ;
        projectModel.notifyListenersNewQuestionImgAdded( pageImage, qImg ) ;
    }
    
    @Override
    public void processImgCanvasCommandKey( int keyCode ) {
        switch( keyCode ) {
            case KeyEvent.VK_N -> showNextTab() ;
            case KeyEvent.VK_X -> closeCurrentTab() ;
        }
    }
    
    @Override
    public void selectionStarted() {
        ImgExtractorPanel imgPanel = ( ImgExtractorPanel )tabPane.getSelectedComponent() ;
        QuestionImage lastSavedImg = projectModel.getContext().getLastSavedImg() ;
        
        if( lastSavedImg != null ) {
            QuestionImage nextQ = lastSavedImg.nextQuestion() ;
            imgPanel.setCurSelTagName( nextQ.getShortFileNameWithoutExtension() ) ;
        }
    }
    
    @Override
    public void selectionEnded() {
        ImgExtractorPanel imgPanel = ( ImgExtractorPanel )tabPane.getSelectedComponent() ;
        imgPanel.clearCurSelTagName() ;
    }
    
    public void questionImgDeleted( QuestionImage qImg ) {
        projectModel.questionImgDeleted( qImg ) ;
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
    
    public void showNextTab() {
        int curTabIndex = tabPane.getSelectedIndex() ;
        if( curTabIndex < tabPane.getTabCount()-1 ) {
            tabPane.setSelectedIndex( curTabIndex + 1 ) ;
        }
    }
    
    public void closeCurrentTab() {
        int curTabIndex = tabPane.getSelectedIndex() ;
        if( curTabIndex >= 0 ) {
            tabPane.removeTabAt( curTabIndex ) ;
        }
    }
    
    public void closePageImageTab( PageImage pageImage ) {
        ImgExtractorPanel panel = panelMap.get( pageImage );
        if( panel != null ) {
            int index = tabPane.indexOfComponent( panel );
            if( index >= 0 ) {
                tabPane.removeTabAt( index );
            }
        }
    }
    
    public void closeAllRemainingTabs( PageImage pageImage ) {
        int targetPageNum = pageImage.getPageNumber();
        for( PageImage pi : new HashMap<>( panelMap ).keySet() ) {
            if( pi.getPageNumber() >= targetPageNum ) {
                closePageImageTab( pi );
            }
        }
    }
    
    public void openAllRemainingTabs( PageImage pageImage ) {
        int targetPageNum = pageImage.getPageNumber() ;
        for( PageImage pi : projectModel.getPageImages() ) {
            if( pi.getPageNumber() >= targetPageNum ) {
                if( !panelMap.containsKey( pi ) ) {
                    executor.submit( () -> loadPageImg( pi ) );
                }
            }
        }
    }
}
