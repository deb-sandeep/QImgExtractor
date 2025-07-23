package com.sandy.sconsole.qimgextractor.ui.project;

import com.sandy.sconsole.qimgextractor.qid.QuestionImage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
public class ProjectContext {
    
    @Getter @Setter
    private String projectName ;
    
    @Getter @Setter
    private int lastLCTSequence = 0 ;
    
    @Getter
    private QuestionImage lastSavedImg ;

    public void reset() {
        projectName = null ;
    }
    
    public void setLastSavedImage( QuestionImage qImg ) {
        lastSavedImg = qImg ;
        if( lastSavedImg.getQId().isLCT() ) {
            lastLCTSequence = lastSavedImg.getQId().getLctSequence() ;
        }
    }
}
