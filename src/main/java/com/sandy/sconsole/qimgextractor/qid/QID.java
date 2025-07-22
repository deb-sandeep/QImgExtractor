package com.sandy.sconsole.qimgextractor.qid;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public abstract class QID {
    
    public static final List<String> COMMON_SAVE_HELP_CONTENTS = Arrays.asList(
        "----------- Save shortcuts --",
        "Ctrl+1 - Increment Subject Code",
        "Ctrl+2 - Increment QType"
    ) ;
    protected QuestionImage parent ;
    
    protected QID( QuestionImage qImg ){
        this.parent = qImg ;
    }
    
    protected abstract void parse( Stack<String> parts ) ;

    public abstract void incrementQuestionNumber() ;
    
    public abstract String getFilePartName() ;
    
}
