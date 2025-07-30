package com.sandy.sconsole.qimgextractor.ui.project.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ProjectModel {

    @Getter private File baseDir ;
    @Getter private File pagesDir ;
    @Getter private File workDir ;
    @Getter private File extractedImgDir ;
    
    private List<PageImage> pageImages ;
    
    public ProjectModel() {}
    
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
        
        log.info( "  Project directory: {}", baseDir.getAbsolutePath() ) ;
        log.info( "  Pages directory: <project-dir>/{}", pagesDir.getName() ) ;
        log.info( "  Work directory: <project-dir>/{}", workDir.getName() ) ;
        log.info( "  Images directory: <project-dir>/{}", extractedImgDir.getName() ) ;
        log.info( "" ) ;
        log.info( "  Loading pages...." ) ;
        
        loadPageImages() ;
    }
    
    private void loadPageImages() {
        
        File[] files = pagesDir.listFiles( f -> f.getName().endsWith( ".png" ) ) ;
        assert files != null ;
        
        pageImages = new ArrayList<>() ;
        for( File file : files ) {
            PageImage pageImg = new PageImage( this, file );
            pageImages.add( pageImg );
        }
    }
    
}
