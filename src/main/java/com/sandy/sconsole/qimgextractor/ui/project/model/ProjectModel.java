package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.sandy.sconsole.qimgextractor.ui.project.imgpanel.SubImgInfo;
import com.sandy.sconsole.qimgextractor.util.AppConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Component
@Slf4j
public class ProjectModel {

    private final AppConfig appConfig ;
    
    @Getter private File baseDir ;
    @Getter private File pagesDir ;
    @Getter private File workDir ;
    @Getter private File extractedImgDir ;
    @Getter private String projectName ;
    @Getter private ProjectContext context;
    @Getter private List<PageImage> pageImages ;
    
    private final List<ProjectModelListener> listeners = new ArrayList<>() ;
    
    public ProjectModel( AppConfig appConfig ) {
        this.appConfig = appConfig ;
    }
    
    public void initialize( File projectDir ) {
        log.info( "Initializing project model." ) ;
        
        this.baseDir = projectDir ;
        this.pagesDir = new File( baseDir, "pages" ) ;
        this.workDir = new File( projectDir, ".workspace" ) ;
        this.extractedImgDir = new File( projectDir, "question-images" ) ;
        this.listeners.clear() ;
        
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
        
        this.projectName = projectDir.getName() ;
        
        context = new ProjectContext() ;
        
        log.info( "  Project directory: {}", baseDir.getAbsolutePath() ) ;
        log.info( "  Pages directory: <project-dir>/{}", pagesDir.getName() ) ;
        log.info( "  Work directory: <project-dir>/{}", workDir.getName() ) ;
        log.info( "  Images directory: <project-dir>/{}", extractedImgDir.getName() ) ;
        log.info( "" ) ;
        log.info( "  Loading pages...." ) ;
        
        loadPageImages() ;
        
        if( appConfig.isRepairProjectOnStartup() ) {
            log.info( "  Repairing project artefacts...." ) ;
            repairProjectArtefacts() ;
        }
    }
    
    public void addListener( ProjectModelListener listener ) {
        listeners.add( listener ) ;
    }
    
    // Assumption: Once a project has been loaded, no new pages can be added
    // to the project. The page images existing in the pages folder forms the
    // domain of pages on which the project will operate.
    private void loadPageImages() {
        
        File[] files = pagesDir.listFiles( f -> f.getName().endsWith( ".png" ) ) ;
        assert files != null ;
        
        pageImages = new ArrayList<>() ;
        
        QuestionImage lastSavedQImg = null ;
        
        for( File file : files ) {
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
            List<File> subImgFiles = pageImg.getSubImgFiles() ;
            for( File subImgFile : subImgFiles ) {
                toDelete.remove( subImgFile ) ;
            }
        }
        
        if( !toDelete.isEmpty() ) {
            for( File file : toDelete ) {
                if( file.delete() ) {
                    log.warn( "    Deleted extraneous image file: {}", file.getAbsolutePath() ) ;
                }
            }
        }
    }
    
    public void notifyListenersNewQuestionImgAdded( PageImage pageImage, QuestionImage qImg ) {
        listeners.forEach( l -> l.newQuestionImgAdded( pageImage, qImg ) ) ;
    }
}
