package com.sandy.sconsole.qimgextractor.qsrc;

import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.qid.QID;
import lombok.EqualsAndHashCode;

import java.util.Stack;

@EqualsAndHashCode(callSuper = false)
public class Generic_QID extends QID {
    
    public Generic_QID( QuestionImage qImg ) {
        super( qImg ) ;
    }
    
    public void parse( Stack<String> parts ) {
        super.parseQTypeAndNumber( parts ) ;
    }
    
    public boolean isValid( Stack<String> parts ) {
        return super.validateQTypeAndNumber( parts ) ;
    }
}
