package com.sandy.sconsole.qimgextractor.ui.project.model;

// Thrown while a project is being loaded, when the user declines a
// destructive repair (dropping metadata or deleting files). It is raised
// before anything is written, so the project on disk is left untouched.
public class ProjectLoadAbortedException extends RuntimeException {

    public ProjectLoadAbortedException( String msg ) {
        super( msg ) ;
    }
}
