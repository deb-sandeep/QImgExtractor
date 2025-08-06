package com.sandy.sconsole.qimgextractor.ui.project.model;

public interface ProjectModelListener {
    
    void newQuestionImgAdded( PageImage pageImage, QuestionImage qImg ) ;
    
    void questionTagNameChanged( QuestionImage qImg, String oldTagName, String newTagName ) ;
    
    void questionImgDeleted( QuestionImage qImg ) ;
}
