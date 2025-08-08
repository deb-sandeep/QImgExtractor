package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.sandy.sconsole.qimgextractor.util.AppUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static com.sandy.sconsole.qimgextractor.QImgExtractor.logStatusMsg;

@Slf4j
public class ProjectContext {
    
    @Getter @Setter
    private int lastLCTSequence = 0 ;
    
    @Getter
    private QuestionImage lastSavedImg ;
    
    @Getter
    private PageImage selectedPageImg ;
    
    @Getter
    private int selectedPageNumber = -1 ;
    
    @Getter
    private boolean partSelectionModeEnabled = false ;
    
    @Getter
    private boolean forceNextImgFlag = false ;

    private final ProjectModel projectModel ;
    
    ProjectContext( ProjectModel projectModel ) {
        this.projectModel = projectModel ;
    }
    
    public void setLastSavedImage( QuestionImage qImg ) {
        lastSavedImg = qImg ;
        if( lastSavedImg != null && lastSavedImg.getQId().isLCT() ) {
            lastLCTSequence = lastSavedImg.getQId().getLctSequence() ;
        }
    }
    
    public void setSelectedPageImg( PageImage pageImg ) {
        this.selectedPageImg = pageImg ;
        this.selectedPageNumber = AppUtil.extractPageNumber( pageImg.getImgFile() ) ;
    }
    
    public void setPartSelectionModeEnabled( boolean value ) {
        this.partSelectionModeEnabled = value ;
        projectModel.notifyListenersPartSelectionModeUpdated( value ) ;
    }
    
    public void setForceNextImgFlag( boolean value ) {
        this.forceNextImgFlag = value ;
        if( value ) {
            logStatusMsg( "ForceNextImgFlag set" ) ;
        }
    }
}
