package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.sandy.sconsole.qimgextractor.ui.project.imgpanel.ImgExtractorPanel;
import com.sandy.sconsole.qimgextractor.util.AppConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

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
        this.context = new ProjectContext() ;
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
    }
    
    // Assumption: Once a project has been loaded, no new pages can be added
    // to the project. The page images existing in the pages folder form the
    // domain of pages on which the project will operate.
    private void loadPageImages() {
        
        File[] files = pagesDir.listFiles( f -> f.getName().endsWith( ".png" ) ) ;
        assert files != null ;
        
        QuestionImage lastSavedQImg = null ;
        
        for( File file : files ) {
            log.info( "      Loading page- {}.", file.getName() + " ..." ) ;
            PageImage pageImg = new PageImage( this, file );
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
        listeners.forEach( l -> l.newQuestionImgAdded( pageImage, qImg ) ) ;
    }
    
    private void notifyListenersTagNameChanged( QuestionImage qImg, String oldTagName, String newTagName ) {
        listeners.forEach( l -> l.questionTagNameChanged( qImg, oldTagName, newTagName ) ) ;
    }
    
    private void notifyListenersQuestionImgDeleted( QuestionImage qImg ) {
        listeners.forEach( l -> l.questionImgDeleted( qImg ) ) ;
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
            
            // Notify the listeners
            notifyListenersTagNameChanged( qImg, oldTagName, newTagName ) ;
        }
        catch( IOException e ) {
            log.error( "Error renaming question image file.", e ) ;
            showErrorMsg( "Failed to rename question image file. Please try again.", e ) ;
        }
    }
    
    public void questionImgDeleted( QuestionImage qImg ) {
        // Delete from the model
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
}
