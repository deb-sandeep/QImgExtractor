package com.sandy.sconsole.qimgextractor.ui.project.imgscraper;

import com.sandy.sconsole.qimgextractor.ui.MainFrame;
import com.sandy.sconsole.qimgextractor.ui.core.tabbedpane.CloseableTabbedPane;
import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.ImgCanvasListener;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.ImgExtractorPanel;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.SelectedRegionMetadata;
import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.state.ProjectContext;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.savedialog.ImgSaveDialog;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.tree.PageQuestionTreePanel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    private final PageQuestionTreePanel treePanel ;
    
    private final Map<PageImage, ImgExtractorPanel> panelMap = new HashMap<>() ;
    // Pages closed in this session, most recent first. Left arrow in
    // command mode reopens them in reverse order of closing.
    private final Deque<PageImage> closedPages = new ArrayDeque<>() ;
    // Page images are decoded and scaled in parallel on this pool.
    private final ExecutorService executor =
            Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() ) ;
    
    // Navigation requests (from remote commands) which arrive while the page
    // images are still being loaded are parked here and served once the
    // load completes. Accessed only on the EDT.
    private boolean pageImagesLoaded = false ;
    
    // Set once a question image is cut in this session. Closing pages while
    // cutting doesn't end the page review, as pages yet to be cut have no
    // question images.
    private boolean imgCutInSession = false ;
    private String pendingQImgFileName = null ;
    
    // When true, all page panels (including ones opened later) are zoomed
    // so that the complete page height is visible. When false, pages are
    // zoomed to the default display width.
    private boolean fitPageZoom = true ;
    
    public ImageScraperUI( ProjectPanel projectPanel ) {
        
        this.projectPanel = projectPanel ;
        this.projectModel = projectPanel.getProjectModel() ;
        this.mainFrame = projectPanel.getMainFrame() ;
        this.saveDialog = new ImgSaveDialog( projectModel ) ;
        this.tabPane = new CloseableTabbedPane() ;
        this.treePanel = new PageQuestionTreePanel( this ) ;
        
        setUpUI() ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() ) ;
        
        tabPane.setTabPlacement( JTabbedPane.RIGHT ) ;
        tabPane.addChangeListener( e -> tabSelectionChanged() ) ;
        tabPane.addTabCloseListener( (tabIndex, component ) -> tabClosing( component ) ) ;
        
        add( tabPane, BorderLayout.CENTER ) ;
        add( treePanel, BorderLayout.WEST ) ;
    }
    
    public void loadPageImages() {
        if( !SwingUtilities.isEventDispatchThread() ) {
            invokeLater( this::loadPageImages ) ;
            return ;
        }
        
        List<PageImage> visiblePages = new ArrayList<>() ;
        for( PageImage pageImg : projectModel.getPageImages() ) {
            if( pageImg.getState().isVisible() ) {
                visiblePages.add( pageImg ) ;
            }
        }
        
        // Modal, so the user can't interact with half loaded pages. Shown
        // in a later EDT task since showing a modal dialog blocks the caller.
        PageLoadProgressDialog progressDialog = new PageLoadProgressDialog(
                mainFrame, projectModel.getProjectName(), visiblePages.size() ) ;
        if( !visiblePages.isEmpty() ) {
            invokeLater( progressDialog::showDialog ) ;
        }
        
        new SwingWorker<Void, PageImage>() {
            @Override
            protected Void doInBackground() {
                projectModel.getContext().setPauseSavePageState( true ) ;
                
                // Decode and scale all pages in parallel...
                List<Future<ImgExtractorPanel>> futures = new ArrayList<>() ;
                for( PageImage pageImg : visiblePages ) {
                    futures.add( executor.submit( () -> {
                        try {
                            return createPagePanel( pageImg ) ;
                        }
                        finally {
                            String fileName = pageImg.getImgFile().getName() ;
                            invokeLater( () -> progressDialog.pageLoaded( fileName ) ) ;
                        }
                    } ) ) ;
                }
                
                // ...and register them in page order as they become ready.
                for( int i=0; i<visiblePages.size(); i++ ) {
                    PageImage pageImg = visiblePages.get( i ) ;
                    try {
                        addPagePanel( pageImg, futures.get( i ).get() ) ;
                        mainFrame.logStausMsg( "Loaded (" + (i+1) + "/" + visiblePages.size() + ") " +
                                               pageImg.getImgFile().getName() ) ;
                    }
                    catch( Exception e ) {
                        log.error( "Error loading page image {}.", pageImg.getImgFile().getName(), e ) ;
                    }
                }
                return null ;
            }
            
            @Override
            protected void done() {
                // Closed before activating the page so that it gets focus.
                progressDialog.finish() ;
                
                QuestionImage lastSavedQImg = projectModel.getContext().getLastSavedImg() ;
                PageImage lastSelectedPageImg = projectModel.getSelectedPageImg() ;
                
                if( lastSelectedPageImg != null ) {
                    activatePageImg( lastSelectedPageImg ) ;
                }
                else if ( lastSavedQImg != null ) {
                    activatePageImg( lastSavedQImg.getPageImg() ) ;
                }
                else {
                    // Tabs are added in queued EDT tasks, so focus after them.
                    invokeLater( () -> focusSelectedPanel() ) ;
                }
                mainFrame.logStausMsg( "Page images loaded." ) ;
                projectModel.getContext().setPauseSavePageState( false ) ;
                projectModel.savePageState() ;
                
                pageImagesLoaded = true ;
                if( pendingQImgFileName != null ) {
                    String qImgFileName = pendingQImgFileName ;
                    pendingQImgFileName = null ;
                    showQuestionImage( qImgFileName ) ;
                }
            }
        }.execute() ;
    }
    
    public void destroy() {
        int numberOfTabs = tabPane.getTabCount() ;
        for( int i=numberOfTabs-1; i>=0; i-- ) {
            ImgExtractorPanel panel = ( ImgExtractorPanel )tabPane.getTabComponentAt( i ) ;
            if( panel != null ) {
                panel.destroy() ;
                tabPane.remove( i ) ;
            }
        }
        executor.shutdown() ;
    }
    
    private void loadPageImg( PageImage pageImg ) {
        addPagePanel( pageImg, createPagePanel( pageImg ) ) ;
    }
    
    // The expensive part of loading a page (reading, decoding and scaling
    // the image). Touches no shared state, so pages can be created in
    // parallel.
    private ImgExtractorPanel createPagePanel( PageImage pageImg ) {
        
        //log.debug( "Loading page image {}.", pageImg.getImgFile().getName() ) ;
        
        int initialDisplayWidth = 1100 ;
        ImgExtractorPanel imgPanel = new ImgExtractorPanel( this ) ;
        imgPanel.setImage( pageImg, initialDisplayWidth ) ;
        return imgPanel ;
    }
    
    // Registers a created page panel and adds its tab (in page sequence).
    // Updates shared state, hence serialized.
    private synchronized void addPagePanel( PageImage pageImg, ImgExtractorPanel imgPanel ) {
        
        File file = pageImg.getImgFile() ;
        
        panelMap.put( pageImg, imgPanel ) ;
        projectModel.addListener( imgPanel ) ;
        
        pageImg.getState().setVisible( true ) ;
        projectModel.savePageState() ;
        
        invokeLater( () -> {
            tabPane.insertTab( file.getName(), null, imgPanel, null,
                               getTabInsertIndex( pageImg ) ) ;
            if( pageImg.getState().isHasAnswerKeys() ) {
                tabPane.raiseAlert( imgPanel ) ;
            }
            if( fitPageZoom ) {
                imgPanel.setFitPageMode( true ) ;
            }
        } ) ;
    }
    
    // Tabs are kept in page sequence, so a page (re)opened later is placed
    // after all the open pages which precede it in the project.
    private int getTabInsertIndex( PageImage pageImg ) {
        List<PageImage> allPages = projectModel.getPageImages() ;
        int pageSeq = allPages.indexOf( pageImg ) ;
        int index = 0 ;
        for( int i=0; i<tabPane.getTabCount(); i++ ) {
            ImgExtractorPanel panel = ( ImgExtractorPanel )tabPane.getComponentAt( i ) ;
            if( allPages.indexOf( panel.getPageImg() ) < pageSeq ) {
                index = i+1 ;
            }
        }
        return index ;
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
        ImgExtractorPanel panel = ( ImgExtractorPanel )component ;
        panelMap.remove( panel.getPageImg() ) ;
        projectModel.removeListener( panel ) ;
        projectModel.setPageImgClosed( panel.getPageImg() ) ;
        
        // Most recently closed page on top, each page at most once.
        closedPages.remove( panel.getPageImg() ) ;
        closedPages.push( panel.getPageImg() ) ;
        
        // Closing the last open page with question images ends the page
        // review. Pages without question images (e.g. the answer key) may
        // still be open. Run later so that the closing completes first.
        boolean closedQuestionPage = !panel.getPageImg().getQImgList().isEmpty() ;
        boolean questionPageOpen = panelMap.keySet().stream()
                                           .anyMatch( p -> !p.getQImgList().isEmpty() ) ;
        if( pageImagesLoaded && !imgCutInSession && closedQuestionPage && !questionPageOpen ) {
            invokeLater( projectPanel::questionPagesReviewed ) ;
        }
    }
    
    @Override
    public String subImageSelected( File imgSrcFile, BufferedImage img,
                                    Rectangle subImgBounds,
                                    int selectionEndAction ) {
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
                imgFileName = getFQFileName( projectModel.getProjectName(),
                                             extractPageNumber( imgSrcFile, projectModel.isManualProject() ),
                                             fileName ) ;
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
            imgCutInSession = true ;
            
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
                extractPageNumber( pageImage.getImgFile(), projectModel.isManualProject() ),
                regionMeta.getTag() + ".png" ) ;
        File imgFile = new File( projectModel.getExtractedImgDir(), fqFileName ) ;
        
        QuestionImage qImg = new QuestionImage( pageImage, imgFile, regionMeta ) ;
        
        pageImage.addQuestionImg( qImg, true ) ;
        projectModel.getContext().setLastSavedImage( qImg ) ;
        
        log.debug( "  Notifying the model listeners." ) ;
        projectModel.notifyListenersNewQuestionImgAdded( pageImage, qImg ) ;
    }
    
    @Override
    public boolean subImageResized( PageImage pageImage, String tag,
                                    BufferedImage img, Rectangle subImgBounds ) {

        QuestionImage qImg = null ;
        for( QuestionImage q : pageImage.getQImgList() ) {
            if( q.getImgRegionMetadata().getTag().equals( tag ) ) {
                qImg = q ;
                break ;
            }
        }
        if( qImg == null ) {
            log.error( "No question image with tag {} on page {}.", tag, pageImage.getImgFile().getName() ) ;
            showErrorMsg( this, "Question image " + tag + " not found. Resize reverted." ) ;
            return false ;
        }

        // Write to a temporary file first so that a failed write doesn't
        // leave the existing question image truncated.
        File imgFile = qImg.getImgFile() ;
        File tmpFile = new File( imgFile.getParentFile(), imgFile.getName() + ".tmp" ) ;
        try {
            ImageIO.write( img, "png", tmpFile ) ;
            Files.move( tmpFile.toPath(), imgFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE ) ;
        }
        catch( Exception e ) {
            log.error( "Error saving resized image {}.", imgFile.getName(), e ) ;
            tmpFile.delete() ;
            showErrorMsg( "Error saving resized image. Resize reverted.", e ) ;
            return false ;
        }

        SelectedRegionMetadata regionMeta = qImg.getImgRegionMetadata() ;
        regionMeta.setRegionBounds( new Rectangle( subImgBounds ) ) ;
        regionMeta.setAnchorPoint( new Point( subImgBounds.x, subImgBounds.y ) ) ;
        pageImage.saveQuestionImgMetadata() ;

        projectModel.getState().setImgCuttingWip( true ) ;
        mainFrame.logStausMsg( "   Resized image file : " + imgFile.getName() ) ;
        return true ;
    }

    @Override
    public void processImgCanvasCommandKey( int keyCode ) {
        switch( keyCode ) {
            case KeyEvent.VK_N -> showNextTab() ;
            case KeyEvent.VK_X, KeyEvent.VK_RIGHT -> closeCurrentTab() ;
            case KeyEvent.VK_LEFT -> reopenLastClosedTab() ;
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
        projectModel.getState().setImgCuttingWip( true ) ;
    }
    
    public void changeSubject( List<QuestionImage> qImgs, String subjectCode ) {
        try {
            projectModel.changeSubject( qImgs, subjectCode ) ;
            projectModel.getState().setImgCuttingWip( true ) ;
        }
        catch( IllegalStateException e ) {
            log.error( "Failed to change subject to {}", subjectCode, e ) ;
            showErrorMsg( this, e.getMessage() ) ;
        }
    }

    // Changes the type of the questions owning the given images. All the
    // images of an owning question (all parts, across pages) are renamed,
    // not just the selected ones, so that a question isn't split.
    public void changeQuestionType( List<QuestionImage> qImgs, String questionType ) {
        Set<Question> questions = new LinkedHashSet<>() ;
        boolean hasLCTCtxImgs = false ;
        for( QuestionImage qImg : qImgs ) {
            Question q = projectModel.findQuestion( qImg ) ;
            if( q != null ) {
                questions.add( q ) ;
            }
            else {
                hasLCTCtxImgs = true ;
            }
        }

        try {
            List<String> skipped = projectModel.changeQuestionTypeOfQuestions( questions, questionType ) ;
            projectModel.getState().setImgCuttingWip( true ) ;
            if( !skipped.isEmpty() || hasLCTCtxImgs ) {
                showErrorMsg( this, "LCT question types can't be changed. Skipped" +
                                    ( skipped.isEmpty() ? " LCT context images." : ": " + String.join( ", ", skipped ) ) ) ;
            }
        }
        catch( IllegalArgumentException | IllegalStateException e ) {
            log.error( "Failed to change question type to {}", questionType, e ) ;
            showErrorMsg( this, e.getMessage() ) ;
        }
    }

    public void questionImgTagNameChanged( QuestionImage qImg, String newTagName ) {
        projectModel.questionImgTagNameChanged( qImg, newTagName ) ;
        projectModel.getState().setImgCuttingWip( true ) ;
    }
    
    public void activatePageImg( PageImage pageImg ) {
        invokeLater( () -> {
            if( panelMap.containsKey( pageImg ) ) {
                ImgExtractorPanel selPanel = ( ImgExtractorPanel )tabPane.getSelectedComponent() ;
                if( selPanel.getPageImg() != pageImg ) {
                    tabPane.setSelectedComponent( panelMap.get( pageImg ) ) ;
                }
                focusSelectedPanel() ;
            }
            else {
                loadPageImg( pageImg ) ;
                invokeLater( () -> {
                    tabPane.setSelectedComponent( panelMap.get( pageImg ) ) ;
                    focusSelectedPanel() ;
                } ) ;
            }
        }) ;
    }
    
    // Selects the tab of the page containing the question image and scrolls
    // the page so that the question region is visible. The file name is of
    // the format <srcId>.<pageNum>.<tagName>[(<partNumber>)].png
    // Must be called on the EDT.
    public void showQuestionImage( String qImgFileName ) {
        if( !pageImagesLoaded ) {
            pendingQImgFileName = qImgFileName ;
            return ;
        }
        
        QuestionImage qImg = findQuestionImage( qImgFileName ) ;
        if( qImg == null ) {
            log.warn( "Question image {} not found in project.", qImgFileName ) ;
            mainFrame.logStausMsg( "Question image not found : " + qImgFileName ) ;
            return ;
        }
        
        PageImage pageImg = qImg.getPageImg() ;
        String tag = qImg.getImgRegionMetadata().getTag() ;
        if( !panelMap.containsKey( pageImg ) ) {
            // Adds the tab asynchronously, hence the selection below is
            // done in a subsequent EDT task.
            loadPageImg( pageImg ) ;
        }
        
        invokeLater( () -> {
            ImgExtractorPanel panel = panelMap.get( pageImg ) ;
            if( panel == null ) return ;
            tabPane.setSelectedComponent( panel ) ;
            invokeLater( () -> {
                if( !panel.scrollToRegion( tag ) ) {
                    log.warn( "Region {} not found on page {}.", tag, pageImg.getImgFile().getName() ) ;
                }
            } ) ;
            mainFrame.logStausMsg( "Showing question image : " + qImgFileName ) ;
        } ) ;
    }
    
    private QuestionImage findQuestionImage( String qImgFileName ) {
        for( PageImage pageImg : projectModel.getPageImages() ) {
            for( QuestionImage qImg : pageImg.getQImgList() ) {
                if( qImg.getImgFile().getName().equals( qImgFileName ) ) {
                    return qImg ;
                }
            }
        }
        return null ;
    }
    
    public void showNextTab() {
        int curTabIndex = tabPane.getSelectedIndex() ;
        if( curTabIndex < tabPane.getTabCount()-1 ) {
            tabPane.setSelectedIndex( curTabIndex + 1 ) ;
        }
    }
    
    // Reopens the most recently closed page which is not already open
    // (it may have been reopened from the tree) and makes it the active tab.
    public void reopenLastClosedTab() {
        while( !closedPages.isEmpty() ) {
            PageImage pageImg = closedPages.pop() ;
            if( !panelMap.containsKey( pageImg ) ) {
                activatePageImg( pageImg ) ;
                return ;
            }
        }
        mainFrame.logStausMsg( "No closed page to reopen." ) ;
    }
    
    public void closeCurrentTab() {
        int curTabIndex = tabPane.getSelectedIndex() ;
        if( curTabIndex >= 0 ) {
            tabPane.remove( curTabIndex ) ;
        }
    }
    
    public void closePageImageTab( PageImage pageImage ) {
        ImgExtractorPanel panel = panelMap.get( pageImage );
        if( panel != null ) {
            int index = tabPane.indexOfComponent( panel );
            if( index >= 0 ) {
                tabPane.remove( index );
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
    
    public void toggleAnswerKeyMarkerForActivePage() {
        ImgExtractorPanel curPanel = ( ImgExtractorPanel )tabPane.getSelectedComponent() ;
        if( curPanel != null ) {
            toggleAnswerKeyMarker( curPanel.getPageImg() ) ;
        }
    }
    
    public void toggleAnswerKeyMarker( PageImage pageImg ) {
        boolean hasAnswerKeys = !pageImg.getState().isHasAnswerKeys() ;
        pageImg.getState().setHasAnswerKeys( hasAnswerKeys ) ;
        projectModel.savePageState() ;
        
        // The page might not be open in a tab, in which case only the
        // tree needs to reflect the change.
        ImgExtractorPanel panel = panelMap.get( pageImg ) ;
        if( panel != null ) {
            if( hasAnswerKeys ) {
                tabPane.raiseAlert( panel ) ;
            }
            else {
                tabPane.lowerAlert( panel ) ;
            }
        }
        treePanel.getTree().repaint() ;
    }

    public void zoomAllToFitPage() {
        fitPageZoom = true ;
        panelMap.values().forEach( p -> p.setFitPageMode( true ) ) ;
    }
    
    public void zoomAllToWidth() {
        fitPageZoom = false ;
        panelMap.values().forEach( ImgExtractorPanel::zoomToWidth ) ;
    }

    // This method is called just before the panel is made visible. Can be used
    // to update the UI state based on any changes that have happened through
    // other project modules.
    public void handlePreActivation() {
        invokeLater( this::focusSelectedPanel ) ;
    }
    
    // Gives keyboard focus to the canvas of the selected page so that
    // command mode keys work without first clicking on the page. Selecting
    // a tab only focuses it when the selection changes, which misses the
    // initial tab at startup and re-activation of this view.
    private void focusSelectedPanel() {
        ImgExtractorPanel selPanel = ( ImgExtractorPanel )tabPane.getSelectedComponent() ;
        if( selPanel != null ) {
            selPanel.requestFocus() ;
        }
    }
}
