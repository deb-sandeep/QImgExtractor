package com.sandy.sconsole.qimgextractor.ui.project.model;

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
    
    private Map<File, PageImage> pageImageMap ;
    
    public ProjectModel( AppConfig appConfig ) {
        this.appConfig = appConfig ;
    }
    
    public void initialize( File projectDir ) {
        log.info( "Initializing project model." ) ;
        
        this.baseDir = projectDir ;
        this.pagesDir = new File( baseDir, "pages" ) ;
        this.workDir = new File( projectDir, ".workspace" ) ;
        this.extractedImgDir = new File( projectDir, "question-images" ) ;
        
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
    
    private void loadPageImages() {
        
        File[] files = pagesDir.listFiles( f -> f.getName().endsWith( ".png" ) ) ;
        assert files != null ;
        
        pageImageMap = new HashMap<>() ;
        pageImages = new ArrayList<>() ;
        
        for( File file : files ) {
            PageImage pageImg = new PageImage( this, file );
            pageImages.add( pageImg );
            pageImageMap.put( file, pageImg ) ;
        }
        Collections.sort( pageImages ) ;
    }
    
    public PageImage getPageImage( File file ) {
        return pageImageMap.get( file ) ;
    }
    
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
}
