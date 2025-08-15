package com.sandy.sconsole.qimgextractor.ui.project.imgscraper.savedialog;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.model.state.ProjectState;

import javax.swing.*;
import javax.swing.filechooser.FileView;
import java.io.File;

import static com.sandy.sconsole.qimgextractor.util.AppUtil.isValidProjectDir;

public class ProjectDirectoryView extends FileView {
    
    private final Icon folderUntouched = SwingUtils.getIcon( "folder_untouched" ) ;
    private final Icon folderImgCuttingWip = SwingUtils.getIcon( "folder_image_cutting_wip" ) ;
    private final Icon folderImgCuttingComplete = SwingUtils.getIcon( "folder_image_cutting_complete" ) ;
    private final Icon folderAnswersMapped = SwingUtils.getIcon( "folder_answers_mapped" ) ;
    private final Icon folderLinkedToTopics = SwingUtils.getIcon( "folder_topics_mapped" ) ;
    private final Icon folderSavedToServer = SwingUtils.getIcon( "folder_saved_to_server" ) ;
    private final Icon folderIncompatible = SwingUtils.getIcon( "folder_incompatible" ) ;
    
    @Override
    public Icon getIcon( File f ) {
        if( f.isDirectory() ) {
            if( isValidProjectDir( f ) ) {
                ProjectState ps = new ProjectState( f ) ;
                if( ps.isSavedToServer() ) {
                    return folderSavedToServer ;
                }
                else if( ps.isTopicsMapped() ) {
                    return folderLinkedToTopics ;
                }
                else if( ps.isAnswersMapped() ) {
                    return folderAnswersMapped ;
                }
                else if( ps.isImgCuttingComplete() ) {
                    return folderImgCuttingComplete ;
                }
                else if( ps.isImgCuttingWip() ) {
                    return folderImgCuttingWip ;
                }
                else {
                    return folderUntouched ;
                }
            }
            else {
                return folderIncompatible ;
            }
        }
        return super.getIcon( f ) ;
    }
}
