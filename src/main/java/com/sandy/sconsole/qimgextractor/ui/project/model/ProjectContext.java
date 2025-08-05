package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.sandy.sconsole.qimgextractor.qid.QuestionImage;
import com.sandy.sconsole.qimgextractor.util.AppUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

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

    ProjectContext() {}
    
    public void setLastSavedImage( QuestionImage qImg ) {
        log.debug( "Setting last saved image to {}", qImg );
        lastSavedImg = qImg ;
        if( lastSavedImg != null && lastSavedImg.getQId().isLCT() ) {
            lastLCTSequence = lastSavedImg.getQId().getLctSequence() ;
        }
    }
    
    public void setSelectedPageImg( PageImage pageImg ) {
        this.selectedPageImg = pageImg ;
        this.selectedPageNumber = AppUtil.extractPageNumber( pageImg.getImgFile() ) ;
    }
}
