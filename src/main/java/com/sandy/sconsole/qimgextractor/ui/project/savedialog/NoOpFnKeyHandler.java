package com.sandy.sconsole.qimgextractor.ui.project.savedialog;

import com.sandy.sconsole.qimgextractor.qid.QuestionImage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class NoOpFnKeyHandler extends SaveFnKeyHandler {

    public NoOpFnKeyHandler() {
        super( "NoOp" ) ;
    }
    
    @Override
    protected QuestionImage mutateQuestionImage( QuestionImage qImg ) {
        log.debug( "No action performed. This is the base function key handler." ) ;
        return qImg ;
    }
}
