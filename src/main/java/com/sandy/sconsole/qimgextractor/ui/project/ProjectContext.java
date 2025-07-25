package com.sandy.sconsole.qimgextractor.ui.project;

import com.sandy.sconsole.qimgextractor.qid.QuestionImage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
public class ProjectContext {
    
    @Getter
    private File projectDir ;
    
    @Getter
    private String projectName ;
    
    @Getter @Setter
    private int lastLCTSequence = 0 ;
    
    @Getter
    private QuestionImage lastSavedImg ;

    public void reset() {
        projectDir = null ;
        projectName = null ;
        lastLCTSequence = 0 ;
        lastSavedImg = null ;
    }
    
    public void setLastSavedImage( QuestionImage qImg ) {
        log.debug( "Setting last saved image to {}", qImg );
        lastSavedImg = qImg ;
        if( lastSavedImg.getQId().isLCT() ) {
            lastLCTSequence = lastSavedImg.getQId().getLctSequence() ;
        }
    }
    
    public void setProjectRootDir( File projectDir ) {
        this.projectDir = projectDir ;
        this.projectName = projectDir.getName() ;
    }
}
