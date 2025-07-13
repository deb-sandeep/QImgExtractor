package com.sandy.sconsole.qimgextractor.ui.project.model;

import java.io.File;

public class ProjectModel {

    private final File baseDir ;
    private final File pagesDir ;
    
    public ProjectModel( File baseDir ) {
        this.baseDir = baseDir ;
        this.pagesDir = new File( baseDir, "pages" ) ;
    }
}
