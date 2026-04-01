package com.sandy.sconsole.qimgextractor.ui.util;

import com.sandy.sconsole.qimgextractor.ui.project.model.state.ProjectState;
import com.sandy.sconsole.qimgextractor.util.AppConfig;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static com.sandy.sconsole.qimgextractor.util.AppUtil.isValidProjectDir;

public class ProjectDirectoryFileSystemView extends FileSystemView {

    private final AppConfig appConfig ;
    private final FileSystemView platformView ;

    public ProjectDirectoryFileSystemView( AppConfig appConfig ) {
        this.appConfig = appConfig ;
        this.platformView = FileSystemView.getFileSystemView() ;
    }

    @Override
    public File createNewFolder( File containingDir ) throws IOException {
        return platformView.createNewFolder( containingDir ) ;
    }

    @Override
    public File[] getFiles( File dir, boolean useFileHiding ) {
        File[] files = platformView.getFiles( dir, useFileHiding ) ;
        return Arrays.stream( files )
                .filter( file -> !shouldHide( file ) )
                .toArray( File[]::new ) ;
    }

    private boolean shouldHide( File file ) {
        if( !appConfig.isHideSyncedSrcFolders() ) {
            return false ;
        }
        if( !file.isDirectory() || !isValidProjectDir( file ) ) {
            return false ;
        }
        ProjectState projectState = new ProjectState( file ) ;
        return projectState.isSavedToServer() ;
    }
}
