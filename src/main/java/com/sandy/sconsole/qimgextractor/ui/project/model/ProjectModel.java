package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.ImgExtractorPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.state.PageImageState;
import com.sandy.sconsole.qimgextractor.ui.project.model.state.ProjectContext;
import com.sandy.sconsole.qimgextractor.ui.project.model.state.ProjectState;
import com.sandy.sconsole.qimgextractor.util.AppConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.sandy.sconsole.qimgextractor.util.AppUtil.showErrorMsg;

@Component
@Slf4j
public class ProjectModel {

    private final AppConfig appConfig ; // Injected
    
    @Getter private File baseDir ;
    @Getter private File pagesDir ;
    @Getter private File workDir ;
    @Getter private File extractedImgDir ;
    @Getter private String projectName ;
    @Getter private ProjectContext context ;
    @Getter private ProjectState state ;
    @Getter private QuestionRepo questionRepo ;
    @Getter private final List<PageImage> pageImages = new ArrayList<>() ;
    
    private final List<ProjectModelListener> listeners = new ArrayList<>() ;
    
    public ProjectModel( AppConfig appConfig ) {
        this.appConfig = appConfig ;
    }
    
    public void initialize( File projectDir ) {
        log.info( "  Initializing project model." ) ;
        
        this.baseDir = projectDir ;
        this.pagesDir = new File( baseDir, "pages" ) ;
        this.workDir = new File( projectDir, ".workspace" ) ;
        this.extractedImgDir = new File( projectDir, "question-images" ) ;
        this.context = new ProjectContext( this ) ;
        this.state = new ProjectState( this ) ;
        this.pageImages.clear() ;
        this.listeners.clear() ;
        this.projectName = projectDir.getName() ;
        
        if( !workDir.exists() ) {
            if( workDir.mkdirs() ) {
                log.info( "  Created workspace directory." ) ;
            }
        }
        
        if( !extractedImgDir.exists() ) {
            if( extractedImgDir.mkdirs() ) {
                log.info( "  Created extracted images directory." ) ;
            }
        }
        
        log.info( "    Project dir : {}", baseDir.getAbsolutePath() ) ;
        log.info( "    Pages dir   : <project-dir>/{}", pagesDir.getName() ) ;
        log.info( "    Work dir    : <project-dir>/{}", workDir.getName() ) ;
        log.info( "    Images dir  : <project-dir>/{}", extractedImgDir.getName() ) ;
        log.info( "    Loading pages...." ) ;
        
        loadPageImages() ;
        if( appConfig.isRepairProjectOnStartup() ) {
            log.info( "    Repairing project artefacts...." ) ;
            repairProjectArtefacts() ;
        }
        
        log.info( "    Initializing question repo...." ) ;
        this.questionRepo = new QuestionRepo( this ) ;
    }
    
    // Assumption: Once a project has been loaded, no new pages can be added
    // to the project. The page images existing in the pages folder form the
    // domain of pages on which the project will operate.
    private void loadPageImages() {
        
        File[] files = pagesDir.listFiles( f -> f.getName().endsWith( ".png" ) ) ;
        Map<String, PageImageState> pageStateMap = loadPageState() ;
        
        assert files != null ;
        
        QuestionImage lastSavedQImg = null ;
        
        for( File file : files ) {
            PageImage pageImg = new PageImage( this, file ) ;
            PageImageState state ;
            if( pageStateMap.containsKey( file.getName() ) ) {
                state = pageStateMap.get( file.getName() ) ;
            }
            else {
                state = new PageImageState( file.getName() ) ;
            }
            pageImg.setState( state ) ;
            pageImages.add( pageImg );
            
            QuestionImage lastQImg = pageImg.getLastQuestionImg() ;
            if( lastQImg != null ) {
                if( lastSavedQImg == null ) {
                    lastSavedQImg = lastQImg ;
                }
                else if( lastQImg.compareTo( lastSavedQImg ) > 0 ) {
                    lastSavedQImg = lastQImg ;
                }
            }
        }
        context.setLastSavedImage( lastSavedQImg ) ;
        Collections.sort( pageImages ) ;
    }
    
    // Key is the file name
    private Map<String, PageImageState> loadPageState() {
        
        File file = new File( this.getWorkDir(), "page-states.json" ) ;
        
        Map<String, PageImageState> stateMap = new HashMap<>();
        if( file.exists() ) {
            try {
                ObjectMapper mapper = new ObjectMapper() ;
                PageImageState[] states = mapper.readValue( file, PageImageState[].class );
                for( PageImageState state : states ) {
                    stateMap.put( state.getFileName(), state ) ;
                }
            }
            catch( IOException e ) {
                log.error( "Error loading page states", e );
            }
        }
        return stateMap;
    }
    
