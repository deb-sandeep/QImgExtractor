package com.sandy.sconsole.qimgextractor.qsrc.aits;

import com.sandy.sconsole.qimgextractor.ui.project.model.qid.QID;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import lombok.EqualsAndHashCode;

import java.util.Stack;

@EqualsAndHashCode(callSuper = false)
public class AITS_QID extends QID {
    
    AITS_QID( QuestionImage qImg ) {
        super( qImg ) ;
    }
    
    public void parse( Stack<String> parts ) {
        super.parseQTypeAndNumber( parts ) ;
    }
    
    public boolean isValid( Stack<String> parts ) {
        return super.validateQTypeAndNumber( parts ) ;
    }
}
