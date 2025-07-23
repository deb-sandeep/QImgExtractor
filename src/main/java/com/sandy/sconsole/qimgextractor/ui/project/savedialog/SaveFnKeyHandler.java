package com.sandy.sconsole.qimgextractor.ui.project.savedialog;

import com.sandy.sconsole.qimgextractor.qid.QuestionImage;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;

@EqualsAndHashCode( callSuper = false )
@Slf4j
public abstract class SaveFnKeyHandler {
    
    @Getter
    private final String name ;
    
    public SaveFnKeyHandler( String name ) {
        this.name = name ;
    }
    
    protected abstract QuestionImage mutateQuestionImage( QuestionImage qImg ) ;
}