    public void savePageState() {
        
        File file = new File( this.getWorkDir(), "page-states.json" );
        List<PageImageState> states = new ArrayList<>();
        
        for( PageImage pageImage : pageImages ) {
            states.add( pageImage.getState() );
        }
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable( SerializationFeature.INDENT_OUTPUT );
            mapper.writeValue( file, states );
        }
        catch( IOException e ) {
            log.error( "Error saving page states", e );
        }
    }
    
    // This function is driven by the configuration 'repairProjectOnStartup'.
    // If set to true, the extracted image folder will be cleaned and synced
    // with the extracted images meta-data. This might cause deletion of images
    // in the question-images folder which are not associated with sub image meta-data
    // for a page.
    private void repairProjectArtefacts() {
        
        File[] files = extractedImgDir.listFiles( f -> f.getName().endsWith( ".png" ) ) ;
        if( files == null || files.length == 0 ) {
            return ;
        }
        
        ArrayList<File> toDelete = new ArrayList<>( List.of( files ) ) ;
        for( PageImage pageImg : pageImages ) {
            List<File> subImgFiles = pageImg.getQuestionImgFiles() ;
            for( File subImgFile : subImgFiles ) {
                toDelete.remove( subImgFile ) ;
            }
        }
        
        if( !toDelete.isEmpty() ) {
            for( File file : toDelete ) {
                if( file.delete() ) {
                    log.warn( "      Deleted extraneous image file: <img-dir>/{}", file.getName() ) ;
                }
            }
        }
    }
    
    public void addListener( ProjectModelListener listener ) {
        listeners.add( listener ) ;
    }
    
    public void removeListener( ImgExtractorPanel panel ) {
        listeners.remove( panel ) ;
    }
    
    public void notifyListenersNewQuestionImgAdded( PageImage pageImage, QuestionImage qImg ) {
        questionRepo.refresh() ;
        listeners.forEach( l -> l.newQuestionImgAdded( pageImage, qImg ) ) ;
    }
    
    private void notifyListenersTagNameChanged( QuestionImage qImg, String oldTagName, String newTagName ) {
        questionRepo.refresh() ;
        listeners.forEach( l -> l.questionTagNameChanged( qImg, oldTagName, newTagName ) ) ;
    }
    
    private void notifyListenersQuestionImgDeleted( QuestionImage qImg ) {
        questionRepo.refresh() ;
        listeners.forEach( l -> l.questionImgDeleted( qImg ) ) ;
    }
    
    public void notifyListenersPartSelectionModeUpdated( boolean value ) {
        listeners.forEach( l -> l.partSelectionModeUpdated( value ) ) ;
    }

    public void questionImgTagNameChanged( QuestionImage qImg, String newTagName ) {
        try {
            String oldTagName = qImg.getImgRegionMetadata().getTag() ;
            
            // Update qImg with the new tag name
            qImg.setNewTagName( newTagName ) ;
            qImg.getImgRegionMetadata().setTag( qImg.getShortFileNameWithoutExtension() ) ;
            qImg.getPageImg().saveQuestionImgMetadata() ;
            
            // Change the file name
            File oldFile = qImg.getImgFile() ;
            File newFile = new File( oldFile.getParentFile(), qImg.getLongFileName() ) ;
            FileUtils.moveFile( oldFile, newFile ) ;
            
            // Set the new file in the question image instance
            qImg.setImgFile( newFile ) ;
            
            // Notify the listeners
            notifyListenersTagNameChanged( qImg, oldTagName, newTagName ) ;
        }
        catch( IOException e ) {
            log.error( "Error renaming question image file.", e ) ;
            showErrorMsg( "Failed to rename question image file. Please try again.", e ) ;
        }
    }
    
    public void questionImgDeleted( QuestionImage qImg ) {
        // Delete it from the model
        qImg.getPageImg().deleteQuestionImg( qImg ) ;
        
        // Notify the listeners - the tree model and the image canvas
        notifyListenersQuestionImgDeleted( qImg ) ;
        
        // Physically delete the file.
        File imgFile = qImg.getImgFile() ;
        if( imgFile.delete() ) {
            log.info( "Deleted question image file: <img-dir>/{}", imgFile.getName() ) ;
        }
        else {
            log.warn( "Failed to delete question image file: <img-dir>/{}", imgFile.getName() ) ;
        }
    }
    
    public void setSelectedPageImg( PageImage selectedPageImg ) {
        context.setSelectedPageImg( selectedPageImg ) ;
        for( PageImage pageImage : pageImages ) {
            pageImage.getState().setSelected( pageImage == selectedPageImg );
        }
        savePageState() ;
    }
    
    public void setPageImgClosed( PageImage closedPageImg ) {
        for( PageImage pageImage : pageImages ) {
            if( pageImage == closedPageImg ) {
                pageImage.getState().setVisible( false ) ;
            }
        }
        savePageState() ;
    }
    
    public PageImage getSelectedPageImg() {
        for( PageImage pageImage : pageImages ) {
            if( pageImage.getState().isSelected() )
                return pageImage ;
        }
        return null ;
    }
}
